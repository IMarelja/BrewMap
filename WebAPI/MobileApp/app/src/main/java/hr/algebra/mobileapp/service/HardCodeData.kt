package hr.algebra.mobileapp.service

import hr.algebra.mobileapp.models.location.Address
import hr.algebra.mobileapp.models.drink.AggregatedRating
import hr.algebra.mobileapp.models.category.Category
import hr.algebra.mobileapp.models.location.DayOpeningHours
import hr.algebra.mobileapp.models.drink.Drink
import hr.algebra.mobileapp.models.location.Location
import hr.algebra.mobileapp.models.paymentoption.PaymentOption
import hr.algebra.mobileapp.models.review.Review

/**
 * Shared in-memory seed data for all hard-code service stubs.
 *
 * Instantiated **once** by [ServiceProvider] only when hard-code mode is active,
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
            role     = "userService"
        ),
        MockUser(
            id       = "hc-003",
            username = "zuzu",
            email    = "zuzu@brewmap.dev",
            password = "Password1!",
            role     = "userService"
        )
    )

    // ── Categories ────────────────────────────────────────────────────────────

    /** Matches the categoryService tags used in [locations]. */
    val categories: List<Category> = listOf(
        Category(tag = "cafe",        name = "Cafe"),
        Category(tag = "bar",         name = "Bar"),
        Category(tag = "coffeeshop",  name = "Coffee Shop"),
        Category(tag = "restaurant",  name = "Restaurant")
    )

    // ── Payment options ───────────────────────────────────────────────────────

    /** Matches the payment option tags used in [locations]. */
    val paymentOptions: List<PaymentOption> = listOf(
        PaymentOption(tag = "cash",   name = "Cash"),
        PaymentOption(tag = "card",   name = "Credit / Debit Card"),
        PaymentOption(tag = "mobile", name = "Mobile Payment")
    )

    // ── Locations ─────────────────────────────────────────────────────────────

    /** Mutable so [hr.algebra.mobileapp.service.location.LocationServiceHardCode] can create/update locations. */
    val locations: MutableList<Location> = mutableListOf(
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

    // ── Drinks ────────────────────────────────────────────────────────────────

    /**
     * Seed drinks spread across several seeded locations.
     * Each drinkService maps to one [Location] via [Drink.availableAtLocationId].
     */
    /** Mutable so [hr.algebra.mobileapp.service.drink.DrinkServiceHardCode] can create/update drinks. */
    val drinks: MutableList<Drink> = mutableListOf(

        // Pivana (hc-loc-001) — 3 drinks
        drink("hc-drk-001", "Ožujsko Pivo",       "Classic Croatian lager on tap.",    "hc-loc-001", 4.8, 3),
        drink("hc-drk-002", "Karlovačko Tamno",   "Dark lager with a malty finish.",   "hc-loc-001", 4.5, 2),
        drink("hc-drk-003", "Aperol Spritz",       "Refreshing Italian aperitif.",      "hc-loc-001", 4.7, 1),

        // Vinkl (hc-loc-002) — 2 drinks
        drink("hc-drk-004", "Flat White",          "Double ristretto with steamed milk.", "hc-loc-002", 4.2, 2),
        drink("hc-drk-005", "Craft IPA",           "Hoppy local India pale ale.",        "hc-loc-002", 3.8, 1),

        // Botaničar (hc-loc-003) — 2 drinks
        drink("hc-drk-006", "Cold Brew",           "12-hour cold-extracted coffee.",    "hc-loc-003", 4.5, 2),
        drink("hc-drk-007", "Gin & Tonic",         "Premium gin with Fever-Tree tonic.", "hc-loc-003", 3.5, 1),

        // Caffe Bar SKA (hc-loc-004) — 2 drinks
        drink("hc-drk-008", "Espresso",            "Short, intense double shot.",       "hc-loc-004", 4.6, 2),
        drink("hc-drk-009", "Cappuccino",          "Classic Italian-style cappuccino.", "hc-loc-004", 4.4, 1),

        // Leggiero Malešnica (hc-loc-007) — 2 drinks
        drink("hc-drk-010", "Cortado",             "Equal parts espresso and warm milk.", "hc-loc-007", 3.7, 2),
        drink("hc-drk-011", "Oat Milk Latte",      "Espresso with steamed oat milk.",  "hc-loc-007", 3.3, 1),

        // Mr. Jack Bar (hc-loc-008) — 2 drinks
        drink("hc-drk-012", "Jack Daniel's Cola",  "Bourbon and cola over ice.",        "hc-loc-008", 4.1, 2),
        drink("hc-drk-013", "Whiskey Sour",        "Whiskey, lemon juice, simple syrup.", "hc-loc-008", 3.9, 1),

        // Cafe Vanilla (hc-loc-009) — 2 drinks
        drink("hc-drk-014", "Vanilla Latte",       "Espresso with vanilla-infused milk.", "hc-loc-009", 4.6, 2),
        drink("hc-drk-015", "Hot Chocolate",       "Thick Belgian chocolate drinkService.",    "hc-loc-009", 4.4, 1),

        // Koncept Mlinček (hc-loc-010) — 2 drinks
        drink("hc-drk-016", "Specialty Filter",   "Single-origin pour-over coffee.",  "hc-loc-010", 4.7, 2),
        drink("hc-drk-017", "Matcha Latte",        "Ceremonial matcha with oat milk.", "hc-loc-010", 4.3, 1)
    )

    // ── Reviews ───────────────────────────────────────────────────────────────

    /**
     * Seed reviews for seeded locations and drinks.
     * Ratings match the [averageRating] / [AggregatedRating.average] values
     * already set on those objects.
     *
     * targetType = "locationService" for locationService reviews,
     * targetType = "product"  for drinkService reviews  (mirrors the backend).
     */
    /** Mutable so [hr.algebra.mobileapp.service.review.ReviewServiceHardCode] can create/update/delete reviews. */
    val reviews: MutableList<Review> = mutableListOf(

        // ── Location reviews ──────────────────────────────────────────────────

        // Pivana (hc-loc-001) — 3 reviews → avg 4.67
        rev("hc-rev-001", "hc-001", "locationService", "hc-loc-001", 5, "Best craft beer in town!"),
        rev("hc-rev-002", "hc-002", "locationService", "hc-loc-001", 5, "Great vibe and cold beer."),
        rev("hc-rev-003", "hc-003", "locationService", "hc-loc-001", 4, "Solid place, a bit loud."),

        // Vinkl (hc-loc-002) — 2 reviews → avg 4.0
        rev("hc-rev-004", "hc-002", "locationService", "hc-loc-002", 4, "Quick coffee, friendly staff."),
        rev("hc-rev-005", "hc-003", "locationService", "hc-loc-002", 4, "Nice after-work spot."),

        // Botaničar (hc-loc-003) — 2 reviews → avg 4.0
        rev("hc-rev-006", "hc-001", "locationService", "hc-loc-003", 4, "Lovely garden terrace."),
        rev("hc-rev-007", "hc-002", "locationService", "hc-loc-003", 4, "Great specialty coffee."),

        // Caffe Bar SKA (hc-loc-004) — 2 reviews → avg 4.5
        rev("hc-rev-008", "hc-002", "locationService", "hc-loc-004", 4, "Dependable espresso."),
        rev("hc-rev-009", "hc-003", "locationService", "hc-loc-004", 5, "Love the terrace scene."),

        // Leggiero Malešnica (hc-loc-007) — 2 reviews → avg 3.5
        rev("hc-rev-010", "hc-002", "locationService", "hc-loc-007", 3, "Decent coffee, nothing special."),
        rev("hc-rev-011", "hc-003", "locationService", "hc-loc-007", 4, "Comfortable seating."),

        // Mr. Jack Bar (hc-loc-008) — 2 reviews → avg 4.0
        rev("hc-rev-012", "hc-001", "locationService", "hc-loc-008", 4, "Classic bourbon cocktails."),
        rev("hc-rev-013", "hc-002", "locationService", "hc-loc-008", 4, "Good atmosphere."),

        // Cafe Vanilla (hc-loc-009) — 2 reviews → avg 4.5
        rev("hc-rev-014", "hc-001", "locationService", "hc-loc-009", 4, "Fresh pastries every morning."),
        rev("hc-rev-015", "hc-003", "locationService", "hc-loc-009", 5, "Best vanilla latte in Zagreb!"),

        // Koncept Mlinček (hc-loc-010) — 2 reviews → avg 4.5
        rev("hc-rev-016", "hc-002", "locationService", "hc-loc-010", 4, "Elegant interior, great coffee."),
        rev("hc-rev-017", "hc-003", "locationService", "hc-loc-010", 5, "Wonderful specialty filter."),

        // ── Drink reviews ─────────────────────────────────────────────────────

        // Ožujsko Pivo (hc-drk-001) — 3 reviews → avg 4.8
        rev("hc-rev-018", "hc-001", "product", "hc-drk-001", 5, "Crisp and refreshing."),
        rev("hc-rev-019", "hc-002", "product", "hc-drk-001", 5, "Classic Croatian lager."),
        rev("hc-rev-020", "hc-003", "product", "hc-drk-001", 4, "Good on tap."),

        // Karlovačko Tamno (hc-drk-002) — 2 reviews → avg 4.5
        rev("hc-rev-021", "hc-002", "product", "hc-drk-002", 5, "Rich malty flavour."),
        rev("hc-rev-022", "hc-003", "product", "hc-drk-002", 4, "Solid dark lager."),

        // Cold Brew (hc-drk-006) — 2 reviews → avg 4.5
        rev("hc-rev-023", "hc-001", "product", "hc-drk-006", 5, "Smooth and punchy."),
        rev("hc-rev-024", "hc-002", "product", "hc-drk-006", 4, "Great cold brew."),

        // Espresso (hc-drk-008) — 2 reviews → avg 4.6
        rev("hc-rev-025", "hc-001", "product", "hc-drk-008", 5, "Perfect short shot."),
        rev("hc-rev-026", "hc-003", "product", "hc-drk-008", 4, "Very intense, love it."),

        // Vanilla Latte (hc-drk-014) — 2 reviews → avg 4.6
        rev("hc-rev-027", "hc-002", "product", "hc-drk-014", 5, "Perfectly sweet."),
        rev("hc-rev-028", "hc-003", "product", "hc-drk-014", 4, "Great flavour balance."),

        // Specialty Filter (hc-drk-016) — 2 reviews → avg 4.7
        rev("hc-rev-029", "hc-001", "product", "hc-drk-016", 5, "Incredible single origin."),
        rev("hc-rev-030", "hc-002", "product", "hc-drk-016", 4, "Very well brewed.")
    )

    // ── Private seed-builder helpers ──────────────────────────────────────────

    private fun rev(
        id: String, userId: String, targetType: String,
        targetId: String, rating: Int, comment: String
    ) = Review(
        id          = id,
        userId      = userId,
        username    = users.find { it.id == userId }?.username,
        targetType  = targetType,
        targetId    = targetId,
        rating      = rating,
        comment     = comment,
        isVisible   = true,
        reportCount = 0,
        createdAt   = "2025-01-01T00:00:00Z",
        updatedAt   = "2025-01-01T00:00:00Z"
    )

    private fun drink(
        id: String, name: String, description: String,
        locationId: String, avgRating: Double, ratingCount: Int
    ) = Drink(
        id                    = id,
        name                  = name,
        description           = description,
        availableAtLocationId = locationId,
        createdByUserId       = "hc-001",
        createdAt             = "2025-01-01T00:00:00Z",
        updatedAt             = "2025-01-01T00:00:00Z",
        aggregatedRating      = AggregatedRating(average = avgRating, count = ratingCount)
    )

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
