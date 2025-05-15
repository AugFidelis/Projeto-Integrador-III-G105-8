package br.com.superid.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
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
import br.com.superid.auth.ui.theme.SuperIDTheme

@OptIn(ExperimentalMaterial3Api::class)
class ResetMasterPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                ResetMasterPasswordScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetMasterPasswordScreen() {
    var password by remember { mutableStateOf("") }
    var newpassword by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Redefinir Senha") },
                navigationIcon = {
                    IconButton(onClick = { /* Voltar */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Voltar",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Mais opções */ }) {
                        Icon(
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
                Image(
                    painter = painterResource(id = R.drawable.superid_logo),
                    contentDescription = "Logo do Aplicativo",
                    modifier = Modifier.size(300.dp)
                )

                Text(
                    "Senha",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .align(Alignment.Start)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Informe sua senha atual") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Nova senha",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .align(Alignment.Start)
                )

                OutlinedTextField(
                    value = newpassword,
                    onValueChange = { newpassword = it },
                    label = { Text("Informe sua nova senha") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        println("Senha: $password, Nova senha: $newpassword")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Redefinir Senha")
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ResetMasterPasswordPreview() {
    SuperIDTheme {
        ResetMasterPasswordScreen()
    }
}
