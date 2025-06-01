package br.com.superid.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.com.superid.main.MainActivity
import br.com.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditCategoriesActivity : ComponentActivity() {
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
                EditCategoriesScreen(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoriesScreen(
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
){
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val context = LocalContext.current

    var expanded by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var addingCategory by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Text("Editar categorias")
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
                        showDialog = true
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
    ) { innerPadding ->
        val auth = Firebase.auth
        val db = Firebase.firestore
        val uid = auth.currentUser?.uid ?: ""

        data class Categoria(val id: String, val nome: String)

        var categories by remember { mutableStateOf<List<Categoria>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        //Atualiza a lista de categorias
        fun fetchCategories(){
            db.collection("Users")
                .document(uid)
                .collection("Categorias")
                .orderBy("DataCriacao")
                .get()
                .addOnSuccessListener { result ->
                    categories = result.map { doc ->
                        Categoria(doc.id, doc.getString("Nome") ?: "")
                    }
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    Log.e("CATEGORIAS", "Erro ao buscar categorias", e)
                    errorMessage = "Erro ao buscar categorias: ${e.localizedMessage}"
                    isLoading = false
                }
        }

        LaunchedEffect(uid) {
            if (uid.isNotBlank()) {
                fetchCategories()
            }
        }

        //Diálogo de adição de categoria
        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    if (!addingCategory) showDialog = false
                },
                title = { Text("Nova categoria") },
                text = {
                    Column {
                        Text("Digite o nome da nova categoria:")
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = { Text("Nome da categoria") },
                            enabled = !addingCategory,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = screenHeight * 0.01f)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Salvar no Firebase
                            if (uid.isNotBlank() && newCategoryName.isNotBlank()) {
                                addingCategory = true
                                val categoriasRef = db.collection("Users").document(uid).collection("Categorias")
                                categoriasRef.document().set(
                                    mapOf(
                                        "Nome" to newCategoryName,
                                        "DataCriacao" to com.google.firebase.Timestamp.now()
                                    )
                                )
                                    .addOnSuccessListener {
                                        showDialog = false
                                        newCategoryName = ""
                                        addingCategory = false

                                        fetchCategories()
                                    }
                                    .addOnFailureListener { e ->
                                        errorMessage = "Erro ao adicionar categoria: ${e.localizedMessage}"
                                        addingCategory = false
                                    }
                            }
                        },
                        enabled = newCategoryName.isNotBlank() && !addingCategory,
                        modifier = Modifier
                            .padding(end = screenWidth * 0.01f)
                    ) {
                        Text("Adicionar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            if (!addingCategory) {
                                showDialog = false
                                newCategoryName = ""
                            }
                        },
                        enabled = !addingCategory
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        //Diálogo de edição de categoria
        var showEditDialog by remember { mutableStateOf(false) }
        var editCategoryOldName by remember { mutableStateOf("") }
        var editCategoryId by remember { mutableStateOf<String?>(null) }
        var editCategoryNewName by remember { mutableStateOf("") }
        var editingCategory by remember { mutableStateOf(false) }

        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { if (!editingCategory) showEditDialog = false },
                title = { Text("Editar categoria") },
                text = {
                    Column {
                        Text("Novo nome para a categoria:")
                        OutlinedTextField(
                            value = editCategoryNewName,
                            onValueChange = { editCategoryNewName = it },
                            label = { Text("Nome da categoria") },
                            enabled = !editingCategory,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = screenHeight * 0.01f)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val categoryId = editCategoryId
                            if (uid.isNotBlank() && categoryId != null && editCategoryNewName.isNotBlank()) {
                                editingCategory = true
                                val categoriasRef = db.collection("Users").document(uid).collection("Categorias")
                                categoriasRef.document(categoryId)
                                    .update("Nome", editCategoryNewName)
                                    .addOnSuccessListener {
                                        showEditDialog = false
                                        editCategoryOldName = ""
                                        editCategoryId = null
                                        editCategoryNewName = ""
                                        editingCategory = false
                                        fetchCategories()
                                    }
                                    .addOnFailureListener { e ->
                                        errorMessage = "Erro ao editar categoria: ${e.localizedMessage}"
                                        editingCategory = false
                                    }
                            }
                        },
                        enabled = editCategoryNewName.isNotBlank() && !editingCategory,
                        modifier = Modifier
                            .padding(end = screenWidth * 0.01f)
                    ) {
                        Text("Salvar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            if (!editingCategory) {
                                showEditDialog = false
                                editCategoryNewName = ""
                            }
                        },
                        enabled = !editingCategory
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        //Diálogo de excluir categoria
        var showDeleteDialog by remember { mutableStateOf(false) }
        var deleteCategoryId by remember { mutableStateOf<String?>(null) }
        var deleteCategoryName by remember { mutableStateOf("") }
        var deletingCategory by remember { mutableStateOf(false) }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { if (!deletingCategory) showDeleteDialog = false },
                title = { Text("Apagar categoria") },
                text = { Text("Tem certeza que deseja apagar a categoria \"$deleteCategoryName\"?") },
                confirmButton = {
                    Button(
                        onClick = {
                            val categoryId = deleteCategoryId
                            if (uid.isNotBlank() && categoryId != null) {
                                deletingCategory = true
                                val categoriasRef = db.collection("Users").document(uid).collection("Categorias")
                                categoriasRef.document(categoryId)
                                    .delete()
                                    .addOnSuccessListener {
                                        showDeleteDialog = false
                                        deleteCategoryId = null
                                        deleteCategoryName = ""
                                        deletingCategory = false
                                        fetchCategories()
                                    }
                                    .addOnFailureListener { e ->
                                        errorMessage = "Erro ao apagar categoria: ${e.localizedMessage}"
                                        deletingCategory = false
                                    }
                            }
                        },
                        enabled = !deletingCategory,
                        modifier = Modifier
                            .padding(end = screenWidth * 0.01f)
                    ) {
                        Text("Apagar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            if (!deletingCategory) {
                                showDeleteDialog = false
                                deleteCategoryId = null
                                deleteCategoryName = ""
                            }
                        },
                        enabled = !deletingCategory
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = screenHeight * 0.015f)
                .padding(horizontal = screenWidth*0.02f)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = screenHeight * 0.04f)
                )
            } else if (errorMessage != null) {
                Text(
                    errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = screenHeight * 0.04f)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(screenHeight*0.01f)
                ) {
                    items(categories){ categoria ->
                        var cardMenuExpanded by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = screenHeight*0.01f)
                                .height(screenHeight * 0.08f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = screenWidth * 0.03f, end = screenWidth * 0.01f, top = screenHeight * 0.008f, bottom = screenHeight * 0.008f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = categoria.nome,
                                    modifier = Modifier
                                        .padding(start = screenWidth * 0.01f, end = screenWidth * 0.01f)
                                        .weight(1f),
                                    style = MaterialTheme.typography.titleMedium
                                )

                                if (categoria.nome != "Sites da Web") {
                                    // Botão de opções
                                    Box {
                                        IconButton(onClick = { cardMenuExpanded = true }) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Mais opções"
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = cardMenuExpanded,
                                            onDismissRequest = { cardMenuExpanded = false },
                                            offset = DpOffset(x = 0.dp, y = 0.dp), // menu alinhado ao botão
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Editar nome") },
                                                onClick = {
                                                    editCategoryId = categoria.id
                                                    editCategoryOldName = categoria.nome
                                                    editCategoryNewName = categoria.nome
                                                    showEditDialog = true
                                                    cardMenuExpanded = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Apagar categoria") },
                                                onClick = {
                                                    deleteCategoryId = categoria.id
                                                    deleteCategoryName = categoria.nome
                                                    showDeleteDialog = true
                                                    cardMenuExpanded = false
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
        }
    }
}