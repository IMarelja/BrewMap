package hr.algebra.mobileapp.service

import hr.algebra.mobileapp.models.Address
import hr.algebra.mobileapp.models.DayOpeningHours
import hr.algebra.mobileapp.models.Location

/**
 * Shared in-memory seed data for all hard-code service stubs.
 *
 * Instantiated **once** by [ServiceProvider] only when `USE_HARDCODE = true`,
 * then injected into every hard-code service via constructor.  Neither stub
 * creates or duplicates this data — they all read from (and, in the case of
 * [users], write to) the single shared instance.
 *
 * Data mirrors `init-db/02-seed.js`.
 */
class HardCodeData {

    // ── Users ─────────────────────────────────────────────────────────────────

    data class MockUser(
        val id: String,
        val username: String,
        val email: String,
        val password: String,
        val role: String
    )

    /**
     * Mutable so [hr.algebra.mobileapp.service.auth.AuthServiceHardCode] can
     * append newly "registered" users for the lifetime of the session.
     */
    val users: MutableList<MockUser> = mutableListOf(
        MockUser(
            id       = "hc-001",
            username = "admin",
            email    = "admin@brewmap.dev",
            password = "Password1!",
            role     = "admin"
        ),
        MockUser(
            id       = "hc-002",
            username = "john_doe",
            email    = "john@brewmap.dev",
            password = "Password1!",
            role     = "user"
        ),
        MockUser(
            id       = "hc-003",
            username = "zuzu",
            email    = "zuzu@brewmap.dev",
            password = "Password1!",
            role     = "user"
        )
    )

    // ── Locations ─────────────────────────────────────────────────────────────

    /** Immutable seed — 12 Zagreb venues from `init-db/02-seed.js`. */
    val locations: List<Location> = listOf(
        loc(
            id = "hc-loc-001", name = "Pivana",
            description = "Spacious neighborhood pub with craft beer on tap, classic bar snacks, and a relaxed evening crowd.",
            street = "Varšavska 11", lon = 15.8457503, lat = 45.7976803,
            categoryTag = "cafe", paymentTags = listOf("cash", "card"),
            hours = hours("08:00", "22:00"), averageRating = 4.67, totalReviews = 3
        ),
        loc(
            id = "hc-loc-002", name = "Vinkl",
            description = "A lively local hangout known for quick coffee, friendly service, and easy after-work drinks.",
            street = "Ilica 63", lon = 15.8457503, lat = 45.7976803,
            categoryTag = "bar", paymentTags = listOf("cash", "card", "mobile"),
            hours = hours("08:00", "22:00"), averageRating = 4.0, totalReviews = 2
        ),
        loc(
            id = "hc-loc-003", name = "Botaničar",
            description = "Green, artsy cafe bar near the botanical garden with specialty coffee by day and cocktails at night.",
            street = "Mesnička 6", lon = 15.8457503, lat = 45.7976803,
            categoryTag = "cafe", paymentTags = listOf("cash"),
            hours = hours("07:00", "20:00"), averageRating = 4.0, totalReviews = 2
        ),
        loc(
            id = "hc-loc-004", name = "Caffe Bar SKA",
            description = "Casual city-center bar with laid-back music, strong espresso, and a dependable terrace scene.",
            street = "Preradovićeva 11", lon = 15.8457503, lat = 45.7976803,
            categoryTag = "cafe", paymentTags = listOf("cash", "card"),
            hours = hours("08:00", "21:00"), averageRating = 4.5, totalReviews = 2
        ),
        loc(
            id = "hc-loc-005", name = "Sabotage",
            description = "Underground-style bar with alternative playlists, late-night energy, and a solid cocktail list.",
            street = "Gajeva 14", lon = 15.8457503, lat = 45.7976803,
            categoryTag = "cafe", paymentTags = listOf("cash", "card", "mobile"),
            hours = hoursClosedSunday("08:00", "20:00"), averageRating = 0.0, totalReviews = 0
        ),
        loc(
            id = "hc-loc-006", name = "Paooro Cocktail Bar",
            description = "Modern cocktail bar mixing classic and signature drinks in an intimate, stylish setting.",
            street = "Petrinjska 4", lon = 15.937394, lat = 45.779911,
            categoryTag = "bar", paymentTags = listOf("cash", "card"),
            hours = hours("07:00", "20:00"), averageRating = 0.0, totalReviews = 0
        ),
        loc(
            id = "hc-loc-007", name = "Leggiero Malešnica",
            description = "Contemporary coffee spot with consistent espresso, comfortable seating, and a calm neighborhood vibe.",
            street = "Jurišićeva 9", lon = 15.907584, lat = 45.806690,
            categoryTag = "coffeeshop", paymentTags = listOf("cash"),
            hours = hours("09:00", "21:00"), averageRating = 3.5, totalReviews = 2
        ),
        loc(
            id = "hc-loc-008", name = "Mr. Jack Bar",
            description = "Compact bar with a social atmosphere, straightforward drinks menu, and frequent local regulars.",
            street = "Bogovićeva 7", lon = 15.885141, lat = 45.810210,
            categoryTag = "restaurant", paymentTags = listOf("cash", "mobile"),
            hours = hoursClosedSunday("06:30", "18:00"), averageRating = 4.0, totalReviews = 2
        ),
        loc(
            id = "hc-loc-009", name = "cafe Vanilla",
            description = "Warm cafe patisserie offering quality coffee, fresh pastries, and a cozy all-day ambience.",
            street = "Tratinska 22", lon = 15.934580, lat = 45.815435,
            categoryTag = "cafe", paymentTags = listOf("cash", "card"),
            hours = hoursClosedSunday("07:00", "19:00"), averageRating = 4.5, totalReviews = 2
        ),
        loc(
            id = "hc-loc-010", name = "Koncept Mlinček by Voilà",
            description = "Boutique concept cafe blending artisan desserts, specialty coffee, and elegant interior design.",
            street = "Vlaška 49", lon = 15.941468, lat = 45.810268,
            categoryTag = "cafe", paymentTags = listOf("cash", "card", "mobile"),
            hours = hours("08:00", "22:00"), averageRating = 4.5, totalReviews = 2
        ),
        loc(
            id = "hc-loc-011", name = "Old School Cafe",
            description = "Retro-inspired cafe with classic decor, affordable drinks, and a relaxed old-Zagreb feel.",
            street = "Savska cesta 144", lon = 15.940502, lat = 45.810228,
            categoryTag = "cafe", paymentTags = listOf("cash", "card"),
            hours = hours("08:00", "22:00"), averageRating = 0.0, totalReviews = 0
        ),
        loc(
            id = "hc-loc-012", name = "Caffe bar Crni mačak",
            description = "Beloved alternative cafe bar with eclectic music, character-filled interior, and a loyal local crowd.",
            street = "Mesnička 12", lon = 15.970731, lat = 45.815080,
            categoryTag = "cafe", paymentTags = listOf("cash", "card"),
            hours = hours("08:00", "22:00"), averageRating = 0.0, totalReviews = 0
        )
    )

