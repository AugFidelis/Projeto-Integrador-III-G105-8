package br.com.superid.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.superid.R
import br.com.superid.main.MainActivity
import br.com.superid.user.WelcomeActivity
import br.com.superid.user.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                LoginScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val auth = FirebaseAuth.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = Color.Black),
                title = { Text("Login") },
                navigationIcon = {
                    IconButton(onClick = {
                        Intent(context, WelcomeActivity::class.java).also{
                            context.startActivity(it)
                        }
                    }) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Voltar",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(Color.Black)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        expanded = !expanded
                    }) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.ic_more),
                            contentDescription = "Mais opções",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(Color.Black)
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
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,

                ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.superid_logo),
                        contentDescription = "Logo do Aplicativo",
                        modifier = Modifier.size(300.dp),
                        colorFilter = ColorFilter.tint(Color.Black)
                    )
                    Text("E-mail", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth(0.8f).align(Alignment.Start))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier=Modifier.height(16.dp))

                    Text("Senha", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth(0.8f).align(Alignment.Start))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = {},
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Link de "Esqueceu a senha?"
                    TextButton(onClick = {}) {
                        Text("Esqueceu sua senha?")
                    }

                    var isLoading by remember { mutableStateOf(false) }

                    // Botão de Fazer Login
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Por favor, preencha todos os campos.",
                                    Toast.LENGTH_SHORT).show()

                                return@Button
                            }

                            isLoading = true
                            auth.signInWithEmailAndPassword(email,password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if(task.isSuccessful){
                                        Log.d("AUTH","Login realizado com sucesso.")

                                        Intent(context, MainActivity::class.java).also {
                                            context.startActivity(it)
                                        }
                                    }else{
                                        Log.e("AUTH","Falha no login.")

                                        val errorMessage = task.exception?.message ?: "Erro ao fazer login."
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                    }
                                }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading //Contrário do isLoading, fica desabilitado enquanto carrega
                    ) {
                        Text("Fazer Login")
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SuperIDTheme {
        LoginScreen()
    }
}