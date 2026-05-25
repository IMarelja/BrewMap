package hr.algebra.mobileapp.service.auth

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.api.API
import hr.algebra.mobileapp.api.HttpMethod
import hr.algebra.mobileapp.auth.TokenManager
import hr.algebra.mobileapp.models.AuthResponse

/**
 * **Production** auth service — delegates every call to the BrewMap REST API.
 *
 * Base URL is read from `res/values/strings.xml` → `api_base_url`
 * (see [hr.algebra.mobileapp.api.API.Companion.createClient]).
 *
 * ## Token handling
 * On a successful response the JWT is handed to [TokenManager]:
 * - login  + rememberMe=true  → 30-day token persisted to EncryptedSharedPreferences
 * - login  + rememberMe=false → 60-min  token kept in memory only
 * - register                  → 60-min  token persisted (user just created an account)
 */
class AuthServiceApi : IAuthService {

    override suspend fun login(
        user: String,
        password: String,
        rememberMe: Boolean
    ): AuthResponse {
        val body = mapOf(
            "user"       to user,
            "password"   to password,
            "rememberMe" to rememberMe
        )
        val client = API.createClient()
        val res = client.request(
            endpoint     = "Auth/login",
            method       = HttpMethod.PATCH,
            body         = body,
            responseType = object : TypeToken<AuthResponse>() {}
        )
        Log.d("AuthServiceApi", "login response: $res")

        if (res.success && res.token != null) {
            TokenManager.saveToken(res.token, rememberMe)
            Log.d("AuthServiceApi", "JWT saved (rememberMe=$rememberMe)")
        }

        return res
    }

    override suspend fun register(
        email: String,
        username: String,
        password: String
    ): AuthResponse {
        val body = mapOf(
            "email"    to email,
            "username" to username,
            "password" to password
        )
        val client = API.createClient()
        val res = client.request(
            endpoint     = "Auth/register",
            method       = HttpMethod.POST,
            body         = body,
            responseType = object : TypeToken<AuthResponse>() {}
        )
        Log.d("AuthServiceApi", "register response: $res")

        // Register always returns a 60-min token; persist it so the user
        // doesn't have to log in again immediately after signing up.
        if (res.success && res.token != null) {
            TokenManager.saveToken(res.token, rememberMe = true)
            Log.d("AuthServiceApi", "JWT saved after registration")
        }

        return res
    }
}
