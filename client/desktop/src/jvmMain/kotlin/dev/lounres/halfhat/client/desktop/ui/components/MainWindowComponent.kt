package dev.lounres.halfhat.client.desktop.ui.components

import androidx.compose.ui.window.WindowState
import dev.lounres.halfhat.api.localization.Language
import dev.lounres.halfhat.client.desktop.ui.components.about.AboutPageComponent
import dev.lounres.halfhat.client.desktop.ui.components.faq.FAQPageComponent
import dev.lounres.halfhat.client.desktop.ui.components.feedback.FeedbackPageComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.GamePageComponent
import dev.lounres.halfhat.client.desktop.ui.components.gameHistory.GameHistoryPageComponent
import dev.lounres.halfhat.client.desktop.ui.components.home.HomePageComponent
import dev.lounres.halfhat.client.desktop.ui.components.news.NewsPageComponent
import dev.lounres.halfhat.client.desktop.ui.components.rules.RulesPageComponent
import dev.lounres.halfhat.client.desktop.ui.components.settings.SettingsPageComponent
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.komponentual.lifecycle.MutableUIComponentLifecycle
import dev.lounres.komponentual.navigation.ChildrenVariants
import dev.lounres.kone.state.KoneState
import kotlinx.coroutines.flow.MutableStateFlow


interface MainWindowComponent {
    val onWindowCloseRequest: () -> Unit
    
    val globalLifecycle: MutableUIComponentLifecycle
    val windowState: WindowState

    val volumeOn: MutableStateFlow<Boolean>
    val language: MutableStateFlow<Language>

    val pageVariants: KoneState<ChildrenVariants<Child.Kind, Child>>
    val openPage: (page: Child.Kind) -> Unit
    val menuList: KoneState<KoneList<MenuItem>>
    
    sealed interface MenuItem {
        data object Separator: MenuItem
        data class Child(val child: MainWindowComponent.Child): MenuItem
    }
    
    sealed interface Child {
        val component: PageComponent
        val kind: Kind
        
        sealed interface Kind {
            enum class Primary : Kind {
                Home, Game
            }
            enum class Secondary : Kind {
                News, Rules, FAQ, GameHistory, Settings, Feedback, About
            }
        }
        sealed interface Primary : Child {
            class Home(override val component: HomePageComponent) : Primary {
                override val kind: Kind get() = Kind.Primary.Home
            }
            class Game(override val component: GamePageComponent) : Primary {
                override val kind: Kind get() = Kind.Primary.Game
            }
        }
        sealed interface Secondary : Child {
            class News(override val component: NewsPageComponent) : Secondary {
                override val kind: Kind get() = Kind.Secondary.News
            }
            class Rules(override val component: RulesPageComponent) : Secondary {
                override val kind: Kind get() = Kind.Secondary.Rules
            }
            class FAQ(override val component: FAQPageComponent) : Secondary {
                override val kind: Kind get() = Kind.Secondary.FAQ
            }
            class GameHistory(override val component: GameHistoryPageComponent) : Secondary {
                override val kind: Kind get() = Kind.Secondary.GameHistory
            }
            class Settings(override val component: SettingsPageComponent) : Secondary {
                override val kind: Kind get() = Kind.Secondary.Settings
            }
            class Feedback(override val component: FeedbackPageComponent) : Secondary {
                override val kind: Kind get() = Kind.Secondary.Feedback
            }
            class About(override val component: AboutPageComponent) : Secondary {
                override val kind: Kind get() = Kind.Secondary.About
            }
        }
    }
}