package hr.algebra.mobileapp.service.paymentoption

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.api.API
import hr.algebra.mobileapp.api.HttpMethod
import hr.algebra.mobileapp.models.PaymentOption

/**
 * **Production** payment option service — delegates every call to the BrewMap REST API.
 *
 * All endpoints require a valid JWT; the token is injected automatically by
 * [hr.algebra.mobileapp.api.API.createClient] via [hr.algebra.mobileapp.auth.TokenManager].
 *
 * Base URL is read from `res/values/strings.xml` → `api_base_url`.
 */
class PaymentOptionServiceApi : IPaymentOptionService {

    // ── GET api/PaymentOption ─────────────────────────────────────────────────

    override suspend fun getAll(): List<PaymentOption> {
        val client = API.createClient()
        val res = client.request(
            endpoint     = "PaymentOption",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<List<PaymentOption>>() {}
        ).getOrThrow()
        Log.d("PaymentOptionServiceApi", "getAll() → ${res.size} payment options")
        return res
    }

    // ── GET api/PaymentOption/{tag} ───────────────────────────────────────────

    override suspend fun getByTag(tag: String): PaymentOption? {
        return try {
            val client = API.createClient()
            val res = client.request(
                endpoint     = "PaymentOption/$tag",
                method       = HttpMethod.GET,
                responseType = object : TypeToken<PaymentOption>() {}
            ).getOrThrow()
            Log.d("PaymentOptionServiceApi", "getByTag($tag) → $res")
            res
        } catch (e: Exception) {
            // 404 — tag not found
            Log.d("PaymentOptionServiceApi", "getByTag($tag) → null (${e.message})")
            null
        }
    }
}
