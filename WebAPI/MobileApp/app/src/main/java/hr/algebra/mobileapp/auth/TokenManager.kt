package hr.algebra.mobileapp.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import hr.algebra.mobileapp.BrewMapApp
import kotlinx.coroutines.flow.MutableSharedFlow
import org.json.JSONObject

/**
 * Manages JWT token persistence and validation for BrewMap.
 *
 * ## Storage strategy
 * - `rememberMe = true`  → token written to [SharedPreferences]. Survives app
 *   restarts and lives until the JWT's own `exp` claim is reached or the user
 *   explicitly logs out.
 * - `rememberMe = false` → token kept only in process memory; cleared when
 *   the app is killed or the user logs out.
 *
 * ## Expiry
 * The server issues:
 *  - login  + rememberMe=true  → 30-day JWT   (1 440 min)
 *  - login  + rememberMe=false → 60-min  JWT
 *  - register                  → 60-min  JWT
 *
 * [getToken] always decodes the `exp` claim and discards expired tokens
 * automatically — no background timer is needed.
 */
object TokenManager {

    private const val TAG        = "TokenManager"
    private const val PREFS_NAME = "brewmap_secure_prefs"
    private const val KEY_JWT    = "jwt_token"

    /**
     * Emitted by [hr.algebra.mobileapp.api.ApiResultExtensions.toServiceResult] whenever
     * a non-auth endpoint returns HTTP 401 (session expired).
     *
     * Collect this in [hr.algebra.mobileapp.MainActivity] to navigate to the login
     * screen and clear the back-stack, regardless of which fragment triggered the call.
     */
    val sessionExpiredEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** In-memory cache — also used as the sole store when rememberMe = false. */
    private var memoryToken: String? = null

    /**
     * Lazy so preferences are opened only on first use.
     */
    private val prefs: SharedPreferences by lazy { buildPrefs() }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Store a JWT returned by the API.
     *
     * @param token      raw JWT string from [hr.algebra.mobileapp.models.AuthResponse]
     * @param rememberMe when true the token is written to encrypted on-device
     *                   storage; when false it lives only for this process lifetime.
     *                   Defaults to **true** to match the client-side convention.
     */
    fun saveToken(token: String?, rememberMe: Boolean = true) {
        if (token == null) {
            Log.w(TAG, "saveToken called with null — nothing saved")
            return
        }
        memoryToken = token
        if (rememberMe) {
            val ok = prefs.edit().putString(KEY_JWT, token).commit()   // commit = synchronous write
            Log.d(TAG, "Token persisted to SharedPreferences (commit=$ok) prefix=${token.take(20)}")
        } else {
            // Make sure no stale persisted token from a previous "remember me"
            // session lingers while this session uses only memory.
            prefs.edit().remove(KEY_JWT).apply()
            Log.d(TAG, "Token stored in memory only (rememberMe=false)")
        }
    }

    /**
     * Returns the stored JWT only if one exists **and has not expired**.
     *
     * Checks the JWT `exp` (Unix epoch, seconds) against the current clock.
     * An expired token is removed from both memory and storage automatically.
     */
    fun getToken(): String? {
        val fromMemory = memoryToken
        val fromPrefs  = if (fromMemory == null) prefs.getString(KEY_JWT, null) else null
        val token      = fromMemory ?: fromPrefs

        Log.d(TAG, "getToken: memory=${fromMemory?.take(20) ?: "null"}  prefs=${fromPrefs?.take(20) ?: "null"}")

        if (token == null) return null

        // Warm the memory cache so subsequent calls avoid a SharedPreferences read.
        if (memoryToken == null) memoryToken = token

        return if (isExpired(token)) {
            Log.d(TAG, "Stored JWT is expired — clearing session")
            clearToken()
            null
        } else {
            token
        }
    }

    /** Returns true when a valid, non-expired token is available. */
    fun isLoggedIn(): Boolean = getToken() != null

    /**
     * Decodes the `id` claim from the stored JWT without verifying the signature.
     *
     * Used by hard-code service stubs that need to associate in-memory write
     * operations (create review, update profile, …) with the currently logged-in user.
     *
     * Returns `null` when there is no token or the token cannot be parsed.
     */
    fun getUserId(): String? {
        val token = getToken() ?: return null
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payloadBytes = Base64.decode(
                parts[1],
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
            )
            JSONObject(String(payloadBytes, Charsets.UTF_8))
                .optString("id")
                .takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode user ID from JWT", e)
            null
        }
    }

    /**
     * Remove the token from memory **and** encrypted storage.
     * Call this on user-initiated logout or when the server rejects the token.
     */
    fun clearToken() {
        memoryToken = null
        prefs.edit().remove(KEY_JWT).apply()
        Log.d(TAG, "Token cleared (logout or expiry)")
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Parses the JWT payload segment (Base64-URL, no padding) and checks the
     * `exp` claim without verifying the signature — we trust the server's
     * signing key; the client only needs to know whether to re-authenticate.
     */
    private fun isExpired(jwt: String): Boolean {
        return try {
            val parts = jwt.split(".")
            if (parts.size != 3) return true          // malformed token

            val payloadBytes = Base64.decode(
                parts[1],
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
            )
            val payload = JSONObject(String(payloadBytes, Charsets.UTF_8))
            val exp = payload.optLong("exp", 0L)
            // exp == 0 means claim is absent → treat as expired
            exp == 0L || System.currentTimeMillis() / 1000 >= exp
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode JWT exp claim — treating as expired", e)
            true
        }
    }

    private fun buildPrefs(): SharedPreferences {
        val context = BrewMapApp.appContext
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
