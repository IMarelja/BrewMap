package hr.algebra.mobileapp.api

import com.google.gson.JsonParser
import hr.algebra.mobileapp.auth.TokenManager

/**
 * Converts any [ApiResult]<T> to a [ServiceResult]<T> with smart error-body parsing.
 *
 * @param fallback   Human-readable message used when the HTTP error body is empty or
 *                   unparseable. Defaults to "Something went wrong."
 * @param treatNotFoundAsEmpty  When `true` an HTTP 404 is mapped to
 *   `ServiceResult(data = null, errors = empty)` — the "not found / nothing here" case —
 *   rather than a [ServiceResult.failure]. Use for methods that already return `T?` where
 *   null is a valid, non-error result (e.g. getBestDrink, getByTag, getById for Review).
 */
fun <T> ApiResult<T>.toServiceResult(
    fallback: String = "Something went wrong.",
    treatNotFoundAsEmpty: Boolean = false
): ServiceResult<T> = when (this) {
    is ApiResult.Success             -> ServiceResult.success(data)
    is ApiResult.UnauthorizedSpecial -> ServiceResult.success(data)
    is ApiResult.Unauthorized        -> {
        // Emit the app-level signal so MainActivity can redirect to login
        // independently of whichever fragment triggered this call.
        TokenManager.sessionExpiredEvent.tryEmit(Unit)
        ServiceResult.unauthorized()
    }
    is ApiResult.NetworkError        -> ServiceResult.networkError()
    is ApiResult.HttpError           -> {
        if (treatNotFoundAsEmpty && code == 404)
            ServiceResult(data = null)   // empty — not an error
        else
            ServiceResult.failure(parseBodyErrors(body, fallback))
    }
}

/**
 * Parses the raw HTTP error body into a list of [ResultError] entries.
 *
 * Handles the three distinct shapes the BrewMap backend produces:
 *
 * | Shape | Example | Result |
 * |---|---|---|
 * | Plain JSON string | `"A drink with that name already exists"` | One `ResultError` with that string |
 * | `{ "message": "…" }` | NotFound with body | One `ResultError` with the message |
 * | ASP.NET ValidationProblemDetails | `{ "errors": { "Field": ["msg1"] } }` | One `ResultError` **per (field × message)** formatted as `"Field: msg1"` |
 *
 * Field-level errors use `flatMap` (not `firstOrNull`) so all validation failures are
 * visible at once — important for `IValidatableObject` like OpeningHours that can report
 * all 7 missing days in one response.
 */
private fun parseBodyErrors(rawBody: String, fallback: String): List<ResultError> {
    if (rawBody.isBlank()) return listOf(ResultError(fallback))
    return try {
        val el = JsonParser.parseString(rawBody)
        when {
            el.isJsonObject -> {
                val obj = el.asJsonObject
                when {
                    // ASP.NET ValidationProblemDetails:
                    //   { "errors": { "Name": ["required"], "Rating": ["1–5"] } }
                    // One ResultError per (field × message): "Name: required", "Rating: 1–5"
                    obj.has("errors") ->
                        obj.getAsJsonObject("errors").entrySet().flatMap { (field, msgs) ->
                            msgs.asJsonArray.map { msg -> ResultError("$field: ${msg.asString}") }
                        }.ifEmpty { listOf(ResultError(fallback)) }

                    // { "message": "Location not found." }
                    obj.has("message") -> listOf(ResultError(obj.get("message").asString))

                    else -> listOf(ResultError(fallback))
                }
            }
            // BadRequest(ex.Message) → plain JSON string
            el.isJsonPrimitive && el.asJsonPrimitive.isString ->
                listOf(ResultError(el.asJsonPrimitive.asString))

            else -> listOf(ResultError(fallback))
        }
    } catch (_: Exception) {
        listOf(ResultError(rawBody.trim().take(300).ifBlank { fallback }))
    }
}
