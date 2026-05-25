package hr.algebra.mobileapp.fragments.main

import android.Manifest
import android.content.pm.PackageManager
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
import hr.algebra.mobileapp.models.Location
import hr.algebra.mobileapp.models.Pin
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

class LocationMapFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var progressPins: ProgressBar

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
        mapView.controller.setZoom(DEFAULT_ZOOM)
        mapView.controller.setCenter(GeoPoint(DEFAULT_LATITUDE, DEFAULT_LONGITUDE))

        mapView.addMapListener(object : MapAdapter() {
            override fun onScroll(event: ScrollEvent?): Boolean {
                schedulePinsReload()
                return true
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
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
            myLocationOverlay?.enableMyLocation()
        }
    }

    private fun schedulePinsReload() {
        uiHandler.removeCallbacks(debouncedPinsReload)
        uiHandler.postDelayed(debouncedPinsReload, 500L)
    }

    private fun loadPinsForCurrentBounds() {
        val bounds = mapView.boundingBox ?: return

        progressPins.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val pins = ServiceProvider.location.getPins(
                    minLon = bounds.lonWest,
                    maxLon = bounds.lonEast,
                    minLat = bounds.latSouth,
                    maxLat = bounds.latNorth
                )
                renderPins(pins)
            } catch (e: Exception) {
                if (context != null) {
                    Toast.makeText(requireContext(), "Failed to load map pins", Toast.LENGTH_SHORT).show()
                }
            } finally {
                progressPins.visibility = View.GONE
            }
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

        pins.forEach { pin ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(pin.latitude, pin.longitude)
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
        marker.snippet = "Fetching location details"
        marker.showInfoWindow()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val location = ServiceProvider.location.getById(locationId)
                markerLocationCache[locationId] = location
                marker.title = location.name
                marker.snippet = buildShortDescription(location)
                marker.showInfoWindow()
            } catch (_: Exception) {
                marker.title = "Unavailable"
                marker.snippet = "Could not load description"
                marker.showInfoWindow()
            } finally {
                loadingLocationIds -= locationId
            }
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
        private const val DEFAULT_ZOOM = 13.0
    }
}
