package com.example.foodfusion.Models

data class FoodItem(
    val foodId: Int,
    val name: String,
    val price: Long,
    val description: String,
    val category: String,
    val foodImage: ByteArray
)