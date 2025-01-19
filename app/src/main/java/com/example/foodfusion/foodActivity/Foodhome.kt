package com.example.foodfusion.foodActivity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.foodfusion.Fragment.AdminFragment
import com.example.foodfusion.Fragment.AllOrderFragment
import com.example.foodfusion.Fragment.Ordersuccessful
import com.example.foodfusion.Fragment.UserFragment
import com.example.foodfusion.Fragment.addfood
import com.example.foodfusion.Fragment.addtocart
import com.example.foodfusion.Fragment.admin_order
import com.example.foodfusion.R
import com.example.foodfusion.Fragment.allfoodItem
import com.example.foodfusion.databinding.ActivityFoodhomeBinding
import com.example.foodfusion.utils.SharedPreferencesHelper
import com.google.android.material.navigation.NavigationView

class foodhome : AppCompatActivity(), AdminFragment.OnSidebarClickListener {
    private lateinit var binding: ActivityFoodhomeBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize binding
        binding = ActivityFoodhomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = binding.drawerLayout
        navigationView = binding.navView

        // Check user role from shared preferences
        val userRole = SharedPreferencesHelper.getUserRole(this)

        // Set navigation menu based on user role
        if (userRole == "admin") {
            navigationView.menu.clear() // Clear any existing menu
            navigationView.inflateMenu(R.menu.nav_menu_admin)
            showFragment(AdminFragment())
        } else {
            navigationView.menu.clear() // Clear any existing menu
            navigationView.inflateMenu(R.menu.nav_menu_user)
            showFragment(UserFragment())
        }

        // Handle navigation item selection
           navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home_user -> showFragment(UserFragment())
                R.id.nav_AddtoCart -> showFragment(addtocart())
                R.id.nav_home_admin -> showFragment(AdminFragment())
                R.id.nav_addfood -> showFragment(addfood())
                R.id.nav_allfood -> showFragment(allfoodItem())
                R.id.nav_order -> showFragment(admin_order())
                R.id.nav_logout ->  logout()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onSidebarClick() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        when (currentFragment) {
            is addtocart ,is AllOrderFragment , is Ordersuccessful-> showFragment(UserFragment())
            is addfood, is allfoodItem ,is admin_order -> showFragment(AdminFragment())
            else -> super.onBackPressed()
        }
    }
  // This function is use logout the admin and user
    private fun logout() {
      val userRole = SharedPreferencesHelper.clearUserSession(this)
      if (userRole != null) {
          val intent = Intent(this, foodlogin::class.java)
          intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
          startActivity(intent)
          finish()
      }
    }
}
