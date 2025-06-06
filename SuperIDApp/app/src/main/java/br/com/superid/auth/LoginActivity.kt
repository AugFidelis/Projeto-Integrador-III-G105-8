package br.com.superid.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.com.superid.R
import br.com.superid.utils.HelperCripto
import br.com.superid.auth.SessionManager
import br.com.superid.main.MainActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import br.com.superid.ui.theme.SuperIDTheme


class LoginActivity : ComponentActivity() {
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
                LoginScreen(
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

/**
 * Tela de login com autenticação via Firebase.
 *
 * Esta função implementa:
 * - Validação básica dos campos de e-mail e senha
 * - Autenticação via Firebase Authentication
 * - Recuperação do salt do usuário no Firestore
 * - Derivação da chave secreta para criptografia
 * - Redirecionamento para recuperação de senha
 *
 * @param useFirebase Quando true, habilita integração com Firebase (modo produção).
 *                    Quando false, exibe apenas a UI sem funcionalidades reais (modo teste).
 *                    Default: true
 * @param modifier Permite customização do layout através de Modifiers do Jetpack Compose
 *
 * @throws SecurityException Se houver falha na derivação da chave criptográfica
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    useFirebase: Boolean = true,
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Estados da UI
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Configuração condicional do Firebase
    val auth = if (useFirebase) Firebase.auth else null
    val db = if (useFirebase) Firebase.firestore else null

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (useFirebase) {
                            (context as? ComponentActivity)?.finish()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
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
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = screenHeight*0.015f)
                .padding(horizontal = screenWidth*0.05f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo da aplicação
            Image(
                painter = painterResource(id = R.drawable.superid_logo),
                contentDescription = "Logo SuperID",
                modifier = Modifier
                    .size(screenHeight*0.25f),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.01f))

            // Campo de e-mail
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                placeholder = { Text("exemplo@email.com") },
                modifier = Modifier.fillMaxWidth()
            )


            // Campo de senha com visualização protegida
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha mestre") },
                placeholder = { Text("Digite sua senha mestre") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(screenHeight*0.025f))

            // Exibição de mensagens de erro
            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(screenHeight*0.025f))
            }

            TextButton(
                onClick = {
                    Intent(context, RecoverMasterPasswordActivity::class.java).also {
                        context.startActivity(it)
                    }
                }
            ) {
                Text("Esqueceu sua senha?")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Preencha todos os campos."
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null

                    // Processo de autenticação com Firebase
                    auth?.signInWithEmailAndPassword(email, password)
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Recupera salt do Firestore
                                val uid = auth.currentUser?.uid.orEmpty()
                                db?.collection("Users")?.document(uid)?.get()
                                    ?.addOnSuccessListener { document ->
                                        val saltBase64 = document.getString("salt")
                                        if (saltBase64.isNullOrBlank()) {
                                            isLoading = false
                                            errorMessage = "Salt não encontrado para este usuário."
                                            return@addOnSuccessListener
                                        }
                                        try {
                                            // Derivação da chave criptográfica
                                            val salt = HelperCripto.decodeFromBase64(saltBase64)
                                            val secretKey = HelperCripto.deriveKeyFromPassword(
                                                password.toCharArray(),
                                                salt
                                            )

                                            // Configuração da sessão
                                            SessionManager.secretKey = secretKey
                                            SessionManager.currentUid = uid

                                            // Redirecionamento para tela principal
                                            isLoading = false
                                            Toast.makeText(context, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                                            Intent(context, MainActivity::class.java).also {
                                                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                context.startActivity(it)
                                            }
                                        } catch (e: Exception) {
                                            isLoading = false
                                            errorMessage = "Erro ao derivar chave: ${e.localizedMessage}"
                                        }
                                    }
                                    ?.addOnFailureListener { e ->
                                        isLoading = false
                                        errorMessage = "Erro ao buscar salt: ${e.localizedMessage}"
                                    }
                            } else {
                                isLoading = false
                                errorMessage = task.exception?.message ?: "Erro ao fazer login."
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight*0.07f),
                enabled = !isLoading
            ) {
                Text(
                    text = if (isLoading) "Carregando..." else "Entrar",
                    fontSize = (screenHeight*0.025f).value.sp
                )
            }
        }
    }
}