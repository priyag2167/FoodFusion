package com.example.foodfusion.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodfusion.Models.CartItem
import com.example.foodfusion.R

class CartAdapter(
    private val cartItems: MutableList<CartItem>,
    private val onDeleteClickListener: OnDeleteClickListener
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    interface OnDeleteClickListener {
        fun onDeleteClick(position: Int)
        fun onQuantityChange(position: Int, newQuantity: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val currentItem = cartItems[position]

        // Bind data to views
        holder.foodName.text = currentItem.name
        holder.foodPrice.text = currentItem.price.toString()
        holder.foodQuantity.text = currentItem.quantity.toString()

        // Increase quantity button click listener
        holder.btnIncrease.setOnClickListener {
            currentItem.quantity++
            notifyDataSetChanged() // Refresh the UI
            onDeleteClickListener.onQuantityChange(position, currentItem.quantity)
        }

        // Decrease quantity button click listener
        holder.btnDecrease.setOnClickListener {
            if (currentItem.quantity > 1) {
                currentItem.quantity--
                notifyDataSetChanged() // Refresh the UI
                onDeleteClickListener.onQuantityChange(position, currentItem.quantity)
            }
        }

        // Handle delete button click
        holder.btnDelete.setOnClickListener {
            onDeleteClickListener.onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodName: TextView = itemView.findViewById(R.id.foodname)
        val foodPrice: TextView = itemView.findViewById(R.id.foodprice)
        val foodQuantity: TextView = itemView.findViewById(R.id.foodquantity)
        val btnIncrease: Button = itemView.findViewById(R.id.btn_increase)
        val btnDecrease: Button = itemView.findViewById(R.id.btn_decrease)
        val btnDelete: ImageView = itemView.findViewById(R.id.delete)
    }
}



