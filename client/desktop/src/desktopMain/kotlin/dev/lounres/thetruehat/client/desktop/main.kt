package dev.lounres.thetruehat.client.desktop

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.thetruehat.client.common.ui.CircleButtonWithIcon
import dev.lounres.thetruehat.client.common.ui.CircleButtonWithImage
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

enum class Language {
    Russian, English
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TTHPage(
    backButtonEnabled: Boolean,
    onBackButtonClick: () -> Unit,
    onLanguageButtonClick: (language: Language) -> Unit,
    onFeedbackButtonClick: () -> Unit,
    pageHeader: @Composable RowScope.() -> Unit,
    pageContent: @Composable () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (backButtonEnabled) {
                CircleButtonWithIcon(
                    modifier = Modifier.align(Alignment.TopStart),
                    icon = Icons.Default.KeyboardArrowLeft,
                    onClick = onBackButtonClick,
                )
            }
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                pageHeader()
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
                            onLanguageButtonClick(Language.Russian)
                        },
                    ) { Text("Русский") }
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            onLanguageButtonClick(Language.English)
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
                    onClick = onFeedbackButtonClick,
                )
            }
        }
        pageContent()
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TTHPageWithHat(
    backButtonEnabled: Boolean,
    onBackButtonClick: () -> Unit,
    onLanguageButtonClick: (language: Language) -> Unit,
    onFeedbackButtonClick: () -> Unit,
    onHatButtonClick: () -> Unit,
    pageContent: @Composable () -> Unit,
) {
    TTHPage(
        backButtonEnabled = backButtonEnabled,
        onBackButtonClick = onBackButtonClick,
        onLanguageButtonClick = onLanguageButtonClick,
        onFeedbackButtonClick = onFeedbackButtonClick,
        pageHeader = {
            CircleButtonWithImage(
                image = painterResource("hat.png"),
                onClick = onHatButtonClick,
            )
        },
        pageContent,
    )
}

@Preview
@Composable
fun HomePagePreview() {
    HomePage(
        backButtonEnabled = true,
        onBackButtonClick = {},
        onLanguageButtonClick = {},
        onFeedbackButtonClick = {},
        onHatButtonClick = {},
        onCreateButtonClick = {},
        onEnterButtonClick = {},
    )
}

