package hr.algebra.mobileapp.models

/** Request body for `POST api/Drink`. */
data class CreateDrinkRequest(
    val name: String,
    val description: String?,
    val locationId: String
)
