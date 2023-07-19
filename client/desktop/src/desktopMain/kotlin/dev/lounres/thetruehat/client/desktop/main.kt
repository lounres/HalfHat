package dev.lounres.thetruehat.client.desktop

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.lounres.thetruehat.client.common.CircleButtonWithIcon
import dev.lounres.thetruehat.client.common.CircleButtonWithImage
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource


//class ClientConnectionMaintainer(
//    val coroutineScope: CoroutineScope,
//    val client: HttpClient,
//    val host: String?,
//    val port: Int?,
//    val path: String?,
//    val retryPeriod: Long,
//) {
//    private val _incoming: Channel<ServerSignal> = Channel(capacity = Channel.UNLIMITED)
//    val incoming: ReceiveChannel<ServerSignal> = _incoming
//    private val _outgoing: Channel<ClientSignal> = Channel(capacity = Channel.UNLIMITED)
//    val outgoing: SendChannel<ClientSignal> = _outgoing
//
//    val connectionJob = coroutineScope.launch {
//        while (isActive) {
//            try {
//                println("1")
//                client.webSocket(host = host, port = port, path = path) {
//                    println("2")
//                    val incomingJob = launch {
//                        for (frame in incoming) {
//                            val converter = converter!!
//
//                            if (!converter.isApplicable(frame)) continue
//
//                            val signal = converter.deserialize<ServerSignal>(content = frame)
//                            _incoming.send(signal)
//                        }
//                    }
//                    println("3")
//                    launch {
//                        for (signal in _outgoing) sendSerialized<ClientSignal>(signal)
//                    }
//                    println("4")
//                    incomingJob.join()
//                    println("5")
//                }
//                println("6")
//            } catch (e: Exception) {
//                println("7")
//                println(e.localizedMessage)
//            }
//            println("8")
//            delay(retryPeriod)
//        }
//    }
//}
//
//val client = HttpClient {
//    WebSockets {
//        pingInterval = 1000
//        contentConverter = KotlinxWebsocketSerializationConverter(Json)
//    }
//}
//
//fun main(): Unit = runBlocking {
//    ClientConnectionMaintainer(
//        coroutineScope = this,
//        client = client,
//        host = "127.0.0.1",
//        port = 8080,
//        path = "/ws",
//        retryPeriod = 3000,
//    ).connectionJob.join()
//}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun Page(
    backButtonEnabled: Boolean = false,
    hatButtonEnabled: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (backButtonEnabled) {
                CircleButtonWithIcon(
                    modifier = Modifier.align(Alignment.TopStart),
                    icon = Icons.Default.KeyboardArrowLeft,
                ) { /* TODO */ }
            }
            if (hatButtonEnabled) {
                CircleButtonWithImage(
                    modifier = Modifier.align(Alignment.TopCenter),
                    image = painterResource("hat.png"),
                ) { /* TODO */ }
            }
            Row(modifier = Modifier.align(Alignment.TopEnd)) {
                var expanded by remember { mutableStateOf(false) }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                        },
                    ) { Text("Русский") }
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                        },
                    ) { Text("English") }
                }
                CircleButtonWithIcon(
                    icon = painterResource("icons/translate_black_x2_24dp.png"),
                ) {
                    expanded = !expanded
                }
                CircleButtonWithIcon(
                    icon = painterResource("icons/feedback_black_x2_24dp.png"),
                ) { /* TODO */ }
            }
        }
        content()
    }
}

@Preview
@Composable
fun Home() {
    Page(
        hatButtonEnabled = true,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(350.dp)
                .padding(vertical = 10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    shape = CircleShape,
                    onClick = { /* TODO */ },
                ) {
                    Text(
                        text = "Создать",
                        fontSize = 36.sp,
                    )
                }
                Button(
                    shape = CircleShape,
                    onClick = { /* TODO */ },
                ) {
                    Text(
                        text = "Войти",
                        fontSize = 36.sp,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NRFASectionHead(
   text: String,
   enabled: Boolean,
   onClick: () -> Unit,
) {
    var hovered by remember { mutableStateOf(false) }
    val highlighted by remember { derivedStateOf { enabled || hovered } }
    val color by remember { derivedStateOf { if (highlighted) Color(33, 164, 216) else Color(105, 105, 105) } }
    Text(
        text = text,
        fontSize = 22.sp,
        color = color,
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .drawBehind {
                drawLine(
                    color,
                    Offset(0f, size.height + 5f),
                    Offset(size.width, size.height + 5f),
                    2f
                )
            },
    )
}

@Preview
@Composable
fun NRFA() {
    Page {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(390.dp)
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                NRFASectionHead(
                    text = "Новости",
                    enabled = true,
                ) {

                }
                NRFASectionHead(
                    text = "Правила",
                    enabled = false,
                ) {

                }
                NRFASectionHead(
                    text = "FAQ",
                    enabled = false,
                ) {

                }
                NRFASectionHead(
                    text = "О нас",
                    enabled = false,
                ) {

                }
            }
            Spacer(Modifier.height(10.dp))
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 10.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

            }
        }
    }
}

@Preview
@Composable
fun RoomEnterPage() {
    Page(
        backButtonEnabled = true,
        hatButtonEnabled = true,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(350.dp)
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var roomId by remember { mutableStateOf("") }
            val roomIdTextStyle = TextStyle(fontSize = 38.sp, textAlign = TextAlign.Center)
            OutlinedTextField(
                placeholder = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text("КЛЮЧ ИГРЫ", style = roomIdTextStyle)
                    }
                },
                value = roomId,
                onValueChange = { roomId = it.uppercase() },
                textStyle = roomIdTextStyle,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Введённый выше ключ необходим для игры.",
                modifier = Modifier.padding(10.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(
                    shape = CircleShape,
                    onClick = {},
                ) {
                    Text("Вставить")
                }
                Button(
                    shape = CircleShape,
                    onClick = {},
                ) {
                    Text("Сгенерировать новый")
                }
            }
            Spacer(
                modifier = Modifier.height(30.dp)
            )
            var nickname by remember { mutableStateOf("") }
            OutlinedTextField(
                placeholder = {
                    Text("Введи своё имя")
                },
                value = nickname,
                onValueChange = { nickname = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Имя нужно, чтобы друзья могли тебя опознать.",
                modifier = Modifier.padding(10.dp),
            )
            Spacer(
                modifier = Modifier.height(10.dp)
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                onClick = {},
            ) {
                Text("Поехали!")
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
fun main() = application {
    Window(
        icon = painterResource("hat.png"),
        title = "TheTrueHat",
        onCloseRequest = ::exitApplication,
    ) {
        Home()
    }
    Window(
        icon = painterResource("hat.png"),
        title = "TheTrueHat",
        onCloseRequest = ::exitApplication,
    ) {
        NRFA()
    }
    Window(
        icon = painterResource("hat.png"),
        title = "TheTrueHat",
        onCloseRequest = ::exitApplication,
    ) {
        RoomEnterPage()
    }
}