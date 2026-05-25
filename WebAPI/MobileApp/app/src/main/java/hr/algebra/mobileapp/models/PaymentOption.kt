package hr.algebra.mobileapp.models

/**
 * Mirrors `ReadPaymentOption` DTO from the BrewMap API.
 *
 * Returned by `GET api/PaymentOption` (list) and `GET api/PaymentOption/{tag}` (single).
 * The [tag] field is the primary key used in a location's `paymentOptionTags` list.
 */
data class PaymentOption(
    val tag: String,
    val name: String
)