@Composable
fun HomePage(
    backButtonEnabled: Boolean,
    onBackButtonClick: () -> Unit,
    onLanguageButtonClick: (language: Language) -> Unit,
    onFeedbackButtonClick: () -> Unit,
    onHatButtonClick: () -> Unit,
    onCreateButtonClick: () -> Unit,
    onEnterButtonClick: () -> Unit,
) {
    TTHPageWithHat(
        backButtonEnabled = backButtonEnabled,
        onBackButtonClick = onBackButtonClick,
        onLanguageButtonClick = onLanguageButtonClick,
        onFeedbackButtonClick = onFeedbackButtonClick,
        onHatButtonClick = onHatButtonClick,
        pageContent = {
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
                        onClick = onCreateButtonClick,
                    ) {
                        Text(
                            text = "Создать",
                            fontSize = 36.sp,
                        )
                    }
                    Button(
                        shape = CircleShape,
                        onClick = onEnterButtonClick,
                    ) {
                        Text(
                            text = "Войти",
                            fontSize = 36.sp,
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun NRFASectionHead(
   text: String,
   enabled: Boolean,
   onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
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
            }
            .clickable(onClick = onClick)
            .hoverable(interactionSource),
    )
}

@Preview
@Composable
fun NRFAPagePreview() {
    NRFAPage(
        backButtonEnabled = true,
        onBackButtonClick = {},
        onLanguageButtonClick = {},
        onFeedbackButtonClick = {},
        onNewsButtonClick = {},
        onRulesButtonClick = {},
        onFaqButtonClick = {},
        onAboutButtonClick = {},
    )
}

@Composable
fun NRFAPage(
    backButtonEnabled: Boolean,
    onBackButtonClick: () -> Unit,
    onLanguageButtonClick: (language: Language) -> Unit,
    onFeedbackButtonClick: () -> Unit,
    onNewsButtonClick: () -> Unit,
    onRulesButtonClick: () -> Unit,
    onFaqButtonClick: () -> Unit,
    onAboutButtonClick: () -> Unit,
) {
    TTHPage(
        backButtonEnabled = backButtonEnabled,
        onBackButtonClick = onBackButtonClick,
        onLanguageButtonClick = onLanguageButtonClick,
        onFeedbackButtonClick = onFeedbackButtonClick,
        pageHeader = {
            NRFASectionHead(
                text = "Новости",
                enabled = true,
                onClick = onNewsButtonClick,
            )
            NRFASectionHead(
                text = "Правила",
                enabled = false,
                onClick = onRulesButtonClick,
            )
            NRFASectionHead(
                text = "FAQ",
                enabled = false,
                onClick = onFaqButtonClick,
            )
            NRFASectionHead(
                text = "О нас",
                enabled = false,
                onClick = onAboutButtonClick,
            )
        },
        pageContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(390.dp)
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
        },
    )
}

@Preview
@Composable
fun RoomEnterPagePreview() {
    RoomEnterPage(
        backButtonEnabled = true,
        onBackButtonClick = {},
        onLanguageButtonClick = {},
        onFeedbackButtonClick = {},
        onHatButtonClick = {},
    )
}

@Composable
fun RoomEnterPage(
    backButtonEnabled: Boolean,
    onBackButtonClick: () -> Unit,
    onLanguageButtonClick: (language: Language) -> Unit,
    onFeedbackButtonClick: () -> Unit,
    onHatButtonClick: () -> Unit,
) {
    TTHPageWithHat(
        backButtonEnabled = backButtonEnabled,
        onBackButtonClick = onBackButtonClick,
        onLanguageButtonClick = onLanguageButtonClick,
        onFeedbackButtonClick = onFeedbackButtonClick,
        onHatButtonClick = onHatButtonClick,
        pageContent = {
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
    )
}

@Preview
@Composable
fun RoomPagePreview1() {
    RoomPage(
        backButtonEnabled = true,
        roomId = "ЗЯНОКУЛЮ",
        userList = listOf("Panther", "Jaguar", "Tiger", "Lion"),
        playerIndex = 0,
        onBackButtonClick = {},
        onLanguageButtonClick = {},
        onFeedbackButtonClick = {},
        onHatButtonClick = {},
    )
}

@Preview
@Composable
fun RoomPagePreview2() {
    RoomPage(
        backButtonEnabled = true,
        roomId = "ЗЯНОКУЛЮ",
        userList = listOf("Panther", "Jaguar", "Tiger", "Lion"),
        playerIndex = 2,
        onBackButtonClick = {},
        onLanguageButtonClick = {},
        onFeedbackButtonClick = {},
        onHatButtonClick = {},
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun RoomPage(
    backButtonEnabled: Boolean,
    roomId: String,
    userList: List<String>,
    playerIndex: Int,
    onBackButtonClick: () -> Unit,
    onLanguageButtonClick: (language: Language) -> Unit,
    onFeedbackButtonClick: () -> Unit,
    onHatButtonClick: () -> Unit,
) {
    TTHPageWithHat(
        backButtonEnabled = backButtonEnabled,
        onBackButtonClick = onBackButtonClick,
        onLanguageButtonClick = onLanguageButtonClick,
        onFeedbackButtonClick = onFeedbackButtonClick,
        onHatButtonClick = onHatButtonClick,
        pageContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(350.dp)
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = roomId,
                        fontSize = 30.sp,
                    )
                    IconButton(
                        onClick = {  }
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { },
                        shape = CircleShape,
                    ) {
                        Text("Копир. ключ")
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Button(
                        onClick = { },
                        shape = CircleShape,
                    ) {
                        Text("Копир. ссылку")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Column {
                    Divider()
                    for ((index, user) in userList.withIndex()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                            if (index == 0)
                                Image(
                                    painterResource("hat.png"),
                                    contentDescription = null,
                                    modifier = Modifier.align(Alignment.CenterStart).size(20.dp)
                                )
                            Row(
                                modifier = Modifier.align(Alignment.Center),
                            ) {
                                Text(
                                    text = user,
                                )
                                if (index == playerIndex)
                                    Text(
                                        text = " (ты)",
                                        fontWeight = FontWeight.Bold
                                    )
                            }
                        }
                        Divider()
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                if (playerIndex == 0)
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CircleShape,
                        onClick = {},
                    ) {
                        Text(
                            "Начать игру",
                            fontSize = 25.sp,
                        )
                    }
                else
                    Text(
                        text = "Игра не началась",
                        fontSize = 25.sp,
                    )
            }
        }
    )
}

//interface RoomSettingsPage: TTHPage {
//    @Composable
//    override fun pageContent() {
//        Column(
//            modifier = Modifier
//                .fillMaxHeight()
//                .width(350.dp)
//                .padding(vertical = 10.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween,
//                modifier = Modifier.fillMaxWidth(),
//            ) {
//                Text(
//                    text = "Играть",
//                    fontSize = 20.sp,
//                )
//                Row {
//                    var expanded by remember { mutableStateOf(false) }
//                    DropdownMenu(
//                        expanded = expanded,
//                        onDismissRequest = { expanded = false }
//                    ) {
//                        DropdownMenuItem(
//                            onClick = { }
//                        ) {
//                            Text(
//                                text = "пока не кончатся слова",
//                            )
//                        }
//                        DropdownMenuItem(
//                            onClick = { }
//                        ) {
//                            Text(
//                                text = "заданное число кругов",
//                            )
//                        }
//                    }
//                    OutlinedButton(
//                        shape = CircleShape,
//                        onClick = { expanded = !expanded },
//                    ) {
//                        Text("пока не кончатся слова")
//                    }
//                }
//            }
//        }
//    }
//}

@Preview
@Composable
fun GamePagePreview() {
    GamePage(
        backButtonEnabled = true,
        wordsNumber = 100,
        showFinishButton = true,
        volumeOn = true,
        speakerNickname = "Panther",
        listenerNickname = "Jaguar",
        onBackButtonClick = {},
        onLanguageButtonClick = {},
        onFeedbackButtonClick = {},
        onHatButtonClick = {},
        onVolumeButtonClick = {},
        onFinishButtonClick = {},
        onExitButtonClick = {},
        header = {},
        footer = {},
    )
}


@OptIn(ExperimentalResourceApi::class)
@Composable
fun GamePage(
    backButtonEnabled: Boolean,
    wordsNumber: Int,
    showFinishButton: Boolean,
    volumeOn: Boolean,
    speakerNickname: String,
    listenerNickname: String,
    onBackButtonClick: () -> Unit,
    onLanguageButtonClick: (language: Language) -> Unit,
    onFeedbackButtonClick: () -> Unit,
    onHatButtonClick: () -> Unit,
    onVolumeButtonClick: () -> Unit,
    onFinishButtonClick: () -> Unit,
    onExitButtonClick: () -> Unit,
    header: @Composable ColumnScope.() -> Unit,
    footer: @Composable ColumnScope.() -> Unit,
) {
    TTHPageWithHat(
        backButtonEnabled = backButtonEnabled,
        onBackButtonClick = onBackButtonClick,
        onLanguageButtonClick = onLanguageButtonClick,
        onFeedbackButtonClick = onFeedbackButtonClick,
        onHatButtonClick = onHatButtonClick,
        pageContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(350.dp)
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$wordsNumber слов",
                    fontSize = 30.sp,
                )
                Row {
                    CircleButtonWithIcon(
                        icon = painterResource("icons/exit_black_x1_24dp.png"),
                        onClick = onExitButtonClick,
                    )
                    CircleButtonWithIcon(
                        icon = painterResource(if (volumeOn) "icons/volume_on_black_x1_24dp.png" else "icons/volume_off_black_x1_24dp.png"),
                        onClick = onVolumeButtonClick
                    )
                    if (showFinishButton)
                        CircleButtonWithIcon(
                            icon = painterResource("icons/finish_black_x1_24dp.png"),
                            onClick = onFinishButtonClick,
                        )
                }
                Spacer(modifier = Modifier.height(10.dp))
                header()
                Spacer(modifier = Modifier.height(30.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = speakerNickname,
                        fontSize = 25.sp,
                        color = Color(15, 170, 74),
                    )
                    Text(
                        text = "объясняет",
                        fontSize = 17.sp,
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "отгадывает",
                        fontSize = 17.sp,
                    )
                    Text(
                        text = listenerNickname,
                        fontSize = 25.sp,
                        color = Color(0, 140, 255),
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
                footer()
            }
        }
    )
}

sealed interface RoundBreakUserRole {
    data object SpeakerWaiting: RoundBreakUserRole
    data object ListenerWaiting: RoundBreakUserRole
    data object SpeakerReady: RoundBreakUserRole
    data object ListenerReady: RoundBreakUserRole
    data class SpeakerIn(val rounds: UInt): RoundBreakUserRole
    data class ListenerIn(val rounds: UInt): RoundBreakUserRole
}

@Preview
@Composable
fun RoundBreakPagePreview1() {
    RoundBreakPage(
        backButtonEnabled = true,
        wordsNumber = 100,
        showFinishButton = true,
        volumeOn = true,
        userRole = RoundBreakUserRole.SpeakerReady,
        speakerNickname = "Panther",
        listenerNickname = "Jaguar",
        onBackButtonClick = {},
        onHatButtonClick = {},
        onLanguageButtonClick = {},
        onFeedbackButtonClick = {},
        onExitButtonClick = {},
        onVolumeButtonClick = {},
        onFinishButtonClick = {},
    )
}

@Preview
@Composable
fun RoundBreakPagePreview2() {
    RoundBreakPage(
        backButtonEnabled = true,
        wordsNumber = 100,
        showFinishButton = true,
        volumeOn = false,
        userRole = RoundBreakUserRole.SpeakerWaiting,
        speakerNickname = "Panther",
        listenerNickname = "Jaguar",
        onBackButtonClick = {},
        onHatButtonClick = {},
        onLanguageButtonClick = {},
        onFeedbackButtonClick = {},
        onExitButtonClick = {},
        onVolumeButtonClick = {},
        onFinishButtonClick = {},
    )
}

@Preview
@Composable
fun RoundBreakPagePreview3() {
    RoundBreakPage(
        backButtonEnabled = true,
        wordsNumber = 100,
        showFinishButton = true,
        volumeOn = true,
        userRole = RoundBreakUserRole.SpeakerIn(3u),
        speakerNickname = "Panther",
        listenerNickname = "Jaguar",
        onBackButtonClick = {},
        onHatButtonClick = {},
        onLanguageButtonClick = {},
        onFeedbackButtonClick = {},
        onExitButtonClick = {},
        onVolumeButtonClick = {},
        onFinishButtonClick = {},
    )
}

@Composable
fun RoundBreakPage(
    backButtonEnabled: Boolean,
    wordsNumber: Int,
    showFinishButton: Boolean,
    volumeOn: Boolean,
    speakerNickname: String,
    listenerNickname: String,
    userRole: RoundBreakUserRole,
    onBackButtonClick: () -> Unit,
    onLanguageButtonClick: (language: Language) -> Unit,
    onFeedbackButtonClick: () -> Unit,
    onHatButtonClick: () -> Unit,
    onVolumeButtonClick: () -> Unit,
    onFinishButtonClick: () -> Unit,
    onExitButtonClick: () -> Unit,
) {
    GamePage(
        backButtonEnabled = backButtonEnabled,
        wordsNumber = wordsNumber,
        showFinishButton = showFinishButton,
        volumeOn = volumeOn,
        speakerNickname = speakerNickname,
        listenerNickname = listenerNickname,
        onBackButtonClick = onBackButtonClick,
        onLanguageButtonClick = onLanguageButtonClick,
        onFeedbackButtonClick = onFeedbackButtonClick,
        onHatButtonClick = onHatButtonClick,
        onVolumeButtonClick = onVolumeButtonClick,
        onFinishButtonClick = onFinishButtonClick,
        onExitButtonClick = onExitButtonClick,
        header = {
            when (userRole) {
                RoundBreakUserRole.SpeakerWaiting, RoundBreakUserRole.SpeakerReady -> {
                    Text(
                        text = "Ты объясняешь",
                        fontSize = 30.sp,
                    )
                }
                RoundBreakUserRole.ListenerWaiting, RoundBreakUserRole.ListenerReady -> {
                    Text(
                        text = "Ты отгадываешь",
                        fontSize = 30.sp,
                    )
                }
                is RoundBreakUserRole.SpeakerIn -> {
                    Text(
                        text = "Подготовка",
                        fontSize = 30.sp,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Ты объясняешь через ${userRole.rounds} ходов",
                        fontSize = 18.sp,
                        color = Color.Gray,
                    )
                }
                is RoundBreakUserRole.ListenerIn -> {
                    Text(
                        text = "Подготовка",
                        fontSize = 30.sp,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Ты отгадываешь через ${userRole.rounds} ходов",
                        fontSize = 18.sp,
                        color = Color.Gray,
                    )
                }
            }
        },
        footer = {
            when(userRole) {
                RoundBreakUserRole.SpeakerWaiting, RoundBreakUserRole.ListenerWaiting, RoundBreakUserRole.SpeakerReady, RoundBreakUserRole.ListenerReady ->
                    Button(
                        enabled = userRole == RoundBreakUserRole.SpeakerWaiting || userRole == RoundBreakUserRole.ListenerWaiting,
                        modifier = Modifier.fillMaxWidth(),
                        shape = CircleShape,
                        onClick = {},
                    ) {
                        Text(
                            when (userRole) {
                                RoundBreakUserRole.SpeakerWaiting -> "Я готов объяснять"
                                RoundBreakUserRole.ListenerWaiting -> "Я готов отгадывать"
                                RoundBreakUserRole.SpeakerReady, RoundBreakUserRole.ListenerReady -> "Подожди напарника"
                                else -> error("There are forgotten cases in button definition on GamePage")
                            },
                            fontSize = 20.sp,
                        )
                    }
                else -> {}
            }
        }
    )
}

//sealed interface RoundUserRole {
//    data object Speaker: RoundUserRole
//    data object Listener: RoundUserRole
//    data class SpeakerIn(val rounds: UInt): RoundUserRole
//    data class ListenerIn(val rounds: UInt): RoundUserRole
//}
//
//@Composable
//fun RoundPage(
//    backButtonEnabled: Boolean,
//    wordsNumber: Int,
//    showFinishButton: Boolean,
//    volumeOn: Boolean,
//    speakerNickname: String,
//    listenerNickname: String,
//    userRole: RoundUserRole,
//    onBackButtonClick: () -> Unit,
//    onLanguageButtonClick: (language: Language) -> Unit,
//    onFeedbackButtonClick: () -> Unit,
//    onHatButtonClick: () -> Unit,
//    onVolumeButtonClick: () -> Unit,
//    onFinishButtonClick: () -> Unit,
//    onExitButtonClick: () -> Unit,
//) {
//    GamePage(
//        backButtonEnabled = backButtonEnabled,
//        wordsNumber = wordsNumber,
//        showFinishButton = showFinishButton,
//        volumeOn = volumeOn,
//        speakerNickname = speakerNickname,
//        listenerNickname = listenerNickname,
//        onBackButtonClick = onBackButtonClick,
//        onLanguageButtonClick = onLanguageButtonClick,
//        onFeedbackButtonClick = onFeedbackButtonClick,
//        onHatButtonClick = onHatButtonClick,
//        onVolumeButtonClick = onVolumeButtonClick,
//        onFinishButtonClick = onFinishButtonClick,
//        onExitButtonClick = onExitButtonClick,
//        header = {
//            when (userRole) {
//                RoundUserRole.Speaker -> {
//                    Text(
//                        text = "Ты объясняешь",
//                    )
//                }
//            }
//        },
//        footer = {
//            when(userRole) {
//                RoundBreakUserRole.SpeakerWaiting, RoundBreakUserRole.ListenerWaiting, RoundBreakUserRole.SpeakerReady, RoundBreakUserRole.ListenerReady ->
//                    Button(
//                        enabled = userRole == RoundBreakUserRole.SpeakerWaiting || userRole == RoundBreakUserRole.ListenerWaiting,
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = CircleShape,
//                        onClick = {},
//                    ) {
//                        Text(
//                            when (userRole) {
//                                RoundBreakUserRole.SpeakerWaiting -> "Я готов объяснять"
//                                RoundBreakUserRole.ListenerWaiting -> "Я готов отгадывать"
//                                RoundBreakUserRole.SpeakerReady, RoundBreakUserRole.ListenerReady -> "Подожди напарника"
//                                else -> error("There are forgotten cases in button definition on GamePage")
//                            },
//                            fontSize = 20.sp,
//                        )
//                    }
//                else -> {}
//            }
//        }
//    )
//}

//var offset = 100
//
//@Composable
//fun windowState() = rememberWindowState(position = WindowPosition(offset.dp, offset.dp)).also { offset += 50 }
//
//@OptIn(ExperimentalResourceApi::class)
//@Composable
//fun ApplicationScope.TestWindow(
//    title: String,
//    content: @Composable FrameWindowScope.() -> Unit
//) {
//    var closed by remember { mutableStateOf(false) }
//    if (!closed)
//        Window(
//            state = windowState(),
//            icon = painterResource("hat.png"),
//            title = title,
//            onCloseRequest = { closed = true },
//            content = content
//        )
//}
//
//fun main() = application {
//    TestWindow("HomePage") {
//        HomePagePreview()
//    }
//    TestWindow("NRFAPage") {
//        NRFAPagePreview()
//    }
//    TestWindow("RoomEnterPage") {
//        RoomEnterPagePreview()
//    }
//    TestWindow("RoomPage 1") {
//        RoomPagePreview1()
//    }
//    TestWindow("RoomPage 2") {
//        RoomPagePreview2()
//    }
////    TestWindow("RoomSettingsPage") {
////        RoomSettingsPagePreview()
////    }
//    TestWindow("GamePage") {
//        GamePagePreview()
//    }
//    TestWindow("RoundEditGamePage 1") {
//        RoundBreakPagePreview1()
//    }
//    TestWindow("RoundEditGamePage 2") {
//        RoundBreakPagePreview2()
//    }
//    TestWindow("RoundEditGamePage 3") {
//        RoundBreakPagePreview3()
//    }
//}