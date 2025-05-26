package br.com.superid.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.com.superid.R
import br.com.superid.main.ui.theme.SuperIDTheme
import br.com.superid.user.EditCategoriesActivity
import br.com.superid.user.ProfileActivity
import br.com.superid.user.WelcomeActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import br.com.superid.main.utils.KeyStoreHelper
import br.com.superid.main.utils.Base64Utils
import br.com.superid.main.QrScannerActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                MainScreen(
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
fun MainScreen(modifier: Modifier = Modifier){
    val auth = Firebase.auth
    val db = Firebase.firestore
    val uid = auth.currentUser?.uid.orEmpty()

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val context = LocalContext.current

    var expanded by remember { mutableStateOf(false) }

    var categorias by remember { mutableStateOf(listOf<String>()) }
    var isLoadingCategorias by remember { mutableStateOf(false) }
    var categoriasErro by remember { mutableStateOf<String?>(null) }

    var mostrarDialogo by remember { mutableStateOf(false) }
    var filtroSelecionado by remember { mutableStateOf("") }
    var categoriaFiltrada by remember { mutableStateOf("Todas") }

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

    LaunchedEffect(uid) {
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
                .orderBy("dataCriacao")
                .get()
                .addOnSuccessListener { result ->
                    val listaSenhas = result.map { doc ->
                        val senhaCriptografadaBase64 = doc.getString("senha") ?: ""
                        val senhaDescriptografada = if(senhaCriptografadaBase64.isNotBlank())
                        try {
                            val senhaCriptografadaBytes = Base64Utils.decodeFromBase64(senhaCriptografadaBase64)
                            val senhaBytes = KeyStoreHelper.decryptData(senhaCriptografadaBytes)
                            String(senhaBytes, Charsets.UTF_8)
                        } catch (e: Exception){
                            "[Falha ao descriptografar]"
                        }
                        else{
                            ""
                        }

                        val loginCriptografadoBase64 = doc.getString("login") ?: ""
                        val loginDescriptografado = if (loginCriptografadoBase64.isNotBlank()) {
                            try {
                                val loginCriptografadoBytes = Base64Utils.decodeFromBase64(loginCriptografadoBase64)
                                val loginBytes = KeyStoreHelper.decryptData(loginCriptografadoBytes)
                                String(loginBytes, Charsets.UTF_8)
                            } catch (e: Exception) {
                                "[Falha ao descriptografar]"
                            }
                        } else {
                            ""
                        }

                        SenhaCard(
                            id = doc.id,
                            title = doc.getString("nome") ?: "",
                            description= doc.getString("descricao") ?: "",
                            login= loginDescriptografado,
                            password= senhaDescriptografada,
                            category= doc.getString("categoria") ?: ""
                        )
                    }
                    senhas = listaSenhas
                    isLoadingSenhas = false
                }
                .addOnFailureListener { e ->
                    senhasErro = "Erro ao buscar senhas: ${e.localizedMessage}"
                    isLoadingSenhas = false
                    senhas = emptyList()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = Color.Black
                ),
                title = {
                    Image(painter = painterResource(R.drawable.superid_basic_logo),
                        contentDescription = "Logo do aplicativo",
                        colorFilter = ColorFilter.tint(Color.Black)
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
                            text = { Text("Ativar modo claro") },
                            onClick = {}
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Intent(context, QrScannerActivity::class.java).also {
                        context.startActivity(it)
                    }
                },
                modifier = Modifier
                    .size(screenWidth*0.2f)
            ) {
                Image(painter = painterResource(R.drawable.qr_code_scanner),
                    contentDescription = "Ícone do scanner de QR Code",
                    colorFilter = ColorFilter.tint(Color.Black),
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
                    colorFilter = ColorFilter.tint(Color.Black)
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
                                    modifier = Modifier.heightIn(max = 300.dp)
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
                    // Mostra indicador de carregamento
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

                                    //Spacer(modifier = Modifier.height(screenHeight*0.01f))

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


