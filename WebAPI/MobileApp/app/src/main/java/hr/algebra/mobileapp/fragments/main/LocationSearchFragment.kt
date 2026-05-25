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
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import hr.algebra.mobileapp.MainActivity
import hr.algebra.mobileapp.R
import hr.algebra.mobileapp.adapters.LocationSearchAdapter
import hr.algebra.mobileapp.service.ServiceProvider
import kotlinx.coroutines.launch

class LocationSearchFragment : Fragment() {

    private lateinit var etSearchQuery: TextInputEditText
    private lateinit var btnSearch: MaterialButton
    private lateinit var tvSearchState: TextView
    private lateinit var rvSearchResults: RecyclerView

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
        btnSearch = view.findViewById(R.id.btn_search)
        tvSearchState = view.findViewById(R.id.tv_search_state)
        rvSearchResults = view.findViewById(R.id.rv_search_results)

        rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        rvSearchResults.adapter = adapter

        btnSearch.setOnClickListener { runSearch() }

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

    private fun runSearch() {
        val query = etSearchQuery.text?.toString()?.trim().orEmpty()
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
                    radiusMeters = 20_000.0
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
}
