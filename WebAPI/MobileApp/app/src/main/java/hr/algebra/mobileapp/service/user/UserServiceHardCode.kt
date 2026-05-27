package hr.algebra.mobileapp.service.user

import hr.algebra.mobileapp.auth.TokenManager
import hr.algebra.mobileapp.models.StrangerProfile
import hr.algebra.mobileapp.models.UserProfile
import hr.algebra.mobileapp.service.HardCodeData

/**
 * **Test / offline** user service — no network required.
 *
 * Reads and writes the in-memory [HardCodeData.users] list.
 * The currently authenticated user is identified via [TokenManager.getUserId].
 */
class UserServiceHardCode(private val data: HardCodeData) : IUserService {

    override suspend fun getMyProfile(): UserProfile {
        val userId = TokenManager.getUserId() ?: throw IllegalStateException("Not authenticated")
        val user   = data.users.find { it.id == userId }
            ?: throw NoSuchElementException("User not found: $userId")
        return UserProfile(id = user.id, username = user.username, email = user.email)
    }

    override suspend fun getUserById(id: String): StrangerProfile {
        val user = data.users.find { it.id == id }
            ?: throw NoSuchElementException("User not found: $id")
        return StrangerProfile(id = user.id, username = user.username)
    }

    override suspend fun updateEmail(newEmail: String, currentPassword: String) {
        val userId = TokenManager.getUserId() ?: throw IllegalStateException("Not authenticated")
        val index  = data.users.indexOfFirst { it.id == userId }
        if (index == -1) throw NoSuchElementException("User not found: $userId")
        val user = data.users[index]
        if (user.password != currentPassword) throw Exception("Incorrect password")
        data.users[index] = user.copy(email = newEmail)
    }

    override suspend fun updatePassword(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ) {
        val userId = TokenManager.getUserId() ?: throw IllegalStateException("Not authenticated")
        val index  = data.users.indexOfFirst { it.id == userId }
        if (index == -1) throw NoSuchElementException("User not found: $userId")
        val user = data.users[index]
        if (user.password != currentPassword)  throw Exception("Incorrect current password")
        if (newPassword != confirmNewPassword) throw Exception("Passwords do not match")
        data.users[index] = user.copy(password = newPassword)
    }
}
