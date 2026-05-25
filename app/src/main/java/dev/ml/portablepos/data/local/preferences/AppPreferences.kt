package dev.ml.portablepos.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val STORE_NAME = stringPreferencesKey("store_name")
        val CASHIER_NAME = stringPreferencesKey("cashier_name")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    }

    val storeName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.STORE_NAME] ?: "PortablePOS"
    }

    val cashierName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.CASHIER_NAME] ?: "Cashier"
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.IS_FIRST_LAUNCH] ?: true
    }

    suspend fun saveStoreName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.STORE_NAME] = name
        }
    }

    suspend fun saveCashierName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CASHIER_NAME] = name
        }
    }

    suspend fun setFirstLaunchDone() {
        context.dataStore.edit { preferences ->
            preferences[Keys.IS_FIRST_LAUNCH] = false
        }
    }
}
