package hr.algebra.mobileapp.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.bind.DateTypeAdapter
import hr.algebra.mobileapp.BrewMapApp
import hr.algebra.mobileapp.R
import hr.algebra.mobileapp.auth.TokenManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import java.util.Date

class API {
    companion object {
        /**
         * Base URL is configured in res/values/strings.xml under "api_base_url".
         * Change that string to point to a different server (e.g. staging, production).
         */
        private val baseUrl: String
            get() = BrewMapApp.appContext.getString(R.string.api_base_url)

        /**
         * Creates a Ktor HTTP client pre-configured with:
         * - the base API URL
         * - an `Authorization: Bearer <token>` header whenever a valid JWT is
         *   stored in [TokenManager] (automatically omitted for anonymous calls
         *   such as login and register, which run before any token is saved).
         */
        fun createClient(): Requestable {
            val gson: Gson = GsonBuilder()
                .registerTypeAdapter(Date::class.java, DateTypeAdapter())
                .create()

            val client = HttpClient(Android) {
                install(ContentNegotiation)
                defaultRequest {
                    url(baseUrl)
                    // Attach Bearer token if one is available and not expired.
                    TokenManager.getToken()?.let { token ->
                        headers.append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }
            }

            return RequestableImpl(client, gson)
        }
    }
}