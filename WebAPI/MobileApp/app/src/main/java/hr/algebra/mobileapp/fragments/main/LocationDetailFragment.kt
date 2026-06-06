package hr.algebra.mobileapp.fragments.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import android.app.TimePickerDialog
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import hr.algebra.mobileapp.models.category.Category
import hr.algebra.mobileapp.models.paymentoption.PaymentOption
import hr.algebra.mobileapp.models.location.Address
import hr.algebra.mobileapp.models.location.Contact
import hr.algebra.mobileapp.models.location.DayOpeningHours
import hr.algebra.mobileapp.models.location.UpdateLocationRequest

class LocationDetailFragment : Fragment() {

    private lateinit var progressDetail: ProgressBar
    private lateinit var tvName: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvMeta: TextView
    private lateinit var mapView: MapView
    private lateinit var btnOpeningHours: MaterialButton
    private lateinit var btnReviews: MaterialButton
    private lateinit var btnDrinks: MaterialButton
    private var detailMarker: Marker? = null
    private val pinBitmap: Bitmap? by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.pin)
    }
    private val scaledPinIconCache = mutableMapOf<Int, Drawable?>()

    private var locationId: String = ""
    private var activeSection: Section = Section.NONE
    private var isOpeningHoursVisible = false

    private var currentLocation : Location? = null
    private lateinit var btnEditLocation: MaterialButton

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
        btnOpeningHours = view.findViewById(R.id.btn_opening_hours)
        btnReviews = view.findViewById(R.id.btn_reviews)
        btnDrinks = view.findViewById(R.id.btn_drinks)
        btnEditLocation = view.findViewById(R.id.btn_edit_location)

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

        btnOpeningHours.setOnClickListener {
            if (isOpeningHoursVisible) {
                hideOpeningHours()
            } else {
                showOpeningHours()
            }
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

        btnEditLocation.setOnClickListener {
            editLocationDialog()
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
        currentLocation = location
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

    private fun showOpeningHours() {
        childFragmentManager.beginTransaction()
            .replace(
                R.id.opening_hours_content_container,
                LocationOpeningHoursListFragment.newInstance(locationId)
            )
            .commit()

        isOpeningHoursVisible = true
        updateOpeningHoursButtonState()
    }

    private fun hideOpeningHours() {
        childFragmentManager.findFragmentById(R.id.opening_hours_content_container)?.let { fragment ->
            childFragmentManager.beginTransaction().remove(fragment).commit()
        }
        isOpeningHoursVisible = false
        updateOpeningHoursButtonState()
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
        updateOpeningHoursButtonState()
        btnReviews.alpha = if (section == Section.REVIEWS) 1.0f else 0.7f
        btnDrinks.alpha = if (section == Section.DRINKS) 1.0f else 0.7f
    }

    private fun updateOpeningHoursButtonState() {
        btnOpeningHours.alpha = if (isOpeningHoursVisible) 1.0f else 0.7f
        btnOpeningHours.text = if (isOpeningHoursVisible) {
            getString(R.string.hide_opening_hours)
        } else {
            getString(R.string.opening_hours)
        }
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


    private fun editLocationDialog(){
        val location = currentLocation

        if(location == null){
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_title_error)
                .setMessage(R.string.error_load_location)
                .setPositiveButton(R.string.btn_ok, null)
                .show()
            return
        }

        val dialogView=layoutInflater.inflate(R.layout.dialog_location_form, null)
        val tilName        = dialogView.findViewById<TextInputLayout>(R.id.til_location_name)
        val tilAddress     = dialogView.findViewById<TextInputLayout>(R.id.til_location_address)
        val tilCity        = dialogView.findViewById<TextInputLayout>(R.id.til_location_city)
        val tilCountry     = dialogView.findViewById<TextInputLayout>(R.id.til_location_country)
        val tilPostalCode  = dialogView.findViewById<TextInputLayout>(R.id.til_location_postal_code)
        val tilCategory    = dialogView.findViewById<TextInputLayout>(R.id.til_location_category)
        val tvOpeningHoursLabel = dialogView.findViewById<android.widget.TextView>(R.id.tv_opening_hours_label)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.et_location_name)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.et_location_description)
        val etAddress = dialogView.findViewById<TextInputEditText>(R.id.et_location_address)
        val etCity = dialogView.findViewById<TextInputEditText>(R.id.et_location_city)
        val etCountry = dialogView.findViewById<TextInputEditText>(R.id.et_location_country)
        val etPostalCode = dialogView.findViewById<TextInputEditText>(R.id.et_location_postal_code)
        val spinnerCategory = dialogView.findViewById<AutoCompleteTextView>(R.id.spinner_location_category)
        val llPaymentOptions = dialogView.findViewById<LinearLayout>(R.id.ll_payment_options)
        val llOpeningHours = dialogView.findViewById<LinearLayout>(R.id.ll_opening_hours)
        val etWebsite = dialogView.findViewById<TextInputEditText>(R.id.et_location_website)

        etName.setText(location.name)
        etDescription.setText(location.description ?: "")
        etAddress.setText(location.address.street)
        etCity.setText(location.address.city)
        etCountry.setText(location.address.country)
        etPostalCode.setText(location.address.postalCode)
        var categories: List<Category> = emptyList()
        var paymentOptions: List<PaymentOption> = emptyList()
        viewLifecycleOwner.lifecycleScope.launch {
            val catResult = ServiceProvider.categoryService.getAll()
            categories = catResult.data ?: emptyList()
            val names = categories.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
            spinnerCategory.setAdapter(adapter)
            val current = categories.firstOrNull { it.tag == location.categoryTag }
            if (current != null) spinnerCategory.setText(current.name, false)

            val payResult = ServiceProvider.paymentOptionService.getAll()
            paymentOptions = payResult.data ?: emptyList()
            paymentOptions.forEach { option ->
                val cb = CheckBox(requireContext()).apply {
                    text = option.name
                    isChecked = option.tag in location.paymentOptionTags
                    buttonTintList = resources.getColorStateList(R.color.brew_checkbox_tint, null)
                    setTextColor(resources.getColor(R.color.brew_dark, null))
                }
                llPaymentOptions.addView(cb)
            }
        }

        fun parseTime(t: String): Pair<Int, Int>? {
            val parts = t.trim().split(":")
            if (parts.size != 2) return null
            val h = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            return Pair(h, m)
        }
        fun fmtTime(h: Int, m: Int) = "$h:${m.toString().padStart(2, '0')}"

        data class DayRow(
            val key: String,
            val tilOpen: TextInputLayout,
            val etOpen: TextInputEditText,
            val tilClose: TextInputLayout,
            val etClose: TextInputEditText,
            val cbClosed: CheckBox
        )

        val defaultOpen  = "8:00"
        val defaultClose = "22:00"

        val days = listOf("monday","tuesday","wednesday","thursday","friday","saturday","sunday")
        val dayLabels = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")
        val dayRows = days.mapIndexed { i, key ->
            val rowView = layoutInflater.inflate(R.layout.item_opening_hours_row, llOpeningHours, false)
            rowView.findViewById<android.widget.TextView>(R.id.tv_day_name).text = dayLabels[i]
            val tilOpen  = rowView.findViewById<TextInputLayout>(R.id.til_open_time)
            val etOpen   = rowView.findViewById<TextInputEditText>(R.id.et_open_time)
            val tilClose = rowView.findViewById<TextInputLayout>(R.id.til_close_time)
            val etClose  = rowView.findViewById<TextInputEditText>(R.id.et_close_time)
            val cbClosed = rowView.findViewById<CheckBox>(R.id.cb_is_closed)

            val dayHours = location.openingHours[key]
            if (dayHours?.isClosed == true) {
                cbClosed.isChecked = true
                etOpen.isEnabled = false
                etClose.isEnabled = false
                tilOpen.isEnabled = false
                tilClose.isEnabled = false
            } else {
                etOpen.setText(dayHours?.open?.let { parseTime(it)?.let { (h,m) -> fmtTime(h,m) } } ?: defaultOpen)
                etClose.setText(dayHours?.close?.let { parseTime(it)?.let { (h,m) -> fmtTime(h,m) } } ?: defaultClose)
            }

            cbClosed.setOnCheckedChangeListener { _, closed ->
                etOpen.isEnabled = !closed
                etClose.isEnabled = !closed
                tilOpen.isEnabled = !closed
                tilClose.isEnabled = !closed
                if (!closed) {
                    if (etOpen.text.isNullOrBlank())  etOpen.setText(defaultOpen)
                    if (etClose.text.isNullOrBlank()) etClose.setText(defaultClose)
                }
            }

            etOpen.setOnClickListener {
                val (initH, initM) = parseTime(etOpen.text.toString()) ?: parseTime(defaultOpen)!!
                TimePickerDialog(requireContext(), { _, h, m ->
                    val (closeH, closeM) = parseTime(etClose.text.toString()) ?: parseTime(defaultClose)!!
                    val openMins  = h * 60 + m
                    val closeMins = closeH * 60 + closeM
                    val (finalH, finalM) = if (openMins >= closeMins) {
                        val adj = closeMins - 60
                        if (adj < 0) Pair(0, 0) else Pair(adj / 60, adj % 60)
                    } else Pair(h, m)
                    etOpen.setText(fmtTime(finalH, finalM))
                }, initH, initM, true).show()
            }

            etClose.setOnClickListener {
                val (initH, initM) = parseTime(etClose.text.toString()) ?: parseTime(defaultClose)!!
                TimePickerDialog(requireContext(), { _, h, m ->
                    val (openH, openM) = parseTime(etOpen.text.toString()) ?: parseTime(defaultOpen)!!
                    val openMins  = openH * 60 + openM
                    val closeMins = h * 60 + m
                    val (finalH, finalM) = if (closeMins <= openMins) {
                        val adj = openMins + 60
                        if (adj >= 24 * 60) Pair(23, 59) else Pair(adj / 60, adj % 60)
                    } else Pair(h, m)
                    etClose.setText(fmtTime(finalH, finalM))
                }, initH, initM, true).show()
            }

            llOpeningHours.addView(rowView)
            DayRow(key, tilOpen, etOpen, tilClose, etClose, cbClosed)
        }

        etWebsite.setText(location.contact?.website ?: "")

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_edit_location)
            .setView(dialogView)
            .setPositiveButton(R.string.btn_save, null)
            .setNegativeButton(R.string.btn_cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name       = etName.text.toString().trim()
                val address    = etAddress.text.toString().trim()
                val city       = etCity.text.toString().trim()
                val country    = etCountry.text.toString().trim()
                val postalCode = etPostalCode.text.toString().trim()
                val selectedCategoryName = spinnerCategory.text.toString().trim()
                val categoryTag = categories.firstOrNull { it.name == selectedCategoryName }?.tag

                val allDaysClosed = dayRows.all { it.cbClosed.isChecked }

                val req = getString(R.string.error_field_required)
                tilName.error       = if (name.isEmpty()) req else null
                tilAddress.error    = if (address.isEmpty()) req else null
                tilCity.error       = if (city.isEmpty()) req else null
                tilCountry.error    = if (country.isEmpty()) req else null
                tilPostalCode.error = if (postalCode.isEmpty()) req else null
                tilCategory.error   = if (categoryTag == null) req else null

                val errorColor   = resources.getColor(R.color.auth_error_border, null)
                val defaultColor = resources.getColor(R.color.brew_dark, null)
                tvOpeningHoursLabel.setTextColor(if (allDaysClosed) errorColor else defaultColor)

                if (name.isEmpty() || address.isEmpty() || city.isEmpty() || country.isEmpty()
                    || postalCode.isEmpty() || categoryTag == null || allDaysClosed) return@setOnClickListener

                val selectedPaymentTags = paymentOptions
                    .filterIndexed { i, _ ->
                        (llPaymentOptions.getChildAt(i) as? CheckBox)?.isChecked == true
                    }
                    .map { it.tag }

                val openingHours = dayRows.associate { row ->
                    val closed = row.cbClosed.isChecked
                    row.key to DayOpeningHours(
                        if (closed) null else row.etOpen.text.toString().trim(),
                        if (closed) null else row.etClose.text.toString().trim(),
                        closed
                    )
                }

                val website = etWebsite.text.toString().trim().takeIf { it.isNotEmpty() }

                dialog.dismiss()
                updateLocation(name, etDescription.text.toString(),
                    address, city, country, postalCode, categoryTag, selectedPaymentTags,
                    openingHours, website)
            }
        }
        dialog.show()
    }

    private fun updateLocation(name: String, description: String,
                               address: String, city: String, country: String,
                               postalCode: String, category: String, paymentTags: List<String>,
                               openingHours: Map<String, DayOpeningHours>, contact: String?){
        viewLifecycleOwner.lifecycleScope.launch {
            val location = currentLocation ?: return@launch

            val fullAddress = Address(address, city, country, postalCode)
            val contact = if(contact != null) Contact(contact) else null

            val request = UpdateLocationRequest(
                name, description.takeIf { it.isNotEmpty() },
                fullAddress, category,
                paymentTags,
                contact,
                openingHours,
                null)

            val result = ServiceProvider.locationService.update(location.id, request)

            if(result.isSuccess){
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.dialog_title_success)
                    .setMessage(R.string.success_location_updated)
                    .setPositiveButton(R.string.btn_ok) { _, _ ->
                        loadLocationDetails(location.id)
                    }
                    .show()
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.dialog_title_error)
                    .setMessage(getString(R.string.error_update_location_format, result.errorMessage() ?: getString(R.string.error_unknown)))
                    .setPositiveButton(R.string.btn_ok, null)
                    .show()
            }
        }
    }


}
