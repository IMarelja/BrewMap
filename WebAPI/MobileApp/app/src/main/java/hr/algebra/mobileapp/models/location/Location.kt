package hr.algebra.mobileapp.models.location

/**
 * Mirrors `ReadLocation` from the BrewMap API.
 */
data class Location(
    val id: String,
    val name: String,
    val description: String?,
    val address: Address,
    val longitude: Double,
    val latitude: Double,
    val categoryTag: String,
    val paymentOptionTags: List<String>,
    val openingHours: Map<String, DayOpeningHours>,
    val contact: Contact?,
    val isActive: Boolean,
    val averageRating: Double,
    val totalReviews: Int,
    val createdAt: String
)

data class Address(
    val street: String,
    val city: String,
    val country: String,
    val postalCode: String
)

/** One day's opening window. [open] / [close] are "HH:mm" strings, or null when [isClosed]. */
data class DayOpeningHours(
    val open: String?,
    val close: String?,
    val isClosed: Boolean
)

data class Contact(
    val website: String?
)
