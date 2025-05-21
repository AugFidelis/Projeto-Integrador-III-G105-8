package br.com.superid.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                MainScreenApp()
            }
        }
    }
}

@Preview(showBackground = true
//    , device = "spec:width=800dp,height=1280dp,dpi=240"
)
@Composable
fun MainScreenApp() {
    MainScreen(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier){
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val context = LocalContext.current

    var expanded by remember { mutableStateOf(false) }

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
            var categorias by remember { mutableStateOf(listOf("Todas", "Sites da web", "Aplicativos", "Teclados de acesso físico")) }

            var mostrarDialogo by remember { mutableStateOf(false) }
            var filtroSelecionado by remember { mutableStateOf("") }
            var categoriaFiltrada by remember { mutableStateOf(categorias.first()) }

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

                Text("Categoria atual")
            }

            //Diálogo de categorias (falta conectar com o firestore para mostrar as categorias)
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

                            LazyColumn(
                                modifier = Modifier
                                    .heightIn(max = 300.dp)
                            ) {
                                items(categorias){ categoria ->
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


            data class ExampleCard(
                val id: Int,
                val title: String,
                val description: String,
                val login: String,
                val password: String,
                val category: String
            )

            //Cards de exemplo temporários, enquanto o firestore não é conectado
            val exampleCards = listOf(
                ExampleCard(
                    id = 1,
                    title = "Exemplo 1",
                    description = "",
                    login = "email1@email.com",
                    password = "senha123",
                    category = "Sites da web"
                ),
                ExampleCard(
                    id = 2,
                    title = "Exemplo 2",
                    description = "Descrição de tamanho normal",
                    login = "email2@email.com",
                    password = "senha456",
                    category = "Aplicativos"
                ),
                ExampleCard(
                    id = 3,
                    title = "Exemplo 3",
                    description = "Descrição muito grande! Descrição muito grande! Descrição muito grande! " +
                            "Descrição muito grande! Descrição muito grande! Descrição muito grande! " +
                            "Descrição muito grande! Descrição muito grande! " +
                            "Descrição muito grande! Descrição muito grande! ",
                    login = "email1@email.com",
                    password = "senha789",
                    category = "Teclados de acesso físico"
                ),
                ExampleCard(
                    id = 3,
                    title = "Exemplo 3",
                    description = "Descrição muito grande! Descrição muito grande! Descrição muito grande! " +
                            "Descrição muito grande! Descrição muito grande! Descrição muito grande! " +
                            "Descrição muito grande! Descrição muito grande! " +
                            "Descrição muito grande! Descrição muito grande! ",
                    login = "email1@email.com",
                    password = "senha789",
                    category = "Sites da web"
                ),
                ExampleCard(
                    id = 3,
                    title = "Exemplo 3",
                    description = "Descrição muito grande! Descrição muito grande! Descrição muito grande! " +
                            "Descrição muito grande! Descrição muito grande! Descrição muito grande! " +
                            "Descrição muito grande! Descrição muito grande! " +
                            "Descrição muito grande! Descrição muito grande! ",
                    login = "email1@email.com",
                    password = "senha789",
                    category = "Teclados de acesso físico"
                ),
            )

            val cardsFiltrados = if(categoriaFiltrada == "Todas"){
                exampleCards
            }else{
                exampleCards.filter { it.category == categoriaFiltrada }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items(cardsFiltrados){ card ->
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
                            Text(text = card.title,
                                fontSize = (screenHeight*0.02f).value.sp,
                                fontWeight = FontWeight.SemiBold
                                )

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
                }
            }


        }
    }
}
