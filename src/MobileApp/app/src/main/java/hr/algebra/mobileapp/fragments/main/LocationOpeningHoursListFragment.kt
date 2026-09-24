package hr.algebra.mobileapp.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import hr.algebra.mobileapp.R
import hr.algebra.mobileapp.models.location.DayOpeningHours
import hr.algebra.mobileapp.models.location.Location
import hr.algebra.mobileapp.service.ServiceProvider
import kotlinx.coroutines.launch

class LocationOpeningHoursListFragment : Fragment() {

    private lateinit var progressOpeningHours: ProgressBar
    private lateinit var tvOpeningHoursState: TextView
    private lateinit var cardOpeningHours: MaterialCardView
    private lateinit var tvOpeningHoursList: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_location_opening_hours_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressOpeningHours = view.findViewById(R.id.progress_opening_hours)
        tvOpeningHoursState = view.findViewById(R.id.tv_opening_hours_state)
        cardOpeningHours = view.findViewById(R.id.card_opening_hours)
        tvOpeningHoursList = view.findViewById(R.id.tv_opening_hours_list)

        val locationId = requireArguments().getString(ARG_LOCATION_ID).orEmpty()
        if (locationId.isBlank()) {
            tvOpeningHoursState.visibility = View.VISIBLE
            tvOpeningHoursState.text = getString(R.string.error_missing_location_id)
            return
        }

        loadOpeningHours(locationId)
    }

    private fun loadOpeningHours(locationId: String) {
        progressOpeningHours.visibility = View.VISIBLE
        tvOpeningHoursState.visibility = View.VISIBLE
        tvOpeningHoursState.text = getString(R.string.state_loading_opening_hours)
        cardOpeningHours.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            val result = ServiceProvider.locationService.getById(locationId)
            when {
                result.isUnauthorized -> { /* MainActivity navigating to login */ }
                result.isNetworkError -> tvOpeningHoursState.text = getString(R.string.error_no_connection)
                result.errors.isNotEmpty() -> tvOpeningHoursState.text = result.errorMessage()
                else -> bindOpeningHours(result.data!!)
            }
            progressOpeningHours.visibility = View.GONE
        }
    }

    private fun bindOpeningHours(location: Location) {
        val rows = DAYS.map { day ->
            getString(
                R.string.opening_hours_day_format,
                getString(day.labelRes),
                location.openingHours[day.key].toDisplayText()
            )
        }

        tvOpeningHoursList.text = rows.joinToString(separator = "\n")
        tvOpeningHoursState.visibility = View.GONE
        cardOpeningHours.visibility = View.VISIBLE
    }

    private fun DayOpeningHours?.toDisplayText(): String {
        if (this == null) return getString(R.string.opening_hours_unavailable)
        if (isClosed) return getString(R.string.opening_hours_closed)

        val openText = open?.takeIf { it.isNotBlank() }
        val closeText = close?.takeIf { it.isNotBlank() }
        return if (openText != null && closeText != null) {
            getString(R.string.opening_hours_time_range_format, openText, closeText)
        } else {
            getString(R.string.opening_hours_unavailable)
        }
    }

    companion object {
        private const val ARG_LOCATION_ID = "arg_location_id"

        private data class OpeningDay(
            val key: String,
            val labelRes: Int
        )

        private val DAYS = listOf(
            OpeningDay("monday", R.string.day_monday),
            OpeningDay("tuesday", R.string.day_tuesday),
            OpeningDay("wednesday", R.string.day_wednesday),
            OpeningDay("thursday", R.string.day_thursday),
            OpeningDay("friday", R.string.day_friday),
            OpeningDay("saturday", R.string.day_saturday),
            OpeningDay("sunday", R.string.day_sunday)
        )

        fun newInstance(locationId: String): LocationOpeningHoursListFragment {
            val fragment = LocationOpeningHoursListFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_LOCATION_ID, locationId)
            }
            return fragment
        }
    }
}
