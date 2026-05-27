package hr.algebra.mobileapp.api

/**
 * Sealed result type returned by every [Requestable.request] call.
 *
 * Callers can pattern-match to handle each case explicitly, or call
 * [getOrThrow] to re-raise failures as exceptions — which is what all
 * `*ServiceApi` implementations do so that calling fragments keep their
 * existing `try / catch` wrappers unchanged.
 *
 * | Subtype        | Meaning                                                        |
 * |----------------|----------------------------------------------------------------|
 * | [Success]      | HTTP 2xx — [data] is the deserialized response body            |
 * | [HttpError]    | HTTP 4xx/5xx (except 401) — [code] and [body] are available   |
 * | [Unauthorized] | HTTP 401 — [hr.algebra.mobileapp.auth.TokenManager] already cleared |
 * | [NetworkError] | No response received — connection error, timeout, etc.         |
 */
sealed class ApiResult<out T> {

    data class Success<T>(val data: T)                    : ApiResult<T>()
    data class HttpError(val code: Int, val body: String) : ApiResult<Nothing>()
    object Unauthorized                                   : ApiResult<Nothing>()
    data class NetworkError(val cause: Exception)         : ApiResult<Nothing>()

    /**
     * Returns [Success.data], or throws an [Exception] describing the failure.
     *
     * Use in service implementations where callers already use `try / catch`.
     */
    fun getOrThrow(): T = when (this) {
        is Success      -> data
        is HttpError    -> throw Exception("HTTP $code: $body")
        is Unauthorized -> throw Exception("Session expired — please log in again")
        is NetworkError -> throw cause
    }
}
