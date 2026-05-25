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
import hr.algebra.mobileapp.service.location.ILocationService
import hr.algebra.mobileapp.service.location.LocationServiceApi
import hr.algebra.mobileapp.service.location.LocationServiceHardCode
import hr.algebra.mobileapp.service.location.LocationServicePersistent
import hr.algebra.mobileapp.service.paymentoption.IPaymentOptionService
import hr.algebra.mobileapp.service.paymentoption.PaymentOptionServiceApi
import hr.algebra.mobileapp.service.paymentoption.PaymentOptionServiceHardCode
import hr.algebra.mobileapp.service.paymentoption.PaymentOptionServicePersistent

/**
 * Application-wide service locator.
 *
 * A **single toggle** switches the entire app between three data modes.
 * Change [MODE] and rebuild — every service switches together.
 *
 * ## Modes
 * | Value               | Data source                                     |
 * |---------------------|-------------------------------------------------|
 * | [Mode.HARD_CODE]    | In-memory seed data, no network (default)       |
 * | [Mode.API]          | Live BrewMap REST API, no local cache           |
 * | [Mode.PERSISTENT]   | Live API with on-device cache ([hr.algebra.mobileapp.cache.PersistentCache]) |
 *
 * ## How to switch
 * ```kotlin
 * private val MODE = Mode.HARD_CODE   // ← offline / seeded data
 * private val MODE = Mode.API         // ← always-fresh API calls
 * private val MODE = Mode.PERSISTENT  // ← API + SharedPreferences cache
 * ```
 *
 * ## Usage
 * ```kotlin
 * ServiceProvider.auth.login(user, password, rememberMe)
 * ServiceProvider.location.search(lon, lat, query = "coffee")
 * ServiceProvider.drink.getByLocationId(locationId)
 * ServiceProvider.category.getAll()
 * ServiceProvider.paymentOption.getAll()
 * ```
 */
object ServiceProvider {

    // ─────────────────────────────────────────────────────────────────────────
    // 🔧  TOGGLE — flip this one line to switch ALL services simultaneously
    // ─────────────────────────────────────────────────────────────────────────
    private val MODE = Mode.HARD_CODE
    // ─────────────────────────────────────────────────────────────────────────

    enum class Mode { HARD_CODE, API, PERSISTENT }

    /**
     * Shared seed data — only created when [MODE] is [Mode.HARD_CODE].
     * Passed by reference into every hard-code service so they all operate
     * on the same lists.
     */
    private val hardCodeData: HardCodeData? =
        if (MODE == Mode.HARD_CODE) HardCodeData() else null

    // ── Auth ──────────────────────────────────────────────────────────────────

    /** Authentication — login and register. */
    val auth: IAuthService = when (MODE) {
        Mode.HARD_CODE  -> AuthServiceHardCode(hardCodeData!!)
        Mode.API,
        Mode.PERSISTENT -> AuthServiceApi()   // auth has no cache layer
    }

    // ── Location ──────────────────────────────────────────────────────────────

    /** Locations — getById, search, and map pins. */
    val location: ILocationService = when (MODE) {
        Mode.HARD_CODE  -> LocationServiceHardCode(hardCodeData!!)
        Mode.API        -> LocationServiceApi()
        Mode.PERSISTENT -> LocationServicePersistent()
    }

    // ── Drink ─────────────────────────────────────────────────────────────────

    /** Drinks — getById, getByLocationId, getBestDrinkByLocationId. */
    val drink: IDrinkService = when (MODE) {
        Mode.HARD_CODE  -> DrinkServiceHardCode(hardCodeData!!)
        Mode.API        -> DrinkServiceApi()
        Mode.PERSISTENT -> DrinkServicePersistent()
    }

    // ── Category ─────────────────────────────────────────────────────────────

    /** Categories — getAll, getByTag. */
    val category: ICategoryService = when (MODE) {
        Mode.HARD_CODE  -> CategoryServiceHardCode(hardCodeData!!)
        Mode.API        -> CategoryServiceApi()
        Mode.PERSISTENT -> CategoryServicePersistent()
    }

    // ── Payment option ────────────────────────────────────────────────────────

    /** Payment options — getAll, getByTag. */
    val paymentOption: IPaymentOptionService = when (MODE) {
        Mode.HARD_CODE  -> PaymentOptionServiceHardCode(hardCodeData!!)
        Mode.API        -> PaymentOptionServiceApi()
        Mode.PERSISTENT -> PaymentOptionServicePersistent()
    }
}
