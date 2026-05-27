package hr.algebra.mobileapp.service.flag

import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.auth.TokenManager
import hr.algebra.mobileapp.models.Flag
import hr.algebra.mobileapp.models.ReportTarget
import java.time.Instant

/**
 * **Test / offline** flag service — stores reports in a session-scoped in-memory list.
 */
class FlagServiceHardCode : IFlagService {

    private val flags = mutableListOf<Flag>()

    override suspend fun create(
        targetType: String,
        targetId: String,
        reason: String,
        description: String?
    ): ServiceResult<Flag> {
        val userId = TokenManager.getUserId()
            ?: return ServiceResult.failure("Not authenticated")
        val now = Instant.now().toString()
        val flag = Flag(
            id               = "hc-flag-${System.currentTimeMillis()}",
            reportedByUserId = userId,
            target           = ReportTarget(type = targetType, id = targetId),
            reason           = reason,
            description      = description,
            status           = "pending",
            resolvedByAdminId = null,
            resolvedAt        = null,
            resolutionNote    = null,
            createdAt         = now,
            updatedAt         = now
        )
        flags.add(flag)
        return ServiceResult.success(flag)
    }
}
