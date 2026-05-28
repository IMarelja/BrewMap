package hr.algebra.mobileapp.fragments.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import hr.algebra.mobileapp.R
import hr.algebra.mobileapp.cache.PersistentCache
import hr.algebra.mobileapp.models.location.Location
import hr.algebra.mobileapp.service.ServiceProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.osmdroid.events.MapAdapter
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlin.math.roundToInt
import androidx.core.graphics.scale
import androidx.core.graphics.drawable.toDrawable

class LocationDetailFragment : Fragment() {

    private lateinit var progressDetail: ProgressBar
    private lateinit var tvName: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvMeta: TextView
    private lateinit var mapView: MapView
    private lateinit var btnReviews: MaterialButton
    private lateinit var btnDrinks: MaterialButton
    private var detailMarker: Marker? = null
    private val pinBitmap: Bitmap? by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.pin)
    }
    private val scaledPinIconCache = mutableMapOf<Int, Drawable?>()

    private var locationId: String = ""
    private var activeSection: Section = Section.NONE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_location_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDetail = view.findViewById(R.id.progress_detail)
        tvName = view.findViewById(R.id.tv_detail_name)
        tvRating = view.findViewById(R.id.tv_detail_rating)
        tvAddress = view.findViewById(R.id.tv_detail_address)
        tvDescription = view.findViewById(R.id.tv_detail_description)
        tvMeta = view.findViewById(R.id.tv_detail_meta)
        mapView = view.findViewById(R.id.detail_map_view)
        btnReviews = view.findViewById(R.id.btn_reviews)
        btnDrinks = view.findViewById(R.id.btn_drinks)

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.addMapListener(object : MapAdapter() {
            override fun onZoom(event: ZoomEvent?): Boolean {
                applyDetailPinScaleForCurrentZoom()
                return true
            }
        })

        locationId = requireArguments().getString(ARG_LOCATION_ID).orEmpty()
        if (locationId.isBlank()) {
            Toast.makeText(requireContext(), getString(R.string.error_missing_location_id), Toast.LENGTH_SHORT).show()
            return
        }

        btnReviews.setOnClickListener {
            if (activeSection == Section.REVIEWS) return@setOnClickListener
            showSection(Section.REVIEWS)
        }

        btnDrinks.setOnClickListener {
            if (activeSection == Section.DRINKS) {
                unloadSection()
            } else {
                showSection(Section.DRINKS)
            }
        }

        updateSectionButtonState(Section.NONE)
        loadLocationDetails(locationId)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        if (ServiceProvider.isPersistent && locationId.isNotBlank()) {
            PersistentCache.remove("loc_id_$locationId")
            PersistentCache.remove("review_loc_$locationId")
            PersistentCache.remove("drink_loc_$locationId")
            PersistentCache.remove("drink_best_$locationId")
        }
        detailMarker = null
        scaledPinIconCache.clear()
        mapView.onDetach()
        super.onDestroyView()
    }

    private fun loadLocationDetails(locationId: String) {
        progressDetail.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            val result = ServiceProvider.locationService.getById(locationId)
            when {
                result.isUnauthorized  -> { /* MainActivity already navigating to log in */ }
                result.isNetworkError  -> Toast.makeText(requireContext(), getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show()
                result.errors.isNotEmpty() -> Toast.makeText(requireContext(), result.errorMessage(), Toast.LENGTH_SHORT).show()
                else -> {
                    val location = result.data!!
                    val metaText = async { buildMetaText(location) }.await()
                    bindLocation(location, metaText)
                }
            }
            progressDetail.visibility = View.GONE
        }
    }

    private suspend fun buildMetaText(location: Location): String = coroutineScope {
        val categoryName = ServiceProvider.categoryService.getByTag(location.categoryTag).data?.name
            ?: location.categoryTag

        val paymentNames = if (location.paymentOptionTags.isEmpty()) {
            "-"
        } else {
            location.paymentOptionTags
                .map { tag ->
                    async {
                        ServiceProvider.paymentOptionService.getByTag(tag).data?.name ?: tag
                    }
                }
                .awaitAll()
                .joinToString(", ")
        }

        getString(R.string.detail_category_payment_format, categoryName, paymentNames)
    }

    @SuppressLint("SetTextI18n")
    private fun bindLocation(location: Location, metaText: String) {
        tvName.text = location.name
        tvRating.text = "${"%.1f".format(location.averageRating)} ★ (${location.totalReviews} reviews)"
        tvAddress.text = "${location.address.street}, ${location.address.city}, ${location.address.country}"
        tvDescription.text = location.description ?: getString(R.string.detail_no_description)
        tvMeta.text = metaText

        val point = GeoPoint(location.latitude, location.longitude)
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(point)

        mapView.overlays.removeAll { it is Marker }
        val pinSizeDp = zoomToPinSizeDp(mapView.zoomLevelDouble)
        val pinVisible = pinSizeDp > MapPinScaleConfig.PIN_HIDE_AT_OR_BELOW_DP
        val marker = Marker(mapView).apply {
            position = point
            icon = createScaledPinDrawable(dpToPx(pinSizeDp))
            setVisible(pinVisible)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = location.name
            snippet = location.address.street
        }
        detailMarker = marker
        mapView.overlays.add(marker)
        if (pinVisible) marker.showInfoWindow()
        mapView.invalidate()
    }

    private fun applyDetailPinScaleForCurrentZoom() {
        val pinSizeDp = zoomToPinSizeDp(mapView.zoomLevelDouble)
        val pinVisible = pinSizeDp > MapPinScaleConfig.PIN_HIDE_AT_OR_BELOW_DP
        detailMarker?.let { marker ->
            marker.icon = createScaledPinDrawable(dpToPx(pinSizeDp))
            marker.setVisible(pinVisible)
            if (!pinVisible && marker.isInfoWindowShown) {
                marker.closeInfoWindow()
            }
        }
        mapView.invalidate()
    }

    @SuppressLint("UseKtx")
    private fun createScaledPinDrawable(targetHeightPx: Int) =
        scaledPinIconCache.getOrPut(targetHeightPx.coerceAtLeast(1)) {
            val source = pinBitmap ?: return@getOrPut null
            if (source.width <= 0 || source.height <= 0) return@getOrPut null

            val safeHeightPx = targetHeightPx.coerceAtLeast(1)
            val targetWidthPx = (safeHeightPx.toFloat() * source.width / source.height)
                .roundToInt()
                .coerceAtLeast(1)
            val scaledBitmap = source.scale(targetWidthPx, safeHeightPx)
            scaledBitmap.toDrawable(resources)
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

    private fun showSection(section: Section) {
        val fragment = when (section) {
            Section.REVIEWS -> LocationReviewsListFragment.newInstance(locationId)
            Section.DRINKS -> LocationDrinksListFragment.newInstance(locationId)
            Section.NONE -> null
        }

        if (fragment == null) {
            unloadSection()
            return
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.location_content_container, fragment)
            .commit()

        activeSection = section
        updateSectionButtonState(section)
    }

    private fun unloadSection() {
        childFragmentManager.findFragmentById(R.id.location_content_container)?.let { fragment ->
            childFragmentManager.beginTransaction().remove(fragment).commit()
        }
        activeSection = Section.NONE
        updateSectionButtonState(Section.NONE)
    }

    private fun updateSectionButtonState(section: Section) {
        btnReviews.alpha = if (section == Section.REVIEWS) 1.0f else 0.7f
        btnDrinks.alpha = if (section == Section.DRINKS) 1.0f else 0.7f
    }

    private enum class Section {
        NONE,
        REVIEWS,
        DRINKS
    }

    companion object {
        private const val ARG_LOCATION_ID = "arg_location_id"

        fun newInstance(locationId: String): LocationDetailFragment {
            val fragment = LocationDetailFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_LOCATION_ID, locationId)
            }
            return fragment
        }
    }
}
