package hr.algebra.mobileapp.service.flag

import hr.algebra.mobileapp.models.Flag

/**
 * Contract for the user-facing flag (report) operation.
 *
 * Two implementations:
 *  - [FlagServiceApi]      — live BrewMap REST API (requires auth token)
 *  - [FlagServiceHardCode] — in-memory stub, no network
 */
interface IFlagService {

    /**
     * `POST api/Flag` — reports a piece of content.
     *
     * @param targetType one of: `"location"`, `"product"`, `"review"`, `"user"`
     * @param targetId   MongoDB ObjectId of the entity being reported
     * @param reason     short description (≥ 3 chars, required by the backend)
     * @param description optional longer explanation
     */
    suspend fun create(
        targetType: String,
        targetId: String,
        reason: String,
        description: String?
    ): Flag
}
