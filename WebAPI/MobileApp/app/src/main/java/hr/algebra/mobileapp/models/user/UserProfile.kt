package hr.algebra.mobileapp.models.user

/** Own profile — mirrors `MyUserProfileRead` from the BrewMap API. */
data class UserProfile(
    val id: String,
    val username: String,
    val email: String
)

/** Public profile of another user — mirrors `StrangerUserProfileRead`. */
data class StrangerProfile(
    val id: String,
    val username: String
)

/** Request body for `PUT api/User/email`. */
data class UpdateEmailRequest(
    val newEmail: String,
    val currentPassword: String
)

/** Request body for `PUT api/User/password`. */
data class UpdatePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmNewPassword: String
)
