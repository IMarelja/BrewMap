package hr.algebra.mobileapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.appbar.MaterialToolbar
import hr.algebra.mobileapp.auth.TokenManager
import hr.algebra.mobileapp.fragments.auth.LoginActivity
import hr.algebra.mobileapp.fragments.main.LocationDetailFragment
import hr.algebra.mobileapp.fragments.main.LocationMapFragment
import hr.algebra.mobileapp.fragments.main.LocationSearchFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById(R.id.main_toolbar)
        bottomNav = findViewById(R.id.main_bottom_nav)

        // Session expiry: any service call that receives HTTP 401 emits here.
        // Redirect to login and clear the entire back-stack so the userService cannot
        // navigate back to a protected screen without re-authenticating.
        lifecycleScope.launch {
            TokenManager.sessionExpiredEvent.collect {
                logout()
            }
        }

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_logout -> {
                    logout()
                    true
                }
                else -> false
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> {
                    showRootFragment(LocationMapFragment(), TAG_MAP)
                    toolbar.title = "Map"
                    true
                }
                R.id.nav_search -> {
                    showRootFragment(LocationSearchFragment(), TAG_SEARCH)
                    toolbar.title = "Search"
                    true
                }
                else -> false
            }
        }

        supportFragmentManager.addOnBackStackChangedListener {
            updateToolbarTitleFromState()
        }

        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_map
        } else {
            updateToolbarTitleFromState()
        }
    }

    fun openLocationDetail(locationId: String) {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.main_fragment_container,
                LocationDetailFragment.newInstance(locationId),
                TAG_DETAIL
            )
            .addToBackStack(TAG_DETAIL)
            .commit()
        toolbar.title = "Location"
    }

    /**
     * Clear the stored JWT and send the userService back to the login screen.
     *
     * Call this from any fragment or menu action that implements "Log out".
     */
    fun logout() {
        TokenManager.clearToken()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showRootFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container, fragment, tag)
            .commit()
    }

    private fun updateToolbarTitleFromState() {
        toolbar.title = if (supportFragmentManager.backStackEntryCount > 0) {
            "Location"
        } else {
            when (bottomNav.selectedItemId) {
                R.id.nav_map -> "Map"
                R.id.nav_search -> "Search"
                else -> "BrewMap"
            }
        }
    }

    companion object {
        private const val TAG_MAP = "tag_map"
        private const val TAG_SEARCH = "tag_search"
        private const val TAG_DETAIL = "tag_detail"
    }
}
