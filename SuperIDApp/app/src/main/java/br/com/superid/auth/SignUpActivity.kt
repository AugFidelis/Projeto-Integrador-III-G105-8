package br.com.superid.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import br.com.superid.R
import br.com.superid.utils.HelperCripto
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.provider.Settings

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SignUpScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    useFirebase: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val auth = if (useFirebase) Firebase.auth else null
    val db = if (useFirebase) Firebase.firestore else null

    Scaffold(
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
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(id = R.drawable.superid_logo),
                contentDescription = "Logo SuperID",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome") },
                placeholder = { Text("Informe seu nome") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                placeholder = { Text("exemplo@email.com") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha mestre") },
                placeholder = { Text("Informe sua senha") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar senha") },
                placeholder = { Text("Confirme sua senha") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                        errorMessage = "Preencha todos os campos."
                        return@Button
                    }

                    if (password != confirmPassword) {
                        errorMessage = "As senhas não coincidem."
                        return@Button
                    }

                    if (useFirebase) {
                        isLoading = true
                        auth?.createUserWithEmailAndPassword(email, password)
                            ?.addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    val uid = auth.currentUser?.uid.orEmpty()
                                    val userRef = db?.collection("Users")?.document(uid)

                                    // === GERAR SALT E SALVAR JUNTO AO USUÁRIO ===
                                    val salt = HelperCripto.generateSalt()
                                    val saltBase64 = HelperCripto.encodeToBase64(salt)

                                    // PEGAR O ANDROID ID
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
                                        val categoriasRef = userRef.collection("Categorias")

                                        // Adiciona as categorias a uma coleção dentro da coleção do usuário
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

                                        Tasks.whenAllComplete(tarefasCategorias)
                                            .addOnSuccessListener {
                                                isLoading = false
                                                Toast.makeText(context, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                                                Intent(context, LoginActivity::class.java).also {
                                                    context.startActivity(it)
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                errorMessage = "Erro ao salvar categorias: ${e.localizedMessage}"
                                            }
                                    }?.addOnFailureListener { e ->
                                        isLoading = false
                                        errorMessage = "Erro ao salvar dados no banco."
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
                    .height(56.dp),
                enabled = !isLoading
            ) {
                Text(text = if (isLoading) "Carregando..." else "Criar conta")
            }
        }
    }
}