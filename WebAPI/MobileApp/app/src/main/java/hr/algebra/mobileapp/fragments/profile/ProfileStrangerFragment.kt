package hr.algebra.mobileapp.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import hr.algebra.mobileapp.R
import hr.algebra.mobileapp.adapters.ReviewAdapter
import hr.algebra.mobileapp.models.user.StrangerProfile
import hr.algebra.mobileapp.service.ServiceProvider
import kotlinx.coroutines.launch

class ProfileStrangerFragment : Fragment() {
    private lateinit var progressStranger: ProgressBar
    private lateinit var tvUsername: TextView
    private lateinit var tvReviewState: TextView
    private lateinit var rvReviews: RecyclerView

    private var userId: String = ""
    private val reviewAdapter = ReviewAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile_stranger, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressStranger = view.findViewById(R.id.progress_stranger)
        tvUsername = view.findViewById(R.id.tv_stranger_username)

        userId = requireArguments().getString(ARG_USER_ID).orEmpty()
        if (userId.isBlank()) {
            Toast.makeText(requireContext(), getString(R.string.error_missing_user_id), Toast.LENGTH_SHORT).show()
            return
        }
        loadProfileDetails(userId)

    }

    private fun loadProfileDetails(userId: String) {
        progressStranger.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            val result = ServiceProvider.userService.getUserById(userId)
            when {
                result.isUnauthorized  -> { /* MainActivity already navigating to log in */ }
                result.isNetworkError  -> Toast.makeText(requireContext(), getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show()
                result.errors.isNotEmpty() -> Toast.makeText(requireContext(), result.errorMessage(), Toast.LENGTH_SHORT).show()
                else -> {
                    val profile = result.data!!
                    bindProfile(profile)
                }
            }
            progressStranger.visibility = View.GONE
        }
    }

    private fun bindProfile(profile: StrangerProfile) {
        tvUsername.text = profile.username
    }

    private fun loadMyReviews() {
        progressStranger.visibility = View.VISIBLE
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
                    tvReviewState.text = if (reviews.isEmpty()) {
                        "This user has no reviews"
                    } else {
                        getString(R.string.state_reviews_loaded, reviews.size)
                    }
                }
            }
            progressStranger.visibility = View.GONE
        }
    }

    companion object {
        private const val ARG_USER_ID = "arg_user_id"

        fun newInstance(userId: String): ProfileStrangerFragment {
            val fragment = ProfileStrangerFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_USER_ID, userId)
            }
            return fragment
        }
    }

}