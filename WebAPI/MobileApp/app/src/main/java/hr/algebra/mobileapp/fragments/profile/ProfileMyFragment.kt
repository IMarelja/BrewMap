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
import com.google.android.material.button.MaterialButton
import hr.algebra.mobileapp.MainActivity
import hr.algebra.mobileapp.R
import hr.algebra.mobileapp.models.user.UserProfile
import hr.algebra.mobileapp.service.ServiceProvider
import kotlinx.coroutines.launch

class ProfileMyFragment : Fragment() {
    private lateinit var progressProfile: ProgressBar
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnSettings: MaterialButton

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

        btnSettings.setOnClickListener {
            openProfileSettings()
        }
        loadProfileDetails()
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

}