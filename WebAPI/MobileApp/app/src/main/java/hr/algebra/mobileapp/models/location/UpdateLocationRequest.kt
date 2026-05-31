package hr.algebra.mobileapp.models.location

/**
 * Request body for `PUT api/Locations/{id}`.
 *
 * Latitude / longitude are intentionally excluded — a locationService cannot be
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