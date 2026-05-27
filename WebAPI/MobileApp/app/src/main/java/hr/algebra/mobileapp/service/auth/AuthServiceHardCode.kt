package hr.algebra.mobileapp.service.auth

import android.util.Base64
import android.util.Log
import android.util.Patterns
import hr.algebra.mobileapp.models.auth.AuthResponse
import hr.algebra.mobileapp.service.HardCodeData

/**
 * **Test / offline** auth service — no network required.
 *
 * All seed data lives in [HardCodeData], which is created once by
 * [hr.algebra.mobileapp.service.ServiceProvider] and shared across every
 * hard-code stub.  This class only contains auth logic.
 *
 * Login accepts username **or** e-mail.  Register appends the new user to
 * [HardCodeData.users] so it survives for the duration of the session.
 *
 * Tokens are unsigned mock JWTs containing `id`, `role`, and `exp`.
 */
class AuthServiceHardCode(private val data: HardCodeData) : IAuthService {

    // ── IAuthService ──────────────────────────────────────────────────────────

    override suspend fun login(
        user: String,
        password: String,
        rememberMe: Boolean
    ): AuthResponse {
        if (user.contains("@") && !isValidEmail(user)) {
            return AuthResponse(
                success = false,
                token = null,
                message = "Please enter a valid email address.",
                statusCode = 400
            )
        }

        val match = data.users.find { it.username == user || it.email == user }

        if (match == null || match.password != password) {
            Log.d("AuthServiceHardCode", "login failed for '$user'")
            return AuthResponse(
                success    = false,
                token      = null,
                message    = "Username/email or password are incorrect.",
                statusCode = 401
            )
        }

        val token = buildMockJwt(match.id, match.role, rememberMe)
        Log.d("AuthServiceHardCode", "login OK  user=${match.username}  role=${match.role}")
        return AuthResponse(
            success    = true,
            token      = token,
            message    = "Login successful.",
            statusCode = 200
        )
    }

    override suspend fun register(
        email: String,
        username: String,
        password: String
    ): AuthResponse {
        if (!isValidEmail(email)) {
            return AuthResponse(
                success = false,
                token = null,
                message = "Please enter a valid email address.",
                statusCode = 400
            )
        }

        val conflict = data.users.find { it.username == username || it.email == email }
        if (conflict != null) {
            val field = if (conflict.email == email) "e-mail" else "username"
            Log.d("AuthServiceHardCode", "register conflict on $field")
            return AuthResponse(
                success    = false,
                token      = null,
                message    = "An account with that $field already exists.",
                statusCode = 409
            )
        }

        val newId = "hc-new-${System.currentTimeMillis()}"
        data.users.add(
            HardCodeData.MockUser(
                id       = newId,
                username = username,
                email    = email,
                password = password,
                role     = "user"
            )
        )

        val token = buildMockJwt(newId, "user", rememberMe = false)
        Log.d("AuthServiceHardCode", "register OK  username=$username")
        return AuthResponse(
            success    = true,
            token      = token,
            message    = "Registration successful.",
            statusCode = 201
        )
    }

    // ── Mock JWT builder ──────────────────────────────────────────────────────

    /**
     * Produces an unsigned JWT (algorithm "none", RFC 7519 §6) so callers can
     * decode the payload without any real crypto.
     *
     * | field | type   | value                            |
     * |-------|--------|----------------------------------|
     * | id    | String | mock user ID                     |
     * | role  | String | "admin" or "user"                |
     * | exp   | Long   | Unix epoch seconds (1 h or 7 d) |
     */
    private fun buildMockJwt(userId: String, role: String, rememberMe: Boolean): String {
        val ttlSeconds = if (rememberMe) 7 * 24 * 3600L else 3600L
        val exp        = System.currentTimeMillis() / 1_000 + ttlSeconds

        val encodedHeader  = base64Url("""{"alg":"none","typ":"JWT"}""")
        val encodedPayload = base64Url("""{"id":"$userId","role":"$role","exp":$exp}""")

        return "$encodedHeader.$encodedPayload."
    }

    private fun base64Url(value: String): String =
        Base64.encodeToString(
            value.toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )

    private fun isValidEmail(value: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(value).matches()
}
