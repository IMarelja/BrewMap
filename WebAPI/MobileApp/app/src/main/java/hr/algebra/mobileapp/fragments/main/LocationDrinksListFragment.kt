package hr.algebra.mobileapp.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import hr.algebra.mobileapp.R
import hr.algebra.mobileapp.adapters.DrinkAdapter
import hr.algebra.mobileapp.service.ServiceProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class LocationDrinksListFragment : Fragment() {

    private lateinit var progressDrinks: ProgressBar
    private lateinit var tvBestDrink: TextView
    private lateinit var tvDrinkState: TextView
    private lateinit var rvDrinks: RecyclerView

    private val drinkAdapter = DrinkAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_location_drinks_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDrinks = view.findViewById(R.id.progress_drinks)
        tvBestDrink = view.findViewById(R.id.tv_best_drink)
        tvDrinkState = view.findViewById(R.id.tv_drink_state)
        rvDrinks = view.findViewById(R.id.rv_drinks)

        rvDrinks.layoutManager = NonScrollableLinearLayoutManager(requireContext())
        rvDrinks.adapter = drinkAdapter
        rvDrinks.isNestedScrollingEnabled = false
        rvDrinks.setHasFixedSize(false)

        val locationId = requireArguments().getString(ARG_LOCATION_ID).orEmpty()
        if (locationId.isBlank()) {
            tvDrinkState.visibility = View.VISIBLE
            tvDrinkState.text = "Missing location id"
            return
        }

        loadDrinks(locationId)
    }

    private fun loadDrinks(locationId: String) {
        progressDrinks.visibility = View.VISIBLE
        tvDrinkState.visibility = View.VISIBLE
        tvDrinkState.text = "Loading drinks..."
        tvBestDrink.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            val drinksDeferred    = async { ServiceProvider.drink.getByLocationId(locationId) }
            val bestDrinkDeferred = async { ServiceProvider.drink.getBestDrinkByLocationId(locationId) }

            val drinksResult    = drinksDeferred.await()
            val bestDrinkResult = bestDrinkDeferred.await()

            when {
                drinksResult.isUnauthorized  -> { /* MainActivity navigating to login */ }
                drinksResult.isNetworkError  -> tvDrinkState.text = "No connection. Please check your internet."
                drinksResult.errors.isNotEmpty() -> {
                    drinkAdapter.submitData(emptyList())
                    tvDrinkState.text = drinksResult.errorMessage()
                }
                else -> {
                    val drinks    = drinksResult.data!!
                    val bestDrink = bestDrinkResult.data   // null = no ratings or error — both treated as "none"

                    drinkAdapter.submitData(drinks)
                    rvDrinks.post { rvDrinks.requestLayout() }

                    tvBestDrink.visibility = View.VISIBLE
                    tvBestDrink.text = if (bestDrink == null) {
                        "Best drink: no ratings yet"
                    } else {
                        "Best drink: ${bestDrink.name} (${"%.1f".format(bestDrink.rating)})"
                    }

                    tvDrinkState.text = if (drinks.isEmpty()) {
                        "No drinks for this location"
                    } else {
                        "${drinks.size} drinks loaded"
                    }
                }
            }

            progressDrinks.visibility = View.GONE
        }
    }

    companion object {
        private const val ARG_LOCATION_ID = "arg_location_id"

        fun newInstance(locationId: String): LocationDrinksListFragment {
            val fragment = LocationDrinksListFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_LOCATION_ID, locationId)
            }
            return fragment
        }
    }
}
