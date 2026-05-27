package hr.algebra.mobileapp.api

/**
 * A single error message to surface to the user.
 *
 * Constructed by [toServiceResult] from raw HTTP responses:
 * - Plain JSON string body   → one ResultError with that string
 * - `{ "message": "…" }`    → one ResultError with the message value
 * - ASP.NET ValidationProblemDetails → one ResultError **per (field × message)**,
 *   formatted as `"Field: error text"` so every broken field is visible at once
 */
data class ResultError(val message: String)

/**
 * The unified return type for every non-auth service interface method.
 *
 * On success [data] is non-null (or null for "not found" on nullable methods) and [errors] is empty.
 * On failure [data] is null and [errors] contains one or more human-readable messages.
 * Two special flags cover cross-cutting concerns that need distinct UI handling:
 *
 * - [isUnauthorized] — the session expired; [hr.algebra.mobileapp.auth.TokenManager.sessionExpiredEvent]
 *   has already been emitted so the app's navigation host can redirect to login.
 * - [isNetworkError] — the device could not reach the server; fragments should show
 *   an "offline" state rather than a generic error.
 *
 * ## Companion helpers
 * ```kotlin
 * ServiceResult.success(data)        // 2xx success
 * ServiceResult.failure("msg")       // plain error
 * ServiceResult.failure(listOf(…))  // multiple errors
 * ServiceResult.unauthorized()       // 401 session expired
 * ServiceResult.networkError()       // connection failure
 * ```
 *
 * ## Fragment usage pattern
 * ```kotlin
 * val result = ServiceProvider.drink.getByLocationId(id)
 * when {
 *     result.isUnauthorized          -> { /* MainActivity already navigating to login */ }
 *     result.isNetworkError          -> showOfflineState()
 *     result.errors.isNotEmpty()     -> showError(result.errorMessage()!!)
 *     else                           -> use(result.data!!)
 * }
 * ```
 */
data class ServiceResult<T>(
    val data: T?,
    val errors: List<ResultError> = emptyList(),
    /** Session expired — [hr.algebra.mobileapp.auth.TokenManager.sessionExpiredEvent] already emitted. */
    val isUnauthorized: Boolean = false,
    /** Device could not reach the server. */
    val isNetworkError: Boolean = false
) {
    /** True when there are no errors and neither cross-cutting flag is set. */
    val isSuccess: Boolean get() = errors.isEmpty() && !isUnauthorized && !isNetworkError

    /**
     * Joins all error messages with `\n` for display in a Toast or single error TextView.
     * Returns null when [isSuccess] is true.
     */
    fun errorMessage(): String? =
        errors.takeIf { it.isNotEmpty() }?.joinToString("\n") { it.message }

    /**
     * Converts this result to [ServiceResult]<[Unit]>, preserving all error/flag state.
     * Use for void-return API methods where the underlying request typed a Map or Any.
     */
    fun mapToUnit(): ServiceResult<Unit> =
        if (isSuccess) success(Unit)
        else ServiceResult(data = null, errors = errors, isUnauthorized = isUnauthorized, isNetworkError = isNetworkError)

    companion object {
        fun <T> success(data: T): ServiceResult<T> =
            ServiceResult(data = data)

        fun <T> failure(message: String): ServiceResult<T> =
            ServiceResult(data = null, errors = listOf(ResultError(message)))

        fun <T> failure(errors: List<ResultError>): ServiceResult<T> =
            ServiceResult(data = null, errors = errors)

        fun <T> unauthorized(): ServiceResult<T> =
            ServiceResult(data = null, isUnauthorized = true)

        fun <T> networkError(): ServiceResult<T> =
            ServiceResult(
                data = null,
                isNetworkError = true,
                errors = listOf(ResultError("No connection. Please check your internet."))
            )
    }
}
