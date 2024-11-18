package dev.lounres.halfhat.client.desktop.ui.implementation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.lounres.halfhat.client.desktop.resources.*
import dev.lounres.halfhat.client.desktop.ui.components.MainWindowComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.about.AboutPageUI
import dev.lounres.halfhat.client.desktop.ui.implementation.faq.FAQPageUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.GamePageActionsUI
import dev.lounres.halfhat.client.desktop.ui.implementation.game.GamePageUI
import dev.lounres.halfhat.client.desktop.ui.implementation.home.HomePageUI
import dev.lounres.halfhat.client.desktop.ui.utils.WorkInProgress
import org.jetbrains.compose.resources.painterResource
import dev.lounres.halfhat.client.desktop.resources.Res as DesktopRes


val commonIconModifier = Modifier.height(24.dp)

sealed interface Page {
    val textName: String
    val icon: @Composable ((isSelected: Boolean) -> Unit)
    val badge: @Composable ((component: MainWindowComponent) -> Unit)
    val actions: @Composable (RowScope.(component: MainWindowComponent) -> Unit)
    val content: @Composable ((component: MainWindowComponent) -> Unit)

    enum class Primary(
        override val textName: String,
        override val icon: @Composable ((isSelected: Boolean) -> Unit),
        override val badge: @Composable ((MainWindowComponent) -> Unit) = {},
        override val actions: @Composable (RowScope.(component: MainWindowComponent) -> Unit) = {},
        override val content: @Composable ((component: MainWindowComponent) -> Unit),
    ) : Page {
        Home(
            textName = "Home",
            icon = { isSelected ->
                Icon(
                    painter = painterResource(DesktopRes.drawable.homePage_dark_png_24dp),
                    modifier = commonIconModifier,
                    contentDescription = "Home page",
                )
            },
            content = { HomePageUI(it.homePageComponent) },
        ),
        Game(
            textName = "Game",
            icon = { isSelected ->
                Icon(
                    painter = painterResource(DesktopRes.drawable.gamePage_dark_png_24dp),
                    modifier = commonIconModifier,
                    contentDescription = "Game page",
                )
            },
            actions = { GamePageActionsUI(it.gamePageComponent) },
            content = { GamePageUI(it.gamePageComponent) },
        ),
    }

    enum class Secondary(
        override val textName: String,
        override val icon: @Composable ((isSelected: Boolean) -> Unit),
        override val badge: @Composable ((MainWindowComponent) -> Unit) = {},
        override val actions: @Composable (RowScope.(component: MainWindowComponent) -> Unit) = {},
        override val content: @Composable ((MainWindowComponent) -> Unit),
    ) : Page {
        News(
            textName = "News",
            icon = { isSelected ->
                Icon(
                    painter = painterResource(DesktopRes.drawable.newsPage_dark_png_24dp),
                    modifier = commonIconModifier,
                    contentDescription = "News page",
                )
            },
            content = { WorkInProgress() },
        ),
        Rules(
            textName = "Rules",
            icon = { isSelected ->
                Icon(
                    painter = painterResource(DesktopRes.drawable.rulesPage_dark_png_24dp),
                    modifier = commonIconModifier,
                    contentDescription = "Rules page",
                )
            },
            content = { WorkInProgress() },
        ),
        FAQ(
            textName = "FAQ",
            icon = { isSelected ->
                Icon(
                    painter = painterResource(DesktopRes.drawable.faqPage_dark_png_24dp),
                    modifier = commonIconModifier,
                    contentDescription = "FAQ page",
                )
            },
            content = { FAQPageUI() },
        ),
        GameHistory(
            textName = "Game history",
            icon = { isSelected ->
                Icon(
                    painter = painterResource(DesktopRes.drawable.gameHistoryPage_dark_png_24dp),
                    modifier = commonIconModifier,
                    contentDescription = "Game history page",
                )
            },
            content = { WorkInProgress() },
        ),
        Settings(
            textName = "Settings",
            icon = { isSelected ->
                Icon(
                    painter = painterResource(DesktopRes.drawable.settingsPage_dark_png_24dp),
                    modifier = commonIconModifier,
                    contentDescription = "Settings page",
                )
            },
            content = { WorkInProgress() },
        ),
        Feedback(
            textName = "Feedback",
            icon = { isSelected ->
                Icon(
                    painter = painterResource(DesktopRes.drawable.feedbackPage_dark_png_24dp),
                    modifier = commonIconModifier,
                    contentDescription = "Feedback page",
                )
            },
            content = { WorkInProgress() },
        ),
        About(
            textName = "About",
            icon = { isSelected ->
                Icon(
                    painter = painterResource(DesktopRes.drawable.aboutPage_dark_png_24dp),
                    modifier = commonIconModifier,
                    contentDescription = "About page",
                )
            },
            content = { AboutPageUI() },
        ),
    }
}