    // ── Private seed-builder helpers ──────────────────────────────────────────

    private fun loc(
        id: String, name: String, description: String,
        street: String, lon: Double, lat: Double,
        categoryTag: String, paymentTags: List<String>,
        hours: Map<String, DayOpeningHours>,
        averageRating: Double, totalReviews: Int
    ) = Location(
        id                = id,
        name              = name,
        description       = description,
        address           = Address(street, "Zagreb", "Croatia", "10000"),
        longitude         = lon,
        latitude          = lat,
        categoryTag       = categoryTag,
        paymentOptionTags = paymentTags,
        openingHours      = hours,
        contact           = null,
        isActive          = true,
        averageRating     = averageRating,
        totalReviews      = totalReviews,
        createdAt         = "2025-01-01T00:00:00Z"
    )

    private fun hours(open: String, close: String): Map<String, DayOpeningHours> = mapOf(
        "monday"    to DayOpeningHours(open, close, false),
        "tuesday"   to DayOpeningHours(open, close, false),
        "wednesday" to DayOpeningHours(open, close, false),
        "thursday"  to DayOpeningHours(open, close, false),
        "friday"    to DayOpeningHours(open, close, false),
        "saturday"  to DayOpeningHours(open, close, false),
        "sunday"    to DayOpeningHours("09:00", "17:00", false)
    )

    private fun hoursClosedSunday(open: String, close: String): Map<String, DayOpeningHours> = mapOf(
        "monday"    to DayOpeningHours(open, close, false),
        "tuesday"   to DayOpeningHours(open, close, false),
        "wednesday" to DayOpeningHours(open, close, false),
        "thursday"  to DayOpeningHours(open, close, false),
        "friday"    to DayOpeningHours(open, close, false),
        "saturday"  to DayOpeningHours(open, close, false),
        "sunday"    to DayOpeningHours(null, null, true)
    )
}
