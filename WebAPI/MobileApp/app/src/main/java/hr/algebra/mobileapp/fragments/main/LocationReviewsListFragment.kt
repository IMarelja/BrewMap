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
import hr.algebra.mobileapp.adapters.ReviewAdapter
import hr.algebra.mobileapp.service.ServiceProvider
import kotlinx.coroutines.launch

class LocationReviewsListFragment : Fragment() {

    private lateinit var progressReviews: ProgressBar
    private lateinit var tvReviewState: TextView
    private lateinit var rvReviews: RecyclerView

    private val reviewAdapter = ReviewAdapter()

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
            tvReviewState.text = "Missing locationService id"
            return
        }

        loadReviews(locationId)
    }

    private fun loadReviews(locationId: String) {
        progressReviews.visibility = View.VISIBLE
        tvReviewState.visibility = View.VISIBLE
        tvReviewState.text = "Loading reviews..."

        viewLifecycleOwner.lifecycleScope.launch {
            val result = ServiceProvider.reviewService.getByLocationId(locationId)
            when {
                result.isUnauthorized      -> { /* MainActivity navigating to login */ }
                result.isNetworkError      -> tvReviewState.text = "No connection. Please check your internet."
                result.errors.isNotEmpty() -> {
                    reviewAdapter.submitData(emptyList())
                    tvReviewState.text = result.errorMessage()
                }
                else -> {
                    val reviews = result.data!!
                    reviewAdapter.submitData(reviews)
                    rvReviews.post { rvReviews.requestLayout() }
                    tvReviewState.text = if (reviews.isEmpty()) {
                        "No reviews for this locationService"
                    } else {
                        "${reviews.size} reviews loaded"
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
