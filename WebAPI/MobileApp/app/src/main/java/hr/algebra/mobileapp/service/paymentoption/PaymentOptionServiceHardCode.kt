package hr.algebra.mobileapp.service.paymentoption

import hr.algebra.mobileapp.models.PaymentOption
import hr.algebra.mobileapp.service.HardCodeData

/**
 * **Test / offline** payment option service — no network required.
 *
 * All seed data lives in [HardCodeData], which is created once by
 * [hr.algebra.mobileapp.service.ServiceProvider] and shared across every
 * hard-code stub. This class only contains query logic.
 */
class PaymentOptionServiceHardCode(private val data: HardCodeData) : IPaymentOptionService {

    override suspend fun getAll(): List<PaymentOption> = data.paymentOptions

    override suspend fun getByTag(tag: String): PaymentOption? =
        data.paymentOptions.find { it.tag.equals(tag, ignoreCase = true) }
}
