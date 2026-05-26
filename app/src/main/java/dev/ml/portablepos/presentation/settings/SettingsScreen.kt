package dev.ml.portablepos.presentation.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import dev.ml.portablepos.ui.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.util.Locale

private val gradientStart = Color(0xFF1565C0)
private val gradientEnd = Color(0xFF1976D2)

@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoading) { if (!uiState.isLoading) isRefreshing = false }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.message) {
        uiState.message?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessage() }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? -> if (uri != null) viewModel.exportBackup(uri) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> if (uri != null) viewModel.importBackup(uri) }

    Scaffold(
        containerColor = Color(0xFFF0F2F5),
        topBar = {
            Box(modifier = Modifier.fillMaxWidth().background(brush = Brush.verticalGradient(listOf(gradientStart, gradientEnd)))) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Settings", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Text("Preferences & Configuration", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                    }
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.padding(end = 16.dp))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = { isRefreshing = true; viewModel.loadSettings() }) {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(16.dp)) {
                // Store Settings
                SettingsCard(title = "Store Settings", icon = Icons.Default.Store) {
                    OutlinedTextField(value = uiState.storeName, onValueChange = viewModel::onStoreNameChange, label = { Text("Store Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = uiState.storeAddress, onValueChange = viewModel::onStoreAddressChange, label = { Text("Store Address") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = uiState.storeContact, onValueChange = viewModel::onStoreContactChange, label = { Text("Contact Number") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = uiState.cashierName, onValueChange = viewModel::onCashierNameChange, label = { Text("Default Cashier Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Receipt Settings
                SettingsCard(title = "Receipt Settings", icon = Icons.Default.Receipt) {
                    OutlinedTextField(value = uiState.receiptFooter, onValueChange = viewModel::onReceiptFooterChange, label = { Text("Receipt Footer Message") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tax Settings
                SettingsCard(title = "Tax / VAT Settings", icon = Icons.Default.Percent) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Enable VAT / Tax", style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = uiState.enableTax, onCheckedChange = viewModel::onEnableTaxChange)
                    }
                    if (uiState.enableTax) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = uiState.taxRateText, onValueChange = viewModel::onTaxRateChange, label = { Text("Tax Rate (%)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Printer Settings
                SettingsCard(title = "Receipt Printer", icon = Icons.Default.Print) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Bluetooth Printer", style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = uiState.printerEnabled, onCheckedChange = viewModel::onPrinterEnabledChange)
                    }
                    if (uiState.printerEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = uiState.printerAddress, onValueChange = viewModel::onPrinterAddressChange, label = { Text("Printer MAC Address") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp), placeholder = { Text("00:11:22:33:44:55") }, leadingIcon = { Icon(Icons.Default.Bluetooth, contentDescription = null) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Save Button
                Button(onClick = { viewModel.save() }, modifier = Modifier.fillMaxWidth().height(48.dp), enabled = !uiState.isSaving, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                    if (uiState.isSaving) { CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp) }
                    else { Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Save Settings", fontWeight = FontWeight.SemiBold) }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Backup & Restore
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Backup & Restore", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Export your data to a backup file or restore from a previous backup.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(java.util.Date()); exportLauncher.launch("PortablePOS_Backup_$ts.db") }, modifier = Modifier.fillMaxWidth().height(48.dp), enabled = !uiState.isExporting && !uiState.isImporting, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                            if (uiState.isExporting) { CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp) }
                            else { Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Export Backup", fontWeight = FontWeight.SemiBold) }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/octet-stream", "application/x-sqlite3")) }, modifier = Modifier.fillMaxWidth().height(48.dp), enabled = !uiState.isExporting && !uiState.isImporting, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)) {
                            if (uiState.isImporting) { CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.size(20.dp), strokeWidth = 2.dp) }
                            else { Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Restore Backup", fontWeight = FontWeight.SemiBold) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // About
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("App Version", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("PortablePOS v2.0", fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}