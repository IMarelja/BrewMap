package hr.algebra.mobileapp.fragments.main

import android.os.Bundle
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
import hr.algebra.mobileapp.models.Location
import hr.algebra.mobileapp.service.ServiceProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

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

        locationId = requireArguments().getString(ARG_LOCATION_ID).orEmpty()
        if (locationId.isBlank()) {
            Toast.makeText(requireContext(), "Missing location id", Toast.LENGTH_SHORT).show()
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
        mapView.onDetach()
        super.onDestroyView()
    }

    private fun loadLocationDetails(locationId: String) {
        progressDetail.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val location = ServiceProvider.location.getById(locationId)
                val metaText = async { buildMetaText(location) }.await()
                bindLocation(location, metaText)
            } catch (_: Exception) {
                Toast.makeText(requireContext(), "Failed to load location details", Toast.LENGTH_SHORT).show()
            } finally {
                progressDetail.visibility = View.GONE
            }
        }
    }

    private suspend fun buildMetaText(location: Location): String = coroutineScope {
        val categoryName = runCatching {
            ServiceProvider.category.getByTag(location.categoryTag)?.name
        }.getOrNull() ?: location.categoryTag

        val paymentNames = if (location.paymentOptionTags.isEmpty()) {
            "-"
        } else {
            location.paymentOptionTags
                .map { tag ->
                    async {
                        runCatching { ServiceProvider.paymentOption.getByTag(tag)?.name }
                            .getOrNull()
                            ?: tag
                    }
                }
                .awaitAll()
                .joinToString(", ")
        }

        "Category: $categoryName\nPayment: $paymentNames"
    }

    private fun bindLocation(location: Location, metaText: String) {
        tvName.text = location.name
        tvRating.text = "${"%.1f".format(location.averageRating)} ★ (${location.totalReviews} reviews)"
        tvAddress.text = "${location.address.street}, ${location.address.city}, ${location.address.country}"
        tvDescription.text = location.description ?: "No description"
        tvMeta.text = metaText

        val point = GeoPoint(location.latitude, location.longitude)
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(point)

        mapView.overlays.removeAll { it is Marker }
        val marker = Marker(mapView).apply {
            position = point
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = location.name
            snippet = location.address.street
        }
        mapView.overlays.add(marker)
        marker.showInfoWindow()
        mapView.invalidate()
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
