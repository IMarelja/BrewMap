package hr.algebra.mobileapp.cache

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.BrewMapApp
import androidx.core.content.edit

/**
 * Generic on-device cache backed by [SharedPreferences] and Gson serialisation.
 *
 * Each entry is stored as a JSON blob paired with an expiry timestamp.
 * Stale entries are evicted lazily on the next [get] call.
 *
 * ## Pre-defined TTLs (use these for consistency)
 * | Constant            | Value | Suitable for              |
 * |---------------------|-------|---------------------------|
 * | [TTL_CATEGORIES]    | 24 h  | Category list (rarely changes) |
 * | [TTL_DRINKS]        | 15 min| Drinks per location        |
 * | [TTL_LOCATIONS]     |  5 min| Location detail / search   |
 *
 * ## Usage
 * ```kotlin
 * // write
 * PersistentCache.put("drink_by_loc_$id", drinks, PersistentCache.TTL_DRINKS)
 *
 * // read
 * val cached = PersistentCache.get("drink_by_loc_$id", object : TypeToken<List<Drink>>() {})
 * ```
 */
object PersistentCache {

    private const val TAG        = "PersistentCache"
    private const val PREFS_NAME = "brewmap_cache"

    /** 24-hour TTL — suitable for categories (rarely change). */
    const val TTL_CATEGORIES: Long = 24 * 60 * 60 * 1_000L

    /** 15-minute TTL — suitable for drinks per location. */
    const val TTL_DRINKS: Long = 15 * 60 * 1_000L

    /** 5-minute TTL — suitable for location detail / search results. */
    const val TTL_LOCATIONS: Long = 5 * 60 * 1_000L

    private val gson: Gson = Gson()

    private val prefs: SharedPreferences by lazy {
        BrewMapApp.appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Serialise [value] to JSON and store it under [key] with the given [ttlMs].
     */
    fun <T> put(key: String, value: T, ttlMs: Long = TTL_LOCATIONS) {
        val entry = CacheEntry(
            expiresAt = System.currentTimeMillis() + ttlMs,
            data      = gson.toJson(value)
        )
        prefs.edit { putString(key, gson.toJson(entry)) }
        Log.d(TAG, "cached '$key' (ttl=${ttlMs / 1000}s)")
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    /**
     * Return the cached value for [key], or null if absent / expired.
     *
     * @param key  storage key used in [put]
     * @param type Gson [TypeToken] describing the expected return type
     */
    fun <T> get(key: String, type: TypeToken<T>): T? {
        val raw   = prefs.getString(key, null) ?: return null
        val entry = try {
            gson.fromJson(raw, CacheEntry::class.java)
        } catch (e: Exception) {
            Log.w(TAG, "corrupt cache entry '$key', evicting")
            prefs.edit().remove(key).apply()
            return null
        }

        if (System.currentTimeMillis() > entry.expiresAt) {
            Log.d(TAG, "stale cache entry '$key', evicting")
            prefs.edit().remove(key).apply()
            return null
        }

        return try {
            gson.fromJson(entry.data, type.type)
        } catch (e: Exception) {
            Log.w(TAG, "failed to deserialise cache entry '$key'", e)
            null
        }
    }

    // ── Eviction ──────────────────────────────────────────────────────────────

    /** Remove a single entry by key (e.g. after a write-through invalidation). */
    fun remove(key: String) {
        prefs.edit { remove(key) }
    }

    /** Wipe the entire cache (e.g. on logout). */
    fun clear() {
        prefs.edit { clear() }
        Log.d(TAG, "cache cleared")
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private data class CacheEntry(val expiresAt: Long, val data: String)
}
