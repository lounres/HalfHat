package dev.lounres.halfhat.client.ui.components

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.navigation.ChildrenVariants
import dev.lounres.halfhat.client.ui.components.game.GamePageComponent
import dev.lounres.halfhat.client.ui.components.home.HomePageComponent
import dev.lounres.halfhat.client.ui.components.miscellanea.MiscellaneaComponent
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.kone.hub.KoneAsynchronousHubView
import dev.lounres.kone.hub.KoneMutableAsynchronousHubView
import dev.lounres.kone.registry.serialization.RegistrySerializableKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable


expect interface MainWindowComponent {
    val globalLifecycle: MutableUIComponentLifecycle
    
    val darkTheme: KoneMutableAsynchronousHubView<DarkTheme, *>
    
    val pageVariants: KoneAsynchronousHubView<ChildrenVariants<MainWindowComponentConfiguration, MainWindowComponentChild, UIComponentContext>, *>
    val openPage: (page: MainWindowComponentConfiguration) -> Unit
}

@Serializable
enum class MainWindowComponentConfiguration(
    val textName: String,
    val path: String,
) {
    Home(textName = "Home", path = "home"),
    Game(textName = "Game", path = "game"),
    Miscellanea(textName = "Miscellanea", path = "misc"),
    ;
    
    data object Key : RegistrySerializableKey<MainWindowComponentConfiguration> {
        override val serializer: KSerializer<MainWindowComponentConfiguration> =
            MainWindowComponentConfiguration.serializer()
    }
}

sealed interface MainWindowComponentChild {
    data class Home(val component: HomePageComponent) : MainWindowComponentChild
    data class Game(val component: GamePageComponent) : MainWindowComponentChild
    data class Miscellanea(val component: MiscellaneaComponent) : MainWindowComponentChild
}