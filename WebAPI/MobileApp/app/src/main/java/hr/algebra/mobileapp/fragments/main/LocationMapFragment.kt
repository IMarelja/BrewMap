package hr.algebra.mobileapp.fragments.main

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import hr.algebra.mobileapp.MainActivity
import hr.algebra.mobileapp.R
import hr.algebra.mobileapp.models.location.Location
import hr.algebra.mobileapp.models.location.Pin
import hr.algebra.mobileapp.service.ServiceProvider
import hr.algebra.mobileapp.state.MapViewportStore
import kotlinx.coroutines.launch
import org.osmdroid.events.MapAdapter
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.roundToInt

class LocationMapFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var progressPins: ProgressBar
    private val pinBitmap: Bitmap? by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.pin)
    }
    private val scaledPinIconCache = mutableMapOf<Int, Drawable?>()
    private var lastAppliedPinHeightPx: Int = -1
    private var lastPinsVisible: Boolean? = null

    private val markerLocationCache = mutableMapOf<String, Location>()
    private val loadingLocationIds = mutableSetOf<String>()

    private var myLocationOverlay: MyLocationNewOverlay? = null

    private val uiHandler = Handler(Looper.getMainLooper())
    private val debouncedPinsReload = Runnable {
        persistCurrentCenter()
        loadPinsForCurrentBounds()
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        configureUserLocationOverlay()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_location_map, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.map_view)
        progressPins = view.findViewById(R.id.progress_pins)

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(MapPinScaleConfig.PIN_DEFAULT_ZOOM_LEVEL)
        mapView.controller.setCenter(GeoPoint(DEFAULT_LATITUDE, DEFAULT_LONGITUDE))

        mapView.addMapListener(object : MapAdapter() {
            override fun onScroll(event: ScrollEvent?): Boolean {
                schedulePinsReload()
                return true
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                applyPinScaleForCurrentZoom()
                schedulePinsReload()
                return true
            }
        })

        ensureLocationPermission()
        schedulePinsReload()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        myLocationOverlay?.enableMyLocation()
    }

    override fun onPause() {
        mapView.onPause()
        myLocationOverlay?.disableMyLocation()
        super.onPause()
    }

    override fun onDestroyView() {
        uiHandler.removeCallbacks(debouncedPinsReload)
        scaledPinIconCache.clear()
        mapView.overlays.clear()
        mapView.onDetach()
        myLocationOverlay = null
        super.onDestroyView()
    }

    private fun ensureLocationPermission() {
        val hasFine = isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarse = isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (hasFine || hasCoarse) {
            configureUserLocationOverlay()
            return
        }

        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun configureUserLocationOverlay() {
        val hasFine = isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarse = isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (!hasFine && !hasCoarse) return

        if (myLocationOverlay == null) {
            val overlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), mapView)
            applyFixedMyLocationIcon(overlay)
            overlay.enableMyLocation()
            overlay.enableFollowLocation()
            mapView.overlays.add(overlay)
            myLocationOverlay = overlay

            overlay.runOnFirstFix {
                activity?.runOnUiThread {
                    overlay.myLocation?.let { myPoint ->
                        mapView.controller.animateTo(myPoint)
                    }
                }
            }
        } else {
            myLocationOverlay?.let { applyFixedMyLocationIcon(it) }
            myLocationOverlay?.enableMyLocation()
        }
    }

    private fun applyFixedMyLocationIcon(overlay: MyLocationNewOverlay) {
        val personBitmap = BitmapFactory.decodeResource(
            requireContext().resources,
            org.osmdroid.library.R.drawable.person
        ) ?: return

        overlay.setPersonIcon(personBitmap)
        // Keep the same icon while moving so it doesn't switch to the white arrow.
        overlay.setDirectionArrow(personBitmap, personBitmap)
    }

    private fun schedulePinsReload() {
        uiHandler.removeCallbacks(debouncedPinsReload)
        uiHandler.postDelayed(debouncedPinsReload, 500L)
    }

    private fun loadPinsForCurrentBounds() {
        val bounds = mapView.boundingBox ?: return

        progressPins.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            val result = ServiceProvider.locationService.getPins(
                minLon = bounds.lonWest,
                maxLon = bounds.lonEast,
                minLat = bounds.latSouth,
                maxLat = bounds.latNorth
            )
            when {
                result.isUnauthorized      -> { /* MainActivity navigating to login */ }
                result.isNetworkError      -> { /* silent — map still shows cached tiles */ }
                result.errors.isNotEmpty() -> {
                    if (context != null) {
                        Toast.makeText(requireContext(), result.errorMessage(), Toast.LENGTH_SHORT).show()
                    }
                }
                else -> renderPins(result.data!!)
            }
            progressPins.visibility = View.GONE
        }
    }

    private fun persistCurrentCenter() {
        val center = mapView.mapCenter ?: return
        MapViewportStore.saveCenter(
            latitude = center.latitude,
            longitude = center.longitude
        )
    }

    private fun renderPins(pins: List<Pin>) {
        mapView.overlays.removeAll { it is Marker }
        val pinSizeDp = zoomToPinSizeDp(mapView.zoomLevelDouble)
        val pinsVisible = pinSizeDp > MapPinScaleConfig.PIN_HIDE_AT_OR_BELOW_DP
        val pinHeightPx = dpToPx(pinSizeDp)
        lastAppliedPinHeightPx = pinHeightPx
        lastPinsVisible = pinsVisible

        pins.forEach { pin ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(pin.latitude, pin.longitude)
                icon = createScaledPinDrawable(pinHeightPx)
                setVisible(pinsVisible)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Location"
                snippet = "Tap pin again in a moment"
                relatedObject = pin.id
                setOnMarkerClickListener { clickedMarker, _ ->
                    onPinClicked(clickedMarker, pin.id)
                    true
                }
            }
            mapView.overlays.add(marker)
        }

        myLocationOverlay?.let { overlay ->
            if (!mapView.overlays.contains(overlay)) {
                mapView.overlays.add(overlay)
            }
        }

        mapView.invalidate()
    }

    private fun applyPinScaleForCurrentZoom() {
        val pinSizeDp = zoomToPinSizeDp(mapView.zoomLevelDouble)
        val pinsVisible = pinSizeDp > MapPinScaleConfig.PIN_HIDE_AT_OR_BELOW_DP
        val pinHeightPx = dpToPx(pinSizeDp)
        if (pinHeightPx == lastAppliedPinHeightPx && pinsVisible == lastPinsVisible) return
        lastAppliedPinHeightPx = pinHeightPx
        lastPinsVisible = pinsVisible

        mapView.overlays.forEach { overlay ->
            val marker = overlay as? Marker ?: return@forEach
            marker.icon = createScaledPinDrawable(pinHeightPx)
            marker.setVisible(pinsVisible)
        }
        mapView.invalidate()
    }

    private fun createScaledPinDrawable(targetHeightPx: Int) =
        scaledPinIconCache.getOrPut(targetHeightPx.coerceAtLeast(1)) {
            val source = pinBitmap ?: return@getOrPut null
            if (source.width <= 0 || source.height <= 0) return@getOrPut null

            val safeHeightPx = targetHeightPx.coerceAtLeast(1)
            val targetWidthPx = (safeHeightPx.toFloat() * source.width / source.height)
                .roundToInt()
                .coerceAtLeast(1)
            val scaledBitmap = Bitmap.createScaledBitmap(source, targetWidthPx, safeHeightPx, true)
            BitmapDrawable(resources, scaledBitmap)
        }

    private fun zoomToPinSizeDp(zoomLevel: Double): Float {
        val clampedZoom = zoomLevel.coerceIn(
            MapPinScaleConfig.PIN_ZOOM_OUT_LEVEL,
            MapPinScaleConfig.PIN_ZOOM_IN_LEVEL
        )
        return if (clampedZoom <= MapPinScaleConfig.PIN_DEFAULT_ZOOM_LEVEL) {
            val denominator = (
                MapPinScaleConfig.PIN_DEFAULT_ZOOM_LEVEL - MapPinScaleConfig.PIN_ZOOM_OUT_LEVEL
            ).takeIf { it > 0.0 } ?: 1.0
            val progress = ((clampedZoom - MapPinScaleConfig.PIN_ZOOM_OUT_LEVEL) / denominator).toFloat()
            lerp(MapPinScaleConfig.PIN_MIN_VISIBLE_DP, MapPinScaleConfig.PIN_DEFAULT_DP, progress)
        } else {
            val denominator = (
                MapPinScaleConfig.PIN_ZOOM_IN_LEVEL - MapPinScaleConfig.PIN_DEFAULT_ZOOM_LEVEL
            ).takeIf { it > 0.0 } ?: 1.0
            val progress = ((clampedZoom - MapPinScaleConfig.PIN_DEFAULT_ZOOM_LEVEL) / denominator).toFloat()
            lerp(MapPinScaleConfig.PIN_DEFAULT_DP, MapPinScaleConfig.PIN_MAX_DP, progress)
        }
    }

    private fun dpToPx(dp: Float): Int {
        return (dp * resources.displayMetrics.density).roundToInt().coerceAtLeast(1)
    }

    private fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t.coerceIn(0f, 1f)
    }

    private fun onPinClicked(marker: Marker, locationId: String) {
        val cached = markerLocationCache[locationId]
        if (cached != null) {
            if (marker.isInfoWindowShown) {
                (requireActivity() as MainActivity).openLocationDetail(locationId)
                return
            }
            marker.title = cached.name
            marker.snippet = buildShortDescription(cached)
            marker.showInfoWindow()
            return
        }

        if (loadingLocationIds.contains(locationId)) {
            marker.showInfoWindow()
            return
        }

        loadingLocationIds += locationId
        marker.title = "Loading..."
        marker.snippet = "Fetching locationService details"
        marker.showInfoWindow()

        viewLifecycleOwner.lifecycleScope.launch {
            val result = ServiceProvider.locationService.getById(locationId)
            when {
                result.isSuccess && result.data != null -> {
                    val location = result.data
                    markerLocationCache[locationId] = location
                    marker.title = location.name
                    marker.snippet = buildShortDescription(location)
                }
                else -> {
                    marker.title = "Unavailable"
                    marker.snippet = "Could not load description"
                }
            }
            marker.showInfoWindow()
            loadingLocationIds -= locationId
        }
    }

    private fun buildShortDescription(location: Location): String {
        val shortDescription = location.description?.take(100)?.trim().orEmpty()
        return if (shortDescription.isBlank()) {
            location.address.street
        } else {
            shortDescription
        }
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val DEFAULT_LATITUDE = 45.8150
        private const val DEFAULT_LONGITUDE = 15.9819
    }
}
