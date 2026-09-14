package com.example.smartsaccoapp.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(@ApplicationContext context: Context) {
    private val pref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = pref.edit()

    fun setLogin(isLoggedIn: Boolean, email: String?, name: String?, role: String?, kycStatus: String?) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        editor.putString(KEY_USER_EMAIL, email)
        editor.putString(KEY_USER_NAME, name)
        editor.putString(KEY_USER_ROLE, role)
        editor.putString(KEY_KYC_STATUS, kycStatus)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserEmail(): String? {
        return pref.getString(KEY_USER_EMAIL, null)
    }

    fun getUserName(): String? {
        return pref.getString(KEY_USER_NAME, null)
    }

    fun getUserRole(): String {
        return pref.getString(KEY_USER_ROLE, "MEMBER") ?: "MEMBER"
    }

    fun getKycStatus(): String {
        return pref.getString(KEY_KYC_STATUS, "PENDING") ?: "PENDING"
    }

    fun setKycStatus(status: String?) {
        editor.putString(KEY_KYC_STATUS, status)
        editor.apply()
    }

    fun logout() {
        editor.clear()
        editor.apply()
    }

    fun saveCredentials(email: String, password: String) {
        editor.putString(KEY_SAVED_EMAIL, email)
        editor.putString(KEY_SAVED_PASSWORD, password)
        editor.apply()
    }

    fun getSavedEmail(): String? = pref.getString(KEY_SAVED_EMAIL, null)
    fun getSavedPassword(): String? = pref.getString(KEY_SAVED_PASSWORD, null)

    companion object {
        private const val PREF_NAME = "SmartSaccoAuth"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_ROLE = "userRole"
        private const val KEY_KYC_STATUS = "kycStatus"
        private const val KEY_SAVED_EMAIL = "savedEmail"
        private const val KEY_SAVED_PASSWORD = "savedPassword"
    }
}
