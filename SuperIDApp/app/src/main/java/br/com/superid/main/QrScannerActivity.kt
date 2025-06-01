package br.com.superid.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.com.superid.ui.theme.SuperIDTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import br.com.superid.main.MainActivity
import br.com.superid.main.MainScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import androidx.camera.core.Preview
import android.annotation.SuppressLint
import android.util.Size
import androidx.camera.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScanner

class QrScannerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
            var isDarkTheme by remember { mutableStateOf(prefs.getBoolean("is_dark_theme", systemDark)) }

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        isDarkTheme = prefs.getBoolean("is_dark_theme", systemDark)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            SuperIDTheme(
                darkTheme = isDarkTheme
            ) {
                QRCodeScannerScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),

                    onToggleTheme = {
                        isDarkTheme = !isDarkTheme
                        prefs.edit().putBoolean("is_dark_theme", isDarkTheme).apply() },
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeScannerScreen(
    modifier: Modifier = Modifier,
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher que pede a permissão ao usuário
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            Toast.makeText(
                context,
                "Permissão de câmera negada.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Pede permissão ao iniciar, se ainda não tiver
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Text("Leitor de QR Code")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? ComponentActivity)?.finish()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Seta de retorno à tela anterior"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        expanded = !expanded
                    }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Ícone de menu"
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                onToggleTheme()
                                expanded = false
                            },
                            text = { Text(if (isDarkTheme) "Ativar modo claro" else "Ativar modo escuro") }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (hasCameraPermission) {
                var scannedValue by remember { mutableStateOf<String?>(null) }
                var lastTriedValue by remember { mutableStateOf<String?>(null) }
                var isProcessing by remember { mutableStateOf(false) }
                var message by remember { mutableStateOf<String?>(null) }

                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onQrCodeScanned = { qrCode -> scannedValue = qrCode }
                )

                scannedValue?.let { qr ->
                    if (!isProcessing && qr != lastTriedValue) {
                        isProcessing = true
                        lastTriedValue = qr
                        coroutineScope.launch(Dispatchers.IO) {
                            val auth = FirebaseAuth.getInstance()
                            val user = auth.currentUser
                            if (user == null) {
                                message = "Usuário não autenticado! Faça login primeiro."
                                isProcessing = false
                                return@launch
                            }
                            val db = FirebaseFirestore.getInstance()
                            db.collection("Login")
                                .whereEqualTo("loginToken", qr)
                                .limit(1)
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    if (!querySnapshot.isEmpty) {
                                        val doc = querySnapshot.documents.first()
                                        doc.reference.update(
                                            mapOf(
                                                "user" to user.uid,
                                                "dataHoraLogin" to Timestamp.now()
                                            )
                                        ).addOnSuccessListener {
                                            Toast.makeText(context, "Login sem senha confirmado!", Toast.LENGTH_SHORT).show()
                                            Intent(context, MainActivity::class.java).also {
                                                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                context.startActivity(it)
                                            }
                                            (context as? ComponentActivity)?.finish()
                                        }.addOnFailureListener { e ->
                                            Log.e("QRLogin", "Erro ao atualizar login.", e)
                                            message = "Erro ao registrar login."
                                            isProcessing = false
                                        }
                                    } else {
                                        message = "QR Code inválido ou expirado."
                                        isProcessing = false
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("QRLogin", "Erro ao buscar login.", e)
                                    message = "Erro ao acessar o banco de dados."
                                    isProcessing = false
                                }
                        }
                    }
                }

                message?.let {
                    // Mostra erro se houver
                    LaunchedEffect(it) {
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                        message = null
                    }
                }
            } else {
                LaunchedEffect(Unit) {
                    Toast.makeText(
                        context,
                        "Permissão de câmera negada. Retornando à tela inicial.",
                        Toast.LENGTH_LONG
                    ).show()
                    (context as? ComponentActivity)?.finish()
                }
            }

            Text(
                text = "Aponte a câmera para o QR Code gerado pelo site parceiro.",
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = screenHeight*0.02f)
                    .padding(horizontal = screenWidth*0.02f)
            )
        }
    }
}


@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onQrCodeScanned: (String) -> Unit = {}
){
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        modifier = modifier,
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // ML Kit BarcodeScanner config
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()
                val scanner = BarcodeScanning.getClient(options)

                // ImageAnalysis para processar frames
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(
                    ContextCompat.getMainExecutor(context),
                    QrCodeAnalyzer(scanner, onQrCodeScanned)
                )

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (exc: Exception) {
                    Log.e("CameraPreview", "Erro ao iniciar a câmera", exc)

                    Toast.makeText(
                        context,
                        "Não foi possível acessar a câmera. Tente novamente.",
                        Toast.LENGTH_LONG
                    ).show()

                    (context as? ComponentActivity)?.finish()
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}

class QrCodeAnalyzer(
    private val scanner: BarcodeScanner,
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { value ->
                            onQrCodeScanned(value)
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}