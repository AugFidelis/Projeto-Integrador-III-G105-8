package br.com.superid.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import br.com.superid.main.ui.theme.SuperIDTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import br.com.superid.auth.SessionManager
import br.com.superid.utils.HelperCripto
import java.nio.charset.StandardCharsets

class AddPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                AddPasswordScreen(
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
fun AddPasswordScreen(modifier: Modifier = Modifier){
    val auth = Firebase.auth
    val db = Firebase.firestore
    val uid = SessionManager.currentUid ?: auth.currentUser?.uid.orEmpty()
    val secretKey = SessionManager.secretKey

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

    LaunchedEffect(uid) { //Atualiza as categorias puxando do firestore quando a página é aberta
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

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = Color.Black
                ),
                title = {
                    Text("Adicionar senha")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        Intent(context, MainActivity::class.java).also{
                            context.startActivity(it)
                        }
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
                .padding(
                    horizontal = screenWidth * 0.05f,
                    vertical = screenHeight * 0.02f
                )
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
                        TrailingIcon(expanded = expandedMenu)
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

                    val novaSenha = hashMapOf(
                        "Categoria" to categoriaSelecionada,
                        "Nome" to nomeSenha,
                        "Login" to loginCriptografadoBase64.ifBlank { null },
                        "SenhaCriptografada" to senhaCriptografadaBase64,
                        "Descricao" to descricao.ifBlank { null },
                        "DataCriacao" to com.google.firebase.Timestamp.now()
                    )

                    db.collection("Users")
                        .document(uid)
                        .collection("Senhas")
                        .add(novaSenha)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Senha salva com sucesso!", Toast.LENGTH_SHORT).show()
                            Intent(context, MainActivity::class.java).also {
                                context.startActivity(it)
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Erro ao salvar senha: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = screenHeight * 0.012f)
                    .height(screenHeight * 0.07f)
            ) {
                Text("Salvar senha")
            }
        }
    }
}