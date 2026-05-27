package hr.algebra.mobileapp.models.location

/**
 * Request body for `POST api/Locations`.
 *
 * [openingHours] must contain all 7 days ("monday"…"sunday") as keys, each
 * mapped to a [DayOpeningHours] — matching the backend's validation requirement.
 */
data class CreateLocationRequest(
    val name: String,
    val description: String?,
    val address: Address,
    val latitude: Double,
    val longitude: Double,
    val categoryTag: String,
    val paymentOptionTags: List<String>,
    val contact: Contact?,
    val openingHours: Map<String, DayOpeningHours>
)

/**
 * Request body for `PUT api/Locations/{id}`.
 *
 * Latitude / longitude are intentionally excluded — a location cannot be
 * moved after creation (backend does not accept them in the update DTO).
 */
data class UpdateLocationRequest(
    val name: String,
    val description: String?,
    val address: Address,
    val categoryTag: String,
    val paymentOptionTags: List<String>,
    val contact: Contact?,
    val openingHours: Map<String, DayOpeningHours>,
    val editComment: String? = null
)
