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

class LoginFragment : Fragment() {

    private lateinit var tilEmail:    TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etEmail:     TextInputEditText
    private lateinit var etPassword:  TextInputEditText
    private lateinit var btnSubmit:       MaterialButton
    private lateinit var btnGoToRegister: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tilEmail       = view.findViewById(R.id.til_email)
        tilPassword    = view.findViewById(R.id.til_password)
        etEmail        = view.findViewById(R.id.et_email)
        etPassword     = view.findViewById(R.id.et_password)
        btnSubmit      = view.findViewById(R.id.btn_login_submit)
        btnGoToRegister = view.findViewById(R.id.btn_go_to_register)

        btnSubmit.setOnClickListener { onLoginSubmit() }

        btnGoToRegister.setOnClickListener {
            (requireActivity() as LoginActivity).showRegisterFragment()
        }
    }

    private fun onLoginSubmit() {
        val email    = etEmail.text?.toString()?.trim() ?: ""
        val password = etPassword.text?.toString()?.trim() ?: ""

        // Reset errors
        tilEmail.error    = null
        tilPassword.error = null

        // Basic validation
        var isValid = true
        if (email.isEmpty()) {
            tilEmail.error = getString(R.string.error_empty_email)
            isValid = false
        }
        if (password.isEmpty()) {
            tilPassword.error = getString(R.string.error_empty_password)
            isValid = false
        }
        if (!isValid) return

        // Disable button while the request is in flight
        btnSubmit.isEnabled = false

        // rememberMe defaults to true — the server will issue a 30-day JWT
        // which TokenManager will persist to EncryptedSharedPreferences.
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ServiceProvider.auth.login(
                    user       = email,
                    password   = password,
                    rememberMe = true
                )

                if (response.success) {
                    TokenManager.saveToken(response.token, rememberMe = true)
                    navigateToMain()
                } else {
                    tilEmail.error = response.message
                        ?: getString(R.string.error_login_failed)
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
