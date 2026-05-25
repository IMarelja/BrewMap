package hr.algebra.mobileapp.state

import android.content.Context
import androidx.core.content.edit
import hr.algebra.mobileapp.BrewMapApp

object MapViewportStore {

    private const val PREFS_NAME = "brewmap_map_viewport"
    private const val KEY_CENTER_LAT = "center_lat"
    private const val KEY_CENTER_LON = "center_lon"

    private val prefs by lazy {
        BrewMapApp.appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveCenter(latitude: Double, longitude: Double) {
        prefs.edit {
            putLong(KEY_CENTER_LAT, java.lang.Double.doubleToRawLongBits(latitude))
            putLong(KEY_CENTER_LON, java.lang.Double.doubleToRawLongBits(longitude))
        }
    }

    fun getCenter(): Pair<Double, Double>? {
        if (!prefs.contains(KEY_CENTER_LAT) || !prefs.contains(KEY_CENTER_LON)) return null
        val latBits = prefs.getLong(KEY_CENTER_LAT, 0L)
        val lonBits = prefs.getLong(KEY_CENTER_LON, 0L)
        return Pair(
            java.lang.Double.longBitsToDouble(latBits),
            java.lang.Double.longBitsToDouble(lonBits)
        )
    }
}
