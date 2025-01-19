package com.example.foodfusion.Fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.foodfusion.Adapters.ImageSliderAdapter
import com.example.foodfusion.Adapters.UserFoodAdapter
import com.example.foodfusion.DBConnection
import com.example.foodfusion.Models.UserFoodItem
import com.example.foodfusion.R
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import com.example.foodfusion.foodActivity.foodlogin
import com.example.foodfusion.utils.SharedPreferencesHelper
import java.sql.SQLException
import java.util.Timer
import java.util.TimerTask

class UserFragment : Fragment() {
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ImageSliderAdapter
    private val images = listOf(R.drawable.fooddemo, R.drawable.fooddemo2, R.drawable.demofood)
    private lateinit var fastfoodRecyclerView: RecyclerView
    private lateinit var foodAdapter: UserFoodAdapter
    private val foodList = mutableListOf<UserFoodItem>()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sidebarButton: ImageView
    private lateinit var navigationView: NavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        drawerLayout = view.findViewById(R.id.drawer_layout)
        sidebarButton = view.findViewById(R.id.sidebar)
        navigationView = view.findViewById(R.id.nav_view)

        sidebarButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // Update the header view with admin information
        navigationView = view.findViewById(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)
        val usernameTextView = headerView.findViewById<TextView>(R.id.username)
        val useremailTextView = headerView.findViewById<TextView>(R.id.useremail)

        // Get admin information from arguments
        val username = arguments?.getString("name")
        val useremail = arguments?.getString("email")

        usernameTextView.text = username
        useremailTextView.text = useremail

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_AddtoCart -> {
                    val addToCartFragment = addtocart()
                    fragmentManager?.beginTransaction()
                        ?.replace(R.id.fragment_container, addToCartFragment)
                        ?.addToBackStack(null)
                        ?.commit()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_allorder -> {
                    val allorderFragment = AllOrderFragment()
                    fragmentManager?.beginTransaction()
                        ?.replace(R.id.fragment_container, allorderFragment)
                        ?.addToBackStack(null)
                        ?.commit()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_logout -> {
                    logoutUser()
                    true
                }
                else -> false
            }
        }

        viewPager = view.findViewById(R.id.viewPager)
        adapter = ImageSliderAdapter(images)
        viewPager.adapter = adapter
        autoSlideImages()

        fastfoodRecyclerView = view.findViewById(R.id.fastfoodRecylcer)
        fastfoodRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        foodAdapter = UserFoodAdapter(foodList) { foodItem ->
            val bundle = Bundle().apply {
                putInt("foodiD", foodItem.foodiD)
            }
            val fragment = fooddetail().apply {
                arguments = bundle
            }
            fragmentManager?.beginTransaction()
                ?.replace(R.id.fragment_container, fragment)
                ?.addToBackStack(null)
                ?.commit()
        }
        fastfoodRecyclerView.adapter = foodAdapter

        fetchFoodData()

        return view
    }

    private fun autoSlideImages() {
        val handler = Handler(Looper.getMainLooper())
        val update = Runnable {
            if (viewPager.currentItem == adapter.itemCount - 1) {
                viewPager.currentItem = 0
            } else {
                viewPager.currentItem += 1
            }
        }

        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                handler.post(update)
            }
        }, 5000, 5000)
    }

    private fun fetchFoodData() {
        Thread {
            val connection = DBConnection.connect()
            if (connection != null) {
                try {
                    val statement = connection.createStatement()
                    val resultSet = statement.executeQuery("SELECT foodiD, Name, food_image, price FROM Addfood")
                    while (resultSet.next()) {
                        val foodiD = resultSet.getInt("foodiD")
                        val name = resultSet.getString("Name")
                        val image = resultSet.getBytes("food_image")
                        val price = resultSet.getString("price")
                        foodList.add(UserFoodItem(foodiD, name, image, price))
                    }
                    activity?.runOnUiThread {
                        foodAdapter.notifyDataSetChanged()
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    connection.close()
                }
            }
        }.start()
    }

    private fun logoutUser() {
        val userRole = context?.let { SharedPreferencesHelper.clearUserSession(it) }
        if (userRole != null) {
            val intent = Intent(context, foodlogin::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }


}
