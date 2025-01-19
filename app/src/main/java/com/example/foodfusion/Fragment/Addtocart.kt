package com.example.foodfusion.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.fragment.app.commit
import com.example.foodfusion.Adapters.CartAdapter
import com.example.foodfusion.DBConnection
import com.example.foodfusion.Models.CartItem
import com.example.foodfusion.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.sql.Timestamp
import java.util.*

class addtocart : Fragment(), CartAdapter.OnDeleteClickListener {
    private lateinit var cartAdapter: CartAdapter
    private lateinit var cartItems: MutableList<CartItem>
    private lateinit var subtotalTextView: TextView
    private lateinit var taxAndFeeTextView: TextView
    private lateinit var totalTextView: TextView
    private lateinit var checkoutButton: Button
    private var userId: Int = 1 // Assuming you have a way to get the logged-in user ID

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_addtocart, container, false)

        subtotalTextView = view.findViewById(R.id.subtotal)
        taxAndFeeTextView = view.findViewById(R.id.taxandfee)
        totalTextView = view.findViewById(R.id.total)
        checkoutButton = view.findViewById(R.id.checkout)

        val recyclerView: RecyclerView = view.findViewById(R.id.cardrecyclerView)
        cartItems = mutableListOf()
        cartAdapter = CartAdapter(cartItems, this)
        recyclerView.adapter = cartAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        fetchDataFromCart()

        checkoutButton.setOnClickListener {
            onCheckout()
        }

        return view
    }

    private fun fetchDataFromCart() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val connection = DBConnection.connect()
                connection?.let {
                    val statement = it.createStatement()
                    val query = "SELECT cardid, userid, Name, price, quantity, food_image FROM Cart"
                    val resultSet = statement.executeQuery(query)

                    val tempCartItems = mutableListOf<CartItem>()

                    while (resultSet.next()) {
                        val cardid = resultSet.getInt("cardid")
                        val userid = resultSet.getInt("userid")
                        val name = resultSet.getString("Name")
                        val price = resultSet.getLong("price")
                        val quantity = resultSet.getInt("quantity")
                        val foodImage = resultSet.getBytes("food_image")

                        val cartItem = CartItem(cardid, userid, name, price, quantity, foodImage)
                        tempCartItems.add(cartItem)
                    }

                    resultSet.close()
                    statement.close()
                    connection.close()

                    withContext(Dispatchers.Main) {
                        cartItems.clear()
                        cartItems.addAll(tempCartItems)
                        cartAdapter.notifyDataSetChanged()
                        updateSubtotal()
                    }
                }
            } catch (e: SQLException) {
                Log.e("FetchDataError", "Error fetching data: ${e.message}")
            }
        }
    }

    override fun onDeleteClick(position: Int) {
        val cartItemToDelete = cartItems[position]

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val connection = DBConnection.connect()
                connection?.let {
                    val statement = it.prepareStatement("DELETE FROM Cart WHERE cardid = ?")
                    statement.setInt(1, cartItemToDelete.cardid)
                    val rowsAffected = statement.executeUpdate()

                    if (rowsAffected > 0) {
                        withContext(Dispatchers.Main) {
                            cartItems.removeAt(position)
                            cartAdapter.notifyItemRemoved(position)
                            Toast.makeText(context, "Item deleted from cart", Toast.LENGTH_SHORT).show()
                            updateSubtotal()
                        }
                    }

                    statement.close()
                    connection.close()
                }
            } catch (e: SQLException) {
                Log.e("DeleteDataError", "Error: ${e.message}")
            }
        }
    }

    override fun onQuantityChange(position: Int, newQuantity: Int) {
        val updatedCartItem = cartItems[position]
        updatedCartItem.quantity = newQuantity
        updateSubtotal()
    }

    private fun calculateSubtotal(): Double {
        var subtotal = 0.0
        for (item in cartItems) {
            subtotal += item.price * item.quantity
        }
        return subtotal
    }

    private fun updateSubtotal() {
        val subtotal = calculateSubtotal()
        val taxAndFee = subtotal * 0.18
        val total = subtotal + taxAndFee

        subtotalTextView.text = String.format("%.2f", subtotal)
        taxAndFeeTextView.text = String.format("%.2f", taxAndFee)
        totalTextView.text = String.format("%.2f", total)
    }

    private fun onCheckout() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val connection = DBConnection.connect()
                connection?.let {
                    val userQuery = "SELECT Name, address, phone FROM Foodlogin WHERE ID = ?"
                    val userStatement = it.prepareStatement(userQuery)
                    userStatement.setInt(1, userId)
                    val userResultSet = userStatement.executeQuery()

                    if (userResultSet.next()) {
                        val userName = userResultSet.getString("Name")
                        val userAddress = userResultSet.getString("address")
                        val userPhone = userResultSet.getString("phone")

                        userResultSet.close()
                        userStatement.close()

                        val insertOrderQuery = """
                            INSERT INTO Orders (UserID, Name, Price, Status, Address, Date, Phone, Quantity)
                            VALUES (?, ?, ?, 'Pending', ?, ?, ?, ?)
                        """.trimIndent()
                        val orderStatement = it.prepareStatement(insertOrderQuery)

                        for (cartItem in cartItems) {
                            orderStatement.setInt(1, userId)
                            orderStatement.setString(2, cartItem.name)
                            orderStatement.setLong(3, cartItem.price * cartItem.quantity)
                            orderStatement.setString(4, userAddress)
                            orderStatement.setTimestamp(5, Timestamp(System.currentTimeMillis()))
                            orderStatement.setString(6, userPhone)
                            orderStatement.setInt(7, cartItem.quantity)
                            orderStatement.addBatch()
                        }

                        orderStatement.executeBatch()
                        orderStatement.close()

                        withContext(Dispatchers.Main) {
                            navigateToOrderSuccessful()
                        }

                        clearCart()
                    }
                }
            } catch (e: SQLException) {
                Log.e("CheckoutError", "Error during checkout: ${e.message}")
            }
        }
    }

    private fun navigateToOrderSuccessful() {
        parentFragmentManager.commit {
            replace(R.id.fragment_container, Ordersuccessful())
            addToBackStack(null) // Optional: Adds the transaction to the back stack
        }
    }

    private fun clearCart() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val connection = DBConnection.connect()
                connection?.let {
                    val statement = it.createStatement()
                    statement.executeUpdate("DELETE FROM Cart WHERE userid = $userId")
                    statement.close()
                    connection.close()

                    withContext(Dispatchers.Main) {
                        cartItems.clear()
                        cartAdapter.notifyDataSetChanged()
                        updateSubtotal()
                    }
                }
            } catch (e: SQLException) {
                Log.e("ClearCartError", "Error clearing cart: ${e.message}")
            }
        }
    }
}
