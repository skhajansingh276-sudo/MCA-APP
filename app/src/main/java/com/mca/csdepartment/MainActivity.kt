package com.mca.csdepartment

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mca.csdepartment.databinding.ActivityMainBinding
import com.mca.csdepartment.fragments.AboutFragment
import com.mca.csdepartment.fragments.ContactFragment
import com.mca.csdepartment.fragments.CoursesFragment
import com.mca.csdepartment.fragments.HelpFragment
import com.mca.csdepartment.fragments.HomeFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Drawer menu
        binding.toolbar.findViewById<android.widget.ImageButton>(R.id.btnMenu).setOnClickListener {
            binding.drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
        }
        findViewById<android.view.View>(R.id.drawer_nav_home)?.setOnClickListener {
            binding.bottomNav.selectedItemId = R.id.nav_home
            binding.drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
        }
        findViewById<android.view.View>(R.id.drawer_nav_courses)?.setOnClickListener {
            binding.bottomNav.selectedItemId = R.id.nav_course
            binding.drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
        }
        findViewById<android.view.View>(R.id.drawer_nav_about)?.setOnClickListener {
            binding.bottomNav.selectedItemId = R.id.nav_about
            binding.drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
        }
        findViewById<android.view.View>(R.id.drawer_nav_contact)?.setOnClickListener {
            binding.bottomNav.selectedItemId = R.id.nav_contact
            binding.drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
        }

        // Load Home screen by default
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            binding.bottomNav.selectedItemId = R.id.nav_home
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home    -> { loadFragment(HomeFragment());    true }
                R.id.nav_course  -> { loadFragment(CoursesFragment()); true }
                R.id.nav_help    -> { loadFragment(HelpFragment());    true }
                R.id.nav_contact -> { loadFragment(ContactFragment()); true }
                R.id.nav_about   -> { loadFragment(AboutFragment());   true }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // ── Navigate to a fragment with back stack ──
    fun navigateToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.slide_in_left,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    // ── Toolbar helpers for Semester screen ──
    fun updateToolbarForSemester(title: String) {
        binding.toolbar.findViewById<View>(R.id.ivAppIcon)?.visibility = View.GONE
        binding.toolbar.findViewById<android.widget.TextView>(R.id.tvToolbarTitle)?.text = title

        // Show hamburger as back indicator
        binding.btnMenu.setImageResource(R.drawable.ic_menu)
    }

    fun restoreToolbar() {
        binding.toolbar.findViewById<View>(R.id.ivAppIcon)?.visibility = View.VISIBLE
        binding.toolbar.findViewById<android.widget.TextView>(R.id.tvToolbarTitle)?.text = getString(R.string.app_name)
    }

    // ── Bottom Nav visibility helpers ──
    fun hideMainBottomNav() {
        binding.bottomNav.visibility = View.GONE
    }

    fun showMainBottomNav() {
        binding.bottomNav.visibility = View.VISIBLE
    }

    // ── Handle back press to restore state ──
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}
