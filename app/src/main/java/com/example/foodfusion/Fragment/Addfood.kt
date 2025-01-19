package com.example.foodfusion.Fragment

import android.R
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.example.foodfusion.DBConnection
import com.example.foodfusion.databinding.FragmentAddfoodBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.sql.PreparedStatement

class addfood : Fragment() {
    private var _binding: FragmentAddfoodBinding? = null
    private val binding get() = _binding!!
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using View Binding
        _binding = FragmentAddfoodBinding.inflate(inflater, container, false)
        // Set the click listener for the food image to open the image selector
        binding.foodimage.setOnClickListener {
            openImageSelector()
        }
        // Set the click listener for the add food button
        binding.addfood.setOnClickListener {
            addFoodToDatabase()
        }
        // set the category spinner
        val category= arrayOf("Beverages" ,"Desserts","Main Course","Fast Food")
        setupSpinner(binding.category, category)

        return binding.root
    }

    private fun setupSpinner(spinner: Spinner, data: Array<String>) {
        val adapter = ArrayAdapter(requireContext(),R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }
    //
    private fun openImageSelector() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            binding.foodimage.setImageURI(selectedImageUri)
        }
    }
    private fun addFoodToDatabase() {
        val foodName = binding.foodname.text.toString()
        val foodPrice = binding.foodprice.text.toString().toLongOrNull()
        val foodDescription = binding.description.text.toString()
        val foodCategory = binding.category.selectedItem.toString()

        if (foodName.isEmpty() || foodPrice == null || foodDescription.isEmpty() || selectedImageUri == null) {
            Toast.makeText(context, "Please fill all field ", Toast.LENGTH_SHORT).show()
            return
        }

        val inputStream: InputStream? = selectedImageUri?.let { context?.contentResolver?.openInputStream(it) }
        val byteArrayOutputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream?.read(buffer).also { length = it ?: -1 } != -1) {
            byteArrayOutputStream.write(buffer, 0, length)
        }
        val foodImage = byteArrayOutputStream.toByteArray()

        // Use Coroutines to insert data
        CoroutineScope(Dispatchers.IO).launch {
            val connection = DBConnection.connect()
            if (connection != null) {
                val query = "INSERT INTO Addfood (Name, price, description, category, food_image) VALUES (?, ?, ?, ?, ?)"
                val preparedStatement: PreparedStatement = connection.prepareStatement(query)
                preparedStatement.setString(1, foodName)
                preparedStatement.setLong(2, foodPrice)
                preparedStatement.setString(3, foodDescription)
                preparedStatement.setString(4, foodCategory)
                preparedStatement.setBytes(5, foodImage)

                try {
                    preparedStatement.executeUpdate()
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "Add Food Successfully", Toast.LENGTH_SHORT).show()
                        refreshFragment()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "error:${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    preparedStatement.close()
                    connection.close()
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "failed to add food", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    // This function is use to Refresh the page
    private fun refreshFragment() {
        parentFragmentManager.beginTransaction().detach(this).attach(this).commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
