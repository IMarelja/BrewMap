package hr.algebra.mobileapp.service.category

import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.models.category.Category

/**
 * Contract for category read operations.
 *
 * Three implementations are available:
 *  - [CategoryServiceApi]        — real HTTP calls to `api/Category`  (requires auth token)
 *  - [CategoryServiceHardCode]   — in-memory stub, no network needed
 *  - [CategoryServicePersistent] — API-backed with on-device cache ([hr.algebra.mobileapp.cache.PersistentCache])
 *
 * Switch between them via [hr.algebra.mobileapp.service.ServiceProvider].
 *
 * Every method returns [ServiceResult]<T>. On success [ServiceResult.data] holds the result;
 * on failure [ServiceResult.errors] contains one or more human-readable messages.
 */
interface ICategoryService {

    /**
     * Fetch every active category.
     * Maps to `GET api/Category`.
     */
    suspend fun getAll(): ServiceResult<List<Category>>

    /**
     * Fetch one category by its unique tag string (e.g. "cafe", "bar").
     * Maps to `GET api/Category/{tag}`.
     * Returns [ServiceResult] with data=null when no category matches [tag] (not an error).
     */
    suspend fun getByTag(tag: String): ServiceResult<Category?>
}
