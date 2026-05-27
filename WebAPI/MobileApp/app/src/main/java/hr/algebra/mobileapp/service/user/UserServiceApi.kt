package hr.algebra.mobileapp.service.user

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.api.API
import hr.algebra.mobileapp.api.HttpMethod
import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.api.toServiceResult
import hr.algebra.mobileapp.models.user.StrangerProfile
import hr.algebra.mobileapp.models.user.UserProfile

/**
 * **Production** user service — delegates every call to the BrewMap REST API.
 */
class UserServiceApi : IUserService {

    // ── GET api/User/me ───────────────────────────────────────────────────────

    override suspend fun getMyProfile(): ServiceResult<UserProfile> {
        val client = API.createClient()
        val result = client.request(
            endpoint     = "User/me",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<UserProfile>() {}
        ).toServiceResult("Could not load your profile.")
        Log.d("UserServiceApi", "getMyProfile → success=${result.isSuccess}")
        return result
    }

    // ── GET api/User/{id} ─────────────────────────────────────────────────────

    override suspend fun getUserById(id: String): ServiceResult<StrangerProfile> {
        val client = API.createClient()
        val result = client.request(
            endpoint     = "User/$id",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<StrangerProfile>() {}
        ).toServiceResult("Could not load user profile.")
        Log.d("UserServiceApi", "getUserById($id) → success=${result.isSuccess}")
        return result
    }

    // ── PUT api/User/email ────────────────────────────────────────────────────

    override suspend fun updateEmail(newEmail: String, currentPassword: String): ServiceResult<Unit> {
        val client = API.createClient()
        val result = client.request(
            endpoint     = "User/email",
            method       = HttpMethod.PUT,
            body         = mapOf("newEmail" to newEmail, "currentPassword" to currentPassword),
            responseType = object : TypeToken<Map<String, Any>>() {}
        ).toServiceResult("Could not update email.").mapToUnit()
        Log.d("UserServiceApi", "updateEmail → success=${result.isSuccess}")
        return result
    }

    // ── PUT api/User/password ─────────────────────────────────────────────────

    override suspend fun updatePassword(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): ServiceResult<Unit> {
        val client = API.createClient()
        val result = client.request(
            endpoint     = "User/password",
            method       = HttpMethod.PUT,
            body         = mapOf(
                "currentPassword"    to currentPassword,
                "newPassword"        to newPassword,
                "confirmNewPassword" to confirmNewPassword
            ),
            responseType = object : TypeToken<Map<String, Any>>() {}
        ).toServiceResult("Could not update password.").mapToUnit()
        Log.d("UserServiceApi", "updatePassword → success=${result.isSuccess}")
        return result
    }
}
