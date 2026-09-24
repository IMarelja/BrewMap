package hr.algebra.mobileapp.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import hr.algebra.mobileapp.MainActivity
import hr.algebra.mobileapp.R
import hr.algebra.mobileapp.auth.TokenManager
import hr.algebra.mobileapp.service.ServiceProvider
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private lateinit var etUsername:         TextInputEditText
    private lateinit var etEmail:            TextInputEditText
    private lateinit var etPassword:         TextInputEditText
    private lateinit var etConfirmPassword:  TextInputEditText
    private lateinit var authMessageCard:    MaterialCardView
    private lateinit var authMessageText:    TextView
    private lateinit var btnSubmit:     MaterialButton
    private lateinit var btnGoToLogin:  MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_register, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etUsername         = view.findViewById(R.id.et_username)
        etEmail            = view.findViewById(R.id.et_email)
        etPassword         = view.findViewById(R.id.et_password)
        etConfirmPassword  = view.findViewById(R.id.et_confirm_password)
        authMessageCard    = view.findViewById(R.id.auth_message_card)
        authMessageText    = view.findViewById(R.id.auth_message_text)
        btnSubmit      = view.findViewById(R.id.btn_register_submit)
        btnGoToLogin   = view.findViewById(R.id.btn_go_to_login)
        authMessageText.movementMethod = ScrollingMovementMethod.getInstance()

        btnSubmit.setOnClickListener { onRegisterSubmit() }

        btnGoToLogin.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun onRegisterSubmit() {
        val username        = etUsername.text?.toString()?.trim() ?: ""
        val email           = etEmail.text?.toString()?.trim() ?: ""
        val password        = etPassword.text?.toString()?.trim() ?: ""
        val confirmPassword = etConfirmPassword.text?.toString()?.trim() ?: ""

        hideFormMessage()

        val validationErrors = mutableListOf<String>()
        if (username.isEmpty()) {
            validationErrors += getString(R.string.error_empty_username)
        }
        if (email.isEmpty()) {
            validationErrors += getString(R.string.error_empty_email)
        } else if (!isValidEmail(email)) {
            validationErrors += getString(R.string.error_invalid_email)
        }
        if (password.isEmpty()) {
            validationErrors += getString(R.string.error_empty_password)
        }
        if (confirmPassword.isEmpty()) {
            validationErrors += getString(R.string.error_empty_confirm_password)
        }
        if (confirmPassword != password) {
            validationErrors += getString(R.string.error_passwords_no_match)
        }
        if (validationErrors.isNotEmpty()) {
            showFormMessage(validationErrors.joinToString("\n"), isError = true)
            return
        }

        // Disable button while the request is in flight
        btnSubmit.isEnabled = false

        // Registration always returns a 60-min JWT; TokenManager persists it
        // so the userService lands in MainActivity without a second login step.
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = ServiceProvider.authService.register(
                    email    = email,
                    username = username,
                    password = password
                )
                val response = result.data

                if (result.isSuccess && response?.success == true) {
                    TokenManager.saveToken(response.token, rememberMe = true)
                    navigateToMain()
                } else {
                    showFormMessage(
                        result.errorMessage() ?: response?.message ?: getString(R.string.error_register_failed),
                        isError = true
                    )
                    btnSubmit.isEnabled = true
                }
            } catch (e: Exception) {
                showFormMessage(getString(R.string.error_network), isError = true)
                btnSubmit.isEnabled = true
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(requireContext(), MainActivity::class.java))
        requireActivity().finish()
    }

    private fun showFormMessage(message: String, isError: Boolean) {
        authMessageText.text = message
        val bgColor = if (isError) R.color.auth_error_bg else R.color.auth_success_bg
        val borderColor = if (isError) R.color.auth_error_border else R.color.auth_success_border
        val textColor = if (isError) R.color.auth_error_text else R.color.auth_success_text

        authMessageCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), bgColor))
        authMessageCard.strokeColor = ContextCompat.getColor(requireContext(), borderColor)
        authMessageText.setTextColor(ContextCompat.getColor(requireContext(), textColor))
        authMessageCard.visibility = View.VISIBLE
    }

    private fun hideFormMessage() {
        authMessageText.text = ""
        authMessageCard.visibility = View.GONE
    }

    private fun isValidEmail(value: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(value).matches()
}
