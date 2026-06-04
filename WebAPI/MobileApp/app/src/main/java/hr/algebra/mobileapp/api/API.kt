package hr.algebra.mobileapp.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.bind.DateTypeAdapter
import hr.algebra.mobileapp.BuildConfig
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import java.util.Date

class API {
    companion object {

        /**
         * Base URL is read from repository root `.env` → `BIND_API_URL` at build time
         * and baked into [BuildConfig.API_BASE_URL].  Supports both http:// and
         * https:// — change BIND_API_URL and rebuild.
         */
        private val baseUrl: String
            get() = BuildConfig.API_BASE_URL

        /**
         * Creates a [Requestable] backed by a Ktor [HttpClient] configured with:
         *
         * - **Timeouts** — 10 s connect, 30 s request (avoids silent hangs on mobile)
         * - **Logging**  — full request/response body logged under Logcat tag `Ktor`
         * - **Auth**     — `Authorization: Bearer <token>` injected on every call when
         *   [TokenManager] holds a valid, non-expired JWT; omitted automatically for
         *   anonymous calls (login / register) that run before any token is stored
         */
        fun createClient(): Requestable {
            val gson: Gson = GsonBuilder()
                .registerTypeAdapter(Date::class.java, DateTypeAdapter())
                .create()

            val client = HttpClient(Android) {

                install(HttpTimeout) {
                    connectTimeoutMillis = 10_000   // 10 s to establish connection
                    requestTimeoutMillis = 30_000   // 30 s total per request
                    socketTimeoutMillis  = 30_000   // 30 s waiting for bytes from server
                }

                install(Logging) {
                    logger = Logger.ANDROID
                    level  = LogLevel.BODY
                }

            }

            return RequestableImpl(client, gson, baseUrl)
        }
    }
}
