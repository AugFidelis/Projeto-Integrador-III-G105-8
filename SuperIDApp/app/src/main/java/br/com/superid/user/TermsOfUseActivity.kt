package br.com.superid.user

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.superid.R
import br.com.superid.auth.SignUpActivity
import br.com.superid.user.ui.theme.SuperIDTheme

class TermsOfUseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                TermsOfUseApp()
            }
        }
    }
}

@Preview(showBackground = true
//    , device = "spec:width=800dp,height=1280dp,dpi=240"
)
@Composable
fun TermsOfUseApp() {
    TermsOfUseScreen(modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center)
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TermsOfUseScreen(modifier: Modifier = Modifier) {
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
                    Text("Termos de uso")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        Intent(context, WelcomeActivity::class.java).also{
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
        }
    ){ innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = screenHeight*0.015f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    //.padding(bottom = 40.dp, top = screenHeight*0.03f)
                    .fillMaxWidth()
                    //.padding(innerPadding)
                    .padding(bottom = screenHeight * 0.03f)
            ) {
                Text(
                    text = "Por favor, leia os termos de uso do aplicativo antes de continuar:",
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    fontSize = (screenHeight * 0.03f).value.sp,
                    modifier = Modifier.padding(screenHeight * 0.03f),
                    lineHeight = (screenHeight * 0.03f).value.sp
                )

                data class TermsCard(
                    val id: Int,
                    val title: String,
                    val description: String,
                    val bulletPoints: List<String>
                )

                val termsCards = listOf(
                    TermsCard(
                        id = 1,
                        title = "1. Objetivo do Aplicativo",
                        description = "O SuperID é um aplicativo desenvolvido com fins " +
                                "educacionais que tem como objetivo:",
                        bulletPoints = listOf(
                            "Armazenar e organizar senhas de forma segura;",
                            "Permitir o login sem senha em sites parceiros, utilizando autenticação por QR Code."
                        )
                    ),
                    TermsCard(
                        id = 2,
                        title = "2. Cadastro e Conta do Usuário",
                        description = "A senha mestre é de sua total responsabilidade. Em caso de esquecimento, será possível recuperá-la somente se o email estiver validado previamente.\n" +
                                "\n" +
                                "Para utilizar o SuperID, é necessário:",
                        bulletPoints = listOf(
                            "Fornecer seu nome, email válido e criar uma senha mestre;",
                            "Validar seu endereço de email;",
                            "Concordar com estes Termos de Uso."
                        )
                    ),
                    TermsCard(
                        id = 3,
                        title = "3. Segurança das Informações",
                        description = "Este aplicativo foi criado com fins educacionais e não " +
                                "segue os mais altos padrões de segurança da indústria.",
                        bulletPoints = listOf(
                            "As senhas cadastradas são criptografadas e armazenadas em um banco de dados seguro (Firebase Firestore).",
                            "O app gera tokens de acesso (accessToken) para cada senha, os quais são usados para autenticação em sites parceiros.",
                            "Nenhum dado é compartilhado com terceiros sem sua autorização."
                        )
                    ),
                    TermsCard(
                        id = 4,
                        title = "4. Uso do Login sem Senha",
                        description = "Ao usar o aplicativo, você entende que:",
                        bulletPoints = listOf(
                            "O login sem senha funciona por meio da leitura de um QR Code gerado por sites parceiros do SuperID.",
                            "Ao escanear o código, você autoriza o envio do seu identificador de usuário (UID) ao site que solicitou o login.",
                            "Cada token de login tem validade limitada e é automaticamente expirado após o tempo definido."
                        )
                    ),
                    TermsCard(
                        id = 5,
                        title = "5. Responsabilidades do Usuário",
                        description = "Ao usar o SuperID, você concorda em:",
                        bulletPoints = listOf(
                            "Utilizar o app apenas para fins legítimos e pessoais;",
                            "Manter sua senha mestre em sigilo;",
                            "Não tentar violar a segurança do aplicativo ou de qualquer site parceiro;",
                            "Informar qualquer uso não autorizado de sua conta."
                        )
                    ),
                    TermsCard(
                        id = 6,
                        title = "6. Limitação de Responsabilidade",
                        description = "Este projeto é um protótipo educacional, não sendo recomendado para uso comercial ou " +
                                "armazenamento de dados sensíveis reais. A equipe responsável pelo SuperID não se responsabiliza por: ",
                        bulletPoints = listOf(
                            "Eventuais perdas de dados; ",
                            "Falhas de segurança; ",
                            "Mau uso do aplicativo. "
                        )
                    ),
                    TermsCard(
                        id = 7,
                        title = "7. Modificações nos Termos",
                        description = "Os Termos de Uso podem ser alterados a " +
                                "qualquer momento para se adequar a melhorias ou mudanças no projeto. ",
                        bulletPoints = listOf(
                            "Sempre que isso acontecer, a nova versão será disponibilizada no aplicativo."
                        )
                    ),


                    )

                val lazyListState = rememberLazyListState()
                val snappingBehavior = rememberSnapFlingBehavior(lazyListState)

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = screenWidth * 0.055f),
                    state = lazyListState,
                    flingBehavior = snappingBehavior,
                    horizontalArrangement = Arrangement.Center
                ) {
                    items(termsCards) { card ->
                        Card(
                            modifier = Modifier
                                .padding(horizontal = screenWidth * 0.02f)
                                //.fillMaxWidth()
                                .width(screenWidth * 0.85f)
                                .height(screenHeight * 0.55f)
                                .align(alignment = Alignment.CenterHorizontally)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.Start,
                                modifier = Modifier.padding(30.dp)
                            ) {
                                //Spacer(modifier = Modifier.height(screenHeight*0.005f))

                                Text(
                                    text = card.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (screenHeight * 0.025f).value.sp
                                )

                                Spacer(modifier = Modifier.height(screenHeight * 0.03f))

                                Text(
                                    text = card.description,
                                    fontSize = (screenHeight * 0.0175f).value.sp
                                )

                                Spacer(modifier = Modifier.height(screenHeight * 0.025f))

                                Text(
                                    text = card.bulletPoints.joinToString(
                                        separator = "\n• ",
                                        prefix = "• "
                                    ),
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    fontSize = (screenHeight * 0.0155f).value.sp
                                )

                            }
                        }
                    }

                }
            }

//-----------------------------------------------------------------------------------------------------

                Spacer(modifier = Modifier.height(screenHeight * 0.04f))
                //Spacer(modifier = Modifier.weight(0.5f))

                var isAccepted by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier.align(Alignment.BottomCenter)
                ){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(screenHeight * 0.06f)
                            .selectable(
                                selected = isAccepted,
                                onClick = { isAccepted = !isAccepted },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = screenHeight * 0.03f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isAccepted,
                            onClick = null
                        )

                        Spacer(modifier = Modifier.width(screenWidth * 0.02f))

                        Text(
                            text = "Confirmo que li, entendi e concordo com as regras e termos acima.",
                            fontSize = (screenHeight * 0.015f).value.sp,
                            lineHeight = (screenHeight*0.0175f).value.sp
                        )
                    }

                    //Spacer(modifier = Modifier.weight(0.5f))
                    Spacer(modifier = Modifier.height(screenHeight * 0.01f))

                    Button(
                        onClick = {
                            Intent(context, SignUpActivity::class.java).also {
                                context.startActivity(it)
                            }
                        },
                        enabled = isAccepted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(screenHeight * 0.07f)
                    ) {
                        Text(
                            text = "Continuar",
                            fontSize = (screenHeight * 0.025f).value.sp
                        )
                    }
                }


            }
        }
    }

