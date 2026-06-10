package hr.algebra.mobileapp.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import hr.algebra.mobileapp.MainActivity
import hr.algebra.mobileapp.R
import hr.algebra.mobileapp.adapters.ReviewAdapter
import hr.algebra.mobileapp.fragments.main.NonScrollableLinearLayoutManager
import hr.algebra.mobileapp.models.review.Review
import hr.algebra.mobileapp.models.user.UserProfile
import hr.algebra.mobileapp.service.ServiceProvider
import kotlinx.coroutines.launch

class ProfileMyFragment : Fragment() {
    private lateinit var progressProfile: ProgressBar
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnSettings: MaterialButton
    private lateinit var tvReviewState: TextView
    private lateinit var rvReviews: RecyclerView

    private val reviewAdapter = ReviewAdapter(showLocationButton = true).apply {
        setOnEditClickListener { review ->
            editReview(review)
        }
        setOnDeleteClickListener { review ->
            deleteReview(review)
        }
        setOnViewLocationClickListener { review ->
            navigateToReviewLocation(review)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile_my, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressProfile = view.findViewById(R.id.progress_profile)
        tvUsername = view.findViewById(R.id.tv_profile_username)
        tvEmail = view.findViewById(R.id.tv_profile_email)
        btnSettings = view.findViewById(R.id.btn_profile_settings)
        tvReviewState = view.findViewById(R.id.tv_review_state_my)
        rvReviews = view.findViewById(R.id.rv_reviews_my)

        rvReviews.layoutManager = NonScrollableLinearLayoutManager(requireContext())
        rvReviews.adapter = reviewAdapter

        btnSettings.setOnClickListener {
            openProfileSettings()
        }
        loadProfileDetails()
        loadMyReviews()
    }

    private fun openProfileSettings() {
        (requireActivity() as MainActivity).openProfileSettings()
    }

    private fun loadProfileDetails() {
        progressProfile.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            val result = ServiceProvider.userService.getMyProfile()
            when {
                result.isUnauthorized  -> { /* MainActivity already navigating to log in */ }
                result.isNetworkError  -> Toast.makeText(requireContext(), getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show()
                result.errors.isNotEmpty() -> Toast.makeText(requireContext(), result.errorMessage(), Toast.LENGTH_SHORT).show()
                else -> {
                    val profile = result.data!!
                    bindProfile(profile)
                }
            }
            progressProfile.visibility = View.GONE
        }
    }

    private fun bindProfile(profile: UserProfile) {
        tvUsername.text = profile.username
        tvEmail.text = profile.email
    }

    private fun loadMyReviews() {
        progressProfile.visibility = View.VISIBLE
        tvReviewState.visibility = View.VISIBLE
        tvReviewState.text = getString(R.string.state_loading_reviews)

        viewLifecycleOwner.lifecycleScope.launch {
            val result = ServiceProvider.reviewService.getMyReviews()
            when {
                result.isUnauthorized      -> { /* MainActivity navigating to login */ }
                result.isNetworkError      -> tvReviewState.text = getString(R.string.error_no_connection)
                result.errors.isNotEmpty() -> {
                    reviewAdapter.submitData(emptyList())
                    tvReviewState.text = result.errorMessage()
                }
                else -> {
                    val reviews = result.data!!
                    reviewAdapter.submitData(reviews)
                    rvReviews.post { rvReviews.requestLayout() }
                    tvReviewState.text = if (reviews.isEmpty()) { "You have no reviews" }
                    else {
                        getString(R.string.state_reviews_loaded, reviews.size)
                    }
                }
            }
            progressProfile.visibility = View.GONE
        }
    }

    private fun ratingFromRadioGroup(rgRating: RadioGroup): Int? = when (rgRating.checkedRadioButtonId) {
        R.id.rb_rating_1 -> 1
        R.id.rb_rating_2 -> 2
        R.id.rb_rating_3 -> 3
        R.id.rb_rating_4 -> 4
        R.id.rb_rating_5 -> 5
        else -> null
    }

    private fun radioButtonIdForRating(rating: Int): Int? = when (rating) {
        1 -> R.id.rb_rating_1
        2 -> R.id.rb_rating_2
        3 -> R.id.rb_rating_3
        4 -> R.id.rb_rating_4
        5 -> R.id.rb_rating_5
        else -> null
    }

    private fun editReview(review: Review) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_review_form, null)
        val rgRating = dialogView.findViewById<RadioGroup>(R.id.rg_rating)
        val etComment = dialogView.findViewById<TextInputEditText>(R.id.et_comment)

        radioButtonIdForRating(review.rating)?.let { rgRating.check(it) }
        etComment.setText(review.comment ?: "")

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_edit_review)
            .setView(dialogView)
            .setPositiveButton(R.string.btn_save) { _, _ ->
                val rating = ratingFromRadioGroup(rgRating)
                val comment = etComment.text.toString().trim().takeIf { it.isNotEmpty() }

                if (rating == null || rating > 5 || rating < 1) {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.dialog_title_error)
                        .setMessage(R.string.error_rating_range)
                        .setPositiveButton(R.string.btn_ok, null)
                        .show()
                    return@setPositiveButton
                }

                updateReview(review.id, rating, comment)
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun deleteReview(review: Review) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_delete_review)
            .setMessage(R.string.confirm_delete_review)
            .setPositiveButton(R.string.btn_delete) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val result = ServiceProvider.reviewService.delete(review.id)

                    if (result.isSuccess) {
                        AlertDialog.Builder(requireContext())
                            .setTitle(R.string.dialog_title_success)
                            .setMessage(R.string.success_review_deleted)
                            .setPositiveButton(R.string.btn_ok) { _, _ ->
                                loadMyReviews()
                            }
                            .show()
                    } else {
                        AlertDialog.Builder(requireContext())
                            .setTitle(R.string.dialog_title_error)
                            .setMessage(getString(R.string.error_delete_review_format, result.errorMessage() ?: getString(R.string.error_unknown)))
                            .setPositiveButton(R.string.btn_ok, null)
                            .show()
                    }
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun updateReview(reviewId: String, rating: Int, comment: String?) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = ServiceProvider.reviewService.update(reviewId, rating, comment)

            if (result.isSuccess) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.dialog_title_success)
                    .setMessage(R.string.success_review_updated)
                    .setPositiveButton(R.string.btn_ok) { _, _ ->
                        loadMyReviews()
                    }
                    .show()
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.dialog_title_error)
                    .setMessage(getString(R.string.error_update_review_format, result.errorMessage() ?: getString(R.string.error_unknown)))
                    .setPositiveButton(R.string.btn_ok, null)
                    .show()
            }
        }
    }

    private fun navigateToReviewLocation(review: Review) {
        viewLifecycleOwner.lifecycleScope.launch {
            val locationId = when (review.targetType) {
                "location" -> review.targetId
                "product" -> {
                    val drinkResult = ServiceProvider.drinkService.getById(review.targetId)
                    if (!drinkResult.isSuccess || drinkResult.data == null) {
                        showLocationLookupError()
                        return@launch
                    }
                    drinkResult.data.availableAtLocationId
                }
                else -> null
            }

            if (locationId == null) {
                showLocationLookupError()
                return@launch
            }

            val locationResult = ServiceProvider.locationService.getById(locationId)
            if (!locationResult.isSuccess || locationResult.data == null) {
                showLocationLookupError()
                return@launch
            }

            (requireActivity() as MainActivity).openLocationDetail(locationId)
        }
    }

    private fun showLocationLookupError() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_error)
            .setMessage(R.string.error_location_not_found)
            .setPositiveButton(R.string.btn_ok, null)
            .show()
    }
}