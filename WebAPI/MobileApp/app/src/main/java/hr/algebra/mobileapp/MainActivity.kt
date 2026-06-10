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
import hr.algebra.mobileapp.fragments.profile.ProfileMyFragment
import hr.algebra.mobileapp.fragments.profile.ProfileSettingsFragment
import hr.algebra.mobileapp.fragments.profile.ProfileStrangerFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val statusBarScrim = findViewById<android.view.View>(R.id.status_bar_scrim)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            statusBarScrim.layoutParams = statusBarScrim.layoutParams.apply { height = systemBars.top }
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
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
                R.id.action_profile -> {
                    showRootFragment(ProfileMyFragment(), TAG_PROFILE)
                    true
                }
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
                    toolbar.title = getString(R.string.map)
                    true
                }
                R.id.nav_search -> {
                    showRootFragment(LocationSearchFragment(), TAG_SEARCH)
                    toolbar.title = getString(R.string.search)
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
        toolbar.title = getString(R.string.title_location)
    }

    fun openStrangerProfile(userId: String) {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.main_fragment_container,
                ProfileStrangerFragment.newInstance(userId),
                TAG_STRANGER
            )
            .addToBackStack(TAG_STRANGER)
            .commit()
        toolbar.title = "Stranger Profile"
    }

    fun openProfileSettings() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.main_fragment_container,
                ProfileSettingsFragment(),
                TAG_SETTINGS
            )
            .addToBackStack(TAG_SETTINGS)
            .commit()
        toolbar.title = "Profile Settings"
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
            getString(R.string.title_location)
        } else {
            when (bottomNav.selectedItemId) {
                R.id.nav_map -> getString(R.string.map)
                R.id.nav_search -> getString(R.string.search)
                else -> getString(R.string.app_display_name)
            }
        }
    }

    companion object {
        private const val TAG_MAP = "tag_map"
        private const val TAG_SEARCH = "tag_search"
        private const val TAG_DETAIL = "tag_detail"
        private const val TAG_PROFILE = "tag_profile"
        private const val TAG_STRANGER = "tag_stranger"
        private const val TAG_SETTINGS = "tag_settings"
    }
}
