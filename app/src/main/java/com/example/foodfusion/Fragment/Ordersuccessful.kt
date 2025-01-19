package com.example.foodfusion.Fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.foodfusion.R

class Ordersuccessful : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ordersuccessful, container, false)

        // Apply animation to the tick icon
        val tickImageView: ImageView = view.findViewById(R.id.imageView11)
        val scaleAnimation = AnimationUtils.loadAnimation(context, R.anim.scale_animation)
        tickImageView.startAnimation(scaleAnimation)

        // Use Handler to delay the navigation
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToAllOrder()
        }, 3000) // 3000 milliseconds = 3 seconds

        return view
    }

    private fun navigateToAllOrder() {
        // Clear the back stack before navigating
        parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        // Navigate to the AllOrderFragment
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, AllOrderFragment())
            commit()
        }
    }
}
