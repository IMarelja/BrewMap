package hr.algebra.mobileapp.service.paymentoption

import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.models.PaymentOption

/**
 * Contract for payment option read operations.
 *
 * Three implementations are available:
 *  - [PaymentOptionServiceApi]        — real HTTP calls to `api/PaymentOption` (requires auth token)
 *  - [PaymentOptionServiceHardCode]   — in-memory stub, no network needed
 *  - [PaymentOptionServicePersistent] — API-backed with on-device cache ([hr.algebra.mobileapp.cache.PersistentCache])
 *
 * Switch between them via [hr.algebra.mobileapp.service.ServiceProvider].
 *
 * Every method returns [ServiceResult]<T>. On success [ServiceResult.data] holds the result;
 * on failure [ServiceResult.errors] contains one or more human-readable messages.
 */
interface IPaymentOptionService {

    /**
     * Fetch every active payment option.
     * Maps to `GET api/PaymentOption`.
     */
    suspend fun getAll(): ServiceResult<List<PaymentOption>>

    /**
     * Fetch one payment option by its unique tag string (e.g. "cash", "card", "mobile").
     * Maps to `GET api/PaymentOption/{tag}`.
     * Returns [ServiceResult] with data=null when no payment option matches [tag] (not an error).
     */
    suspend fun getByTag(tag: String): ServiceResult<PaymentOption?>
}
