package hr.algebra.mobileapp.models.flag

/**
 * Identifies the reported entity.
 *
 * [type] — one of: `"locationService"`, `"product"` (drinkService), `"reviewService"`, `"userService"`.
 * [id]   — MongoDB ObjectId string of the entity.
 */
data class ReportTarget(
    val type: String,
    val id: String
)

/** Request body for `POST api/Flag`. */
data class CreateFlagRequest(
    val target: ReportTarget,
    val reason: String,
    val description: String?
)

/**
 * Mirrors `ReadFlag` from the BrewMap API.
 *
 * [status] — one of: `"pending"`, `"reviewed"`, `"resolved"`.
 */
data class Flag(
    val id: String,
    val reportedByUserId: String,
    val target: ReportTarget,
    val reason: String,
    val description: String?,
    val status: String,
    val resolvedByAdminId: String?,
    val resolvedAt: String?,
    val resolutionNote: String?,
    val createdAt: String,
    val updatedAt: String
)
