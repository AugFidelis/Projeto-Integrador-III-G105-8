package br.com.superid.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import br.com.superid.main.ui.theme.SuperIDTheme
import br.com.superid.main.utils.KeyStoreHelper
import br.com.superid.main.utils.Base64Utils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert

class EditPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KeyStoreHelper.createKeyIfNotExists()
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                // Pegue o docId passado pela Intent
                val docId = intent.getStringExtra("passwordDocId") ?: ""
                EditPasswordScreen(
                    docId = docId,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPasswordScreen(docId: String, modifier: Modifier = Modifier) {
    val auth = Firebase.auth
    val db = Firebase.firestore
    val uid = auth.currentUser?.uid.orEmpty()

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val context = LocalContext.current

    var expanded by remember { mutableStateOf(false) }

    var categorias by remember { mutableStateOf(listOf<String>()) }
    var categoriaSelecionada by remember { mutableStateOf<String?>(null) }
    var expandedMenu by remember { mutableStateOf(false) }

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
    LaunchedEffect(docId, uid) {
        if (docId.isNotBlank() && uid.isNotBlank()) {
            db.collection("Users")
                .document(uid)
                .collection("Senhas")
                .document(docId)
                .get()
                .addOnSuccessListener { doc ->
                    categoriaSelecionada = doc.getString("categoria")
                    nomeSenha = doc.getString("nome") ?: ""
                    descricao = doc.getString("descricao") ?: ""
                    dataCriacao = doc.getTimestamp("dataCriacao") // Preserve original creation date

                    // Descriptografar login
                    val loginCripto = doc.getString("login")
                    login = if (!loginCripto.isNullOrBlank()) {
                        try {
                            val bytes = Base64Utils.decodeFromBase64(loginCripto)
                            String(KeyStoreHelper.decryptData(bytes), Charsets.UTF_8)
                        } catch (e: Exception) {
                            ""
                        }
                    } else ""

                    // Descriptografar senha
                    val senhaCripto = doc.getString("senha")
                    senha = if (!senhaCripto.isNullOrBlank()) {
                        try {
                            val bytes = Base64Utils.decodeFromBase64(senhaCripto)
                            String(KeyStoreHelper.decryptData(bytes), Charsets.UTF_8)
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
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = Color.Black
                ),
                title = { Text("Editar senha") },
                navigationIcon = {
                    IconButton(onClick = {
                        Intent(context, MainActivity::class.java).also {
                            context.startActivity(it)
                        }
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
                            text = { Text("Ativar modo claro") },
                            onClick = {}
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = screenWidth * 0.05f, vertical = screenHeight * 0.02f)
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

                    val loginBytes = login.toByteArray(Charsets.UTF_8)
                    val loginCriptografadoBytes = KeyStoreHelper.encryptData(loginBytes)
                    val loginCriptografadoBase64 = Base64Utils.encodeToBase64(loginCriptografadoBytes)

                    val senhaBytes = senha.toByteArray(Charsets.UTF_8)
                    val senhaCriptografadaBytes = KeyStoreHelper.encryptData(senhaBytes)
                    val senhaCriptografadaBase64 = Base64Utils.encodeToBase64(senhaCriptografadaBytes)

                    val novaSenha = mutableMapOf<String, Any?>(
                        "categoria" to categoriaSelecionada,
                        "nome" to nomeSenha,
                        "login" to loginCriptografadoBase64.ifBlank { null },
                        "senha" to senhaCriptografadaBase64,
                        "descricao" to descricao.ifBlank { null }
                    )

                    // Só inclui dataCriacao se não for null
                    dataCriacao?.let { novaSenha["dataCriacao"] = it }

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
                    .padding(vertical = screenHeight * 0.012f)
                    .height(screenHeight * 0.07f)
            ) {
                Text("Salvar alterações")
            }
        }
    }
}