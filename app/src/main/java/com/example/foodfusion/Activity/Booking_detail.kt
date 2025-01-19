package com.example.foodfusion.Activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.foodfusion.DBConnection
import com.example.foodfusion.R
import com.example.foodfusion.databinding.ActivityBookingDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.Calendar

class booking_detail : AppCompatActivity() {
    private lateinit var roomNoTextView: TextView
    private lateinit var bookingIdTextView: TextView
    private lateinit var roomImageView: ImageView
    private lateinit var binding: ActivityBookingDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_booking_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityBookingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backbtn.setOnClickListener {
            startActivity(Intent(this, all_booking::class.java))
            finish()
        }

        roomNoTextView = findViewById(R.id.roomno)
        bookingIdTextView = findViewById(R.id.bookingid)
        roomImageView = findViewById(R.id.roomimage)

        val roomNo = intent.getIntExtra("RoomNo", -1)
        val bookingId = intent.getIntExtra("BookingId", -1)

        roomNoTextView.text = roomNo.toString()
        bookingIdTextView.text = bookingId.toString()

        // It is use to Fetch Booking details of the User
        FetchBookingDetailsTask().execute(roomNo, bookingId)

       // This Button is use to check out
        binding.checkout.setOnClickListener {
            UpdateCheckoutStatusTask().execute(roomNo, bookingId)
            UpdateRoomStatusTask().execute(roomNo)
        }
         binding.editBooking.setOnClickListener {
             UpdateBookingDetails(bookingId)
         }

    }

    private fun UpdateBookingDetails(bookingId: Int) {
        val roomNo = roomNoTextView.text.toString().toInt()
        val noOfDays = binding.noofday.text.toString().toInt()
        val date =binding.arrivalday.text?.toString()
        CoroutineScope(Dispatchers.IO).launch {
            val roomPrice = fetchRoomPrice(roomNo)
            if (roomPrice != null) {
                if (date != null) {
                    updateBookingInDB(noOfDays, bookingId, roomPrice,date)
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@booking_detail, "Booking details updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@booking_detail, "Error fetching room price", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateBookingInDB(noOfDays: Int, bookingId: Int, roomPrice: Int, date: String) {
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        try {
            connection = DBConnection.connect()

            // Parse the date string to java.sql.Date
            val dateFormat = SimpleDateFormat("yyyy-MM-dd") // Updated format to match the date string
            val parsedDate = dateFormat.parse(date)
            val sqlDate = java.sql.Date(parsedDate.time)

            // Calculate the checkout date based on numberOfDays
            val calendar = Calendar.getInstance()
            if (parsedDate != null) {
                calendar.time = parsedDate
            }
            calendar.add(Calendar.DAY_OF_YEAR, noOfDays)
            val checkoutDate = java.sql.Date(calendar.timeInMillis)

            val query = "UPDATE Booking SET no_of_days = ?, room_price = ?, checkout_date = ? WHERE booking_id = ?"
            preparedStatement = connection?.prepareStatement(query)
            preparedStatement?.setInt(1, noOfDays)
            preparedStatement?.setInt(2, roomPrice * noOfDays) // Calculate the total price based on the new number of days
            preparedStatement?.setDate(3, checkoutDate)
            preparedStatement?.setInt(4, bookingId)
            preparedStatement?.executeUpdate()
        } catch (e: SQLException) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: java.text.ParseException) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this, "Date Parse Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } finally {
            preparedStatement?.close()
            connection?.close()
        }
    }

    private fun fetchRoomPrice(roomNo: Int): Int? {
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        var roomPrice: Int? = null
        try {
            connection = DBConnection.connect()
            val query = "SELECT room_price FROM Rooms WHERE room_no = ?"
            preparedStatement = connection?.prepareStatement(query)
            preparedStatement?.setInt(1, roomNo)
            val resultSet: ResultSet? = preparedStatement?.executeQuery()
            if (resultSet?.next() == true) {
                roomPrice = resultSet.getInt("room_price")
            }
            resultSet?.close()
            preparedStatement?.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            connection?.close()
        }
        return roomPrice
    }


    // This function is use to update the Rooms Status
    private inner class UpdateRoomStatusTask : AsyncTask<Int, Void, Boolean>() {
        override fun doInBackground(vararg params: Int?): Boolean {
            val roomNo = params[0]
            var success = false
            val connection = DBConnection.connect()
            if (connection != null && roomNo != null) {
                try {
                    val query = "UPDATE Rooms SET status='Available' WHERE room_no=?"
                    val statement: PreparedStatement = connection.prepareStatement(query)
                    statement.setInt(1, roomNo)
                    val rowsUpdated = statement.executeUpdate()
                    success = rowsUpdated > 0
                    statement.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                } finally {
                    connection.close()
                }
            }
            return success
        }

        override fun onPostExecute(success: Boolean) {
            if (success) {
                Toast.makeText(this@booking_detail, "Room status updated ", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@booking_detail, "Error updating room status", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private inner class FetchBookingDetailsTask : AsyncTask<Int, Void, BookingDetails?>() {
        override fun doInBackground(vararg params: Int?): BookingDetails? {
            val roomNo = params[0]
            val bookingId = params[1]
            var bookingDetails: BookingDetails? = null
            val connection: Connection? = DBConnection.connect()
            if (connection != null && roomNo != null && bookingId != null) {
                try {
                    val query = "SELECT * FROM Booking b JOIN Rooms r ON b.room_no = r.room_no WHERE b.room_no = ? AND b.booking_id = ?"
                    val statement: PreparedStatement = connection.prepareStatement(query)
                    statement.setInt(1, roomNo)
                    statement.setInt(2, bookingId)
                    val resultSet: ResultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val status = resultSet.getString("checkout_status")
                        val bookingStatus = resultSet.getString("booking_status")
                        val customerName = resultSet.getString("name")
                        val customerNo = resultSet.getString("phone")
                        val customerEmail = resultSet.getString("email")
                        val noOfDays = resultSet.getInt("no_of_days")
                        val arrivalDate = resultSet.getString("date_arrival")
                        val amount = resultSet.getInt("room_price")
                        val roomImageBlob = resultSet.getBytes("room_image")
                        val roomCategory=resultSet.getString("room_type")

                        val roomImage = roomImageBlob?.let {
                            BitmapFactory.decodeByteArray(it, 0, it.size)
                        }

                        bookingDetails = BookingDetails(roomNo, bookingId, status, bookingStatus, customerName, customerNo, customerEmail, noOfDays, arrivalDate, amount,roomCategory,roomImage)
                    }
                    resultSet.close()
                    statement.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                } finally {
                    connection.close()
                }
            }
            return bookingDetails
        }

        override fun onPostExecute(bookingDetails: BookingDetails?) {
            if (bookingDetails != null) {
                binding.status.setText(bookingDetails.status)
                binding.bookingstatus.setText(bookingDetails.bookingStatus)
                binding.customername.setText(bookingDetails.customerName)
                binding.customerno.setText(bookingDetails.customerNo)
                binding.customeremail.setText(bookingDetails.customerEmail)
                binding.noofday.setText(bookingDetails.noOfDays.toString())
                binding.arrivalday.setText(bookingDetails.arrivalDate)
                binding.amount.setText(bookingDetails.amount.toString())
                binding.category.text = bookingDetails.category
                bookingDetails.roomImage?.let {
                    roomImageView.setImageBitmap(it)
                }
            } else {
                Toast.makeText(this@booking_detail, "Error fetching booking details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private inner class UpdateCheckoutStatusTask : AsyncTask<Int, Void, Boolean>() {
        override fun doInBackground(vararg params: Int?): Boolean {
            val roomNo = params[0]
            val bookingId = params[1]
            var success = false
            val connection: Connection? = DBConnection.connect()
            if (connection != null && roomNo != null && bookingId != null) {
                try {
                    val query = "UPDATE Booking SET checkout_status = 'Checked Out' WHERE booking_status = 'Paid' AND room_no = ? AND booking_id = ?"
                    val statement: PreparedStatement = connection.prepareStatement(query)
                    statement.setInt(1, roomNo)
                    statement.setInt(2, bookingId)
                    val rowsUpdated = statement.executeUpdate()
                    success = rowsUpdated > 0
                    statement.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                } finally {
                    connection.close()
                }
            }
            return success
        }

        override fun onPostExecute(success: Boolean) {
            if (success) {
                Toast.makeText(this@booking_detail, "Check-out status updated", Toast.LENGTH_SHORT).show()
                finish()  // Close the activity after updating the status
            } else {
                Toast.makeText(this@booking_detail, "Error updating check-out status", Toast.LENGTH_SHORT).show()
            }
        }

    }

}

data class BookingDetails(
    val roomNo: Int,
    val bookingId: Int,
    val status: String,
    val bookingStatus: String,
    val customerName: String,
    val customerNo: String,
    val customerEmail: String,
    val noOfDays: Int,
    val arrivalDate: String,
    val amount: Int,
    val category: String,
    val roomImage: Bitmap?
)
