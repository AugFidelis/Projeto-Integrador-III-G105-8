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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.R
import br.com.superid.auth.ui.theme.SuperIDTheme
import androidx.compose.material3.OutlinedTextFieldDefaults


@OptIn(ExperimentalMaterial3Api::class)
class RecoverMasterPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                RecoverMasterPasswordScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoverMasterPasswordScreen() {
    var email by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar senha") },
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
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Image(
                    painter = painterResource(id = R.drawable.superid_logo),
                    contentDescription = "Logo do Aplicativo",
                    modifier = Modifier.size(150.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Informe o email cadastrado para receber o link de redefinição da senha mestre. O email precisa ter sido validado previamente.",
                    fontSize = 14.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "E-Mail",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                        .align(Alignment.Start)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = {Text("exemplo@email.com")},
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = VisualTransformation.None,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Gray,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedLabelColor = Color.Gray,
                        unfocusedLabelColor = Color.LightGray,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedPlaceholderColor = Color.LightGray,
                        unfocusedPlaceholderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        println("Enviando link para: $email")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Enviar link")
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun RecoverMasterPasswordPreview() {
    SuperIDTheme {
        RecoverMasterPasswordScreen()
    }
}
