package br.com.superid.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import br.com.superid.ui.theme.SuperIDTheme
import br.com.superid.auth.SessionManager
import br.com.superid.utils.HelperCripto
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.nio.charset.StandardCharsets

/**
 * Activity para edição de senhas salvas no aplicativo.
 *
 * Funcionalidades:
 * - Carrega os dados existentes da senha selecionada
 * - Permite editar todos os campos (nome, login, senha, descrição, categoria)
 * - Criptografa os dados sensíveis antes de salvar
 * - Mantém a data de criação original
 * - Valida campos obrigatórios antes de salvar
 */
class EditPasswordActivity : ComponentActivity() {
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
                // Pegue o docId passado pela Intent
                val docId = intent.getStringExtra("passwordDocId") ?: ""
                EditPasswordScreen(
                    docId = docId,
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
 * Tela de edição de senha com formulário completo.
 *
 * @param docId ID do documento da senha no Firestore
 * @param onToggleTheme Callback para alternar entre tema claro/escuro
 * @param isDarkTheme Indica se o tema escuro está ativo
 * @param modifier Modificador para customização do layout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPasswordScreen(
    docId: String,
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    // Configuração do Firebase e dados do usuário
    val auth = Firebase.auth
    val db = Firebase.firestore
    val uid = SessionManager.currentUid ?: auth.currentUser?.uid.orEmpty()
    val secretKey = SessionManager.secretKey

    // Dimensões responsivas
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val context = LocalContext.current

    // Estados da UI
    var expanded by remember { mutableStateOf(false) }

    var categorias by remember { mutableStateOf(listOf<String>()) }
    var categoriaSelecionada by remember { mutableStateOf<String?>(null) }
    var expandedMenu by remember { mutableStateOf(false) }

    // Campos do formulário
    var nomeSenha by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var dataCriacao by remember { mutableStateOf<com.google.firebase.Timestamp?>(null) }

    // Carregar categorias
    LaunchedEffect(uid) {
        if (uid.isNotBlank()) {
            db.collection("Users")
                .document(uid)
                .collection("Categorias")
                .orderBy("DataCriacao")
                .get()
                .addOnSuccessListener { result ->
                    val nomes = result.mapNotNull { it.getString("Nome") }
                    categorias = nomes
                }
        }
    }

    // Carregar dados da senha para edição
    LaunchedEffect(docId, uid, secretKey) {
        if (docId.isNotBlank() && uid.isNotBlank() && secretKey != null) {
            db.collection("Users")
                .document(uid)
                .collection("Senhas")
                .document(docId)
                .get()
                .addOnSuccessListener { doc ->
                    categoriaSelecionada = doc.getString("Categoria")
                    nomeSenha = doc.getString("Nome") ?: ""
                    descricao = doc.getString("Descricao") ?: ""
                    dataCriacao = doc.getTimestamp("DataCriacao") // Preserve original creation date

                    // Descriptografar login (se criptografado)
                    val loginCripto = doc.getString("Login")
                    login = if (!loginCripto.isNullOrBlank()) {
                        try {
                            val bytes = HelperCripto.decodeFromBase64(loginCripto)
                            String(HelperCripto.decryptData(bytes, secretKey), Charsets.UTF_8)
                        } catch (e: Exception) {
                            ""
                        }
                    } else ""

                    // Descriptografar senha
                    val senhaCripto = doc.getString("SenhaCriptografada")
                    senha = if (!senhaCripto.isNullOrBlank()) {
                        try {
                            val bytes = HelperCripto.decodeFromBase64(senhaCripto)
                            String(HelperCripto.decryptData(bytes, secretKey), Charsets.UTF_8)
                        } catch (e: Exception) {
                            ""
                        }
                    } else ""

                    isLoading = false
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Erro ao carregar senha", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
        } else if (secretKey == null) {
            Toast.makeText(context, "Sessão expirada. Faça login novamente.", Toast.LENGTH_LONG).show()
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
        return
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = { Text("Editar senha") },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? ComponentActivity)?.finish()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Mais"
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(
                    horizontal = screenWidth * 0.05f)
                .padding(top= screenHeight*0.02f ,bottom = screenHeight*0.015f)
        ) {
            // Categoria dropdown
            Text("Categoria", modifier = Modifier.padding(bottom = screenHeight * 0.002f))
            ExposedDropdownMenuBox(
                expanded = expandedMenu,
                onExpandedChange = { expandedMenu = !expandedMenu },
            ) {
                OutlinedTextField(
                    value = categoriaSelecionada ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = {},
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMenu)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false }
                ) {
                    categorias.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria) },
                            onClick = {
                                categoriaSelecionada = categoria
                                expandedMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.015f))

            Text("Nome da senha", modifier = Modifier.padding(bottom = screenHeight * 0.003f))
            OutlinedTextField(
                value = nomeSenha,
                onValueChange = { nomeSenha = it },
                placeholder = { Text("www.exemplo.com.br") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.012f))

            Text("Login (opcional)", modifier = Modifier.padding(bottom = screenHeight * 0.003f))
            OutlinedTextField(
                value = login,
                onValueChange = { login = it },
                placeholder = { Text("Nome de usuário, E-mail, etc...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.012f))

            Text("Senha", modifier = Modifier.padding(bottom = screenHeight * 0.003f))
            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                placeholder = { Text("Informe sua senha") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.012f))

            Text("Descrição (opcional)", modifier = Modifier.padding(bottom = screenHeight * 0.003f))
            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                placeholder = { Text("Descrição do site, etc...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.15f)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Botão de salvar alterações
            Button(
                onClick = {
                    if (categoriaSelecionada.isNullOrBlank() || nomeSenha.isBlank() || senha.isBlank()) {
                        Toast.makeText(context, "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (secretKey == null) {
                        Toast.makeText(context, "Sessão expirada. Faça login novamente.", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    // Criptografa login e senha (se login não estiver vazio)
                    val loginCriptografadoBase64 = if (login.isNotBlank()) {
                        val loginBytes = login.toByteArray(StandardCharsets.UTF_8)
                        val loginCriptografadoBytes = HelperCripto.encryptData(loginBytes, secretKey)
                        HelperCripto.encodeToBase64(loginCriptografadoBytes)
                    } else ""

                    val senhaBytes = senha.toByteArray(StandardCharsets.UTF_8)
                    val senhaCriptografadaBytes = HelperCripto.encryptData(senhaBytes, secretKey)
                    val senhaCriptografadaBase64 = HelperCripto.encodeToBase64(senhaCriptografadaBytes)

                    val novaSenha = mutableMapOf<String, Any?>(
                        "Categoria" to categoriaSelecionada,
                        "Nome" to nomeSenha,
                        "Login" to loginCriptografadoBase64.ifBlank { null },
                        "SenhaCriptografada" to senhaCriptografadaBase64,
                        "Descricao" to descricao.ifBlank { null }
                    )

                    // Só inclui DataCriacao se não for null
                    dataCriacao?.let { novaSenha["DataCriacao"] = it }

                    db.collection("Users")
                        .document(uid)
                        .collection("Senhas")
                        .document(docId)
                        .set(novaSenha)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Senha atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                            Intent(context, MainActivity::class.java).also {
                                context.startActivity(it)
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Erro ao atualizar senha: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.07f)
            ) {
                Text("Salvar alterações",
                    fontSize = (screenHeight*0.025f).value.sp
                )
            }
        }
    }
}