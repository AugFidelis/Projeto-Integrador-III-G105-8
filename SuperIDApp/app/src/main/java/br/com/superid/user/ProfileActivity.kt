package br.com.superid.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.com.superid.R
import br.com.superid.auth.LoginActivity
import br.com.superid.auth.ResetMasterPasswordActivity
import br.com.superid.main.MainActivity
import br.com.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
            var isDarkTheme by remember { mutableStateOf(prefs.getBoolean("is_dark_theme", systemDark)) }

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        isDarkTheme = prefs.getBoolean("is_dark_theme", systemDark)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            SuperIDTheme(
                darkTheme = isDarkTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProfileScreen(
                        onToggleTheme = {
                            isDarkTheme = !isDarkTheme
                            prefs.edit().putBoolean("is_dark_theme", isDarkTheme).apply() },
                        isDarkTheme = isDarkTheme
                    )
                }
                }
            }
        }
    }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val db = FirebaseFirestore.getInstance()

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    var expanded by remember { mutableStateOf(false) }

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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Text("Perfil")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? ComponentActivity)?.finish()
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
                            onClick = {
                                onToggleTheme()
                                expanded = false
                            },
                            text = { Text(if (isDarkTheme) "Ativar modo claro" else "Ativar modo escuro") }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(bottom = screenHeight*0.015f)
                .padding(horizontal = screenWidth*0.05f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.superid_logo),
                contentDescription = "Logo SUPERID",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier.size(screenHeight*0.25f)
            )

            Spacer(modifier = Modifier.height(screenHeight*0.025f))

            Text("Informações da conta", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(screenHeight*0.02f))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(screenHeight*0.015f)) {
                    Text("Nome: $userName")

                    Spacer(modifier = Modifier.height(screenHeight * 0.015f))

                    Text("E-mail: $userEmail")

                    Spacer(modifier = Modifier.height(screenHeight * 0.015f))

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

            Column(
                verticalArrangement = Arrangement.spacedBy(screenHeight*0.01f)
            ){
                Button(
                    onClick = {
                        context.startActivity(Intent(context, ResetMasterPasswordActivity::class.java))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight*0.07f)
                ) {
                    Text("Redefinir senha mestre",
                        fontSize = (screenHeight*0.025f).value.sp
                    )
                }

                OutlinedButton(
                    onClick = {
                        auth.signOut()
                        context.startActivity(Intent(context, LoginActivity::class.java))
                        activity?.finish()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight*0.07f)
                ) {
                    Text("Sair da conta",
                        fontSize = (screenHeight*0.025f).value.sp
                    )
                }
            }
        }
    }
}