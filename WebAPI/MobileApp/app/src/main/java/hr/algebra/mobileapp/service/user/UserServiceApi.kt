package hr.algebra.mobileapp.service.user

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.api.API
import hr.algebra.mobileapp.api.HttpMethod
import hr.algebra.mobileapp.models.StrangerProfile
import hr.algebra.mobileapp.models.UserProfile

/**
 * **Production** user service — delegates every call to the BrewMap REST API.
 */
class UserServiceApi : IUserService {

    // ── GET api/User/me ───────────────────────────────────────────────────────

    override suspend fun getMyProfile(): UserProfile {
        val client = API.createClient()
        val res = client.request(
            endpoint     = "User/me",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<UserProfile>() {}
        ).getOrThrow()
        Log.d("UserServiceApi", "getMyProfile → ${res.username}")
        return res
    }

    // ── GET api/User/{id} ─────────────────────────────────────────────────────

    override suspend fun getUserById(id: String): StrangerProfile {
        val client = API.createClient()
        val res = client.request(
            endpoint     = "User/$id",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<StrangerProfile>() {}
        ).getOrThrow()
        Log.d("UserServiceApi", "getUserById($id) → ${res.username}")
        return res
    }

    // ── PUT api/User/email ────────────────────────────────────────────────────

    override suspend fun updateEmail(newEmail: String, currentPassword: String) {
        val client = API.createClient()
        client.request(
            endpoint     = "User/email",
            method       = HttpMethod.PUT,
            body         = mapOf("newEmail" to newEmail, "currentPassword" to currentPassword),
            responseType = object : TypeToken<Map<String, Any>>() {}
        ).getOrThrow()
        Log.d("UserServiceApi", "updateEmail → done")
    }

    // ── PUT api/User/password ─────────────────────────────────────────────────

    override suspend fun updatePassword(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ) {
        val client = API.createClient()
        client.request(
            endpoint     = "User/password",
            method       = HttpMethod.PUT,
            body         = mapOf(
                "currentPassword"    to currentPassword,
                "newPassword"        to newPassword,
                "confirmNewPassword" to confirmNewPassword
            ),
            responseType = object : TypeToken<Map<String, Any>>() {}
        ).getOrThrow()
        Log.d("UserServiceApi", "updatePassword → done")
    }
}
