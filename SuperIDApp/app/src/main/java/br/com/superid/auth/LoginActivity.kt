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
            SuperIDTheme{
                LoginScreen(
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
fun LoginScreen(
    useFirebase: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val auth = if (useFirebase) Firebase.auth else null
    val db = if (useFirebase) Firebase.firestore else null

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
                placeholder = { Text("Digite sua senha mestre") },
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
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Preencha todos os campos."
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null

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
                                            val salt = HelperCripto.decodeFromBase64(saltBase64)
                                            // Deriva a chave da senha-mestra + salt
                                            val secretKey = HelperCripto.deriveKeyFromPassword(
                                                password.toCharArray(),
                                                salt
                                            )
                                            // Guarde a chave em algum lugar seguro da memória (Singleton SessionManager em auth)
                                            SessionManager.secretKey = secretKey
                                            SessionManager.currentUid = uid

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
                    .height(56.dp),
                enabled = !isLoading
            ) {
                Text(text = if (isLoading) "Carregando..." else "Entrar")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    // Redefinição de senha via Firebase Auth
                    if (email.isBlank()) {
                        errorMessage = "Informe o e-mail para redefinir a senha."
                    } else {
                        isLoading = true
                        auth?.sendPasswordResetEmail(email)
                            ?.addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "Verifique seu e-mail para redefinir a senha.", Toast.LENGTH_LONG).show()
                                } else {
                                    errorMessage = task.exception?.message ?: "Erro ao enviar e-mail de redefinição."
                                }
                            }
                    }
                }
            ) {
                Text("Esqueceu sua senha?")
            }
        }
    }
}