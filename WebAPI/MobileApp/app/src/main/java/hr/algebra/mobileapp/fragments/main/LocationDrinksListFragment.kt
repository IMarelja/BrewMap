package hr.algebra.mobileapp.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import hr.algebra.mobileapp.R
import hr.algebra.mobileapp.adapters.DrinkAdapter
import hr.algebra.mobileapp.models.drink.CreateDrinkRequest
import hr.algebra.mobileapp.models.drink.Drink
import hr.algebra.mobileapp.service.ServiceProvider
import kotlinx.coroutines.NonCancellable.parent
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class LocationDrinksListFragment : Fragment() {

    private lateinit var progressDrinks: ProgressBar
    private lateinit var tvBestDrink: TextView
    private lateinit var tvDrinkState: TextView
    private lateinit var rvDrinks: RecyclerView

    private lateinit var btnAddDrink: MaterialButton

    private val drinkAdapter = DrinkAdapter().apply {
        setOnItemLongClickListener { drink ->
            editDrinkDialog(drink)
        }
    }

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
            tvDrinkState.text = getString(R.string.error_missing_location_id)
            return
        }

        btnAddDrink = view.findViewById(R.id.btn_add_drink)
        btnAddDrink.setOnClickListener {
            createDrinkDialog()
        }

        loadDrinks(locationId)
    }

    private fun createDrinkDialog(){
        val dialogView = layoutInflater.inflate(R.layout.dialog_drink_form, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.et_drink_name)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.et_drink_description)

        AlertDialog.Builder(requireContext())
            .setTitle("Create drink")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val description = etDescription.text.toString().trim().takeIf { it.isNotEmpty() }

                if (name.isEmpty()) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Error")
                        .setMessage("Name cannot be empty")
                        .setPositiveButton("OK", null)
                        .show()
                    return@setPositiveButton
                }

                createDrink(name, description)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createDrink(name: String, description: String?) {
        viewLifecycleOwner.lifecycleScope.launch {

            val locationId = requireArguments().getString(ARG_LOCATION_ID).orEmpty()
            val request = CreateDrinkRequest( name, description, locationId)
            val drinkResult = ServiceProvider.drinkService.create(request)

            if (drinkResult.isSuccess){
                AlertDialog.Builder(requireContext())
                    .setTitle("Success")
                    .setMessage("Drink created successfully")
                    .setPositiveButton("OK") { _, _ ->
                        loadDrinks(locationId)
                    }
                    .show()
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle("Error")
                    .setMessage("Unable to create drink: ${drinkResult.errorMessage()}")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    private fun editDrinkDialog(drink: Drink){
        val dialogView = layoutInflater.inflate(R.layout.dialog_drink_form, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.et_drink_name)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.et_drink_description)

        etName.setText(drink.name)
        etDescription.setText(drink.description ?: "")

        AlertDialog.Builder(requireContext())
            .setTitle("Edit drink")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val description = etDescription.text.toString().trim().takeIf { it.isNotEmpty() }

                if (name.isEmpty()) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Error")
                        .setMessage("Name cannot be empty")
                        .setPositiveButton("OK", null)
                        .show()
                    return@setPositiveButton
                }

                updateDrink(drink.id, name, description, drink.availableAtLocationId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateDrink(drinkId: String, name: String, description: String?, locationId: String){
        viewLifecycleOwner.lifecycleScope.launch {
            val result = ServiceProvider.drinkService.update(drinkId, name, description)

            if (result.isSuccess) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Success")
                    .setMessage("Drink updated successfully")
                    .setPositiveButton("OK") { _, _ ->
                        loadDrinks(locationId)
                    }
                    .show()
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle("Error")
                    .setMessage("Unable to update drink: ${result.errorMessage()}")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }


    private fun loadDrinks(locationId: String) {
        progressDrinks.visibility = View.VISIBLE
        tvDrinkState.visibility = View.VISIBLE
        tvDrinkState.text = getString(R.string.state_loading_drinks)
        tvBestDrink.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            val drinksDeferred    = async { ServiceProvider.drinkService.getByLocationId(locationId) }
            val bestDrinkDeferred = async { ServiceProvider.drinkService.getBestDrinkByLocationId(locationId) }

            val drinksResult    = drinksDeferred.await()
            val bestDrinkResult = bestDrinkDeferred.await()

            when {
                drinksResult.isUnauthorized  -> { /* MainActivity navigating to login */ }
                drinksResult.isNetworkError  -> tvDrinkState.text = getString(R.string.error_no_connection)
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
                        getString(R.string.state_best_drink_none)
                    } else {
                        getString(R.string.state_best_drink_format, bestDrink.name, bestDrink.rating)
                    }

                    tvDrinkState.text = if (drinks.isEmpty()) {
                        getString(R.string.state_no_drinks)
                    } else {
                        getString(R.string.state_drinks_loaded, drinks.size)
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
