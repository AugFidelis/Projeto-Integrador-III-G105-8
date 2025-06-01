package br.com.superid.auth

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.com.superid.R
import br.com.superid.utils.HelperCripto
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import br.com.superid.ui.theme.SuperIDTheme
import br.com.superid.auth.LoginActivity


class SignUpActivity : ComponentActivity() {
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
                SignUpScreen(
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
 * Tela de cadastro de novos usuários com suporte à autenticação via Firebase.
 *
 * Esta função implementa a interface de cadastro com os seguintes fluxos:
 * Coleta nome, e-mail, senha mestre e confirmação de senha,
 *
 * Valida os campos conforme regras:
 *   -Todos campos obrigatórios
 *   - Formato de e-mail válido
 *   - Coincidência entre senha e confirmação,
 *
 * Quando `useFirebase = true`:
 *   - Cria conta no Firebase Authentication
 *   - Envia e-mail de verificação
 *   - Armazena dados complementares no Firestore:
 *     - Nome do usuário
 *     - Salt para criptografia (codificado em Base64)
 *     - Android ID do dispositivo
 *   - Cria categorias padrão para o novo usuário,
 *
 * @param useFirebase Quando `true`, habilita integração com Firebase (autenticação e banco de dados).
 *                    Quando `false`, exibe apenas o formulário sem funcionalidades reais (modo de demonstração).
 *                    Default: `true`
 * @param modifier Permite customização do layout através de Modifiers do Jetpack Compose.
 *                 Útil para ajustar padding, tamanho ou outros aspectos visuais quando incorporado em layouts complexos.
 *
 * @throws SecurityException Se tentar acessar o Android ID sem permissão adequada (quando useFirebase=true)
 *
 * @see Firebase.auth Para operações de autenticação
 * @see Firebase.firestore Para armazenamento de dados do usuário
 * @see HelperCripto Para geração do salt e codificação Base64
 *
 * @sample SignUpScreen(useFirebase = false) Para visualizar apenas o layout
 */

@SuppressLint("HardwareIds")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    useFirebase: Boolean = true,
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Estados para campos de entrada e controle de UI
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Configuração condicional do Firebase
    val auth = if (useFirebase) Firebase.auth else null  // Instância de autenticação (null em modo demo)
    val db = if (useFirebase) Firebase.firestore else null // Instância do Firestore (null em modo demo)

    var expanded by remember { mutableStateOf(false) }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    Scaffold(
        // TopBar com botão de voltar e menu dropdown
        topBar = {
            TopAppBar(
                title = { Text("Cadastro") },
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

        // Conteúdo principal da tela de cadastro
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = screenHeight * 0.015f)
                .padding(horizontal = screenWidth * 0.05f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Logo do app e título da tela
            Image(
                painter = painterResource(id = R.drawable.superid_logo),
                contentDescription = "Logo SuperID",
                modifier = Modifier.size(screenHeight * 0.25f),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.01f))

            Text(
                text = "Insira os dados da conta:",
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                fontSize = (screenHeight * 0.02f).value.sp,
                lineHeight = (screenHeight * 0.03f).value.sp
            )

            // Campos de entrada: nome, e-mail, senha e confirmação
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome") },
                placeholder = { Text("Informe seu nome") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(screenHeight*0.01f))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                placeholder = { Text("exemplo@email.com") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(screenHeight*0.01f))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha mestre") },
                placeholder = { Text("Informe sua senha") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(screenHeight*0.01f))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar senha") },
                placeholder = { Text("Confirme sua senha") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.025f))

            // Exibe mensagens de erro, se houver
            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }

            // Botão de ação principal: Criação de conta
            Box(modifier = Modifier.fillMaxSize()) {
                Button(
                    onClick = {
                        // Validações básicas de preenchimento, formato e senha
                        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                            errorMessage = "Preencha todos os campos."
                            return@Button
                        }

                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            errorMessage = "E-mail inválido. Verifique o formato."
                            return@Button
                        }

                        if (password != confirmPassword) {
                            errorMessage = "As senhas não coincidem."
                            return@Button
                        }

                        // Criação da conta no Firebase Auth
                        if (useFirebase) {
                            isLoading = true
                            auth?.createUserWithEmailAndPassword(email, password)
                                ?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = auth.currentUser

                                        // Envio do e-mail de verificação
                                        user?.sendEmailVerification()
                                            ?.addOnCompleteListener { verifyTask ->
                                                if (!verifyTask.isSuccessful) {
                                                    isLoading = false
                                                    errorMessage = "Erro ao enviar verificação. Use um e-mail válido."
                                                    user?.delete()
                                                    return@addOnCompleteListener
                                                }

                                                // Armazena dados adicionais no Firestore
                                                val uid = user.uid
                                                val userRef = db?.collection("Users")?.document(uid)

                                                val salt = HelperCripto.generateSalt()
                                                val saltBase64 = HelperCripto.encodeToBase64(salt)
                                                val androidId = Settings.Secure.getString(
                                                    context.contentResolver,
                                                    Settings.Secure.ANDROID_ID
                                                )

                                                userRef?.set(
                                                    mapOf(
                                                        "Nome" to name,
                                                        "salt" to saltBase64,
                                                        "androidId" to androidId
                                                    )
                                                )?.addOnSuccessListener {
                                                    // Criação de categorias padrão
                                                    val categoriasRef = userRef.collection("Categorias")
                                                    val tarefasCategorias = listOf(
                                                        categoriasRef.document().set(
                                                            mapOf(
                                                                "Nome" to "Sites da Web",
                                                                "DataCriacao" to com.google.firebase.Timestamp.now()
                                                            )
                                                        ),
                                                        categoriasRef.document().set(
                                                            mapOf(
                                                                "Nome" to "Aplicativos",
                                                                "DataCriacao" to com.google.firebase.Timestamp.now()
                                                            )
                                                        ),
                                                        categoriasRef.document().set(
                                                            mapOf(
                                                                "Nome" to "Teclados de acesso físico",
                                                                "DataCriacao" to com.google.firebase.Timestamp.now()
                                                            )
                                                        )
                                                    )

                                                    // Confirma sucesso geral e redireciona para login
                                                    Tasks.whenAllComplete(tarefasCategorias)
                                                        .addOnSuccessListener {
                                                            isLoading = false
                                                            Toast.makeText(
                                                                context,
                                                                "Conta criada! Verifique seu e-mail.",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                            context.startActivity(Intent(context, LoginActivity::class.java))
                                                        }
                                                        .addOnFailureListener { e ->
                                                            isLoading = false
                                                            errorMessage = "Erro ao salvar categorias: ${e.localizedMessage}"
                                                        }
                                                }?.addOnFailureListener {
                                                    isLoading = false
                                                    errorMessage = "Erro ao salvar dados no banco."
                                                }
                                            }
                                    } else {
                                        isLoading = false
                                        errorMessage = task.exception?.message ?: "Erro ao criar conta."
                                    }
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight * 0.07f)
                        .align(Alignment.BottomCenter),
                    enabled = !isLoading
                ) {
                    Text(
                        text = if (isLoading) "Carregando..." else "Criar conta",
                        fontSize = (screenHeight * 0.025f).value.sp
                    )
                }
            }
        }
    }
}
