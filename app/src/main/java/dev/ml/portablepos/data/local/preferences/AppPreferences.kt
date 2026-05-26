package dev.ml.portablepos.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
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
        val STORE_ADDRESS = stringPreferencesKey("store_address")
        val STORE_CONTACT = stringPreferencesKey("store_contact")
        val RECEIPT_FOOTER = stringPreferencesKey("receipt_footer")
        val CASHIER_NAME = stringPreferencesKey("cashier_name")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val TAX_RATE = doublePreferencesKey("tax_rate")
        val ENABLE_TAX = booleanPreferencesKey("enable_tax")
        val PRINTER_ADDRESS = stringPreferencesKey("printer_address")
        val PRINTER_ENABLED = booleanPreferencesKey("printer_enabled")
        val GCASH_NUMBER = stringPreferencesKey("gcash_number")
    }

    val storeName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.STORE_NAME] ?: "PortablePOS"
    }

    val storeAddress: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.STORE_ADDRESS] ?: ""
    }

    val storeContact: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.STORE_CONTACT] ?: ""
    }

    val receiptFooter: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.RECEIPT_FOOTER] ?: ""
    }

    val cashierName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.CASHIER_NAME] ?: "Cashier"
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.IS_FIRST_LAUNCH] ?: true
    }

    val taxRate: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[Keys.TAX_RATE] ?: 0.0
    }

    val enableTax: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.ENABLE_TAX] ?: false
    }

    val printerAddress: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.PRINTER_ADDRESS] ?: ""
    }

    val printerEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.PRINTER_ENABLED] ?: false
    }

    val gcashNumber: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.GCASH_NUMBER] ?: ""
    }

    suspend fun saveStoreName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.STORE_NAME] = name
        }
    }

    suspend fun saveStoreAddress(address: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.STORE_ADDRESS] = address
        }
    }

    suspend fun saveStoreContact(contact: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.STORE_CONTACT] = contact
        }
    }

    suspend fun saveReceiptFooter(footer: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.RECEIPT_FOOTER] = footer
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

    suspend fun saveTaxRate(rate: Double) {
        context.dataStore.edit { preferences ->
            preferences[Keys.TAX_RATE] = rate
        }
    }

    suspend fun saveEnableTax(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ENABLE_TAX] = enabled
        }
    }

    suspend fun savePrinterAddress(address: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.PRINTER_ADDRESS] = address
        }
    }

    suspend fun savePrinterEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.PRINTER_ENABLED] = enabled
        }
    }

    suspend fun saveGcashNumber(number: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.GCASH_NUMBER] = number
        }
    }
}