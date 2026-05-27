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
 * ## Initialization
 * Every service property is **lazy** — it is constructed only the first time it is accessed.
 * In `API` / `PERSISTENT` mode, [HardCodeData] is never allocated at all because no lazy
 * property ever references it in those modes. [MODE] is the only eagerly-evaluated field.
 *
 * ## Usage
 * ```kotlin
 * ServiceProvider.authService.login(user, password, rememberMe)
 * ServiceProvider.locationService.search(lon, lat, query = "coffee")
 * ServiceProvider.locationService.create(request)
 * ServiceProvider.drinkService.getByLocationId(locationId)
 * ServiceProvider.drinkService.create(request)
 * ServiceProvider.reviewService.createForLocation(locationId, rating = 5, comment = "Great!")
 * ServiceProvider.userService.getMyProfile()
 * ServiceProvider.flagService.create("location", locationId, reason = "Spam", description = null)
 * ```
 */
object ServiceProvider {

    // ─────────────────────────────────────────────────────────────────────────
    // MODE is the only eagerly-evaluated field — it is a simple enum lookup
    // from BuildConfig and must be ready before any lazy initializer runs.
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

    /**
     * Shared seed data for the HARD_CODE mode.
     *
     * Lazy so it is only allocated when a HARD_CODE service property is first accessed.
     * In API / PERSISTENT mode no service property ever reads this field, so [HardCodeData]
     * is never constructed.
     */
    private val hardCodeData: HardCodeData by lazy { HardCodeData() }

    // ── Auth ──────────────────────────────────────────────────────────────────

    /** Authentication service — login, register */
    val authService: IAuthService by lazy {
        when (MODE) {
            Mode.HARD_CODE  -> AuthServiceHardCode(hardCodeData)
            Mode.API -> AuthServiceApi()
            Mode.PERSISTENT -> AuthServiceApi()
        }
    }

    // ── Location ──────────────────────────────────────────────────────────────

    /** Location service — getById, search, getPins, create, update */
    val locationService: ILocationService by lazy {
        when (MODE) {
            Mode.HARD_CODE  -> LocationServiceHardCode(hardCodeData)
            Mode.API        -> LocationServiceApi()
            Mode.PERSISTENT -> LocationServicePersistent()
        }
    }

    // ── Drink ─────────────────────────────────────────────────────────────────

    /** Drink service — getById, getByLocationId, getBestDrinkByLocationId, create, update */
    val drinkService: IDrinkService by lazy {
        when (MODE) {
            Mode.HARD_CODE  -> DrinkServiceHardCode(hardCodeData)
            Mode.API        -> DrinkServiceApi()
            Mode.PERSISTENT -> DrinkServicePersistent()
        }
    }

    // ── Category ─────────────────────────────────────────────────────────────

    /** Category service — getAll, getByTag */
    val categoryService: ICategoryService by lazy {
        when (MODE) {
            Mode.HARD_CODE  -> CategoryServiceHardCode(hardCodeData)
            Mode.API        -> CategoryServiceApi()
            Mode.PERSISTENT -> CategoryServicePersistent()
        }
    }

    // ── Payment option ────────────────────────────────────────────────────────

    /** Payment option service — getAll, getByTag */
    val paymentOptionService: IPaymentOptionService by lazy {
        when (MODE) {
            Mode.HARD_CODE  -> PaymentOptionServiceHardCode(hardCodeData)
            Mode.API        -> PaymentOptionServiceApi()
            Mode.PERSISTENT -> PaymentOptionServicePersistent()
        }
    }

    // ── Review ────────────────────────────────────────────────────────────────

    /** Review service — getById, getByLocationId, getByDrinkId, getMyReviews, getByUserId,
     *  createForLocation, createForDrink, update, delete */
    val reviewService: IReviewService by lazy {
        when (MODE) {
            Mode.HARD_CODE  -> ReviewServiceHardCode(hardCodeData)
            Mode.API        -> ReviewServiceApi()
            Mode.PERSISTENT -> ReviewServicePersistent()
        }
    }

    // ── User ──────────────────────────────────────────────────────────────────

    /** User profile — getMyProfile, getUserById, updateEmail, updatePassword. */
    val userService: IUserService by lazy {
        when (MODE) {
            Mode.HARD_CODE  -> UserServiceHardCode(hardCodeData)
            Mode.API -> UserServiceApi()
            Mode.PERSISTENT -> UserServiceApi()   // user data is personal — no caching
        }
    }

    // ── Flag ──────────────────────────────────────────────────────────────────

    /** Content reporting — create a flag against a location, drink, review, or user. */
    val flagService: IFlagService by lazy {
        when (MODE) {
            Mode.HARD_CODE  -> FlagServiceHardCode()
            Mode.API -> FlagServiceApi()
            Mode.PERSISTENT -> FlagServiceApi()
        }
    }
}
