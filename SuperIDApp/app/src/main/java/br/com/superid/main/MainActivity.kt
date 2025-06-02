package br.com.superid.main

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.com.superid.R
import br.com.superid.ui.theme.SuperIDTheme
import br.com.superid.user.EditCategoriesActivity
import br.com.superid.user.ProfileActivity
import br.com.superid.user.WelcomeActivity
import br.com.superid.main.QrScannerActivity
import br.com.superid.main.EditPasswordActivity
import br.com.superid.auth.SessionManager
import br.com.superid.main.AddPasswordActivity
import br.com.superid.utils.HelperCripto
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Activity principal do aplicativo que exibe e gerencia as senhas salvas.
 *
 * Funcionalidades:
 * - Listagem de senhas por categoria
 * - Adição/edição/exclusão de senhas
 * - Filtro por categorias
 * - Gerenciamento de temas (claro/escuro)
 * - Scanner de QR Code para login rápido
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
            var isDarkTheme by remember { mutableStateOf(prefs.getBoolean("is_dark_theme", systemDark)) }

            // Observador para atualizar o tema quando a activity for retomada
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
                MainScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),

                    onToggleTheme = {
                        isDarkTheme = !isDarkTheme
                        prefs.edit().putBoolean("is_dark_theme", isDarkTheme).apply() },
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

