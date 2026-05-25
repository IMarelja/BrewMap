package hr.algebra.mobileapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import hr.algebra.mobileapp.auth.TokenManager
import hr.algebra.mobileapp.fragments.auth.LoginActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Clear the stored JWT and send the user back to the login screen.
     *
     * Call this from any fragment or menu action that implements "Log out".
     * Example:
     * ```kotlin
     * (requireActivity() as MainActivity).logout()
     * ```
     */
    fun logout() {
        TokenManager.clearToken()
        val intent = Intent(this, LoginActivity::class.java)
        // Clear the back stack so the user can't navigate back into the app.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
