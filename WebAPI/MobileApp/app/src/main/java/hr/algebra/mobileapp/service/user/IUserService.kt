package hr.algebra.mobileapp.service.user

import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.models.user.StrangerProfile
import hr.algebra.mobileapp.models.user.UserProfile

/**
 * Contract for user profile operations.
 *
 * Two implementations:
 *  - [UserServiceApi]      — live BrewMap REST API (requires auth token)
 *  - [UserServiceHardCode] — in-memory stub, no network
 *
 * Every method returns [ServiceResult]<T>. On success [ServiceResult.data] holds the result;
 * on failure [ServiceResult.errors] contains one or more human-readable messages.
 */
interface IUserService {

    /** `GET api/User/me` — returns the profile of the currently authenticated user. */
    suspend fun getMyProfile(): ServiceResult<UserProfile>

    /** `GET api/User/{id}` — returns the public profile of any user. */
    suspend fun getUserById(id: String): ServiceResult<StrangerProfile>

    /** `PUT api/User/email` — updates the authenticated user's e-mail address. */
    suspend fun updateEmail(newEmail: String, currentPassword: String): ServiceResult<Unit>

    /** `PUT api/User/password` — changes the authenticated user's password. */
    suspend fun updatePassword(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): ServiceResult<Unit>
}
