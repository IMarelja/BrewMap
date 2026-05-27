package hr.algebra.mobileapp.models.auth

data class AuthResponse(
    val success: Boolean,
    val token: String?,
    val message: String?,
    val statusCode: Int
)