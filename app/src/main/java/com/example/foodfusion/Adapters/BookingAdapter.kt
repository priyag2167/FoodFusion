package com.example.foodfusion.Adapters

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodfusion.Activity.booking_detail
import com.example.foodfusion.Models.Booking
import com.example.foodfusion.R

class BookingAdapter(private val context: Context, private val bookingList: List<Booking>) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomNoTextView: TextView = itemView.findViewById(R.id.roomno)
        val bookingIdTextView: TextView = itemView.findViewById(R.id.bookingid)
        val checkInDateTextView: TextView = itemView.findViewById(R.id.checkindate)
        val roomImageView: ImageView = itemView.findViewById(R.id.roomimage) // ImageView for the room image
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.booking_item, parent, false)
        return BookingViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val currentItem = bookingList[position]
        holder.roomNoTextView.text = currentItem.roomNo.toString()
        holder.bookingIdTextView.text = currentItem.bookingId.toString()
        holder.checkInDateTextView.text = currentItem.checkInDate

        // Convert the byte array to a bitmap and set it to the ImageView
        val bitmap =
            currentItem.roomImage?.let { BitmapFactory.decodeByteArray(currentItem.roomImage, 0, it.size) }
            holder.roomImageView.setImageBitmap(bitmap)

        // Set the click listener for the item
        holder.itemView.setOnClickListener {
            val intent = Intent(context, booking_detail::class.java).apply {
                putExtra("RoomNo", currentItem.roomNo)
                putExtra("BookingId", currentItem.bookingId)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = bookingList.size
}

