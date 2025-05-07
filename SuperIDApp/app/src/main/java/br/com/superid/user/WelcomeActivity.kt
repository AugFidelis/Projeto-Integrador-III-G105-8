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
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import br.com.superid.user.ui.theme.SuperIDTheme
import kotlin.math.roundToInt

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                SuperIDApp()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SuperIDApp() {
    WelcomeScreen(modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(modifier: Modifier = Modifier) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    //Pega a altura e largura da tela em dp para incluir tamanhos consistentes com o tamanho da tela do dispositivo

    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(bottom = 40.dp, top = screenHeight*0.03f)
            .fillMaxWidth()
        ) {
        Image(painter = painterResource(id = R.drawable.superid_logo),
            contentDescription = "Logo SuperID",
            colorFilter = ColorFilter.tint(Color.Black),
            //colorFilter temporário, enquanto fundo escuro não é adicionado
            modifier = Modifier.size(screenHeight*0.25f)
        )

        Spacer(modifier = Modifier.weight(0.5f))

        //---------------------------------------------------------------------------------

        val lazyListState = rememberLazyListState()
        val snappingBehavior = rememberSnapFlingBehavior(lazyListState)

        data class WelcomeCard(
            val id: Int,
            val imageResource: Int,
            val text: String,
            val imageDescription: String
        )

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
            items(welcomeCards) { card ->
                Card(modifier = Modifier
                    .padding(horizontal = screenWidth * 0.02f)
                    //.fillMaxWidth()
                    .width(screenWidth * 0.85f)
                    .heightIn(max = screenHeight * 0.45f)
                    .align(alignment = Alignment.CenterHorizontally)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(30.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        Image(painter = painterResource(card.imageResource),
                            contentDescription = card.imageDescription,
                            colorFilter = ColorFilter.tint(Color.Black),
                            modifier = Modifier.size(screenWidth*0.3f)
                        )

                        Spacer(modifier = Modifier.weight(2f))

                        Text(textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = (screenHeight*0.022f).value.sp,
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(onClick = {
                Intent(context, TermsOfUseActivity::class.java).also {
                    context.startActivity(it)
                }
            },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
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
                    .padding(horizontal = 20.dp)
                    .height(screenHeight*0.07f)
            ) {
                Text(text = "Fazer Login",
                    fontSize = (screenHeight*0.025f).value.sp
                    )
            }
        }
    }
}
