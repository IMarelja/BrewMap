package hr.algebra.mobileapp.api
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*


class RequestableImpl(var client: HttpClient, var gson: Gson) : Requestable {
    override suspend fun <T> request(
        endpoint: String,
        method: HttpMethod,
        body: Any?,
        headers: Map<String, String>,
        responseType: TypeToken<T>
    ): T {
        val response = client.request(endpoint) {
            this.method = when (method) {
                HttpMethod.GET -> io.ktor.http.HttpMethod.Get
                HttpMethod.POST -> io.ktor.http.HttpMethod.Post
                HttpMethod.PUT -> io.ktor.http.HttpMethod.Put
                HttpMethod.DELETE -> io.ktor.http.HttpMethod.Delete
                HttpMethod.PATCH -> io.ktor.http.HttpMethod.Patch
            }
            headers.forEach { (key, value) ->
                header(key, value)
            }
            if (body != null) {
                contentType(ContentType.Application.Json)
                val gson = Gson()
                val json = gson.toJson(body, LinkedHashMap::class.java)
                setBody(json)
            }
        }

        val json = response.bodyAsText()
        return gson.fromJson(json, responseType.type)
    }
}