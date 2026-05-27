package hr.algebra.mobileapp.api

import com.google.gson.reflect.TypeToken

interface Requestable {

    /**
     * Executes an HTTP request and returns a typed [ApiResult].
     *
     * - Network / connection failures → [ApiResult.NetworkError]
     * - HTTP 401                      → [ApiResult.Unauthorized] (token auto-cleared)
     * - HTTP 4xx / 5xx               → [ApiResult.HttpError]
     * - HTTP 2xx                      → [ApiResult.Success] with deserialized body
     *
     * Never throws — use [ApiResult.getOrThrow] in callers that prefer exceptions.
     */
    suspend fun <T> request(
        endpoint: String,
        method: HttpMethod,
        body: Any? = null,
        headers: Map<String, String> = emptyMap(),
        responseType: TypeToken<T>
    ): ApiResult<T>
}

enum class HttpMethod {
    GET, POST, PUT, DELETE, PATCH
}