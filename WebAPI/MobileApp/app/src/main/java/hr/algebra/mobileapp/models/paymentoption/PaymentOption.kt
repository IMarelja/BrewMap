package hr.algebra.mobileapp.models.paymentoption

/**
 * Mirrors `ReadPaymentOption` DTO from the BrewMap API.
 *
 * Returned by `GET api/PaymentOption` (list) and `GET api/PaymentOption/{tag}` (single).
 * The [tag] field is the primary key used in a locationService's `paymentOptionTags` list.
 */
data class PaymentOption(
    val tag: String,
    val name: String
)