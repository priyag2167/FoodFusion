package com.example.foodfusion.Models

data class Room(
    val roomNo: Int,
    val category: String,
    val status: String,
    val roomImage: ByteArray,
    val roomprice:Int,
    val features: String,
    val floorNo: Int
)
