package hr.algebra.mobileapp.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import hr.algebra.mobileapp.MainActivity
import hr.algebra.mobileapp.R
import hr.algebra.mobileapp.auth.TokenManager
import hr.algebra.mobileapp.service.ServiceProvider
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private lateinit var tilUsername:        TextInputLayout
    private lateinit var tilEmail:           TextInputLayout
    private lateinit var tilPassword:        TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etUsername:         TextInputEditText
    private lateinit var etEmail:            TextInputEditText
    private lateinit var etPassword:         TextInputEditText
    private lateinit var etConfirmPassword:  TextInputEditText
    private lateinit var btnSubmit:     MaterialButton
    private lateinit var btnGoToLogin:  MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_register, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tilUsername        = view.findViewById(R.id.til_username)
        tilEmail           = view.findViewById(R.id.til_email)
        tilPassword        = view.findViewById(R.id.til_password)
        tilConfirmPassword = view.findViewById(R.id.til_confirm_password)
        etUsername         = view.findViewById(R.id.et_username)
        etEmail            = view.findViewById(R.id.et_email)
        etPassword         = view.findViewById(R.id.et_password)
        etConfirmPassword  = view.findViewById(R.id.et_confirm_password)
        btnSubmit      = view.findViewById(R.id.btn_register_submit)
        btnGoToLogin   = view.findViewById(R.id.btn_go_to_login)

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

        // Reset errors
        tilUsername.error        = null
        tilEmail.error           = null
        tilPassword.error        = null
        tilConfirmPassword.error = null

        // Basic validation
        var isValid = true
        if (username.isEmpty()) {
            tilUsername.error = getString(R.string.error_empty_username)
            isValid = false
        }
        if (email.isEmpty()) {
            tilEmail.error = getString(R.string.error_empty_email)
            isValid = false
        }
        if (password.isEmpty()) {
            tilPassword.error = getString(R.string.error_empty_password)
            isValid = false
        }
        if (confirmPassword != password) {
            tilConfirmPassword.error = getString(R.string.error_passwords_no_match)
            isValid = false
        }
        if (!isValid) return

        // Disable button while the request is in flight
        btnSubmit.isEnabled = false

        // Registration always returns a 60-min JWT; TokenManager persists it
        // so the user lands in MainActivity without a second login step.
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ServiceProvider.auth.register(
                    email    = email,
                    username = username,
                    password = password
                )

                if (response.success) {
                    TokenManager.saveToken(response.token, rememberMe = true)
                    navigateToMain()
                } else {
                    // Surface the server's message in the most relevant field
                    val msg = response.message ?: getString(R.string.error_register_failed)
                    when {
                        msg.contains("username", ignoreCase = true) -> tilUsername.error = msg
                        msg.contains("email",    ignoreCase = true) -> tilEmail.error    = msg
                        else                                         -> tilEmail.error    = msg
                    }
                    btnSubmit.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_network),
                    Toast.LENGTH_LONG
                ).show()
                btnSubmit.isEnabled = true
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(requireContext(), MainActivity::class.java))
        requireActivity().finish()
    }
}
