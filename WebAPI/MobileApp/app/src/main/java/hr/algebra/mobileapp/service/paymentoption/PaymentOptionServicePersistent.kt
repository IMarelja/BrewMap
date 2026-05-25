package hr.algebra.mobileapp.service.paymentoption

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.cache.PersistentCache
import hr.algebra.mobileapp.models.PaymentOption

/**
 * **Cache-aside** payment option service — serves data from [PersistentCache]
 * when available and fresh, falling back to [PaymentOptionServiceApi] on a miss.
 *
 * Payment options change as rarely as categories, so they use the 24-hour TTL.
 *
 * ## Cache keys & TTLs
 * | Operation   | Key                    | TTL   |
 * |-------------|------------------------|-------|
 * | [getAll]    | `payment_all`          | 24 h  |
 * | [getByTag]  | `payment_<tag>`        | 24 h  |
 */
class PaymentOptionServicePersistent : IPaymentOptionService {

    private val api = PaymentOptionServiceApi()

    override suspend fun getAll(): List<PaymentOption> {
        val key  = "payment_all"
        val type = object : TypeToken<List<PaymentOption>>() {}

        PersistentCache.get(key, type)?.let {
            Log.d("PaymentOptionServicePersistent", "cache hit: getAll() → ${it.size}")
            return it
        }

        val fresh = api.getAll()
        PersistentCache.put(key, fresh, PersistentCache.TTL_CATEGORIES)
        return fresh
    }

    override suspend fun getByTag(tag: String): PaymentOption? {
        val key  = "payment_$tag"
        val type = object : TypeToken<PaymentOption>() {}

        PersistentCache.get(key, type)?.let {
            Log.d("PaymentOptionServicePersistent", "cache hit: getByTag($tag)")
            return it
        }

        val fresh = api.getByTag(tag)
        if (fresh != null) {
            PersistentCache.put(key, fresh, PersistentCache.TTL_CATEGORIES)
        }
        return fresh
    }
}
