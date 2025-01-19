package com.example.foodfusion.Fragment

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodfusion.Adapters.FoodItemAdapter
import com.example.foodfusion.DBConnection
import com.example.foodfusion.Models.FoodItem
import com.example.foodfusion.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class allfoodItem : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var foodItemAdapter: FoodItemAdapter
    private lateinit var progressBar: ProgressBar
    private val foodItemList: MutableList<FoodItem> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_allfood_item, container, false)

        recyclerView = view.findViewById(R.id.fooditemrecyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        foodItemAdapter = FoodItemAdapter(foodItemList)
        recyclerView.adapter = foodItemAdapter

        progressBar = view.findViewById(R.id.progressBar)
        context?.let { ContextCompat.getColor(it, R.color.orange) }
            ?.let { progressBar.indeterminateDrawable.setColorFilter(it, PorterDuff.Mode.SRC_IN) }

        fetchDataFromDatabase()

        return view
    }

    private fun fetchDataFromDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            showProgressBar()

            val connection: Connection? = DBConnection.connect()
            connection?.let {
                val query = "SELECT foodiD, Name, price, description, category, food_image FROM Addfood"
                val statement: PreparedStatement = connection.prepareStatement(query)
                val resultSet: ResultSet = statement.executeQuery()

                while (resultSet.next()) {
                    val foodItem = FoodItem(
                        foodId = resultSet.getInt("foodiD"),
                        name = resultSet.getString("Name"),
                        price = resultSet.getLong("price"),
                        description = resultSet.getString("description"),
                        category = resultSet.getString("category"),
                        foodImage = resultSet.getBytes("food_image")
                    )
                    foodItemList.add(foodItem)
                }

                resultSet.close()
                statement.close()
                connection.close()
            }

            withContext(Dispatchers.Main) {
                foodItemAdapter.notifyDataSetChanged()
                hideProgressBar()
            }
        }
    }

    private suspend fun showProgressBar() {
        withContext(Dispatchers.Main) {
            progressBar.visibility = View.VISIBLE
        }
    }

    private suspend fun hideProgressBar() {
        withContext(Dispatchers.Main) {
            progressBar.visibility = View.GONE
        }
    }
}
