package hr.algebra.mobileapp.service.auth

import android.util.Base64
import android.util.Log
import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.models.auth.AuthResponse
import hr.algebra.mobileapp.service.HardCodeData
import hr.algebra.mobileapp.service.RequestBodyValidator

/**
 * **Test / offline** authService service — no network required.
 *
 * All seed data lives in [HardCodeData], which is created once by
 * [hr.algebra.mobileapp.service.ServiceProvider] and shared across every
 * hard-code stub.  This class only contains authService logic.
 *
 * Login accepts username **or** e-mail.  Register appends the new userService to
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
    ): ServiceResult<AuthResponse> {
        val validationErrors = RequestBodyValidator.validateLogin(user, password)
        if (validationErrors.isNotEmpty()) {
            return ServiceResult.failure(validationErrors)
        }

        val match = data.users.find { it.username == user || it.email == user }

        if (match == null || match.password != password) {
            Log.d("AuthServiceHardCode", "login failed for '$user'")
            return ServiceResult.failure("Username/email or password are incorrect.")
        }

        val token = buildMockJwt(match.id, match.role, rememberMe)
        Log.d("AuthServiceHardCode", "login OK  userService=${match.username}  role=${match.role}")
        return ServiceResult.success(AuthResponse(
            success    = true,
            token      = token,
            message    = "Login successful.",
            statusCode = 200
        ))
    }

    override suspend fun register(
        email: String,
        username: String,
        password: String
    ): ServiceResult<AuthResponse> {
        val validationErrors = RequestBodyValidator.validateRegister(email, username, password)
        if (validationErrors.isNotEmpty()) {
            return ServiceResult.failure(validationErrors)
        }

        val conflict = data.users.find { it.username == username || it.email == email }
        if (conflict != null) {
            val field = if (conflict.email == email) "e-mail" else "username"
            Log.d("AuthServiceHardCode", "register conflict on $field")
            return ServiceResult.failure("An account with that $field already exists.")
        }

        val newId = "hc-new-${System.currentTimeMillis()}"
        data.users.add(
            HardCodeData.MockUser(
                id       = newId,
                username = username,
                email    = email,
                password = password,
                role     = "userService"
            )
        )

        val token = buildMockJwt(newId, "userService", rememberMe = false)
        Log.d("AuthServiceHardCode", "register OK  username=$username")
        return ServiceResult.success(AuthResponse(
            success    = true,
            token      = token,
            message    = "Registration successful.",
            statusCode = 201
        ))
    }

    // ── Mock JWT builder ──────────────────────────────────────────────────────

    /**
     * Produces an unsigned JWT (algorithm "none", RFC 7519 §6) so callers can
     * decode the payload without any real crypto.
     *
     * | field | type   | value                            |
     * |-------|--------|----------------------------------|
     * | id    | String | mock userService ID                     |
     * | role  | String | "admin" or "userService"                |
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
}
