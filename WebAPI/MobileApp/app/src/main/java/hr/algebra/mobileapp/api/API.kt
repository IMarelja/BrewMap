package hr.algebra.mobileapp.api
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.internal.bind.DateTypeAdapter
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Date

class API {
    companion object {
        fun createClient(): Requestable {
            val gson: Gson = GsonBuilder()
                .registerTypeAdapter(Date::class.java, DateTypeAdapter())
                .create()

            val client = HttpClient(Android) {
                install(ContentNegotiation)
                defaultRequest {
                    url("http://10.0.2.2:5239/api/")
                }
            }

            return RequestableImpl(client, gson)
        }
    }
}