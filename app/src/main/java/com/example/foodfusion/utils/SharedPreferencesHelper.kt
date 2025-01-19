package com.example.foodfusion.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesHelper {

    private const val PREFS_NAME = "foodfusion_prefs"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_USER_NAME = "user_name"


    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveUser(context: Context, email: String, role: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(KEY_USER_EMAIL, email)
        editor.putString(KEY_USER_ROLE, role)
        editor.apply()
    }

    fun getUserRole(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_USER_ROLE, null)
    }

    fun getUserEmail(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_USER_EMAIL, null)
    }

    fun clearUser(context: Context) {
        val editor = getSharedPreferences(context).edit()
        editor.clear()
        editor.apply()
    }

    fun clearUserSession(context: Context): String? {
        val userRole = getUserRole(context)
        clearUser(context)
        return userRole
    }
}
