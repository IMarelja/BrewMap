package hr.algebra.mobileapp.service.auth

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.api.API
import hr.algebra.mobileapp.api.HttpMethod
import hr.algebra.mobileapp.models.AuthResponse

/**
 * **Production** auth service — delegates every call to the BrewMap REST API.
 *
 * Base URL is read from `res/values/strings.xml` → `api_base_url`
 * (see [hr.algebra.mobileapp.api.API.Companion.createClient]).
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
        val client = API.Companion.createClient()
        val res = client.request(
            endpoint     = "Auth/login",
            method       = HttpMethod.PATCH,
            body         = body,
            responseType = object : TypeToken<AuthResponse>() {}
        )
        Log.d("AuthServiceApi", "login response: $res")
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
        val client = API.Companion.createClient()
        val res = client.request(
            endpoint     = "Auth/register",
            method       = HttpMethod.POST,
            body         = body,
            responseType = object : TypeToken<AuthResponse>() {}
        )
        Log.d("AuthServiceApi", "register response: $res")
        return res
    }
}
