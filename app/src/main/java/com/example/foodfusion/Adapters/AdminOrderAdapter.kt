package com.example.foodfusion.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.foodfusion.DBConnection
import com.example.foodfusion.Models.Order
import com.example.foodfusion.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.ResultSet

class AdminOrderAdapter(private val orderList: List<Order>) :
    RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderName: TextView = itemView.findViewById(R.id.orderName)
        val orderPrice: TextView = itemView.findViewById(R.id.orderPrice)
        val orderStatusSpinner: Spinner = itemView.findViewById(R.id.orderStatusSpinner)
        val orderDate: TextView = itemView.findViewById(R.id.orderDate)
        val orderAddress: TextView = itemView.findViewById(R.id.orderAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_order_item, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position]
        holder.orderName.text = order.name
        holder.orderPrice.text = "Price: ${order.price}"
        holder.orderDate.text = "Date: ${order.date}"
        holder.orderAddress.text = "Address: ${order.address}"

        // Set up the spinner
        val statusOptions = arrayOf("Pending", "Processing", "Completed", "Cancelled")
        val adapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_item, statusOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.orderStatusSpinner.adapter = adapter

        // Fetch and set the spinner value from the database
        fetchOrderStatus(order.orderID) { status ->
            val currentStatusIndex = statusOptions.indexOf(status)
            if (currentStatusIndex >= 0) {
                holder.orderStatusSpinner.setSelection(currentStatusIndex)
            }
        }

        // Handle spinner item selection
        holder.orderStatusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val newStatus = statusOptions[position]
                if (newStatus != order.status) {
                    order.status = newStatus
                    updateOrderStatus(order.orderID, newStatus) // Call the function to update the status in the database
                    Toast.makeText(holder.itemView.context, "Status changed to $newStatus", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    override fun getItemCount(): Int = orderList.size

    private fun updateOrderStatus(orderId: Int, newStatus: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val connection: Connection? = DBConnection.connect()
            if (connection != null) {
                try {
                    val statement = connection.prepareStatement("UPDATE Orders SET Status = ? WHERE OrderID = ?")
                    statement.setString(1, newStatus)
                    statement.setInt(2, orderId)
                    statement.executeUpdate()
                    statement.close()
                    connection.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun fetchOrderStatus(orderId: Int, callback: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            var status = "Pending" // Default value
            val connection: Connection? = DBConnection.connect()
            if (connection != null) {
                try {
                    val statement = connection.prepareStatement("SELECT Status FROM Orders WHERE OrderID = ?")
                    statement.setInt(1, orderId)
                    val resultSet: ResultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        status = resultSet.getString("Status")
                    }
                    resultSet.close()
                    statement.close()
                    connection.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            withContext(Dispatchers.Main) {
                callback(status)
            }
        }
    }
}
