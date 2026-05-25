package hr.algebra.mobileapp.service.paymentoption

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
 */
interface IPaymentOptionService {

    /**
     * Fetch every active payment option.
     * Maps to `GET api/PaymentOption`.
     */
    suspend fun getAll(): List<PaymentOption>

    /**
     * Fetch one payment option by its unique tag string (e.g. "cash", "card", "mobile").
     * Maps to `GET api/PaymentOption/{tag}`.
     * Returns null when no payment option matches [tag].
     */
    suspend fun getByTag(tag: String): PaymentOption?
}
