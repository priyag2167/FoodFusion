package com.example.foodfusion.Activity

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
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
import com.example.foodfusion.databinding.ActivityAllBookingBinding
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

class all_booking : AppCompatActivity() {
    private lateinit var binding: ActivityAllBookingBinding
    private lateinit var bookingList: MutableList<Booking>
    private lateinit var bookingAdapter: BookingAdapter
    private lateinit var loader: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_all_booking)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityAllBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loader = binding.loader // Initialize loader
        loader.indeterminateDrawable.setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_IN)

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
        binding.bookingpage.setOnClickListener {
            binding.bookingpage.setColorFilter(Color.parseColor("#0071FE"), PorterDuff.Mode.SRC_IN)
            startActivity(Intent(this, all_booking::class.java))
            finish()
        }
        binding.adddbooking.setOnClickListener {
            //binding.adddbooking.setColorFilter(Color.parseColor("#0071FE"), PorterDuff.Mode.SRC_IN)
            startActivity(Intent(this, addbooking::class.java))
            finish()
        }
        binding.addbtn.setOnClickListener {
            startActivity(Intent(this,addbtn::class.java))
            finish()
        }

        binding.accountpage.setOnClickListener {
            binding.accountpage.setColorFilter(Color.parseColor("#0071FE"), PorterDuff.Mode.SRC_IN)
            startActivity(Intent(this, hotelprofile::class.java))
            finish()
        }

        binding.ongoing.setOnClickListener {
            startActivity(Intent(this, all_booking::class.java))
            finish()
        }
        binding.complete.setOnClickListener {
            startActivity(Intent(this, booking_detail_complete::class.java))
            finish()
        }
        bookingList = mutableListOf()
        bookingAdapter = BookingAdapter(this, bookingList)

        binding.bookingdetailrecyclerview.apply {
            layoutManager = LinearLayoutManager(this@all_booking)
            adapter = bookingAdapter
        }

        FetchBookingsTask().execute()
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
                SELECT b.booking_id, b.room_no, b.date_arrival, r.room_image
                FROM Booking b
                JOIN Rooms r ON b.room_no = r.room_no
                WHERE b.checkout_status = 'ongoing'
            """)

                while (resultSet.next()) {
                    val bookingId = resultSet.getInt("booking_id")
                    val roomNo = resultSet.getInt("room_no")
                    val checkInDate = resultSet.getString("date_arrival")
                    val roomImage = resultSet.getBytes("room_image")
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

//            if (result.isEmpty()) {
//                // Handle empty list, show a message or take appropriate action
//                binding.noBookingsMessage.visibility = View.VISIBLE
//            } else {
//                binding.noBookingsMessage.visibility = View.GONE
//            }
        }
    }
}
