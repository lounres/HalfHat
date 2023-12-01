package dev.lounres.thetruehat.client.desktop.ui

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource


var offset = 50

@Composable
fun windowState() = rememberWindowState(position = WindowPosition(offset.dp, offset.dp)).also { offset += 25 }

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ApplicationScope.TestWindow(
    title: String,
    content: @Composable FrameWindowScope.() -> Unit
) {
    var closed by remember { mutableStateOf(false) }
    if (!closed)
        Window(
            state = windowState(),
            icon = painterResource("hat.png"),
            title = title,
            onCloseRequest = { closed = true },
            content = content
        )
}

fun main() = application {
    TestWindow("HomePage") {
        HomePageUIPreview()
    }
    TestWindow("NRFAPage") {
        NewsRulesFaqAboutPageUIPreview()
    }
    TestWindow("RoomEnterPage") {
        RoomEnterPageUIPreview()
    }
    TestWindow("RoomPage 1") {
        RoomPageUIPreview1()
    }
    TestWindow("RoomPage 2") {
        RoomPageUIPreview2()
    }
//    TestWindow("RoomSettingsPage") {
//        RoomSettingsPagePreview()
//    }
    TestWindow("GamePage") {
        GamePageUIPreview()
    }
    TestWindow("RoundEditGamePage 1") {
        RoundBreakPageUIPreview1()
    }
    TestWindow("RoundEditGamePage 2") {
        RoundBreakPageUIPreview2()
    }
    TestWindow("RoundEditGamePage 3") {
        RoundBreakPageUIPreview3()
    }
}