package com.example.foodfusion.Adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodfusion.Models.UserFoodItem
import com.example.foodfusion.R

class UserFoodAdapter(
    private val foodList: List<UserFoodItem>,
    private val onItemClicked: (UserFoodItem) -> Unit
) : RecyclerView.Adapter<UserFoodAdapter.FoodViewHolder>() {

    class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodName: TextView = itemView.findViewById(R.id.foodname)
        val foodImage: ImageView = itemView.findViewById(R.id.foodimage)
        val foodPrice: TextView = itemView.findViewById(R.id.foodprice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fast_food_item, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val foodItem = foodList[position]
        holder.foodName.text = foodItem.name
        holder.foodPrice.text = foodItem.price

        val bitmap = foodItem.image?.let { BitmapFactory.decodeByteArray(foodItem.image, 0, it.size) }
        holder.foodImage.setImageBitmap(bitmap)

        holder.itemView.setOnClickListener {
            onItemClicked(foodItem)
        }
    }

    override fun getItemCount() = foodList.size
}
