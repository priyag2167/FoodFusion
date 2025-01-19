package com.example.foodfusion.Activity

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodfusion.Adapters.RoomAdapter
import com.example.foodfusion.DBConnection
import com.example.foodfusion.Models.Room
import com.example.foodfusion.R
import com.example.foodfusion.databinding.ActivityAllRoomBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class all_room : AppCompatActivity(), RoomAdapter.OnItemClickListener {
    private lateinit var binding: ActivityAllRoomBinding
    private lateinit var roomAdapter: RoomAdapter
    private var roomList: MutableList<Room> = mutableListOf()
    private lateinit var loader: ProgressBar // Add this line


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_all_room)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityAllRoomBinding.inflate(layoutInflater)
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
        binding.addrooms.setOnClickListener {
            startActivity(Intent(this,addroom::class.java))
            finish()
        }
        binding.addbtn.setOnClickListener {
            startActivity(Intent(this, addbtn::class.java))
            finish()
        }

        binding.bookingpage.setOnClickListener {
            startActivity(Intent(this,all_booking::class.java))
            finish()
        }

        binding.accountpage.setOnClickListener {
            binding.accountpage.setColorFilter(Color.parseColor("#0071FE"), PorterDuff.Mode.SRC_IN)
            startActivity(Intent(this, hotelprofile::class.java))
            finish()
        }

        // Initialize RecyclerView
         roomAdapter = RoomAdapter(roomList, this)
          binding.roomdetailrecyclerview.apply {
            layoutManager = LinearLayoutManager(this@all_room)
            adapter = roomAdapter
        }


        // Fetch data from the database
        fetchRoomsFromDatabase()
        // Show loader before fetching data
        loader.visibility = View.VISIBLE
    }

    private fun fetchRoomsFromDatabase() {
        CoroutineScope(Dispatchers.Main).launch {
            val rooms = withContext(Dispatchers.IO) {
                getRoomsFromDB()
            }
            if (rooms != null) {
                roomList.clear()
                roomList.addAll(rooms)
                roomAdapter.notifyDataSetChanged()
                // Hide loader after data is loaded
                loader.visibility = View.GONE
            } else {
                Toast.makeText(this@all_room, "Failed to fetch data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getRoomsFromDB(): List<Room>? {
        val roomList = mutableListOf<Room>()
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        var resultSet: ResultSet? = null
        return try {
            connection = DBConnection.connect()
            val query = "SELECT room_no, category, status, room_image, room_price, floor_no, features FROM Rooms"
            preparedStatement = connection?.prepareStatement(query)
            resultSet = preparedStatement?.executeQuery()

            while (resultSet?.next() == true) {
                val roomNo = resultSet.getInt("room_no")
                val category = resultSet.getString("category")
                val status =  resultSet.getString("status")
                val roomImage = resultSet.getBytes("room_image")
                val roomPrice = resultSet.getInt("room_price")
                val floorNo = resultSet.getInt("floor_no")
                val features = resultSet.getString("features")

                roomList.add(Room(roomNo, category, status, roomImage, roomPrice, features,floorNo))
            }
            roomList
        } catch (e: SQLException) {
            e.printStackTrace()
            null
        } finally {
            resultSet?.close()
            preparedStatement?.close()
            connection?.close()
        }
    }

    override fun onItemClick(room: Room) {
        val intent = Intent(this, Roomdetail::class.java).apply {
            putExtra("roomNo", room.roomNo)
            putExtra("category", room.category)
            putExtra("status", room.status)
            putExtra("roomPrice", room.roomprice)
            putExtra("floorNo", room.floorNo)
            putExtra("features", room.features)
        }
        startActivity(intent)
    }
}
