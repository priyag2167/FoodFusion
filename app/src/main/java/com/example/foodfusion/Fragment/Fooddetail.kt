package com.example.foodfusion.Fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.foodfusion.DBConnection
import com.example.foodfusion.R
import com.example.foodfusion.utils.SharedPreferencesHelper
import java.io.ByteArrayOutputStream
import java.sql.SQLException

class fooddetail : Fragment() {
    private lateinit var btnIncrease: Button
    private lateinit var btnDecrease: Button
    private lateinit var tvQuantity: TextView
    private lateinit var foodName: TextView
    private lateinit var foodPrice: TextView
    private lateinit var addToCartButton: Button
    private var foodiD: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_fooddetail, container, false)

        btnIncrease = view.findViewById(R.id.btn_increase)
        btnDecrease = view.findViewById(R.id.btn_decrease)
        tvQuantity = view.findViewById(R.id.foodquantity)
        foodName = view.findViewById(R.id.foodname)
        foodPrice = view.findViewById(R.id.foodprice)
        addToCartButton = view.findViewById(R.id.addtocart)

        var quantity = tvQuantity.text.toString().toInt()
        btnIncrease.setOnClickListener {
            quantity += 1
            tvQuantity.text = quantity.toString()
        }

        btnDecrease.setOnClickListener {
            if (quantity > 1) {
                quantity -= 1
                tvQuantity.text = quantity.toString()
            }
        }

        foodiD = arguments?.getInt("foodiD")
        if (foodiD != null) {
            fetchFoodDetails(foodiD!!)
        }

        addToCartButton.setOnClickListener {
            addToCart()
        }

        return view
    }

    private fun fetchFoodDetails(foodiD: Int) {
        Thread {
            val connection = DBConnection.connect()
            if (connection != null) {
                try {
                    val statement = connection.createStatement()
                    val resultSet = statement.executeQuery("SELECT Name, price, description, food_image FROM Addfood WHERE foodiD = $foodiD")
                    if (resultSet.next()) {
                        val name = resultSet.getString("Name")
                        val price = resultSet.getString("price")
                        val description = resultSet.getString("description")
                        val imageBytes = resultSet.getBytes("food_image")

                        activity?.runOnUiThread {
                            foodName.text = name
                            foodPrice.text = price
                            view?.findViewById<TextView>(R.id.description)?.text = description

                            // Set the image if available
                            if (imageBytes != null) {
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                view?.findViewById<ImageView>(R.id.foodimage)?.setImageBitmap(bitmap)
                            }
                        }
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                    activity?.runOnUiThread {
                        // Handle error
                    }
                } finally {
                    connection.close()
                }
            }
        }.start()
    }

    private fun addToCart() {
        val email = SharedPreferencesHelper.getUserEmail(requireContext())
        if (email != null && foodiD != null) {
            fetchUserIdAndAddToCart(email, foodiD!!)
        }
    }

    private fun fetchUserIdAndAddToCart(email: String, foodiD: Int) {
        Thread {
            val connection = DBConnection.connect()
            if (connection != null) {
                try {
                    val statement = connection.createStatement()
                    val resultSet = statement.executeQuery("SELECT ID FROM Foodlogin WHERE Email = '$email'")
                    if (resultSet.next()) {
                        val userId = resultSet.getInt("ID")
                        addFoodToCart(userId)
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                    activity?.runOnUiThread {
                        // Handle error
                    }
                } finally {
                    connection.close()
                }
            }
        }.start()
    }

    private fun addFoodToCart(userId: Int) {
        Thread {
            val connection = DBConnection.connect()
            if (connection != null) {
                try {
                    val statement = connection.createStatement()
                    val name = foodName.text.toString()
                    val price = foodPrice.text.toString().toLong()
                    val quantity = tvQuantity.text.toString().toInt()
                    val imageBytes = (view?.findViewById<ImageView>(R.id.foodimage)?.drawable as? BitmapDrawable)?.bitmap?.let { bitmap ->
                        ByteArrayOutputStream().apply {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
                        }.toByteArray()
                    }

                    if (imageBytes != null) {
                        val sql = "INSERT INTO Cart (userid, Name, price, quantity, food_image) VALUES (?, ?, ?, ?, ?)"
                        val preparedStatement = connection.prepareStatement(sql)
                        preparedStatement.setInt(1, userId)
                        preparedStatement.setString(2, name)
                        preparedStatement.setLong(3, price)
                        preparedStatement.setInt(4, quantity)
                        preparedStatement.setBytes(5, imageBytes)

                        preparedStatement.executeUpdate()
                        activity?.runOnUiThread {
                            // Notify the user that the item was added to the cart
                            Toast.makeText(context, "Item Added", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                    activity?.runOnUiThread {
                        // Handle error
                        Toast.makeText(context, "error:${e.message}", Toast.LENGTH_SHORT).show()

                    }
                } finally {
                    connection.close()
                }
            }
        }.start()
    }
}
