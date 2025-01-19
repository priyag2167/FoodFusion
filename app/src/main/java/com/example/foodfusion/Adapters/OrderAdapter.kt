package com.example.foodfusion.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodfusion.Models.Order
import com.example.foodfusion.R

class OrderAdapter(private val orderList: List<Order>) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderName: TextView = itemView.findViewById(R.id.orderName)
        val orderPrice: TextView = itemView.findViewById(R.id.orderPrice)
        val orderStatus: TextView = itemView.findViewById(R.id.orderStatus)
        val orderDate: TextView = itemView.findViewById(R.id.orderDate)
        val orderAddress: TextView = itemView.findViewById(R.id.orderAddress)
        val orderQuantity: TextView = itemView.findViewById(R.id.orderquantity)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position]
        holder.orderName.text = order.name
        holder.orderPrice.text = "Price: ${order.price}"
        holder.orderStatus.text = "Status: ${order.status}"
        holder.orderDate.text = "Date: ${order.date}"
        holder.orderAddress.text = "Address: ${order.address}"
        holder.orderQuantity.text = "Quantity: ${order.quantity}"
    }

    override fun getItemCount(): Int = orderList.size
}
