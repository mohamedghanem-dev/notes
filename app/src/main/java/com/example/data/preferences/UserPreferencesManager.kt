package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.util.SecurityUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "notes_user_settings")

class UserPreferencesManager(private val context: Context) {

    companion object {
        private val KEY_LANGUAGE = stringPreferencesKey("language") // "en" or "ar"
        private val KEY_APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        private val KEY_PASSWORD_HASH = stringPreferencesKey("password_hash")
        private val KEY_PASSWORD_SALT = stringPreferencesKey("password_salt")
        private val KEY_THEME = stringPreferencesKey("app_theme") // "system", "light", "dark"
        private val KEY_IS_GRID_VIEW = booleanPreferencesKey("is_grid_view")
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_LANGUAGE] ?: "en"
    }

    val isAppLockEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_APP_LOCK_ENABLED] ?: false
    }

    val hasPasswordSetFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        !preferences[KEY_PASSWORD_HASH].isNullOrBlank()
    }

    val appThemeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME] ?: "system"
    }

    val isGridViewFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_GRID_VIEW] ?: true
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = language
        }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_APP_LOCK_ENABLED] = enabled
        }
    }

    suspend fun setPassword(password: String): Boolean {
        if (password.isBlank()) return false
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(password, salt)
        context.dataStore.edit { preferences ->
            preferences[KEY_PASSWORD_SALT] = salt
            preferences[KEY_PASSWORD_HASH] = hash
            preferences[KEY_APP_LOCK_ENABLED] = true
        }
        return true
    }

    suspend fun removePassword() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_PASSWORD_HASH)
            preferences.remove(KEY_PASSWORD_SALT)
            preferences[KEY_APP_LOCK_ENABLED] = false
        }
    }

    suspend fun verifyPassword(inputPassword: String): Boolean {
        val preferences = context.dataStore.data.first()
        val hash = preferences[KEY_PASSWORD_HASH] ?: ""
        val salt = preferences[KEY_PASSWORD_SALT] ?: ""
        return SecurityUtils.verifyPassword(inputPassword, salt, hash)
    }

    suspend fun setAppTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME] = theme
        }
    }

    suspend fun setGridView(isGrid: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_GRID_VIEW] = isGrid
        }
    }
}
