package hr.algebra.mobileapp.api
import com.google.gson.reflect.TypeToken

interface Requestable {
    suspend fun <T> request(
        endpoint: String,
        method: HttpMethod,
        body: Any? = null,
        headers: Map<String, String> = emptyMap(),
        responseType: TypeToken<T>
    ): T
}

enum class HttpMethod {
    GET, POST, PUT, DELETE, PATCH
}