package com.nshm.hostelout.utils

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREF_NAME = "EHostelSession"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_TYPE = "user_type"

    private lateinit var prefs: SharedPreferences

    // Initialize this in MainActivity
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    enum class UserRole {
        STUDENT, TEACHER, WARDEN
    }

    var userId: Long
        get() = if (::prefs.isInitialized) prefs.getLong(KEY_USER_ID, -1) else -1
        set(value) {
            if (::prefs.isInitialized) {
                prefs.edit().putLong(KEY_USER_ID, value).apply()
            }
        }

    var userType: UserRole
        get() {
            if (!::prefs.isInitialized) return UserRole.STUDENT
            val type = prefs.getString(KEY_USER_TYPE, UserRole.STUDENT.name)
            return try {
                UserRole.valueOf(type!!)
            } catch (e: Exception) {
                UserRole.STUDENT
            }
        }
        set(value) {
            if (::prefs.isInitialized) {
                prefs.edit().putString(KEY_USER_TYPE, value.name).apply()
            }
        }

    val isLoggedIn: Boolean
        get() = userId != -1L

    fun clearSession() {
        if (::prefs.isInitialized) {
            prefs.edit().clear().apply()
        }
    }
}