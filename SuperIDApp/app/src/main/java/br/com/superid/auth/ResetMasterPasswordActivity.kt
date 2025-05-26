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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import br.com.superid.R
import br.com.superid.auth.ui.theme.SuperIDTheme
import br.com.superid.user.ProfileActivity
import br.com.superid.user.WelcomeActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

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
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    var expanded by remember { mutableStateOf(false) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

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
                        Intent(context, ProfileActivity::class.java).also{
                            context.startActivity(it)
                        }
                    }) {
//                        Image(painter = painterResource(R.drawable.returnarrow),
//                            contentDescription = "Seta de retorno à tela anterior",
//                            colorFilter = ColorFilter.tint(Color.Black)
//                            )
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.superid_logo),
                    contentDescription = "Logo do Aplicativo",
                    modifier = Modifier.size(180.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Senha atual", style = MaterialTheme.typography.bodyLarge)
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Informe sua senha atual") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Nova senha", style = MaterialTheme.typography.bodyLarge)
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Informe sua nova senha") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (user != null && user.email != null) {
                            isLoading = true
                            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

                            user.reauthenticate(credential)
                                .addOnSuccessListener {
                                    user.updatePassword(newPassword)
                                        .addOnSuccessListener {
                                            isLoading = false
                                            Toast.makeText(
                                                context,
                                                "Senha atualizada com sucesso!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            (context as? ComponentActivity)?.finish()
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            Toast.makeText(
                                                context,
                                                "Erro ao atualizar senha: ${e.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    Toast.makeText(
                                        context,
                                        "Senha atual incorreta: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        } else {
                            Toast.makeText(context, "Usuário não autenticado.", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Redefinindo...")
                    } else {
                        Text("Redefinir Senha")
                    }
                }
            }
        }
    )
}
