package hr.algebra.mobileapp.models.flag

/** Request body for `POST api/Flag`. */
data class CreateFlagRequest(
    val target: ReportTarget,
    val reason: String,
    val description: String?
)
