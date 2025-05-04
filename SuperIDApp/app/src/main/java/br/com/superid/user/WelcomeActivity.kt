package br.com.superid.user

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.R
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
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 40.dp, top = 90.dp)
        ) {
        Image(painter = painterResource(id = R.drawable.superid_logo),
            contentDescription = "Logo SuperID",
            colorFilter = ColorFilter.tint(Color.Black),
            //colorFilter temporário, enquanto fundo escuro não é adicionado
            //modifier = Modifier.padding(top = 20.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        val lazyListState = rememberLazyListState()
        val snappingBehavior = rememberSnapFlingBehavior(lazyListState)

        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
//                .padding(horizontal = 10.dp),
            contentPadding = PaddingValues(horizontal = 40.dp),
            state = lazyListState,
            flingBehavior = snappingBehavior

        ) {
            item {
                Card(modifier = Modifier
                    //.padding(horizontal = 10.dp)
                    //.fillMaxWidth()
                    .height(400.dp)
                    .width(320.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(30.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        Image(painter = painterResource(R.drawable.welcomelock),
                            contentDescription = "Ícone de cadeado",
                            colorFilter = ColorFilter.tint(Color.Black)
                        )

                        Spacer(modifier = Modifier.weight(2f))

                        Text(textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            text = "Com o SuperID, você armazena todas " +
                                    "as suas senhas em um só lugar, de forma " +
                                    "segura e acessível. Não se preocupe mais em " +
                                    "lembrar dezenas de códigos diferentes.")

                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                Card(modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .height(400.dp)
                    .width(320.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(30.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        Image(painter = painterResource(R.drawable.welcomeqr),
                            contentDescription = "Ícone de QR Code",
                            colorFilter = ColorFilter.tint(Color.Black)
                        )

                        Spacer(modifier = Modifier.weight(2f))

                        Text(textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            text = "Cansado de digitar senhas? Com o SuperID, você pode " +
                                    "acessar sites parceiros usando apenas o seu celular " +
                                    "e um QR Code. É rápido, seguro e sem complicações.")

                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                Card(modifier = Modifier
//                    .padding(horizontal = 20.dp)
                    .height(400.dp)
                    .width(320.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(30.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        Image(
                            painter = painterResource(R.drawable.welcomeopenfolder),
                            contentDescription = "Ícone de Pasta Aberta",
                            colorFilter = ColorFilter.tint(Color.Black)
                        )

                        Spacer(modifier = Modifier.weight(2f))

                        Text(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            text = "Agrupe suas senhas por categorias como sites, aplicativos ou " +
                                    "dispositivos físicos. Tudo organizado para você encontrar " +
                                    "o que precisa com facilidade."
                        )

                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }


        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(50.dp)
            ) {
                Text(text = "Realizar Cadastro",
                    fontSize = 20.sp
                    )
            }

            Button(onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(50.dp)
            ) {
                Text(text = "Fazer Login",
                    fontSize = 20.sp
                    )
            }
        }
    }
}
