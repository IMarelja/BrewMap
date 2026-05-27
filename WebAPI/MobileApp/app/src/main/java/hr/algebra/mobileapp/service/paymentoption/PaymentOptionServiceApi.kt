package hr.algebra.mobileapp.service.paymentoption

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.api.API
import hr.algebra.mobileapp.api.HttpMethod
import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.api.toServiceResult
import hr.algebra.mobileapp.models.PaymentOption

/**
 * **Production** payment option service — delegates every call to the BrewMap REST API.
 *
 * All endpoints require a valid JWT; the token is injected automatically by
 * [hr.algebra.mobileapp.api.API.createClient] via [hr.algebra.mobileapp.auth.TokenManager].
 */
class PaymentOptionServiceApi : IPaymentOptionService {

    // ── GET api/PaymentOption ─────────────────────────────────────────────────

    override suspend fun getAll(): ServiceResult<List<PaymentOption>> {
        val client = API.createClient()
        val result = client.request(
            endpoint     = "PaymentOption",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<List<PaymentOption>>() {}
        ).toServiceResult("Could not load payment options.")
        Log.d("PaymentOptionServiceApi", "getAll() → success=${result.isSuccess}")
        return result
    }

    // ── GET api/PaymentOption/{tag} ───────────────────────────────────────────

    override suspend fun getByTag(tag: String): ServiceResult<PaymentOption?> {
        val client = API.createClient()
        val result = client.request(
            endpoint     = "PaymentOption/$tag",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<PaymentOption?>() {}
        ).toServiceResult(treatNotFoundAsEmpty = true)
        Log.d("PaymentOptionServiceApi", "getByTag($tag) → data=${result.data}")
        return result
    }
}
