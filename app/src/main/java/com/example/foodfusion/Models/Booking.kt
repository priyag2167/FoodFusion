package com.example.foodfusion.Models


data class Booking(
    val bookingId: Int,
    val roomNo: Int,
    val checkInDate: String,
    val roomImage: ByteArray?
)

