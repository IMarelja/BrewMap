package hr.algebra.mobileapp.fragments.profile

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import hr.algebra.mobileapp.R
import hr.algebra.mobileapp.service.ServiceProvider
import kotlinx.coroutines.launch

class ProfileSettingsFragment : Fragment() {

    private lateinit var errorMessageCard: MaterialCardView
    private lateinit var errorMessageText: TextView
    private lateinit var etNewEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnUpdateEmail: MaterialButton
    private lateinit var etCurrentPassword: TextInputEditText
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var etConfirmNewPassword: TextInputEditText
    private lateinit var btnUpdatePassword: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile_settings, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        errorMessageCard = view.findViewById(R.id.error_message_card)
        errorMessageText = view.findViewById(R.id.error_message_text)
        etNewEmail = view.findViewById(R.id.et_new_email)
        etPassword = view.findViewById(R.id.et_password)
        btnUpdateEmail = view.findViewById(R.id.btn_update_email)
        etCurrentPassword = view.findViewById(R.id.et_current_password)
        etNewPassword = view.findViewById(R.id.et_new_password)
        etConfirmNewPassword = view.findViewById(R.id.et_confirm_new_password)
        btnUpdatePassword = view.findViewById(R.id.btn_update_password)

        btnUpdateEmail.setOnClickListener {
            updateEmail()
        }

        btnUpdatePassword.setOnClickListener {
            updatePassword()
        }

    }

    private fun updateEmail() {
        val newEmail = etNewEmail.text?.toString()?.trim() ?: ""
        val password = etPassword.text?.toString()?.trim() ?: ""

        hideFormMessage()

        val validationErrors = mutableListOf<String>()
        if (newEmail.isEmpty()) {
            validationErrors += getString(R.string.error_empty_email)
        } else if (newEmail.contains("@") && !isValidEmail(newEmail)) {
            validationErrors += getString(R.string.error_invalid_email)
        }
        if (password.isEmpty()) {
            validationErrors += getString(R.string.error_empty_password)
        }
        if (validationErrors.isNotEmpty()) {
            showFormMessage(validationErrors.joinToString("\n"), isError = true)
            return
        }

        // Disable button while the request is in flight
        btnUpdateEmail.isEnabled = false

        // rememberMe defaults to true — the server will issue a 30-day JWT
        // which TokenManager will persist to EncryptedSharedPreferences.
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = ServiceProvider.userService.updateEmail(
                    newEmail = newEmail,
                    currentPassword = password
                )
                val response = result.data

                if (result.isSuccess) {
                    showFormMessage(
                        "Email updated.",
                        isError = false
                    )
                    btnUpdateEmail.isEnabled = true

                } else {
                    showFormMessage(
                        "Unable to update email. Please try again.",
                        isError = true
                    )
                    btnUpdateEmail.isEnabled = true
                }
            } catch (e: Exception) {
                showFormMessage(getString(R.string.error_network), isError = true)
                btnUpdateEmail.isEnabled = true
            }
        }
    }

    private fun updatePassword() {
        val currentPassword = etCurrentPassword.text?.toString()?.trim() ?: ""
        val newPassword = etNewPassword.text?.toString()?.trim() ?: ""
        val confirmNewPassword = etConfirmNewPassword.text?.toString()?.trim() ?: ""

        hideFormMessage()

        val validationErrors = mutableListOf<String>()
        if (currentPassword.isEmpty()) {
            validationErrors += "Current Password cannot be empty"
        }
        if (newPassword.isEmpty()) {
            validationErrors += "New Password cannot be empty"
        }
        if (confirmNewPassword.isEmpty()) {
            validationErrors += "New Password Confirmation cannot be empty"
        }
        if (validationErrors.isNotEmpty()) {
            showFormMessage(validationErrors.joinToString("\n"), isError = true)
            return
        }

        // Disable button while the request is in flight
        btnUpdatePassword.isEnabled = false

        // rememberMe defaults to true — the server will issue a 30-day JWT
        // which TokenManager will persist to EncryptedSharedPreferences.
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = ServiceProvider.userService.updatePassword(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    confirmNewPassword = confirmNewPassword
                )
                val response = result.data

                if (result.isSuccess) {
                    showFormMessage(
                        "Password updated",
                        isError = false
                    )
                    btnUpdatePassword.isEnabled = true

                } else {
                    showFormMessage(
                        "Unable to update password. Please try again.",
                        isError = true
                    )
                    btnUpdatePassword.isEnabled = true
                }
            } catch (e: Exception) {
                showFormMessage(getString(R.string.error_network), isError = true)
                btnUpdatePassword.isEnabled = true
            }
        }
    }

    private fun isValidEmail(value: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(value).matches()

    private fun showFormMessage(message: String, isError: Boolean) {
        errorMessageText.text = message
        val bgColor = if (isError) R.color.auth_error_bg else R.color.auth_success_bg
        val borderColor = if (isError) R.color.auth_error_border else R.color.auth_success_border
        val textColor = if (isError) R.color.auth_error_text else R.color.auth_success_text

        errorMessageCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), bgColor))
        errorMessageCard.strokeColor = ContextCompat.getColor(requireContext(), borderColor)
        errorMessageText.setTextColor(ContextCompat.getColor(requireContext(), textColor))
        errorMessageCard.visibility = View.VISIBLE
    }

    private fun hideFormMessage() {
        errorMessageText.text = ""
        errorMessageCard.visibility = View.GONE
    }


}