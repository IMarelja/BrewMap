package hr.algebra.mobileapp.models.location

/**
 * Mirrors `ReadPin` from the BrewMap API.
 * Lightweight map marker — only id and coordinates, no full locationService details.
 */
data class Pin(
    val id: String,
    val longitude: Double,
    val latitude: Double
)