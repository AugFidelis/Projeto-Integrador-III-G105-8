package br.com.superid.auth

import android.app.Activity
import android.content.Intent
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
import br.com.superid.ui.theme.SuperIDTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.platform.LocalContext
import br.com.superid.user.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth

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

/**
 * Tela de recuperação de senha mestre.
 *
 * Fluxo principal:
 * - Usuário informa e-mail cadastrado
 * - Sistema verifica se o formato de e-mail é validado
 * - Envia link de redefinição via Firebase Auth
 * - Redireciona para tela de login após sucesso
 *
 * @throws FirebaseAuthException Em caso de falha na comunicação com Firebase
 *
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoverMasterPasswordScreen() {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = Color.Black
                ),
                title = {
                    Text("Termos de uso")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        Intent(context, LoginActivity::class.java).also {
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

                // Instruções para o usuário
                Text(
                    text = "Informe o email cadastrado para receber o link de redefinição da senha mestre. O email precisa ter sido validado previamente.",
                    fontSize = 14.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Campo de e-mail
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
                    label = { Text("exemplo@email.com") },
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
                        if (email.isNotEmpty()) {
                            currentUser?.reload()?.addOnCompleteListener {
                                val user = auth.currentUser
                                if (user != null && user.isEmailVerified) {
                                    auth.sendPasswordResetEmail(email)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(
                                                    context,
                                                    "Link de recuperação enviado para $email",
                                                    Toast.LENGTH_LONG
                                                ).show()

                                                context.startActivity(Intent(context, LoginActivity::class.java))
                                                if (context is Activity) context.finish()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Erro ao enviar o link. Verifique o email.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Conta não verificada. Verifique seu e-mail antes de continuar.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Por favor, insira um email válido.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
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



