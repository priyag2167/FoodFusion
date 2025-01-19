package com.example.foodfusion.Adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodfusion.Models.FoodItem
import com.example.foodfusion.R


class FoodItemAdapter(private val foodItemList: List<FoodItem>) : RecyclerView.Adapter<FoodItemAdapter.FoodItemViewHolder>() {

    class FoodItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodImageView: ImageView = view.findViewById(R.id.foodimage)
        val foodNameTextView: TextView = view.findViewById(R.id.foodname)
        val foodPriceTextView: TextView = view.findViewById(R.id.foodprice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.food_item, parent, false)
        return FoodItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodItemViewHolder, position: Int) {
        val foodItem = foodItemList[position]
        holder.foodNameTextView.text = foodItem.name
        holder.foodPriceTextView.text = foodItem.price.toString()

        val foodImage = BitmapFactory.decodeByteArray(foodItem.foodImage, 0, foodItem.foodImage.size)
        holder.foodImageView.setImageBitmap(foodImage)
    }

    override fun getItemCount(): Int = foodItemList.size
}