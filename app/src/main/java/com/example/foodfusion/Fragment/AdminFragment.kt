package com.example.foodfusion.Fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.foodfusion.DBConnection
import com.example.foodfusion.R
import com.example.foodfusion.databinding.FragmentAdminBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.ResultSet

class AdminFragment : Fragment() {
    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!
    private var listener: OnSidebarClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSidebarClickListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnSidebarClickListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageView>(R.id.sidebar).setOnClickListener {
            listener?.onSidebarClick()
        }

        // Fetch and set the counts
        fetchPendingOrdersCount()
        fetchDeliveredOrdersCount()
    }

    private fun fetchPendingOrdersCount() {
        CoroutineScope(Dispatchers.IO).launch {
            val pendingCount = getPendingOrdersCount()
            withContext(Dispatchers.Main) {
                binding.pendingorder.text = pendingCount.toString()
            }
        }
    }

    private fun fetchDeliveredOrdersCount() {
        CoroutineScope(Dispatchers.IO).launch {
            val deliveredCount = getDeliveredOrdersCount()
            withContext(Dispatchers.Main) {
                binding.deliverorder.text = deliveredCount.toString()
            }
        }
    }

    private fun getPendingOrdersCount(): Int {
        var count = 0
        val connection: Connection? = DBConnection.connect()
        if (connection != null) {
            try {
                val statement = connection.prepareStatement(
                    "SELECT COUNT(*) AS count FROM Orders WHERE Status <> 'Completed'"
                )
                val resultSet: ResultSet = statement.executeQuery()
                if (resultSet.next()) {
                    count = resultSet.getInt("count")
                }
                resultSet.close()
                statement.close()
                connection.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return count
    }

    private fun getDeliveredOrdersCount(): Int {
        var count = 0
        val connection: Connection? = DBConnection.connect()
        if (connection != null) {
            try {
                val statement = connection.prepareStatement(
                    "SELECT COUNT(*) AS count FROM Orders WHERE Status = 'Completed'"
                )
                val resultSet: ResultSet = statement.executeQuery()
                if (resultSet.next()) {
                    count = resultSet.getInt("count")
                }
                resultSet.close()
                statement.close()
                connection.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return count
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface OnSidebarClickListener {
        fun onSidebarClick()
    }
}
