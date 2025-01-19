package com.example.foodfusion.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodfusion.Adapters.OrderAdapter
import com.example.foodfusion.DBConnection
import com.example.foodfusion.Models.Order
import com.example.foodfusion.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.ResultSet

class AllOrderFragment : Fragment() {

    private lateinit var orderRecyclerView: RecyclerView
    private lateinit var orderAdapter: OrderAdapter
    private val orderList: MutableList<Order> = mutableListOf()
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_allorder, container, false)

        // Initialize RecyclerView and ProgressBar
        orderRecyclerView = view.findViewById(R.id.orderRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        orderRecyclerView.layoutManager = LinearLayoutManager(context)
        orderAdapter = OrderAdapter(orderList)
        orderRecyclerView.adapter = orderAdapter

        // Show progress bar while loading data
        progressBar.visibility = View.VISIBLE

        // Fetch data from database
        fetchOrdersFromDatabase()

        return view
    }

    override fun onResume() {
        super.onResume()
        // Handle the back button press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Navigate to the user fragment
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container, UserFragment())
                    commit()
                }
            }
        })
    }

    private fun fetchOrdersFromDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            val connection: Connection? = DBConnection.connect()
            if (connection != null) {
                try {
                    val statement = connection.createStatement()
                    val resultSet: ResultSet = statement.executeQuery("SELECT * FROM Orders")
                    val orders = mutableListOf<Order>()
                    while (resultSet.next()) {
                        val order = Order(
                            orderID = resultSet.getInt("OrderID"),
                            userID = resultSet.getInt("UserID"),
                            name = resultSet.getString("Name"),
                            price = resultSet.getLong("Price"),
                            status = resultSet.getString("Status"),
                            address = resultSet.getString("Address"),
                            date = resultSet.getString("Date"),
                            phone = resultSet.getString("Phone"),
                            quantity = resultSet.getInt("Quantity")
                        )
                        orders.add(order)
                    }
                    resultSet.close()
                    statement.close()
                    connection.close()

                    // Update the RecyclerView on the main thread
                    withContext(Dispatchers.Main) {
                        orderList.clear()
                        orderList.addAll(orders)
                        orderAdapter.notifyDataSetChanged()
                        // Hide progress bar when data is loaded
                        progressBar.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(context, "Failed to fetch orders", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Failed to connect to database", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
