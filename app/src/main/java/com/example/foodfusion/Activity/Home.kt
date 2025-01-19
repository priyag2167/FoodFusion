package com.example.foodfusion.Activity

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodfusion.Adapters.BookingAdapter
import com.example.foodfusion.DBConnection
import com.example.foodfusion.Models.Booking
import com.example.foodfusion.R
import com.example.foodfusion.databinding.ActivityHomeBinding
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

class Home : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var bookingList: MutableList<Booking>
    private lateinit var bookingAdapter: BookingAdapter
    private lateinit var availableRoomsTextView: TextView
    private lateinit var occupiedRoomsTextView: TextView
    private lateinit var loader: ProgressBar

    lateinit var adView: AdView
    lateinit var adRequest: AdRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize binding first before setContentView
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Set on apply window insets listener to adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize AdMob
        MobileAds.initialize(this) {
            // Callback if needed
        }
        adView = binding.adView
        adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // Initialize TextViews and loader
        availableRoomsTextView = binding.roomavailable
        occupiedRoomsTextView = binding.occupiedroom
        loader = binding.loader
        loader.indeterminateDrawable.setColorFilter(
            ContextCompat.getColor(this, R.color.blue),
            PorterDuff.Mode.SRC_IN
        )

        // Set up click listeners for navigation buttons
        binding.dashboardpage.setOnClickListener {
            binding.dashboardpage.setColorFilter(Color.parseColor("#0071FE"), PorterDuff.Mode.SRC_IN)
            startActivity(Intent(this, Home::class.java))
            finish()
        }

        binding.roompage.setOnClickListener {
            binding.roompage.setColorFilter(Color.parseColor("#0071FE"), PorterDuff.Mode.SRC_IN)
            startActivity(Intent(this, all_room::class.java))
            finish()
        }

        binding.addbtn.setOnClickListener {
            binding.addbtn.setColorFilter(Color.parseColor("#0071FE"), PorterDuff.Mode.SRC_IN)
            startActivity(Intent(this, addbtn::class.java))
            finish()
        }

        binding.bookingpage.setOnClickListener {
            binding.bookingpage.setColorFilter(Color.parseColor("#0071FE"), PorterDuff.Mode.SRC_IN)
            startActivity(Intent(this, all_booking::class.java))
            finish()
        }

        binding.Allsee.setOnClickListener {
            startActivity(Intent(this, all_booking::class.java))
            finish()
        }

        binding.accountpage.setOnClickListener {
            startActivity(Intent(this, hotelprofile::class.java))
            finish()
        }

        // Initialize RecyclerView and adapter
        bookingList = mutableListOf()
        bookingAdapter = BookingAdapter(this, bookingList)

        binding.bookingdetailrecyclerview.apply {
            layoutManager = LinearLayoutManager(this@Home)
            adapter = bookingAdapter
        }

        // Fetch data in background tasks
        FetchBookingsTask().execute()
        FetchAvailableRoomsTask().execute()
        FetchOccupiedRoomsTask().execute()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up AdView resources
        adView.destroy()
    }

    override fun onPause() {
        super.onPause()
        // Pause AdView
        adView.pause()
    }

    override fun onResume() {
        super.onResume()
        // Resume AdView
        adView.resume()
    }

    private inner class FetchBookingsTask : AsyncTask<Void, Void, List<Booking>>() {
        override fun onPreExecute() {
            super.onPreExecute()
            // Show loader before fetching data
            loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: Void?): List<Booking> {
            val bookings = mutableListOf<Booking>()
            val connection: Connection? = DBConnection.connect()
            if (connection != null) {
                val statement: Statement = connection.createStatement()
                val resultSet: ResultSet = statement.executeQuery("""
                    SELECT b.booking_id, b.room_no, b.date_arrival, r.room_image, b.checkout_status
                    FROM Booking b
                    JOIN Rooms r ON b.room_no = r.room_no
                    WHERE b.checkout_status = 'Ongoing'
                """)

                while (resultSet.next()) {
                    val bookingId = resultSet.getInt("booking_id")
                    val roomNo = resultSet.getInt("room_no")
                    val checkInDate = resultSet.getString("date_arrival")
                    val roomImage = resultSet.getBytes("room_image")
                    val checkoutStatus = resultSet.getString("checkout_status") // Get checkout_status
                    bookings.add(Booking(bookingId, roomNo, checkInDate, roomImage))
                }
                resultSet.close()
                statement.close()
                connection.close()
            }
            return bookings
        }

        override fun onPostExecute(result: List<Booking>) {
            super.onPostExecute(result)
            bookingList.clear()
            bookingList.addAll(result)
            bookingAdapter.notifyDataSetChanged()
            // Hide loader after data is loaded
            loader.visibility = View.GONE
        }
    }

    private inner class FetchAvailableRoomsTask : AsyncTask<Void, Void, Int>() {
        override fun doInBackground(vararg params: Void?): Int {
            var availableRoomsCount = 0
            val connection: Connection? = DBConnection.connect()
            if (connection != null) {
                val statement: Statement = connection.createStatement()
                val resultSet: ResultSet = statement.executeQuery("""
                    SELECT COUNT(*) AS count
                    FROM Rooms
                    WHERE status = 'available'
                """)

                if (resultSet.next()) {
                    availableRoomsCount = resultSet.getInt("count")
                }

                resultSet.close()
                statement.close()
                connection.close()
            }
            return availableRoomsCount
        }

        override fun onPostExecute(result: Int) {
            super.onPostExecute(result)
            // Update the TextView with the available rooms count
            availableRoomsTextView.text = result.toString()
        }
    }

    private inner class FetchOccupiedRoomsTask : AsyncTask<Void, Void, Int>() {
        override fun doInBackground(vararg params: Void?): Int {
            var occupiedRoomsCount = 0
            val connection: Connection? = DBConnection.connect()
            if (connection != null) {
                val statement: Statement = connection.createStatement()
                val resultSet: ResultSet = statement.executeQuery("""
                    SELECT COUNT(*) AS count
                    FROM Rooms
                    WHERE status = 'Unavailable'
                """)

                if (resultSet.next()) {
                    occupiedRoomsCount = resultSet.getInt("count")
                }

                resultSet.close()
                statement.close()
                connection.close()
            }
            return occupiedRoomsCount
        }

        override fun onPostExecute(result: Int) {
            super.onPostExecute(result)
            // Update the TextView with the occupied rooms count
            occupiedRoomsTextView.text = result.toString()
        }
    }
}
