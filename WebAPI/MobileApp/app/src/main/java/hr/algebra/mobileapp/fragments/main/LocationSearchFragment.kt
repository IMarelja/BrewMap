package hr.algebra.mobileapp.fragments.main

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import hr.algebra.mobileapp.MainActivity
import hr.algebra.mobileapp.R
import hr.algebra.mobileapp.adapters.LocationSearchAdapter
import hr.algebra.mobileapp.cache.PersistentCache
import hr.algebra.mobileapp.models.location.Address
import hr.algebra.mobileapp.models.location.Contact
import hr.algebra.mobileapp.models.location.CreateLocationRequest
import hr.algebra.mobileapp.models.location.DayOpeningHours
import hr.algebra.mobileapp.service.ServiceProvider
import hr.algebra.mobileapp.state.MapViewportStore
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class LocationSearchFragment : Fragment() {

    private lateinit var etSearchQuery: TextInputEditText
    private lateinit var btnToggleAdvancedSearch: MaterialButton
    private lateinit var layoutAdvancedSearch: LinearLayout
    private lateinit var etDrinkKeyword: TextInputEditText
    private lateinit var etDistance: MaterialAutoCompleteTextView
    private lateinit var tvMinRatingValue: TextView
    private lateinit var sliderMinRating: Slider
    private lateinit var chipGroupCategories: ChipGroup
    private lateinit var chipGroupPaymentOptions: ChipGroup
    private lateinit var btnSearch: MaterialButton
    private lateinit var tvSearchState: TextView
    private lateinit var rvSearchResults: RecyclerView

    private var advancedExpanded = false
    private var selectedRadius = SearchRadius.DEFAULT

    private val adapter = LocationSearchAdapter { location ->
        (requireActivity() as MainActivity).openLocationDetail(location.id)
    }

    private lateinit var fabAddLocation: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_location_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etSearchQuery = view.findViewById(R.id.et_search_query)
        btnToggleAdvancedSearch = view.findViewById(R.id.btn_toggle_advanced_search)
        layoutAdvancedSearch = view.findViewById(R.id.layout_advanced_search)
        etDrinkKeyword = view.findViewById(R.id.et_drink_keyword)
        etDistance = view.findViewById(R.id.et_distance)
        tvMinRatingValue = view.findViewById(R.id.tv_min_rating_value)
        sliderMinRating = view.findViewById(R.id.slider_min_rating)
        chipGroupCategories = view.findViewById(R.id.chip_group_categories)
        chipGroupPaymentOptions = view.findViewById(R.id.chip_group_payment_options)
        btnSearch = view.findViewById(R.id.btn_search)
        tvSearchState = view.findViewById(R.id.tv_search_state)
        rvSearchResults = view.findViewById(R.id.rv_search_results)

        rvSearchResults.layoutManager = NonScrollableLinearLayoutManager(requireContext())
        rvSearchResults.adapter = adapter
        rvSearchResults.isNestedScrollingEnabled = false
        rvSearchResults.setHasFixedSize(false)

        fabAddLocation = view.findViewById(R.id.fab_add_location)
        fabAddLocation.setOnClickListener {
            createLocationDialog()
        }


        setupAdvancedSearch()
        loadAdvancedFilterOptions()

        btnSearch.setOnClickListener { runSearch() }
        btnToggleAdvancedSearch.setOnClickListener { toggleAdvancedSearch() }

        etSearchQuery.setOnEditorActionListener { _, actionId, event ->
            val imeAction = actionId == EditorInfo.IME_ACTION_SEARCH
            val enterDown = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            if (imeAction || enterDown) {
                runSearch()
                true
            } else {
                false
            }
        }

        if (savedInstanceState == null) {
            runSearch()
        }
    }

    private fun setupAdvancedSearch() {
        layoutAdvancedSearch.visibility = View.GONE
        layoutAdvancedSearch.alpha = 0f
        layoutAdvancedSearch.scaleY = 0.85f
        layoutAdvancedSearch.pivotY = 0f
        setupDistanceDropdown()
        updateMinRatingLabel(sliderMinRating.value)

        sliderMinRating.addOnChangeListener { _, value, _ ->
            updateMinRatingLabel(value)
        }
    }

    private fun loadAdvancedFilterOptions() {
        viewLifecycleOwner.lifecycleScope.launch {
            val categoriesDeferred      = async { ServiceProvider.categoryService.getAll() }
            val paymentOptionsDeferred  = async { ServiceProvider.paymentOptionService.getAll() }
            val categoriesResult        = categoriesDeferred.await()
            val paymentOptionsResult    = paymentOptionsDeferred.await()

            if (categoriesResult.isSuccess && categoriesResult.data != null) {
                populateFilterChips(
                    chipGroup = chipGroupCategories,
                    values = categoriesResult.data.associate { it.tag to it.name }
                )
            }
            if (paymentOptionsResult.isSuccess && paymentOptionsResult.data != null) {
                populateFilterChips(
                    chipGroup = chipGroupPaymentOptions,
                    values = paymentOptionsResult.data.associate { it.tag to it.name }
                )
            }
            if (categoriesResult.errors.isNotEmpty() || paymentOptionsResult.errors.isNotEmpty()) {
                tvSearchState.visibility = View.VISIBLE
                tvSearchState.text = getString(R.string.error_load_filter_options)
            }
        }
    }

    private fun setupDistanceDropdown() {
        val dropdownAdapter = ArrayAdapter(
            requireContext(),
            R.layout.item_distance_dropdown,
            SearchRadius.values()
        )
        etDistance.setAdapter(dropdownAdapter)
        etDistance.setText(selectedRadius.label, false)
        etDistance.setOnItemClickListener { _, _, position, _ ->
            selectedRadius = dropdownAdapter.getItem(position) ?: SearchRadius.DEFAULT
        }
    }

    private fun populateFilterChips(
        chipGroup: ChipGroup,
        values: Map<String, String>
    ) {
        chipGroup.removeAllViews()
        values.forEach { (tag, name) ->
            val chip = Chip(requireContext()).apply {
                id = View.generateViewId()
                text = name
                this.tag = tag
                isCheckable = true
                isClickable = true
                isCheckedIconVisible = false
            }
            chipGroup.addView(chip)
        }
    }

    private fun toggleAdvancedSearch() {
        if (advancedExpanded) {
            collapseAdvancedSearch()
        } else {
            expandAdvancedSearch()
        }
        advancedExpanded = !advancedExpanded
    }

    private fun expandAdvancedSearch() {
        btnToggleAdvancedSearch.text = getString(R.string.btn_hide_advanced_search)
        layoutAdvancedSearch.visibility = View.VISIBLE
        layoutAdvancedSearch.animate().cancel()
        layoutAdvancedSearch.alpha = 0f
        layoutAdvancedSearch.scaleY = 0.85f
        layoutAdvancedSearch.animate()
            .alpha(1f)
            .scaleY(1f)
            .setDuration(180L)
            .start()
    }

    private fun collapseAdvancedSearch() {
        btnToggleAdvancedSearch.text = getString(R.string.advanced_search)
        layoutAdvancedSearch.animate().cancel()
        layoutAdvancedSearch.animate()
            .alpha(0f)
            .scaleY(0.85f)
            .setDuration(150L)
            .withEndAction { layoutAdvancedSearch.visibility = View.GONE }
            .start()
    }

    private fun updateMinRatingLabel(value: Float) {
        tvMinRatingValue.text = if (value <= 0f) {
            getString(R.string.minimum_rating_any)
        } else {
            getString(R.string.min_rating_format)
        }
    }

    private fun getSelectedTags(chipGroup: ChipGroup): List<String> =
        chipGroup.checkedChipIds
            .mapNotNull { chipId ->
                chipGroup.findViewById<Chip>(chipId)?.tag?.toString()
            }

    private fun runSearch() {
        if (ServiceProvider.isPersistent) PersistentCache.clearByPrefix("loc_search_")
        val query = etSearchQuery.text?.toString()?.trim().orEmpty()
        val drinkKeyword = etDrinkKeyword.text?.toString()?.trim().orEmpty()
        val minRating = sliderMinRating.value.takeIf { it > 0f }?.toDouble()
        val categoryTags = getSelectedTags(chipGroupCategories).ifEmpty { null }
        val paymentOptionTags = getSelectedTags(chipGroupPaymentOptions).ifEmpty { null }
        val (lon, lat) = getSearchCenter()

        btnSearch.isEnabled = false
        tvSearchState.visibility = View.VISIBLE
        tvSearchState.text = getString(R.string.state_searching)

        viewLifecycleOwner.lifecycleScope.launch {
            val result = ServiceProvider.locationService.search(
                longitude = lon,
                latitude = lat,
                query = query.ifBlank { null },
                minRating = minRating,
                drinkQuery = drinkKeyword.ifBlank { null },
                categoryTags = categoryTags,
                paymentOptionTags = paymentOptionTags,
                radiusMeters = selectedRadius.meters
            )
            when {
                result.isUnauthorized      -> { /* MainActivity navigating to login */ }
                result.isNetworkError      -> tvSearchState.text = getString(R.string.error_no_connection)
                result.errors.isNotEmpty() -> {
                    adapter.submitData(emptyList())
                    tvSearchState.text = result.errorMessage()
                }
                else -> {
                    val results = result.data!!
                    adapter.submitData(results)
                    rvSearchResults.post { rvSearchResults.requestLayout() }
                    tvSearchState.text = if (results.isEmpty()) {
                        getString(R.string.state_no_locations)
                    } else {
                        getString(R.string.state_locations_found, results.size)
                    }
                }
            }
            btnSearch.isEnabled = true
        }
    }

    private fun getSearchCenter(): Pair<Double, Double> {
        val fallback = Pair(DEFAULT_LONGITUDE, DEFAULT_LATITUDE)
        MapViewportStore.getCenter()?.let { (lat, lon) ->
            return Pair(lon, lat)
        }

        val context = context ?: return fallback

        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) return fallback

        val manager = context.getSystemService(LocationManager::class.java) ?: return fallback
        val providers = manager.getProviders(true)

        val bestLocation = providers
            .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }

        return if (bestLocation == null) fallback else Pair(bestLocation.longitude, bestLocation.latitude)
    }

    companion object {
        private const val DEFAULT_LATITUDE = 45.8150
        private const val DEFAULT_LONGITUDE = 15.9819
    }

    private enum class SearchRadius(
        val label: String,
        val meters: Double
    ) {
        KM_5("5 km", 5_000.0),
        KM_10("10 km", 10_000.0),
        KM_20("20 km", 20_000.0);

        override fun toString(): String = label

        companion object {
            val DEFAULT = KM_20
        }
    }

    private fun createLocationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_location_form, null)
        val etLocationName = dialogView.findViewById<TextInputEditText>(R.id.et_location_name)
        val etLocationDescription = dialogView.findViewById<TextInputEditText>(R.id.et_location_description)
        val etLocationAddress = dialogView.findViewById<TextInputEditText>(R.id.et_location_address)
        val etLocationCity = dialogView.findViewById<TextInputEditText>(R.id.et_location_city)
        val etLocationCountry = dialogView.findViewById<TextInputEditText>(R.id.et_location_country)
        val etLocationPostalCode = dialogView.findViewById<TextInputEditText>(R.id.et_location_postal_code)
        val etLocationLatitude = dialogView.findViewById<TextInputEditText>(R.id.et_location_latitude)
        val etLocationLongitude = dialogView.findViewById<TextInputEditText>(R.id.et_location_longitude)
        val etLocationCategory = dialogView.findViewById<TextInputEditText>(R.id.et_location_category)
        val etPaymentOptions = dialogView.findViewById<TextInputEditText>(R.id.et_location_payment_options)
        val etOpenTime = dialogView.findViewById<TextInputEditText>(R.id.et_location_open_time)
        val etClosingTime = dialogView.findViewById<TextInputEditText>(R.id.et_location_close_time)
        val cbSundayClosed = dialogView.findViewById<CheckBox>(R.id.cb_location_sunday_closed)
        val etContact = dialogView.findViewById<TextInputEditText>(R.id.et_location_website)
        val dialogMapView = dialogView.findViewById<MapView>(R.id.dialog_map_view)

        val (initialLon, initialLat) = getSearchCenter()
        val initialPoint = GeoPoint(initialLat, initialLon)

        dialogMapView.setTileSource(TileSourceFactory.MAPNIK)
        dialogMapView.setMultiTouchControls(true)
        dialogMapView.controller.setZoom(15.0)
        dialogMapView.controller.setCenter(initialPoint)
        dialogMapView.setOnTouchListener { v, _ ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }

        etLocationLatitude.setText("%.6f".format(initialPoint.latitude))
        etLocationLongitude.setText("%.6f".format(initialPoint.longitude))

        val dialogMarker = Marker(dialogMapView).apply {
            position = initialPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            isDraggable = true
            setOnMarkerClickListener { _, _ -> true }
        }

        fun updateCoords(point: GeoPoint) {
            etLocationLatitude.setText("%.6f".format(point.latitude))
            etLocationLongitude.setText("%.6f".format(point.longitude))
        }

        dialogMarker.setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
            override fun onMarkerDrag(m: Marker) = updateCoords(m.position)
            override fun onMarkerDragEnd(m: Marker) = updateCoords(m.position)
            override fun onMarkerDragStart(m: Marker) {}
        })

        dialogMapView.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                dialogMarker.position = p
                updateCoords(p)
                dialogMapView.invalidate()
                return true
            }
            override fun longPressHelper(p: GeoPoint): Boolean = false
        }))
        dialogMapView.overlays.add(dialogMarker)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_create_location)
            .setView(dialogView)
            .setPositiveButton(R.string.btn_create) { _, _ ->
                val name = etLocationName.text.toString().trim()
                val description = etLocationDescription.text.toString().trim()
                val address = etLocationAddress.text.toString().trim()
                val city = etLocationCity.text.toString().trim()
                val country = etLocationCountry.text.toString().trim()
                val postalCode = etLocationPostalCode.text.toString().trim()
                val latitude = etLocationLatitude.text.toString().toDoubleOrNull()
                val longitude = etLocationLongitude.text.toString().toDoubleOrNull()
                val category = etLocationCategory.text.toString().trim()

                if (name.isEmpty() || address.isEmpty() || city.isEmpty() || country.isEmpty()
                    || postalCode.isEmpty() || latitude == null || longitude == null || category.isEmpty()
                ) {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.dialog_title_error)
                        .setMessage(R.string.error_fill_all_fields)
                        .setPositiveButton(R.string.btn_ok, null)
                        .show()
                    return@setPositiveButton
                }

                val paymentTags = etPaymentOptions.text.toString().split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                val openTime = etOpenTime.text.toString().trim()
                val closeTime = etClosingTime.text.toString().trim()
                val sundayClosed = cbSundayClosed.isChecked
                val contact = etContact.text.toString().trim().takeIf { it.isNotEmpty() }

                createLocation(name, description, address, city, country, postalCode, latitude, longitude, category, paymentTags, openTime, closeTime, sundayClosed, contact)
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .setOnDismissListener { dialogMapView.onDetach() }
            .create()

        dialog.show()
        dialogMapView.onResume()
    }

    private fun createLocation(
        name: String,
        description: String,
        address: String,
        city: String,
        country: String,
        postalCode: String,
        latitude: Double,
        longitude: Double,
        category: String,
        paymentTags: List<String>,
        openTime: String,
        closeTime: String,
        sundayClosed: Boolean,
        contact: String?
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            val address = Address(address, city, country, postalCode)
            val contact = if(contact != null) Contact(contact) else null
            val openingHours = mapOf(
                "monday" to DayOpeningHours(openTime, closeTime, false),
                "tuesday" to DayOpeningHours(openTime, closeTime, false),
                "wednesday" to DayOpeningHours(openTime, closeTime, false),
                "thursday" to DayOpeningHours(openTime, closeTime, false),
                "friday" to DayOpeningHours(openTime, closeTime, false),
                "saturday" to DayOpeningHours(openTime, closeTime, false),
                "sunday" to DayOpeningHours(
                    if (sundayClosed) null else openTime,
                    if (sundayClosed) null else closeTime,
                    sundayClosed
                )
            )

            val request = CreateLocationRequest(name, description, address, latitude, longitude, category, paymentTags, contact, openingHours)
            val result = ServiceProvider.locationService.create(request)
            if(result.isSuccess){
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.dialog_title_success)
                    .setMessage(R.string.success_location_created)
                    .setPositiveButton(R.string.btn_ok) {_, _, ->
                        (requireActivity() as MainActivity).openLocationDetail(result.data!!.id)
                    }
                    .show()
            } else{
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.dialog_title_error)
                    .setMessage(getString(R.string.error_create_location_format, result.errorMessage()))
                    .setPositiveButton(R.string.btn_ok, null)
                    .show()
            }
        }
    }
}
