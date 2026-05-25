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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import hr.algebra.mobileapp.MainActivity
import hr.algebra.mobileapp.R
import hr.algebra.mobileapp.adapters.LocationSearchAdapter
import hr.algebra.mobileapp.service.ServiceProvider
import hr.algebra.mobileapp.state.MapViewportStore
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

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

        rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        rvSearchResults.adapter = adapter

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
            try {
                val categoriesDeferred = async { ServiceProvider.category.getAll() }
                val paymentOptionsDeferred = async { ServiceProvider.paymentOption.getAll() }
                val categories = categoriesDeferred.await()
                val paymentOptions = paymentOptionsDeferred.await()
                populateFilterChips(
                    chipGroup = chipGroupCategories,
                    values = categories.associate { it.tag to it.name }
                )
                populateFilterChips(
                    chipGroup = chipGroupPaymentOptions,
                    values = paymentOptions.associate { it.tag to it.name }
                )
            } catch (_: Exception) {
                tvSearchState.visibility = View.VISIBLE
                tvSearchState.text = "Failed to load advanced filter options."
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
        btnToggleAdvancedSearch.text = "Hide advnaced search"
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
        btnToggleAdvancedSearch.text = "Advnaced search"
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
            "Minimum rating: Any"
        } else {
            "Minimum rating: $value+"
        }
    }

    private fun getSelectedTags(chipGroup: ChipGroup): List<String> =
        chipGroup.checkedChipIds
            .mapNotNull { chipId ->
                chipGroup.findViewById<Chip>(chipId)?.tag?.toString()
            }

    private fun runSearch() {
        val query = etSearchQuery.text?.toString()?.trim().orEmpty()
        val drinkKeyword = etDrinkKeyword.text?.toString()?.trim().orEmpty()
        val minRating = sliderMinRating.value.takeIf { it > 0f }?.toDouble()
        val categoryTags = getSelectedTags(chipGroupCategories).ifEmpty { null }
        val paymentOptionTags = getSelectedTags(chipGroupPaymentOptions).ifEmpty { null }
        val (lon, lat) = getSearchCenter()

        btnSearch.isEnabled = false
        tvSearchState.visibility = View.VISIBLE
        tvSearchState.text = "Searching..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val results = ServiceProvider.location.search(
                    longitude = lon,
                    latitude = lat,
                    query = query.ifBlank { null },
                    minRating = minRating,
                    drinkQuery = drinkKeyword.ifBlank { null },
                    categoryTags = categoryTags,
                    paymentOptionTags = paymentOptionTags,
                    radiusMeters = selectedRadius.meters
                )
                adapter.submitData(results)
                tvSearchState.text = if (results.isEmpty()) {
                    "No locations found"
                } else {
                    "${results.size} locations found"
                }
            } catch (e: Exception) {
                adapter.submitData(emptyList())
                tvSearchState.text = "Search failed. Try again."
            } finally {
                btnSearch.isEnabled = true
            }
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
}
