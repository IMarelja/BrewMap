package hr.algebra.mobileapp.service.user

import hr.algebra.mobileapp.models.StrangerProfile
import hr.algebra.mobileapp.models.UserProfile

/**
 * Contract for user profile operations.
 *
 * Two implementations:
 *  - [UserServiceApi]      — live BrewMap REST API (requires auth token)
 *  - [UserServiceHardCode] — in-memory stub, no network
 */
interface IUserService {

    /** `GET api/User/me` — returns the profile of the currently authenticated user. */
    suspend fun getMyProfile(): UserProfile

    /** `GET api/User/{id}` — returns the public profile of any user. */
    suspend fun getUserById(id: String): StrangerProfile

    /** `PUT api/User/email` — updates the authenticated user's e-mail address. */
    suspend fun updateEmail(newEmail: String, currentPassword: String)

    /** `PUT api/User/password` — changes the authenticated user's password. */
    suspend fun updatePassword(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    )
}