/**
 * Tela principal que exibe a lista de senhas e funcionalidades relacionadas.
 *
 * @param onToggleTheme Callback para alternar entre tema claro/escuro
 * @param isDarkTheme Indica se o tema escuro está ativo
 * @param modifier Modificador para customização do layout
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
){
    // Configuração do Firebase e dados do usuário
    val auth = Firebase.auth
    val db = Firebase.firestore
    val uid = SessionManager.currentUid ?: auth.currentUser?.uid.orEmpty()
    val secretKey = SessionManager.secretKey

    // Dimensões responsivas
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val context = LocalContext.current

    // Estados da UI
    var expanded by remember { mutableStateOf(false) }

    var categorias by remember { mutableStateOf(listOf<String>()) }
    var isLoadingCategorias by remember { mutableStateOf(false) }
    var categoriasErro by remember { mutableStateOf<String?>(null) }

    var mostrarDialogo by remember { mutableStateOf(false) }
    var filtroSelecionado by remember { mutableStateOf("") }
    var categoriaFiltrada by remember { mutableStateOf("Todas") }

    // Modelo de dados para as senhas
    data class SenhaCard(
        val id: String,
        val title: String,
        val description: String,
        val login: String,
        val password: String,
        val category: String
    )

    var senhas by remember { mutableStateOf(listOf<SenhaCard>()) }
    var isLoadingSenhas by remember { mutableStateOf(false) }
    var senhasErro by remember { mutableStateOf<String?>(null) }

    // Para controlar qual menu dropdown está aberto em cada card
    var expandedCardMenuIndex by remember { mutableStateOf(-1) }
    var confirmarExclusaoIndex by remember { mutableStateOf(-1) }

    var mostrarAvisoLimpeza by remember { mutableStateOf(false) }

    // Função auxiliar para limpar todas as senhas do usuário
    fun limparTodasSenhas(uid: String, db: com.google.firebase.firestore.FirebaseFirestore, onFinish: () -> Unit) {
        db.collection("Users").document(uid).collection("Senhas")
            .get()
            .addOnSuccessListener { result ->
                val batch = db.batch()
                for (doc in result) {
                    batch.delete(doc.reference)
                }
                batch.commit().addOnSuccessListener {
                    onFinish()
                }
            }
    }

    // Carrega categorias e senhas
    LaunchedEffect(uid, secretKey) {
        if (uid.isNotBlank()) {
            isLoadingCategorias = true
            db.collection("Users")
                .document(uid)
                .collection("Categorias")
                .orderBy("DataCriacao")
                .get()
                .addOnSuccessListener { result ->
                    val nomes = result.mapNotNull { it.getString("Nome") }.toMutableList()
                    if (!nomes.contains("Todas")) nomes.add(0, "Todas")
                    categorias = nomes

                    // Atualiza o filtro se necessário
                    if (filtroSelecionado.isBlank()) filtroSelecionado = nomes.firstOrNull() ?: ""
                    if (categoriaFiltrada.isBlank()) categoriaFiltrada = nomes.firstOrNull() ?: ""
                    isLoadingCategorias = false
                }
                .addOnFailureListener { e ->
                    categoriasErro = "Erro ao buscar categorias: ${e.localizedMessage}"
                    isLoadingCategorias = false
                    categorias = listOf("Todas")
                }

            isLoadingSenhas = true
            db.collection("Users").document(uid)
                .collection("Senhas")
                .orderBy("DataCriacao") // Corrigido para "DataCriacao"
                .get()
                .addOnSuccessListener { result ->
                    val listaSenhas = result.map { doc ->
                        val senhaCriptografadaBase64 = doc.getString("SenhaCriptografada") ?: ""
                        // Descriptografa usando HelperCripto e secretKey
                        val senhaDescriptografada = if(senhaCriptografadaBase64.isNotBlank() && secretKey != null)
                            try {
                                val senhaCriptografadaBytes = HelperCripto.decodeFromBase64(senhaCriptografadaBase64)
                                val senhaBytes = HelperCripto.decryptData(senhaCriptografadaBytes, secretKey)
                                String(senhaBytes, Charsets.UTF_8)
                            } catch (e: Exception){
                                "[Falha ao descriptografar]"
                            }
                        else{
                            ""
                        }

                        // Login pode ser criptografado também, ou salvo como texto simples
                        val loginCriptografadoBase64 = doc.getString("Login") ?: ""
                        val loginDescriptografado = if (loginCriptografadoBase64.isNotBlank() && secretKey != null) {
                            try {
                                val loginCriptografadoBytes = HelperCripto.decodeFromBase64(loginCriptografadoBase64)
                                val loginBytes = HelperCripto.decryptData(loginCriptografadoBytes, secretKey)
                                String(loginBytes, Charsets.UTF_8)
                            } catch (e: Exception) {
                                "[Falha ao descriptografar]"
                            }
                        } else {
                            doc.getString("Login") ?: ""
                        }

                        SenhaCard(
                            id = doc.id,
                            title = doc.getString("Nome") ?: "",
                            description= doc.getString("Descricao") ?: "",
                            login= loginDescriptografado,
                            password= senhaDescriptografada,
                            category= doc.getString("Categoria") ?: ""
                        )
                    }

                    // Verifica se todas as senhas falharam ao descriptografar (ou se existe pelo menos uma senha no resultado)
                    val todasFalharam = listaSenhas.isNotEmpty() && listaSenhas.all { it.password == "[Falha ao descriptografar]" }

                    if (todasFalharam) {
                        // Limpa todas as senhas do usuário
                        limparTodasSenhas(uid, db) {
                            // Depois da limpeza, zera a lista e mostra aviso
                            senhas = emptyList()
                            mostrarAvisoLimpeza = true
                            isLoadingSenhas = false
                        }
                    } else {
                        senhas = listaSenhas
                        isLoadingSenhas = false
                    }
                }
                .addOnFailureListener { e ->
                    senhasErro = "Erro ao buscar senhas: ${e.localizedMessage}"
                    isLoadingSenhas = false
                    senhas = emptyList()
                }
        }
    }

    if (mostrarAvisoLimpeza) {
        AlertDialog(
            onDismissRequest = { mostrarAvisoLimpeza = false },
            confirmButton = {
                TextButton(onClick = { mostrarAvisoLimpeza = false }) { Text("OK") }
            },
            title = { Text("Senhas removidas") },
            text = { Text("Sua senha-mestra foi redefinida. Por segurança, todas as senhas salvas anteriormente foram removidas, pois não podem mais ser acessadas.") }
        )
    }

    val isEmailVerified = auth.currentUser?.isEmailVerified

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Image(painter = painterResource(R.drawable.superid_basic_logo),
                        contentDescription = "Logo do aplicativo",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                },
                navigationIcon = {},
                actions = {
                    IconButton(onClick = {
                        Intent(context, AddPasswordActivity::class.java).also {
                            context.startActivity(it)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Ícone de adicionar senhas",
                        )
                    }

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
                            text = { Text("Minhas categorias") },
                            onClick = {
                                Intent(context, EditCategoriesActivity::class.java).also {
                                    context.startActivity(it)
                                }
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Perfil") },
                            onClick = {
                                Intent(context, ProfileActivity::class.java).also {
                                    context.startActivity(it)
                                }
                            }
                        )

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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if(isEmailVerified == true) {
                        Intent(context, QrScannerActivity::class.java).also {
                            context.startActivity(it)
                        }
                    }else{
                        Toast.makeText(context, "Verifique seu e-mail para usar este recurso!", Toast.LENGTH_LONG)
                            .show()
                    }
                },
                modifier = Modifier.size(screenWidth*0.2f)
            ) {
                Image(painter = painterResource(R.drawable.qr_code_scanner),
                    contentDescription = "Ícone do scanner de QR Code",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(screenWidth*0.03f)
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(bottom = screenHeight * 0.015f)
        ) {
            Row(modifier = Modifier
                .padding(screenHeight * 0.02f)
                .clickable {
                    mostrarDialogo = true
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(painter = painterResource(R.drawable.filter_list),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )

                Spacer(modifier = Modifier.width(screenWidth*0.015f))

                Text("Categoria atual: $categoriaFiltrada")
            }

            if (mostrarDialogo){
                BasicAlertDialog(
                    modifier = Modifier
                        .padding(screenHeight*0.02f),
                    onDismissRequest = { mostrarDialogo = false }
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(screenHeight*0.02f)
                        ) {
                            Text("Selecione a categoria:")

                            Spacer(modifier = Modifier.height(screenHeight*0.01f))

                            if (isLoadingCategorias) {
                                Text("Carregando categorias...")
                            } else if (categoriasErro != null) {
                                Text(categoriasErro!!, color = Color.Red)
                            } else {
                                LazyColumn(
                                    modifier = Modifier.heightIn(max = screenHeight*0.4f)
                                ) {
                                    items(categorias) { categoria ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { filtroSelecionado = categoria }
                                                .padding(vertical = 4.dp)
                                        ) {
                                            RadioButton(
                                                selected = filtroSelecionado == categoria,
                                                onClick = { filtroSelecionado = categoria }
                                            )
                                            Text(
                                                text = categoria,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(screenHeight*0.01f))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { mostrarDialogo = false }) {
                                    Text("Cancelar")
                                }
                                Spacer(modifier = Modifier.width(screenWidth*0.01f))
                                TextButton(
                                    onClick = {
                                        categoriaFiltrada = filtroSelecionado
                                        mostrarDialogo = false
                                    }
                                ) {
                                    Text("Confirmar")
                                }
                            }
                        }
                    }
                }
            }

            val cardsFiltrados = if(categoriaFiltrada == "Todas"){
                senhas
            }else{
                senhas.filter { it.category == categoriaFiltrada }
            }

            when {
                isLoadingSenhas -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = screenHeight * 0.04f),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator()
                    }
                }
                senhasErro != null -> {
                    Text(
                        senhasErro!!,
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = screenHeight * 0.04f)
                    )
                }
                else -> {
                    if (senhas.isEmpty()) {
                        Text(
                            "Nenhuma senha cadastrada.",
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = screenHeight * 0.04f)
                        )
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        itemsIndexed(cardsFiltrados) { index, card ->
                            var cardMenuExpanded by remember { mutableStateOf(false) }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = screenHeight*0.01f)
                                    .padding(horizontal = screenHeight*0.02f)
                                    .align(alignment = Alignment.CenterHorizontally),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(screenWidth*0.03f)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = card.title,
                                            fontSize = (screenHeight*0.02f).value.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Box{
                                            IconButton(
                                                onClick = { cardMenuExpanded = true },
                                                modifier = Modifier
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "Mais opções"
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = cardMenuExpanded,
                                                onDismissRequest = { cardMenuExpanded = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Modificar senha") },
                                                    onClick = {
                                                        cardMenuExpanded = false
                                                        // Navegar para a tela de edição, passando o id do documento (card.id)
                                                        Intent(context, EditPasswordActivity::class.java).apply {
                                                            putExtra("passwordDocId", card.id)
                                                        }.also {
                                                            context.startActivity(it)
                                                        }
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Excluir senha", color = Color.Red) },
                                                    onClick = {
                                                        cardMenuExpanded = false
                                                        confirmarExclusaoIndex = index
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Text(text = "Categoria: ${card.category}")

                                    Spacer(modifier = Modifier.height(screenHeight*0.01f))

                                    if(card.description.isNotEmpty()){
                                        Text(text = card.description)
                                        Spacer(modifier = Modifier.height(screenHeight*0.01f))
                                    }

                                    if(card.login.isNotEmpty()){
                                        Text(text = "Login: ${card.login}")
                                        Spacer(modifier = Modifier.height(screenHeight*0.005f))
                                    }

                                    Text(text = "Senha: ${card.password}")
                                }
                            }

                            if (confirmarExclusaoIndex == index) {
                                AlertDialog(
                                    onDismissRequest = { confirmarExclusaoIndex = -1 },
                                    title = { Text("Excluir senha") },
                                    text = { Text("Tem certeza que deseja excluir essa senha?") },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                confirmarExclusaoIndex = -1
                                                // Excluir senha no Firestore
                                                db.collection("Users").document(uid)
                                                    .collection("Senhas")
                                                    .document(card.id)
                                                    .delete()
                                                    .addOnSuccessListener {
                                                        Toast.makeText(context, "Senha excluída com sucesso!", Toast.LENGTH_SHORT).show()
                                                        // Remover da lista localmente sem recarregar tudo
                                                        senhas = senhas.filter { it.id != card.id }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Toast.makeText(context, "Erro ao excluir senha: ${e.message}", Toast.LENGTH_LONG).show()
                                                    }
                                            }
                                        ) {
                                            Text("Excluir", color = Color.Red)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { confirmarExclusaoIndex = -1 }) {
                                            Text("Cancelar")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}