package hr.algebra.mobileapp.fragments.main

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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import hr.algebra.mobileapp.R
import hr.algebra.mobileapp.adapters.ReviewAdapter
import hr.algebra.mobileapp.models.review.Review
import hr.algebra.mobileapp.service.ServiceProvider
import kotlinx.coroutines.NonCancellable.parent
import kotlinx.coroutines.launch

class LocationReviewsListFragment : Fragment() {

    private lateinit var progressReviews: ProgressBar
    private lateinit var tvReviewState: TextView
    private lateinit var rvReviews: RecyclerView

    private lateinit var btnAddReview: MaterialButton

    private val reviewAdapter = ReviewAdapter().apply {
        setOnEditClickListener { review ->
            editReview(review)
        }
        setOnDeleteClickListener { review ->
            deleteReview(review)
        }
        setOnReportReviewClickListener { review ->
            reportReviewDialog(review)
        }
        setOnReportUserClickListener { review ->
            reportUserDialog(review)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_location_reviews_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressReviews = view.findViewById(R.id.progress_reviews)
        tvReviewState = view.findViewById(R.id.tv_review_state)
        rvReviews = view.findViewById(R.id.rv_reviews)

        rvReviews.layoutManager = NonScrollableLinearLayoutManager(requireContext())
        rvReviews.adapter = reviewAdapter
        rvReviews.isNestedScrollingEnabled = false
        rvReviews.setHasFixedSize(false)

        val locationId = requireArguments().getString(ARG_LOCATION_ID).orEmpty()
        if (locationId.isBlank()) {
            tvReviewState.visibility = View.VISIBLE
            tvReviewState.text = getString(R.string.error_missing_location_id)
            return
        }

        btnAddReview = view.findViewById(R.id.btn_add_review)
        btnAddReview.setOnClickListener {
            addReviewDialog()
        }

        loadReviews(locationId)
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

    private fun addReviewDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_review_form, null)
        val rgRating = dialogView.findViewById<RadioGroup>(R.id.rg_rating)
        val etComment = dialogView.findViewById<TextInputEditText>(R.id.et_comment)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_add_review)
            .setView(dialogView)
            .setPositiveButton(R.string.btn_save) {_,_ ->
                val rating = ratingFromRadioGroup(rgRating)
                val comment = etComment.text.toString().trim().takeIf { it.isNotEmpty() }

                if(rating == null || rating > 5 || rating < 1){

                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.dialog_title_error)
                        .setMessage(R.string.error_rating_range)
                        .setPositiveButton(R.string.btn_ok, null)
                        .show()
                    return@setPositiveButton
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    val locationId = requireArguments().getString(ARG_LOCATION_ID).orEmpty()
                    val result = ServiceProvider.reviewService.createForLocation(locationId, rating, comment)

                    if(result.isSuccess){
                        AlertDialog.Builder(requireContext())
                            .setTitle(R.string.dialog_title_success)
                            .setMessage(R.string.success_review_added)
                            .setPositiveButton(R.string.btn_ok) { _, _ ->
                                loadReviews(locationId)
                            }
                            .show()
                    } else {
                        AlertDialog.Builder(requireContext())
                            .setTitle(R.string.dialog_title_error)
                            .setMessage(getString(R.string.error_add_review_format, result.errorMessage() ?: getString(R.string.error_unknown)))
                            .setPositiveButton(R.string.btn_ok, null)
                            .show()
                    }
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
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

                updateReview(review.id, rating, comment, review.targetId)
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
                                loadReviews(review.targetId)
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

    private fun updateReview(reviewId: String, rating: Int, comment: String?, locationId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = ServiceProvider.reviewService.update(reviewId, rating, comment)

            if (result.isSuccess) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.dialog_title_success)
                    .setMessage(R.string.success_review_updated)
                    .setPositiveButton(R.string.btn_ok) { _, _ ->
                        loadReviews(locationId)
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

    private fun reportReviewDialog(review: Review) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_report, null)
        val tilReason = dialogView.findViewById<TextInputLayout>(R.id.til_report_reason)
        val etReason = dialogView.findViewById<TextInputEditText>(R.id.et_report_reason)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.et_report_description)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_report_review)
            .setView(dialogView)
            .setPositiveButton(R.string.btn_submit, null)
            .setNegativeButton(R.string.btn_cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val reason = etReason.text.toString().trim()
                val description = etDescription.text.toString().trim().takeIf { it.isNotEmpty() }

                tilReason.error = if (reason.length < 3) getString(R.string.error_report_reason_too_short) else null
                if (reason.length < 3) return@setOnClickListener

                dialog.dismiss()
                reportReview(review.id, reason, description)
            }
        }
        dialog.show()
    }

    private fun reportReview(reviewId: String, reason: String, description: String?) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = ServiceProvider.flagService.create("review", reviewId, reason, description)

            if (result.isSuccess) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.dialog_title_success)
                    .setMessage(R.string.success_review_reported)
                    .setPositiveButton(R.string.btn_ok, null)
                    .show()
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.dialog_title_error)
                    .setMessage(getString(R.string.error_report_review_format, result.errorMessage() ?: getString(R.string.error_unknown)))
                    .setPositiveButton(R.string.btn_ok, null)
                    .show()
            }
        }
    }

    private fun reportUserDialog(review: Review) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_report, null)
        val tilReason = dialogView.findViewById<TextInputLayout>(R.id.til_report_reason)
        val etReason = dialogView.findViewById<TextInputEditText>(R.id.et_report_reason)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.et_report_description)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_report_user)
            .setView(dialogView)
            .setPositiveButton(R.string.btn_submit, null)
            .setNegativeButton(R.string.btn_cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val reason = etReason.text.toString().trim()
                val description = etDescription.text.toString().trim().takeIf { it.isNotEmpty() }

                tilReason.error = if (reason.length < 3) getString(R.string.error_report_reason_too_short) else null
                if (reason.length < 3) return@setOnClickListener

                dialog.dismiss()
                reportUser(review.userId, reason, description)
            }
        }
        dialog.show()
    }

    private fun reportUser(userId: String, reason: String, description: String?) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = ServiceProvider.flagService.create("user", userId, reason, description)

            if (result.isSuccess) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.dialog_title_success)
                    .setMessage(R.string.success_user_reported)
                    .setPositiveButton(R.string.btn_ok, null)
                    .show()
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.dialog_title_error)
                    .setMessage(getString(R.string.error_report_user_format, result.errorMessage() ?: getString(R.string.error_unknown)))
                    .setPositiveButton(R.string.btn_ok, null)
                    .show()
            }
        }
    }

    private fun loadReviews(locationId: String) {
        progressReviews.visibility = View.VISIBLE
        tvReviewState.visibility = View.VISIBLE
        tvReviewState.text = getString(R.string.state_loading_reviews)

        viewLifecycleOwner.lifecycleScope.launch {
            val result = ServiceProvider.reviewService.getByLocationId(locationId)
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
                    tvReviewState.text = if (reviews.isEmpty()) {
                        getString(R.string.state_no_reviews)
                    } else {
                        getString(R.string.state_reviews_loaded, reviews.size)
                    }
                }
            }
            progressReviews.visibility = View.GONE
        }
    }

    companion object {
        private const val ARG_LOCATION_ID = "arg_location_id"

        fun newInstance(locationId: String): LocationReviewsListFragment {
            val fragment = LocationReviewsListFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_LOCATION_ID, locationId)
            }
            return fragment
        }
    }
}
