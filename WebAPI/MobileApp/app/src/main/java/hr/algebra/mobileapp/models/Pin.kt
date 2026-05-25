package hr.algebra.mobileapp.models

/**
 * Mirrors `ReadPin` from the BrewMap API.
 * Lightweight map marker — only id and coordinates, no full location details.
 */
data class Pin(
    val id: String,
    val longitude: Double,
    val latitude: Double
)
