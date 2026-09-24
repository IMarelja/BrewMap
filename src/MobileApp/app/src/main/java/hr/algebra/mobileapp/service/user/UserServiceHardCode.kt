package hr.algebra.mobileapp.service.user

import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.auth.TokenManager
import hr.algebra.mobileapp.models.user.StrangerProfile
import hr.algebra.mobileapp.models.user.UserProfile
import hr.algebra.mobileapp.service.HardCodeData

/**
 * **Test / offline** userService service — no network required.
 *
 * Reads and writes the in-memory [HardCodeData.users] list.
 * The currently authenticated userService is identified via [TokenManager.getUserId].
 */
class UserServiceHardCode(private val data: HardCodeData) : IUserService {

    override suspend fun getMyProfile(): ServiceResult<UserProfile> {
        val userId = TokenManager.getUserId()
            ?: return ServiceResult.failure("Not authenticated")
        val user = data.users.find { it.id == userId }
            ?: return ServiceResult.failure("User not found")
        return ServiceResult.success(UserProfile(id = user.id, username = user.username, email = user.email))
    }

    override suspend fun getUserById(id: String): ServiceResult<StrangerProfile> {
        val user = data.users.find { it.id == id }
            ?: return ServiceResult.failure("User not found")
        return ServiceResult.success(StrangerProfile(id = user.id, username = user.username))
    }

    override suspend fun updateEmail(newEmail: String, currentPassword: String): ServiceResult<Unit> {
        val userId = TokenManager.getUserId()
            ?: return ServiceResult.failure("Not authenticated")
        val index = data.users.indexOfFirst { it.id == userId }
        if (index == -1) return ServiceResult.failure("User not found")
        val user = data.users[index]
        if (user.password != currentPassword) return ServiceResult.failure("Incorrect password")
        data.users[index] = user.copy(email = newEmail)
        return ServiceResult.success(Unit)
    }

    override suspend fun updatePassword(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): ServiceResult<Unit> {
        val userId = TokenManager.getUserId()
            ?: return ServiceResult.failure("Not authenticated")
        val index = data.users.indexOfFirst { it.id == userId }
        if (index == -1) return ServiceResult.failure("User not found")
        val user = data.users[index]
        if (user.password != currentPassword) return ServiceResult.failure("Incorrect current password")
        if (newPassword != confirmNewPassword) return ServiceResult.failure("Passwords do not match")
        data.users[index] = user.copy(password = newPassword)
        return ServiceResult.success(Unit)
    }
}
