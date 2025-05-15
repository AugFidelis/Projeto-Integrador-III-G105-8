package br.com.superid.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.superid.R
import br.com.superid.user.ui.theme.SuperIDTheme

class MainActivity : ComponentActivity() {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Voltar",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.ic_more),
                            contentDescription = "Mais opções",
                            modifier = Modifier.size(24.dp)
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
                        modifier = Modifier.size(300.dp)
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

                    // Botão de Fazer Login
                    Button(
                        onClick = {
                            println("E-mail: $email, Senha: $password")
                        },
                        modifier = Modifier.fillMaxWidth()
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