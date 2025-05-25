package br.com.superid.user

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.R
import br.com.superid.auth.LoginActivity
import br.com.superid.auth.ResetMasterPasswordActivity
import br.com.superid.main.MainActivity
import br.com.superid.user.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProfileScreen()
                }
                }
            }
        }
    }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val db = FirebaseFirestore.getInstance()

    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var isEmailVerified by remember { mutableStateOf(currentUser?.isEmailVerified == true) }
    var emailEnviado by remember { mutableStateOf(false) }

    // Carrega os dados do usuário logado a partir do Firestore
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            db.collection("Users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        userName = document.getString("Nome") ?: "Nome não disponível"
                        userEmail = Firebase.auth.currentUser?.email ?: "E-mail não disponível"
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, MainActivity::class.java))
                        activity?.finish()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Mais opções")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.superid_logo),
                contentDescription = "Logo SUPERID",
                modifier = Modifier.height(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Informações da conta", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nome: $userName")
                    Text("E-mail: $userEmail")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Status de verificação:")

                    if (isEmailVerified) {
                        Text(
                            "Verificado",
                            fontWeight = FontWeight.Bold,
                            color = Color.Green
                        )
                    } else {
                        Column {
                            Text(
                                "Não verificado [Verificar agora]",
                                modifier = Modifier.clickable {
                                    currentUser?.sendEmailVerification()
                                        ?.addOnSuccessListener {
                                            emailEnviado = true
                                            Toast.makeText(
                                                context,
                                                "E-mail de verificação enviado!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        ?.addOnFailureListener {
                                            Toast.makeText(
                                                context,
                                                "Erro ao enviar o e-mail",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                },
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    currentUser?.reload()?.addOnSuccessListener {
                                        isEmailVerified = currentUser.isEmailVerified
                                        if (isEmailVerified) {
                                            Toast.makeText(context, "Email verificado!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Ainda não verificado.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Text("Atualizar status")
                            }
                            if (emailEnviado) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "E-mail de verificação enviado!",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    context.startActivity(Intent(context, ResetMasterPasswordActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Redefinir senha mestre")
            }

            OutlinedButton(
                onClick = {
                    auth.signOut()
                    context.startActivity(Intent(context, LoginActivity::class.java))
                    activity?.finish()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sair da conta")
            }
        }
    }
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
fun ProfileScreenPreview() {
    SuperIDTheme {
        FakeProfileScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FakeProfileScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.superid_logo),
                contentDescription = "Logo SUPERID",
                modifier = Modifier.height(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Informações da conta", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nome: João da Silva")
                    Text("E-mail: joao@email.com")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Status de verificação:")
                    Text(
                        "Não verificado [Verificar agora]",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Email de verificação será enviado aqui",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("Redefinir senha mestre")
            }
            OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("Sair da conta")
            }
        }
    }
}


