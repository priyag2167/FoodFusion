package com.example.foodfusion.Models

data class Order(
    val orderID: Int,
    val userID: Int,
    val name: String,
    val price: Long,
    var status: String,
    val address: String,
    val date: String,
    val phone: String,
    val quantity: Int,
)
