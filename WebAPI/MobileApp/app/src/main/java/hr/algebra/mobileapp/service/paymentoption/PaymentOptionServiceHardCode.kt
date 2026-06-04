package hr.algebra.mobileapp.service.paymentoption

import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.models.paymentoption.PaymentOption
import hr.algebra.mobileapp.service.HardCodeData

/**
 * **Test / offline** payment option service — no network required.
 *
 * All seed data lives in [HardCodeData], which is created once by
 * [hr.algebra.mobileapp.service.ServiceProvider] and shared across every
 * hard-code stub. This class only contains query logic.
 */
class PaymentOptionServiceHardCode(private val data: HardCodeData) : IPaymentOptionService {

    override suspend fun getAll(): ServiceResult<List<PaymentOption>> =
        ServiceResult.success(data.paymentOptions)

    override suspend fun getByTag(tag: String): ServiceResult<PaymentOption?> =
        ServiceResult.success(data.paymentOptions.find { it.tag.equals(tag, ignoreCase = true) })
}
