package dev.ml.portablepos.presentation.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.ml.portablepos.data.local.database.AppDatabase
import dev.ml.portablepos.data.local.preferences.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

data class SettingsUiState(
    val storeName: String = "PortablePOS",
    val storeAddress: String = "",
    val storeContact: String = "",
    val receiptFooter: String = "",
    val cashierName: String = "Cashier",
    val enableTax: Boolean = false,
    val taxRate: Double = 0.0,
    val taxRateText: String = "0",
    val printerEnabled: Boolean = false,
    val printerAddress: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                appPreferences.storeName.collect { name ->
                    _uiState.update { it.copy(storeName = name) }
                }
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try {
                appPreferences.storeAddress.collect { addr ->
                    _uiState.update { it.copy(storeAddress = addr) }
                }
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try {
                appPreferences.storeContact.collect { contact ->
                    _uiState.update { it.copy(storeContact = contact) }
                }
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try {
                appPreferences.receiptFooter.collect { footer ->
                    _uiState.update { it.copy(receiptFooter = footer) }
                }
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try {
                appPreferences.cashierName.collect { name ->
                    _uiState.update { it.copy(cashierName = name) }
                }
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try {
                appPreferences.enableTax.collect { enabled ->
                    _uiState.update { it.copy(enableTax = enabled) }
                }
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try {
                appPreferences.taxRate.collect { rate ->
                    _uiState.update { it.copy(taxRate = rate, taxRateText = (rate * 100).toInt().toString(), isLoading = false) }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
        viewModelScope.launch {
            try {
                appPreferences.printerEnabled.collect { enabled ->
                    _uiState.update { it.copy(printerEnabled = enabled) }
                }
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try {
                appPreferences.printerAddress.collect { addr ->
                    _uiState.update { it.copy(printerAddress = addr) }
                }
            } catch (_: Exception) {}
        }
    }

    fun onStoreNameChange(name: String) { _uiState.update { it.copy(storeName = name) } }
    fun onStoreAddressChange(addr: String) { _uiState.update { it.copy(storeAddress = addr) } }
    fun onStoreContactChange(contact: String) { _uiState.update { it.copy(storeContact = contact) } }
    fun onReceiptFooterChange(footer: String) { _uiState.update { it.copy(receiptFooter = footer) } }
    fun onCashierNameChange(name: String) { _uiState.update { it.copy(cashierName = name) } }
    fun onEnableTaxChange(enabled: Boolean) { _uiState.update { it.copy(enableTax = enabled) } }

    fun onTaxRateChange(value: String) {
        val filtered = value.filter { it.isDigit() }
        val rate = (filtered.toIntOrNull() ?: 0).coerceIn(0, 100)
        _uiState.update { it.copy(taxRateText = filtered, taxRate = rate / 100.0) }
    }

    fun onPrinterEnabledChange(enabled: Boolean) { _uiState.update { it.copy(printerEnabled = enabled) } }
    fun onPrinterAddressChange(addr: String) { _uiState.update { it.copy(printerAddress = addr) } }

    fun save() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, message = null) }
            try {
                val s = _uiState.value
                appPreferences.saveStoreName(s.storeName)
                appPreferences.saveStoreAddress(s.storeAddress)
                appPreferences.saveStoreContact(s.storeContact)
                appPreferences.saveReceiptFooter(s.receiptFooter)
                appPreferences.saveCashierName(s.cashierName)
                appPreferences.saveEnableTax(s.enableTax)
                appPreferences.saveTaxRate(s.taxRate)
                appPreferences.savePrinterEnabled(s.printerEnabled)
                appPreferences.savePrinterAddress(s.printerAddress)
                _uiState.update { it.copy(isSaving = false, message = "Settings saved successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save settings") }
            }
        }
    }

    fun clearMessage() { _uiState.update { it.copy(message = null) } }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, message = null, error = null) }
            try {
                withContext(Dispatchers.IO) {
                    val dbPath = context.getDatabasePath("portable_pos_database").absolutePath
                    val dbFile = File(dbPath)
                    if (!dbFile.exists()) throw IllegalStateException("Database file not found")
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        FileInputStream(dbFile).use { input -> input.copyTo(output) }
                    } ?: throw IllegalStateException("Failed to open output stream")
                }
                _uiState.update { it.copy(isExporting = false, message = "Backup exported successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, error = e.message ?: "Failed to export backup") }
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, message = null, error = null) }
            try {
                withContext(Dispatchers.IO) {
                    AppDatabase.closeDatabase()
                    val dbPath = context.getDatabasePath("portable_pos_database").absolutePath
                    val dbFile = File(dbPath)
                    val walFile = File("$dbPath-wal")
                    val shmFile = File("$dbPath-shm")
                    dbFile.delete(); walFile.delete(); shmFile.delete()
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(dbFile).use { output -> input.copyTo(output) }
                    } ?: throw IllegalStateException("Failed to open input stream")
                }
                _uiState.update { it.copy(isImporting = false, message = "Backup restored successfully. Restart app to apply changes.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isImporting = false, error = e.message ?: "Failed to import backup") }
            }
        }
    }
}