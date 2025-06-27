package dev.lounres.halfhat.client.desktop.ui.components

import androidx.compose.ui.window.WindowState
import dev.lounres.halfhat.Language
import dev.lounres.halfhat.client.common.logic.wordsProviders.DeviceGameWordsProviderRegistry
import dev.lounres.halfhat.client.common.logic.wordsProviders.DeviceGameWordsProviderRegistryKey
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultVariants
import dev.lounres.halfhat.client.common.ui.components.about.RealAboutPageComponent
import dev.lounres.halfhat.client.common.ui.components.faq.RealFAQPageComponent
import dev.lounres.halfhat.client.common.ui.components.feedback.RealFeedbackPageComponent
import dev.lounres.halfhat.client.common.ui.components.game.RealGamePageComponent
import dev.lounres.halfhat.client.common.ui.components.gameHistory.RealGameHistoryPageComponent
import dev.lounres.halfhat.client.common.ui.components.home.RealHomePageComponent
import dev.lounres.halfhat.client.common.ui.components.news.RealNewsPageComponent
import dev.lounres.halfhat.client.common.ui.components.rules.RealRulesPageComponent
import dev.lounres.halfhat.client.common.ui.components.settings.RealSettingsPageComponent
import dev.lounres.komponentual.lifecycle.MutableUIComponentLifecycle
import dev.lounres.komponentual.lifecycle.UIComponentLifecycleKey
import dev.lounres.komponentual.navigation.ChildrenVariants
import dev.lounres.komponentual.navigation.MutableVariantsNavigation
import dev.lounres.komponentual.navigation.set
import dev.lounres.kone.collections.interop.toKoneList
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.build
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.set.build
import dev.lounres.kone.collections.utils.map
import dev.lounres.kone.state.KoneState
import dev.lounres.kone.state.map
import kotlinx.coroutines.flow.MutableStateFlow


class RealMainWindowComponent(
    deviceGameWordsProviderRegistry: DeviceGameWordsProviderRegistry,
    
    override val windowState: WindowState = WindowState(),
    override val onWindowCloseRequest: () -> Unit = {},
    
    initialVolumeOn: Boolean = true,
    initialLanguage: Language = Language.English,
    
    initialSelectedPage: MainWindowComponent.Child.Kind = MainWindowComponent.Child.Kind.Primary.Game /* TODO: Page.Primary.Home */,
): MainWindowComponent {
    override val globalLifecycle: MutableUIComponentLifecycle = MutableUIComponentLifecycle()
    private val globalComponentContext = UIComponentContext {
        UIComponentLifecycleKey correspondsTo globalLifecycle
        DeviceGameWordsProviderRegistryKey correspondsTo deviceGameWordsProviderRegistry
    }
    
    override val volumeOn: MutableStateFlow<Boolean> = MutableStateFlow(initialVolumeOn)
    override val language: MutableStateFlow<Language> = MutableStateFlow(initialLanguage)
    
    private val pageVariantsNavigation = MutableVariantsNavigation<MainWindowComponent.Child.Kind>()
    
    override val pageVariants: KoneState<ChildrenVariants<MainWindowComponent.Child.Kind, MainWindowComponent.Child>> =
        globalComponentContext.uiChildrenDefaultVariants(
            source = pageVariantsNavigation,
            allVariants = {
                KoneSet.build {
                    +MainWindowComponent.Child.Kind.Primary.entries.toKoneList()
                    +MainWindowComponent.Child.Kind.Secondary.entries.toKoneList()
                }
            },
            initialVariant = { initialSelectedPage }
        ) { configuration, componentContext ->
            when (configuration) {
                MainWindowComponent.Child.Kind.Primary.Home ->
                    MainWindowComponent.Child.Primary.Home(
                        RealHomePageComponent()
                    )
                MainWindowComponent.Child.Kind.Primary.Game ->
                    MainWindowComponent.Child.Primary.Game(
                        RealGamePageComponent(
                            componentContext = componentContext,
//                            localDictionariesRegistry = localDictionariesRegistry,
                            volumeOn = volumeOn
                        )
                    )
                MainWindowComponent.Child.Kind.Secondary.News ->
                    MainWindowComponent.Child.Secondary.News(
                        RealNewsPageComponent()
                    )
                MainWindowComponent.Child.Kind.Secondary.Rules ->
                    MainWindowComponent.Child.Secondary.Rules(
                        RealRulesPageComponent()
                    )
                MainWindowComponent.Child.Kind.Secondary.FAQ ->
                    MainWindowComponent.Child.Secondary.FAQ(
                        RealFAQPageComponent(
                            onFeedbackLinkClick = { pageVariantsNavigation.set(MainWindowComponent.Child.Kind.Secondary.FAQ) }
                        )
                    )
                MainWindowComponent.Child.Kind.Secondary.GameHistory ->
                    MainWindowComponent.Child.Secondary.GameHistory(
                        RealGameHistoryPageComponent()
                    )
                MainWindowComponent.Child.Kind.Secondary.Settings ->
                    MainWindowComponent.Child.Secondary.Settings(
                        RealSettingsPageComponent()
                    )
                MainWindowComponent.Child.Kind.Secondary.Feedback ->
                    MainWindowComponent.Child.Secondary.Feedback(
                        RealFeedbackPageComponent()
                    )
                MainWindowComponent.Child.Kind.Secondary.About ->
                    MainWindowComponent.Child.Secondary.About(
                        RealAboutPageComponent()
                    )
            }
        }
    
    override val openPage: (page: MainWindowComponent.Child.Kind) -> Unit = { page -> pageVariantsNavigation.set(page) }
    
    sealed interface MenuItemByKind {
        data object Separator: MenuItemByKind
        data class Child(val child: MainWindowComponent.Child.Kind): MenuItemByKind
    }
    
    private val menuListByKinds: KoneList<MenuItemByKind> = KoneList.build {
        +MainWindowComponent.Child.Kind.Primary.entries.toKoneList().map { MenuItemByKind.Child(it) }
        +MenuItemByKind.Separator
        +MainWindowComponent.Child.Kind.Secondary.entries.toKoneList().map { MenuItemByKind.Child(it) }
    }
    override val menuList: KoneState<KoneList<MainWindowComponent.MenuItem>> =
        pageVariants.map { childrenVariants ->
            menuListByKinds.map {
                when (it) {
                    is MenuItemByKind.Child -> MainWindowComponent.MenuItem.Child(childrenVariants.allVariants[it.child])
                    MenuItemByKind.Separator -> MainWindowComponent.MenuItem.Separator
                }
            }
        }
}