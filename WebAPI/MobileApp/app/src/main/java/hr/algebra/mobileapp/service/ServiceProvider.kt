package hr.algebra.mobileapp.service

import hr.algebra.mobileapp.service.auth.AuthServiceApi
import hr.algebra.mobileapp.service.auth.AuthServiceHardCode
import hr.algebra.mobileapp.service.auth.IAuthService
import hr.algebra.mobileapp.service.category.CategoryServiceApi
import hr.algebra.mobileapp.service.category.CategoryServiceHardCode
import hr.algebra.mobileapp.service.category.CategoryServicePersistent
import hr.algebra.mobileapp.service.category.ICategoryService
import hr.algebra.mobileapp.service.drink.DrinkServiceApi
import hr.algebra.mobileapp.service.drink.DrinkServiceHardCode
import hr.algebra.mobileapp.service.drink.DrinkServicePersistent
import hr.algebra.mobileapp.service.drink.IDrinkService
import hr.algebra.mobileapp.service.flag.FlagServiceApi
import hr.algebra.mobileapp.service.flag.FlagServiceHardCode
import hr.algebra.mobileapp.service.flag.IFlagService
import hr.algebra.mobileapp.service.location.ILocationService
import hr.algebra.mobileapp.service.location.LocationServiceApi
import hr.algebra.mobileapp.service.location.LocationServiceHardCode
import hr.algebra.mobileapp.service.location.LocationServicePersistent
import hr.algebra.mobileapp.service.paymentoption.IPaymentOptionService
import hr.algebra.mobileapp.service.paymentoption.PaymentOptionServiceApi
import hr.algebra.mobileapp.service.paymentoption.PaymentOptionServiceHardCode
import hr.algebra.mobileapp.service.paymentoption.PaymentOptionServicePersistent
import hr.algebra.mobileapp.service.review.IReviewService
import hr.algebra.mobileapp.service.review.ReviewServiceApi
import hr.algebra.mobileapp.service.review.ReviewServiceHardCode
import hr.algebra.mobileapp.service.review.ReviewServicePersistent
import hr.algebra.mobileapp.BuildConfig
import hr.algebra.mobileapp.service.user.IUserService
import hr.algebra.mobileapp.service.user.UserServiceApi
import hr.algebra.mobileapp.service.user.UserServiceHardCode

/**
 * Application-wide service locator.
 *
 * The active mode is driven by `APP_MODE` in the repository root `.env` file.
 * Change it there and rebuild — every service switches together.
 * The build fails immediately if the value is missing or not one of the three valid keys.
 *
 * ## Modes
 * | `.env` value        | Data source                                     |
 * |---------------------|-------------------------------------------------|
 * | `HARD_CODE`         | In-memory seed data, no network                 |
 * | `API`               | Live BrewMap REST API, no local cache           |
 * | `PERSISTENT`        | Live API with on-device cache ([hr.algebra.mobileapp.cache.PersistentCache]) |
 *
 * ## Usage
 * ```kotlin
 * ServiceProvider.auth.login(user, password, rememberMe)
 * ServiceProvider.location.search(lon, lat, query = "coffee")
 * ServiceProvider.location.create(request)
 * ServiceProvider.drink.getByLocationId(locationId)
 * ServiceProvider.drink.create(request)
 * ServiceProvider.review.createForLocation(locationId, rating = 5, comment = "Great!")
 * ServiceProvider.user.getMyProfile()
 * ServiceProvider.flag.create("location", locationId, reason = "Spam", description = null)
 * ```
 */
object ServiceProvider {

    // ─────────────────────────────────────────────────────────────────────────
    // Mode is sourced from BuildConfig.APP_MODE, which is injected at compile
    // time from APP_MODE in the repository root .env file.
    // ─────────────────────────────────────────────────────────────────────────
    private val MODE: Mode = Mode.fromKey(BuildConfig.APP_MODE)
    // ─────────────────────────────────────────────────────────────────────────

    enum class Mode(val key: String) {
        HARD_CODE("HARD_CODE"),
        API("API"),
        PERSISTENT("PERSISTENT");

        companion object {
            fun fromKey(value: String): Mode =
                entries.firstOrNull { it.key == value }
                    ?: error(
                        "Unknown APP_MODE '$value'. " +
                        "Valid values: ${entries.joinToString { it.key }}"
                    )
        }
    }

    /** Shared seed data — only created when [MODE] is [Mode.HARD_CODE]. */
    private val hardCodeData: HardCodeData? =
        if (MODE == Mode.HARD_CODE) HardCodeData() else null

    // ── Auth ──────────────────────────────────────────────────────────────────

    val auth: IAuthService = when (MODE) {
        Mode.HARD_CODE  -> AuthServiceHardCode(hardCodeData!!)
        Mode.API -> AuthServiceApi()
        Mode.PERSISTENT -> AuthServiceApi()
    }

    // ── Location ──────────────────────────────────────────────────────────────

    val location: ILocationService = when (MODE) {
        Mode.HARD_CODE  -> LocationServiceHardCode(hardCodeData!!)
        Mode.API        -> LocationServiceApi()
        Mode.PERSISTENT -> LocationServicePersistent()
    }

    // ── Drink ─────────────────────────────────────────────────────────────────

    val drink: IDrinkService = when (MODE) {
        Mode.HARD_CODE  -> DrinkServiceHardCode(hardCodeData!!)
        Mode.API        -> DrinkServiceApi()
        Mode.PERSISTENT -> DrinkServicePersistent()
    }

    // ── Category ─────────────────────────────────────────────────────────────

    val category: ICategoryService = when (MODE) {
        Mode.HARD_CODE  -> CategoryServiceHardCode(hardCodeData!!)
        Mode.API        -> CategoryServiceApi()
        Mode.PERSISTENT -> CategoryServicePersistent()
    }

    // ── Payment option ────────────────────────────────────────────────────────

    val paymentOption: IPaymentOptionService = when (MODE) {
        Mode.HARD_CODE  -> PaymentOptionServiceHardCode(hardCodeData!!)
        Mode.API        -> PaymentOptionServiceApi()
        Mode.PERSISTENT -> PaymentOptionServicePersistent()
    }

    // ── Review ────────────────────────────────────────────────────────────────

    val review: IReviewService = when (MODE) {
        Mode.HARD_CODE  -> ReviewServiceHardCode(hardCodeData!!)
        Mode.API        -> ReviewServiceApi()
        Mode.PERSISTENT -> ReviewServicePersistent()
    }

    // ── User ──────────────────────────────────────────────────────────────────

    /** User profile — getMyProfile, getUserById, updateEmail, updatePassword. */
    val user: IUserService = when (MODE) {
        Mode.HARD_CODE  -> UserServiceHardCode(hardCodeData!!)
        Mode.API,
        Mode.PERSISTENT -> UserServiceApi()   // user data is personal — no caching
    }

    // ── Flag ──────────────────────────────────────────────────────────────────

    /** Content reporting — create a flag against a location, drink, review, or user. */
    val flag: IFlagService = when (MODE) {
        Mode.HARD_CODE  -> FlagServiceHardCode()
        Mode.API,
        Mode.PERSISTENT -> FlagServiceApi()
    }
}
