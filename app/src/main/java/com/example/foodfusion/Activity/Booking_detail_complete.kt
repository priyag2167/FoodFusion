package com.example.foodfusion.Activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.foodfusion.DBConnection
import com.example.foodfusion.R
import com.example.foodfusion.databinding.ActivityBookingDetailCompleteBinding
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class booking_detail_complete : AppCompatActivity() {
    private lateinit var binding: ActivityBookingDetailCompleteBinding
    private lateinit var adapter: BookingDetailsAdapter
    private lateinit var loader: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_booking_detail_complete)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityBookingDetailCompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize loader
        loader = binding.loader
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
            binding.adddbooking.setColorFilter(Color.parseColor("#0071FE"), PorterDuff.Mode.SRC_IN)
            startActivity(Intent(this, addbooking::class.java))
            finish()
        }
        binding.addbtn.setOnClickListener {
            startActivity(Intent(this,addbtn::class.java))
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

        // Set up RecyclerView
        binding.bookingdetailrecyclerview.layoutManager = LinearLayoutManager(this)
        adapter = BookingDetailsAdapter()
        binding.bookingdetailrecyclerview.adapter = adapter

        // Fetch complete bookings
        FetchCompleteBookingsTask().execute()
    }

    private inner class FetchCompleteBookingsTask : AsyncTask<Void, Void, List<BookingDetails>>() {
        override fun onPreExecute() {
            super.onPreExecute()
            binding.loader.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: Void?): List<BookingDetails> {
            val bookingDetailsList = mutableListOf<BookingDetails>()
            val connection: Connection? = DBConnection.connect()
            if (connection != null) {
                try {
                    val query = """
                    SELECT b.room_no, b.booking_id, b.checkout_date, r.room_image 
                    FROM Booking b 
                    JOIN Rooms r ON b.room_no = r.room_no 
                    WHERE b.checkout_status = 'Checked Out'
                """
                    val statement: PreparedStatement = connection.prepareStatement(query)
                    val resultSet: ResultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        val roomNo = resultSet.getInt("room_no")
                        val bookingId = resultSet.getInt("booking_id")
                        val checkoutDate = resultSet.getString("checkout_date")
                        val roomImageBlob = resultSet.getBytes("room_image")
                        val roomImage = roomImageBlob?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                        bookingDetailsList.add(BookingDetails(roomNo, bookingId, checkoutDate, roomImage))
                    }
                    resultSet.close()
                    statement.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                } finally {
                    connection.close()
                }
            }
            return bookingDetailsList
        }

        override fun onPostExecute(result: List<BookingDetails>) {
            binding.loader.visibility = View.GONE
            if (result.isNotEmpty()) {
                adapter.setBookingDetails(result)
            } else {
                Toast.makeText(this@booking_detail_complete, "No complete bookings found", Toast.LENGTH_SHORT).show()
            }
        }
    }


    data class BookingDetails(
        val roomNo: Int,
        val bookingId: Int,
        val checkoutDate: String,
        val roomImage: Bitmap? // Add roomImage to the data class
    )


    // RecyclerView Adapter
    class BookingDetailsAdapter : RecyclerView.Adapter<BookingDetailsAdapter.BookingViewHolder>() {
        private var bookingDetailsList: List<BookingDetails> = listOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.booking_item, parent, false)
            return BookingViewHolder(view)
        }

        override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
            val bookingDetails = bookingDetailsList[position]
            holder.bind(bookingDetails)
        }

        override fun getItemCount(): Int {
            return bookingDetailsList.size
        }

        fun setBookingDetails(bookingDetailsList: List<BookingDetails>) {
            this.bookingDetailsList = bookingDetailsList
            notifyDataSetChanged()
        }

        class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val roomNoTextView: TextView = itemView.findViewById(R.id.roomno)
            private val bookingIdTextView: TextView = itemView.findViewById(R.id.bookingid)
            private val checkoutDateTextView: TextView = itemView.findViewById(R.id.checkindate)
            private val roomImageView: ImageView = itemView.findViewById(R.id.roomimage) // Update the ImageView ID if necessary

            fun bind(bookingDetails: BookingDetails) {
                roomNoTextView.text = bookingDetails.roomNo.toString()
                bookingIdTextView.text = bookingDetails.bookingId.toString()
                checkoutDateTextView.text = bookingDetails.checkoutDate
                bookingDetails.roomImage?.let {
                    roomImageView.setImageBitmap(it)
                } ?: run {
                    roomImageView.setImageResource(R.drawable.demorooom) // Set a placeholder image if no image is available
                }
            }
        }
    }

}
