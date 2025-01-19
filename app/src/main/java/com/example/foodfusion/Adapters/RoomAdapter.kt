package com.example.foodfusion.Adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodfusion.Models.Room
import com.example.foodfusion.R

class RoomAdapter(private val roomList: List<Room>, private val itemClickListener: OnItemClickListener) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(room: Room)
    }

    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomImage: ImageView = itemView.findViewById(R.id.roomimage)
        val roomNo: TextView = itemView.findViewById(R.id.roomno)
        val roomCategory: TextView = itemView.findViewById(R.id.roomcategory)
        val roomStatus: TextView = itemView.findViewById(R.id.roomstatus)

        init {
            itemView.setOnClickListener {
                itemClickListener.onItemClick(roomList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.room_item, parent, false)
        return RoomViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val currentRoom = roomList[position]
        holder.roomNo.text = currentRoom.roomNo.toString()
        holder.roomCategory.text = currentRoom.category
        holder.roomStatus.text = currentRoom.status
        

        // Convert byte array to Bitmap and set it to the ImageView
        val bitmap = BitmapFactory.decodeByteArray(currentRoom.roomImage, 0, currentRoom.roomImage.size)
        holder.roomImage.setImageBitmap(bitmap)
    }

    override fun getItemCount() = roomList.size
}
