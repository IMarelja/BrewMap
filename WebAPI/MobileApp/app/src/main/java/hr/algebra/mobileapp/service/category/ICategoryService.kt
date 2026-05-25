package hr.algebra.mobileapp.service.category

import hr.algebra.mobileapp.models.Category

/**
 * Contract for category read operations.
 *
 * Three implementations are available:
 *  - [CategoryServiceApi]        — real HTTP calls to `api/Category`  (requires auth token)
 *  - [CategoryServiceHardCode]   — in-memory stub, no network needed
 *  - [CategoryServicePersistent] — API-backed with on-device cache ([hr.algebra.mobileapp.cache.PersistentCache])
 *
 * Switch between them via [hr.algebra.mobileapp.service.ServiceProvider].
 */
interface ICategoryService {

    /**
     * Fetch every active category.
     * Maps to `GET api/Category`.
     */
    suspend fun getAll(): List<Category>

    /**
     * Fetch one category by its unique tag string (e.g. "cafe", "bar").
     * Maps to `GET api/Category/{tag}`.
     * Returns null when no category matches [tag].
     */
    suspend fun getByTag(tag: String): Category?
}
