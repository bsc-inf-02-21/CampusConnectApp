package com.example.campusconnectapp.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    private val _notificationsEnabled = MutableStateFlow(isNotificationsEnabled())
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _realtimeSyncEnabled = MutableStateFlow(isRealtimeSyncEnabled())
    val realtimeSyncEnabled: StateFlow<Boolean> = _realtimeSyncEnabled.asStateFlow()

    fun isNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        _notificationsEnabled.value = enabled
    }

    fun isRealtimeSyncEnabled(): Boolean {
        return prefs.getBoolean(KEY_REALTIME_SYNC_ENABLED, true)
    }

    fun setRealtimeSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REALTIME_SYNC_ENABLED, enabled).apply()
        _realtimeSyncEnabled.value = enabled
    }

    companion object {
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_REALTIME_SYNC_ENABLED = "realtime_sync_enabled"

        @Volatile
        private var INSTANCE: AppPreferencesManager? = null

        fun getInstance(context: Context): AppPreferencesManager {
            return INSTANCE ?: synchronized(this) {
                val instance = AppPreferencesManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
