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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class SettingsUiState(
    val storeName: String = "PortablePOS",
    val cashierName: String = "Cashier",
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
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load settings") }
            }
        }
        viewModelScope.launch {
            try {
                appPreferences.cashierName.collect { name ->
                    _uiState.update { it.copy(cashierName = name, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load settings") }
            }
        }
    }

    fun onStoreNameChange(name: String) {
        _uiState.update { it.copy(storeName = name) }
    }

    fun onCashierNameChange(name: String) {
        _uiState.update { it.copy(cashierName = name) }
    }

    fun save() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, message = null) }
            try {
                appPreferences.saveStoreName(_uiState.value.storeName)
                appPreferences.saveCashierName(_uiState.value.cashierName)
                _uiState.update { it.copy(isSaving = false, message = "Settings saved successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save settings") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, message = null, error = null) }
            try {
                withContext(Dispatchers.IO) {
                    val dbPath = context.getDatabasePath("portable_pos_database").absolutePath
                    val dbFile = File(dbPath)
                    if (!dbFile.exists()) {
                        throw IllegalStateException("Database file not found")
                    }
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        FileInputStream(dbFile).use { input ->
                            input.copyTo(output)
                        }
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

                    dbFile.delete()
                    walFile.delete()
                    shmFile.delete()

                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(dbFile).use { output ->
                            input.copyTo(output)
                        }
                    } ?: throw IllegalStateException("Failed to open input stream")
                }
                _uiState.update { it.copy(isImporting = false, message = "Backup restored successfully. Restart app to apply changes.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isImporting = false, error = e.message ?: "Failed to import backup") }
            }
        }
    }
}
