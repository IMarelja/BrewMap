package hr.algebra.mobileapp.service

import android.util.Patterns
import hr.algebra.mobileapp.api.ResultError
import hr.algebra.mobileapp.models.flag.CreateFlagRequest
import hr.algebra.mobileapp.models.location.CreateLocationRequest
import hr.algebra.mobileapp.models.location.DayOpeningHours
import hr.algebra.mobileapp.models.location.UpdateLocationRequest
import java.net.URL

object RequestBodyValidator {
    private val requiredDays = listOf(
        "monday",
        "tuesday",
        "wednesday",
        "thursday",
        "friday",
        "saturday",
        "sunday"
    )
    private val timeRegex = Regex("^([01]\\d|2[0-3]):([0-5]\\d)$")
    private val allowedFlagTargetTypes = setOf("location", "product", "review", "user")

    fun validateLogin(user: String, password: String): List<ResultError> =
        buildList {
            requireNotBlank("User", user)
            requireNotBlank("Password", password)
        }

    fun validateRegister(email: String, username: String, password: String): List<ResultError> =
        buildList {
            requireNotBlank("Email", email)
            if (email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                add(ResultError("Email must be a valid email address."))
            }
            requireNotBlank("Username", username)
            requireNotBlank("Password", password)
        }

    fun validateCreateLocation(request: CreateLocationRequest): List<ResultError> =
        buildList {
            requireNotBlank("Name", request.name)
            requireNotBlank("CategoryTag", request.categoryTag)
            validateOptionalUrl("Contact.Website", request.contact?.website)
            validateOpeningHours(request.openingHours)
        }

    fun validateUpdateLocation(request: UpdateLocationRequest): List<ResultError> =
        buildList {
            requireNotBlank("Name", request.name)
            requireNotBlank("CategoryTag", request.categoryTag)
            validateOptionalUrl("Contact.Website", request.contact?.website)
            validateOpeningHours(request.openingHours)
        }

    fun validateCreateDrink(name: String, locationId: String): List<ResultError> =
        buildList {
            requireNotBlank("Name", name)
            requireNotBlank("LocationId", locationId)
        }

    fun validateUpdateDrink(name: String): List<ResultError> =
        buildList {
            requireNotBlank("Name", name)
        }

    fun validateCreateFlag(
        request: CreateFlagRequest
    ): List<ResultError> =
        buildList {
            val targetType = request.target.type
            val targetId = request.target.id
            val reason = request.reason

            requireNotBlank("Target.Type", targetType)
            if (targetType.isNotBlank() && targetType !in allowedFlagTargetTypes) {
                add(ResultError("Target.Type must be one of: location, product, review, user."))
            }
            requireNotBlank("Target.Id", targetId)
            requireNotBlank("Reason", reason)
            if (reason.isNotBlank() && reason.length < 3) {
                add(ResultError("Reason must be at least 3 characters"))
            }
        }

    private fun MutableList<ResultError>.requireNotBlank(field: String, value: String) {
        if (value.isBlank()) {
            add(ResultError("$field is required."))
        }
    }

    private fun MutableList<ResultError>.validateOpeningHours(
        openingHours: Map<String, DayOpeningHours>
    ) {
        requiredDays.forEach { day ->
            val hours = openingHours[day]
            if (hours == null) {
                add(ResultError("OpeningHours must include '$day'."))
            } else {
                validateDayOpeningHours("OpeningHours.$day", hours)
            }
        }
    }

    private fun MutableList<ResultError>.validateDayOpeningHours(
        field: String,
        hours: DayOpeningHours
    ) {
        if (hours.isClosed) {
            if (!hours.open.isNullOrBlank() && !hours.open.equals("string", ignoreCase = true)) {
                add(ResultError("$field.Open must be null when IsClosed is true."))
            }
            if (!hours.close.isNullOrBlank() && !hours.close.equals("string", ignoreCase = true)) {
                add(ResultError("$field.Close must be null when IsClosed is true."))
            }
            return
        }

        if (hours.open.isNullOrBlank() || hours.open.equals("string", ignoreCase = true) ||
            hours.close.isNullOrBlank() || hours.close.equals("string", ignoreCase = true)
        ) {
            add(ResultError("$field: Open and Close are required when IsClosed is false."))
        }

        if (!timeRegex.matches(hours.open.orEmpty())) {
            add(ResultError("$field.Open time must be in HH:mm format."))
        }

        if (!timeRegex.matches(hours.close.orEmpty())) {
            add(ResultError("$field.Close time must be in HH:mm format."))
        }
    }

    private fun MutableList<ResultError>.validateOptionalUrl(field: String, value: String?) {
        if (value.isNullOrBlank()) return

        val isValid = runCatching {
            val url = URL(value)
            url.toURI()
            !url.protocol.isNullOrBlank() && !url.host.isNullOrBlank()
        }.getOrDefault(false)

        if (!isValid) {
            add(ResultError("$field must be a valid URL."))
        }
    }
}
