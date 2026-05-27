package hr.algebra.mobileapp.service.auth

import android.util.Log
import android.util.Patterns
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.api.API
import hr.algebra.mobileapp.api.ApiResult
import hr.algebra.mobileapp.api.HttpMethod
import hr.algebra.mobileapp.auth.TokenManager
import hr.algebra.mobileapp.models.auth.AuthResponse

/**
 * **Production** auth service — delegates every call to the BrewMap REST API.
 *
 * Base URL is read from `result/values/strings.xml` → `api_base_url`
 * (see [hr.algebra.mobileapp.api.API.Companion.createClient]).
 *
 * ## Token handling
 * On a successful response the JWT is handed to [TokenManager]:
 * - login  + rememberMe=true  → 30-day token persisted to EncryptedSharedPreferences
 * - login  + rememberMe=false → 60-min  token kept in memory only
 * - register                  → 60-min  token persisted (user just created an account)
 */
class AuthServiceApi : IAuthService {
    private val gson = Gson()

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

        val body = mapOf(
            "user"       to user,
            "password"   to password,
            "rememberMe" to rememberMe
        )
        val client = API.createClient()
        val resultRequest = client.request(
            endpoint               = "Auth/login",
            method                 = HttpMethod.PATCH,
            body                   = body,
            responseType           = object : TypeToken<AuthResponse>() {},
            allowUnauthorizedBody  = true   // 401 = wrong credentials, not session expired
        )
        val result = mapAuthResult(resultRequest, "Login failed. Please try again.")
        Log.d("AuthServiceApi", "login response: $result")

        if (result.success && result.token != null) {
            TokenManager.saveToken(result.token, rememberMe)
            Log.d("AuthServiceApi", "JWT saved (rememberMe=$rememberMe)")
        }

        return result
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

        val body = mapOf(
            "email"    to email,
            "username" to username,
            "password" to password
        )
        val client = API.createClient()
        val resultRequest = client.request(
            endpoint               = "Auth/register",
            method                 = HttpMethod.POST,
            body                   = body,
            responseType           = object : TypeToken<AuthResponse>() {},
            allowUnauthorizedBody  = true   // 401 = e.g. username already taken, not session expired
        )
        val result = mapAuthResult(resultRequest, "Registration failed. Please try again.")
        Log.d("AuthServiceApi", "register response: $result")

        // Register always returns a 60-min token; persist it so the user
        // doesn't have to log in again immediately after signing up.
        if (result.success && result.token != null) {
            TokenManager.saveToken(result.token, rememberMe = true)
            Log.d("AuthServiceApi", "JWT saved after registration")
        }

        return result
    }

    private fun mapAuthResult(result: ApiResult<AuthResponse>, fallbackMessage: String): AuthResponse =
        when (result) {
            is ApiResult.Success             -> result.data
            is ApiResult.HttpError           -> parseHttpError(result.code, result.body, fallbackMessage)
            is ApiResult.UnauthorizedSpecial -> result.data   // API returned a typed 401 body
            is ApiResult.Unauthorized        -> AuthResponse( // fallback — should not happen for auth calls
                success    = false,
                token      = null,
                message    = "Authentication failed.",
                statusCode = 401
            )
            is ApiResult.NetworkError        -> AuthResponse(
                success    = false,
                token      = null,
                message    = "Failed to connect to the API.",
                statusCode = 503
            )
        }

    private fun parseHttpError(code: Int, rawBody: String, fallbackMessage: String): AuthResponse {
        return try {
            val parsed = gson.fromJson(rawBody, AuthResponse::class.java)
            if (parsed == null) {
                AuthResponse(
                    success = false,
                    token = null,
                    message = fallbackMessage,
                    statusCode = code
                )
            } else {
                parsed.copy(
                    statusCode = if (parsed.statusCode == 0) code else parsed.statusCode,
                    message = parsed.message ?: fallbackMessage
                )
            }
        } catch (_: Exception) {
            AuthResponse(
                success = false,
                token = null,
                message = rawBody.ifBlank { fallbackMessage },
                statusCode = code
            )
        }
    }

    private fun isValidEmail(value: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(value).matches()
}
