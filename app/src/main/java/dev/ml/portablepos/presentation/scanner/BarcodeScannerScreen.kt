package dev.ml.portablepos.presentation.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaActionSound
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import dev.ml.portablepos.domain.model.ScannerMode
import dev.ml.portablepos.presentation.navigation.Screen
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    navController: NavHostController,
    mode: String,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    ) }
    var showPermissionRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            showPermissionRationale = true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.setMode(mode)
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigation.collect { navEvent ->
            when (navEvent) {
                is ScannerNavigation.NavigateToAddProduct -> {
                    navController.navigate(Screen.AddProduct.createRoute(navEvent.barcode))
                }
                is ScannerNavigation.BarcodeExists -> {
                    Toast.makeText(context, "Barcode already exists", Toast.LENGTH_SHORT).show()
                }
                is ScannerNavigation.GoBack -> {
                    val previousRoute = navController.previousBackStackEntry?.destination?.route
                    navController.popBackStack()
                    if (shouldOpenPosAfterScannerPop(viewModel.scanMode, previousRoute)) {
                        navController.navigate(Screen.POS.route)
                    }
                }
                else -> {}
            }
        }
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { navController.popBackStack() },
            title = { Text("Camera Permission Required") },
            text = {
                Text("Camera permission is needed to scan barcodes. Please grant camera access in app settings.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionRationale = false
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }) {
                    Text("Grant")
                }
            },
            dismissButton = {
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showProductNotFoundDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissProductNotFoundDialog() },
            title = { Text("Product Not Found") },
            text = { Text("Product with barcode ${uiState.notFoundBarcode} was not found. Register as new product?") },
            confirmButton = {
                TextButton(onClick = { viewModel.navigateToAddProduct() }) {
                    Text("Register")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissProductNotFoundDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Auto-dismiss product-added confirmation after 1.5s
    if (uiState.lastAddedProductName != null) {
        LaunchedEffect(uiState.lastAddedProductName) {
            delay(1500)
            viewModel.clearLastAddedProduct()
        }
    }

    if (!hasCameraPermission) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Camera permission is required for barcode scanning",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            onBarcodeScanned = { barcode ->
                if (!viewModel.isDuplicateScan(barcode)) {
                    viewModel.processBarcode(barcode)
                    try {
                        playBeepSound(context)
                        vibrateDevice(context)
                    } catch (_: Exception) { }
                }
            }
        )

        ScannerOverlay()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when (viewModel.scanMode) {
                                ScannerMode.PRODUCT_REGISTRATION -> "Register Product"
                                ScannerMode.SALE -> "Scan for Sale"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleTorch() }) {
                            Icon(
                                if (uiState.torchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = if (uiState.torchOn) "Turn off flashlight" else "Turn on flashlight"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.5f),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent
        ) { padding ->
            Text(
                text = "Place barcode inside the frame",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .padding(bottom = 80.dp)
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 32.dp),
                textAlign = TextAlign.Center
            )
        }

        if (uiState.lastAddedProductName != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 160.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = " ${uiState.lastAddedProductName} added to cart",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (viewModel.scanMode == ScannerMode.SALE && uiState.totalCartItems > 0) {
            Button(
                onClick = { viewModel.doneScanning() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1565C0)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = " Done (${uiState.totalCartItems})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

internal fun shouldOpenPosAfterScannerPop(scanMode: ScannerMode, previousRoute: String?): Boolean {
    return scanMode == ScannerMode.SALE && previousRoute != Screen.POS.route
}

@Composable
private fun CameraPreview(
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val barcodeScanner = BarcodeScanning.getClient()
                    val analysisExecutor = Executors.newSingleThreadExecutor()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                        try {
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                barcodeScanner.process(image)
                                    .addOnSuccessListener(analysisExecutor) { barcodes ->
                                        for (barcode in barcodes) {
                                            barcode.rawValue?.let { value ->
                                                onBarcodeScanned(value)
                                                return@addOnSuccessListener
                                            }
                                        }
                                    }
                                    .addOnCompleteListener(analysisExecutor) {
                                        try { imageProxy.close() } catch (_: Exception) { }
                                    }
                            } else {
                                imageProxy.close()
                            }
                        } catch (e: Exception) {
                            try { imageProxy.close() } catch (_: Exception) { }
                        }
                    }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun ScannerOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val frameSize = minOf(canvasWidth, canvasHeight) * 0.7f
        val left = (canvasWidth - frameSize) / 2
        val top = (canvasHeight - frameSize) / 2

        drawRect(
            color = Color.Black.copy(alpha = 0.4f),
            topLeft = Offset(0f, 0f),
            size = Size(canvasWidth, top)
        )
        drawRect(
            color = Color.Black.copy(alpha = 0.4f),
            topLeft = Offset(0f, top + frameSize),
            size = Size(canvasWidth, canvasHeight - top - frameSize)
        )
        drawRect(
            color = Color.Black.copy(alpha = 0.4f),
            topLeft = Offset(0f, top),
            size = Size(left, frameSize)
        )
        drawRect(
            color = Color.Black.copy(alpha = 0.4f),
            topLeft = Offset(left + frameSize, top),
            size = Size(canvasWidth - left - frameSize, frameSize)
        )

        drawRoundRect(
            color = Color.White,
            topLeft = Offset(left, top),
            size = Size(frameSize, frameSize),
            cornerRadius = CornerRadius(16f, 16f),
            style = Stroke(width = 3f)
        )
    }
}

private fun playBeepSound(context: Context) {
    try {
        val sound = MediaActionSound()
        sound.load(MediaActionSound.FOCUS_COMPLETE)
        sound.play(MediaActionSound.FOCUS_COMPLETE)
    } catch (_: Exception) { }
}

private fun vibrateDevice(context: Context) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(200)
            }
        }
    } catch (_: Exception) { }
}
