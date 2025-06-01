package br.com.superid.user

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.R
import br.com.superid.auth.LoginActivity
import br.com.superid.ui.theme.SuperIDTheme
import kotlin.math.roundToInt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.saveable.rememberSaveable
import android.content.Context
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Activity de boas-vindas do aplicativo SuperID.
 *
 * Funcionalidades principais:
 * - Apresenta um carrossel com cards explicativos sobre as funcionalidades do app
 * - Oferece opções de cadastro e login
 * - Permite alternar entre tema claro e escuro
 * - Layout responsivo que se adapta a diferentes tamanhos de tela
 */
class WelcomeActivity : ComponentActivity() {
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
                WelcomeScreen(modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(),

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
 * Tela de boas-vindas do aplicativo.
 *
 * @param onToggleTheme Callback para alternar entre tema claro/escuro
 * @param isDarkTheme Boolean que indica se o tema escuro está ativo
 * @param modifier Modifier para personalizar o layout
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    // Dimensões responsivas
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    //Pega a altura e largura da tela em dp para incluir tamanhos consistentes com o tamanho da tela do dispositivo

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    var expanded by remember { mutableStateOf(false) }

                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Ícone do menu"
                        )

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
                }
            )
        }
    ){ innerPadding ->
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(innerPadding)
                .padding(bottom = screenHeight*0.015f)
                .fillMaxWidth()
        ) {
            Image(painter = painterResource(id = R.drawable.superid_logo),
                contentDescription = "Logo SuperID",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier.size(screenHeight*0.25f)
            )

            Spacer(modifier = Modifier.height(screenHeight*0.01f))

            //---------------------------------------------------------------------------------

            val lazyListState = rememberLazyListState()
            val snappingBehavior = rememberSnapFlingBehavior(lazyListState)

            // Data class para representar cada card de boas-vindas
            data class WelcomeCard(
                val id: Int,
                val imageResource: Int,
                val text: String,
                val imageDescription: String
            )

            // Lista de cards explicativos
            val welcomeCards = listOf(
                WelcomeCard(
                    id = 1,
                    imageResource = R.drawable.welcomelock,
                    text = "Com o SuperID, você armazena todas " +
                            "as suas senhas em um só lugar, de forma " +
                            "segura e acessível. Não se preocupe mais em " +
                            "lembrar dezenas de códigos diferentes.",
                    imageDescription = "Ícone de cadeado"
                ),
                WelcomeCard(
                    id = 2,
                    imageResource = R.drawable.welcomeqr,
                    text = "Cansado de digitar senhas? Com o SuperID, você pode " +
                            "acessar sites parceiros usando apenas o seu celular " +
                            "e um QR Code. É rápido, seguro e sem complicações.",
                    imageDescription = "Ícone de QR Code"
                ),
                WelcomeCard(
                    id = 3,
                    imageResource = R.drawable.welcomeopenfolder,
                    text = "Agrupe suas senhas por categorias como sites, aplicativos ou " +
                            "dispositivos físicos. Tudo organizado para você encontrar " +
                            "o que precisa com facilidade.",
                    imageDescription = "Ícone de Pasta Aberta"
                )
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = screenWidth * 0.055f),
                state = lazyListState,
                flingBehavior = snappingBehavior,
                horizontalArrangement = Arrangement.Center

            ) {
                // Itera sobre cada card de boas-vindas
                items(welcomeCards) { card ->
                    Card(modifier = Modifier
                        .padding(horizontal = screenWidth * 0.02f)
                        .width(screenWidth * 0.85f)
                        .heightIn(max = screenHeight * 0.45f)
                        .align(alignment = Alignment.CenterHorizontally)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(screenWidth*0.075f)
                        ) {
                            Spacer(modifier = Modifier.weight(1f))

                            Image(painter = painterResource(card.imageResource),
                                contentDescription = card.imageDescription,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                                modifier = Modifier.size(screenWidth*0.25f)
                            )

                            Spacer(modifier = Modifier.weight(2f))

                            Text(textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = (screenHeight*0.02f).value.sp,
                                text = card.text,
                                lineHeight = (screenHeight * 0.03f).value.sp
                            )

                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

            }

            Spacer(modifier = Modifier.weight(1f))

            val context = LocalContext.current

            Column(
                verticalArrangement = Arrangement.spacedBy(screenHeight*0.01f)
            ) {
                Button(onClick = {
                    Intent(context, TermsOfUseActivity::class.java).also {
                        context.startActivity(it)
                    }
                },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = screenWidth*0.05f)
                        .height(screenHeight*0.07f)
                ) {
                    Text(text = "Realizar Cadastro",
                        fontSize = (screenHeight*0.025f).value.sp
                    )
                }

                Button(onClick = {
                    Intent(context, LoginActivity::class.java).also {
                        context.startActivity(it)
                    }
                },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = screenWidth*0.05f)
                        .height(screenHeight*0.07f)
                ) {
                    Text(text = "Fazer Login",
                        fontSize = (screenHeight*0.025f).value.sp
                    )
                }
            }
        }
    }
}
