package com.example.foodfusion.Models

data class CartItem(
    val cardid: Int,
    val userid: Int,
    val name: String,
    val price: Long,
    var quantity: Int,
    val foodImage: ByteArray
)
