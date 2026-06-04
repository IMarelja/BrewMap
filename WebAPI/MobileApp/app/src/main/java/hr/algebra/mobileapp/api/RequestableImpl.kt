package hr.algebra.mobileapp.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.auth.TokenManager
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class RequestableImpl(
    private val client: HttpClient,
    private val gson: Gson,
    private val baseUrl: String,
) : Requestable {

    override suspend fun <T> request(
        endpoint: String,
        method: HttpMethod,
        body: Any?,
        headers: Map<String, String>,
        responseType: TypeToken<T>,
        allowUnauthorizedBody: Boolean
    ): ApiResult<T> {
        // Build the full URL explicitly so Ktor never mangles the path.
        // e.g. "http://host/api/" + "Auth/login" → "http://host/api/Auth/login"
        val url = baseUrl.trimEnd('/') + "/" + endpoint.trimStart('/')

        // Resolve the token at call-time, not at client-creation time, so:
        //  - anonymous calls (login/register) never carry a stale header
        //  - authenticated calls always carry the current token
        val token = TokenManager.getToken()

        return try {
            val response: HttpResponse = client.request(url) {
                this.method = when (method) {
                    HttpMethod.GET    -> io.ktor.http.HttpMethod.Get
                    HttpMethod.POST   -> io.ktor.http.HttpMethod.Post
                    HttpMethod.PUT    -> io.ktor.http.HttpMethod.Put
                    HttpMethod.DELETE -> io.ktor.http.HttpMethod.Delete
                    HttpMethod.PATCH  -> io.ktor.http.HttpMethod.Patch
                }

                if (token != null) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }

                headers.forEach { (key, value) -> header(key, value) }

                if (body != null) {
                    contentType(ContentType.Application.Json)
                    // BUG FIX: use the outer 'gson' (with DateTypeAdapter registered),
                    // not a bare Gson() — and serialize as the object's actual type,
                    // not LinkedHashMap (which erased all non-map fields).
                    setBody(gson.toJson(body))
                }
            }

            Log.d("RequestableImpl", "${method.name} $url → HTTP ${response.status.value}")

            if (response.status == HttpStatusCode.Unauthorized) {
                if (allowUnauthorizedBody) {
                    // Auth endpoints (login / register): 401 = "bad credentials".
                    // Deserialize the body into T so the caller gets the API's error
                    // message. Token is NOT cleared.
                    val rawBody = response.bodyAsText()
                    return try {
                        ApiResult.UnauthorizedSpecial(gson.fromJson(rawBody, responseType.type))
                    } catch (_: Exception) {
                        ApiResult.Unauthorized
                    }
                }
                // All other endpoints: 401 = "session expired".
                return ApiResult.Unauthorized
            }

            val rawBody = response.bodyAsText()

            // Non-2xx: surface the error code and raw body to the caller.
            if (!response.status.isSuccess()) {
                return ApiResult.HttpError(response.status.value, rawBody)
            }

            // 204 No Content (or any genuinely empty body): nothing to deserialize.
            @Suppress("UNCHECKED_CAST")
            if (rawBody.isBlank()) return ApiResult.Success(Unit as T)

            ApiResult.Success(gson.fromJson(rawBody, responseType.type))

        } catch (e: Exception) {
            Log.e("RequestableImpl", "Request failed: ${e.message}", e)
            ApiResult.NetworkError(e)
        }
    }
}
