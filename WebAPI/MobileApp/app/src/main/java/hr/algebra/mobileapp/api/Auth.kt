package hr.algebra.mobileapp.api

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.models.AuthResponse
import io.ktor.http.headers
import retrofit2.http.PATCH

class Auth {
    companion object {
        suspend fun login(user: String, password: String, rememberMe: Boolean): AuthResponse {
            val requestBody = mapOf(
                "user" to user,
                "password" to password,
                "rememberMe" to rememberMe
            )
            val client = API.createClient()
            val res = client.request(
                endpoint = "Auth/login",
                method = HttpMethod.PATCH,
                body = requestBody,
                responseType = object: TypeToken<AuthResponse>() {})
            Log.d("test", res.toString())
            return res
        }
        suspend fun register(email: String, username: String, password: String): AuthResponse {
            val requestBody = mapOf(
                "email" to email,
                "username" to username,
                "password" to password
            )
            val client = API.createClient()
            val res = client.request(
                endpoint = "Auth/register",
                method = HttpMethod.POST,
                body = requestBody,
                responseType = object: TypeToken<AuthResponse>() {})
            Log.d("test", res.toString())
            return res
        }
    }
}