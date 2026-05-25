package hr.algebra.mobileapp.service

import hr.algebra.mobileapp.service.auth.AuthServiceApi
import hr.algebra.mobileapp.service.auth.AuthServiceHardCode
import hr.algebra.mobileapp.service.auth.IAuthService
import hr.algebra.mobileapp.service.location.ILocationService
import hr.algebra.mobileapp.service.location.LocationServiceApi
import hr.algebra.mobileapp.service.location.LocationServiceHardCode

/**
 * Application-wide service locator.
 *
 * A **single toggle** switches the entire app between the real API and the
 * hard-coded stubs.  When hard-code mode is active, one [HardCodeData]
 * instance is created and shared across every stub — so a user registered via
 * [auth] is immediately visible to any other service that reads [HardCodeData.users].
 *
 * ## How to switch
 *
 * ```kotlin
 * private const val USE_HARDCODE = true   // ← offline / seeded data, no network
 * private const val USE_HARDCODE = false  // ← real BrewMap API
 * ```
 *
 * ## Usage
 *
 * ```kotlin
 * ServiceProvider.auth.login(user, password, rememberMe)
 * ServiceProvider.auth.register(email, username, password)
 *
 * ServiceProvider.product.getById(id)
 * ServiceProvider.product.search(lon, lat, query = "coffee")
 * ServiceProvider.product.getPins(minLon, maxLon, minLat, maxLat)
 * ```
 */
object ServiceProvider {

    // ─────────────────────────────────────────────────────────────────────────
    // 🔧  TOGGLE — flip this one line to switch ALL services simultaneously
    // ─────────────────────────────────────────────────────────────────────────
    private const val USE_HARDCODE = true
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Shared seed data — only created when [USE_HARDCODE] is `true`.
     * Passed by reference into every hard-code service so they all operate
     * on the same lists.
     */
    private val hardCodeData: HardCodeData? = if (USE_HARDCODE) HardCodeData() else null

    /** Authentication — login and register. */
    val auth: IAuthService = if (USE_HARDCODE) {
        AuthServiceHardCode(hardCodeData!!)
    } else {
        AuthServiceApi()
    }

    /** Locations — getById, search, and map pins. */
    val product: ILocationService = if (USE_HARDCODE) {
        LocationServiceHardCode(hardCodeData!!)
    } else {
        LocationServiceApi()
    }
}
