package dev.lounres.halfhat.client.ui.components

import androidx.compose.ui.window.WindowState
import dev.lounres.halfhat.Language
import dev.lounres.halfhat.client.logic.settings.DeviceGameDefaultSettingsKey
import dev.lounres.halfhat.client.logic.wordsProviders.DeviceGameWordsProviderID
import dev.lounres.halfhat.client.logic.wordsProviders.DeviceGameWordsProviderRegistry
import dev.lounres.halfhat.client.logic.wordsProviders.DeviceGameWordsProviderRegistryKey
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.ui.components.about.RealAboutPageComponent
import dev.lounres.halfhat.client.ui.components.faq.RealFAQPageComponent
import dev.lounres.halfhat.client.ui.components.feedback.RealFeedbackPageComponent
import dev.lounres.halfhat.client.ui.components.game.RealGamePageComponent
import dev.lounres.halfhat.client.ui.components.gameHistory.RealGameHistoryPageComponent
import dev.lounres.halfhat.client.ui.components.home.RealHomePageComponent
import dev.lounres.halfhat.client.ui.components.news.RealNewsPageComponent
import dev.lounres.halfhat.client.ui.components.rules.RealRulesPageComponent
import dev.lounres.halfhat.client.ui.components.settings.RealSettingsPageComponent
import dev.lounres.halfhat.client.utils.logger
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleKey
import dev.lounres.halfhat.client.components.lifecycle.newMutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.logger.LoggerKey
import dev.lounres.halfhat.client.components.navigation.ChildrenVariants
import dev.lounres.halfhat.client.components.navigation.NavigationControllerSpec
import dev.lounres.halfhat.client.components.navigation.controller.NavigationRoot
import dev.lounres.halfhat.client.components.navigation.controller.setUpNavigationControl
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultVariantsNode
import dev.lounres.halfhat.client.logic.settings.LanguageKey
import dev.lounres.halfhat.client.logic.settings.VolumeOnKey
import dev.lounres.halfhat.client.logic.settings.language
import dev.lounres.halfhat.client.logic.settings.volumeOn
import dev.lounres.halfhat.client.storage.settings.Settings
import dev.lounres.halfhat.client.storage.settings.SettingsSerializer
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.halfhat.client.ui.theming.darkTheme
import dev.lounres.komponentual.navigation.set
import dev.lounres.kone.collections.interop.toKoneList
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.build
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.set.build
import dev.lounres.kone.collections.utils.map
import dev.lounres.kone.collections.utils.plusAssign
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import dev.lounres.kone.hub.KoneMutableAsynchronousHubView
import dev.lounres.kone.hub.map
import dev.lounres.kone.registry.RegistryKey
import dev.lounres.kone.registry.correspondsTo
import dev.lounres.kone.registry.serialization.RegistrySerializableKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


class RealMainWindowComponent(
    override val globalLifecycle: MutableUIComponentLifecycle,
    
    override val windowState: WindowState = WindowState(),
    override val onWindowCloseRequest: () -> Unit = {},
    
    override val darkTheme: KoneMutableAsynchronousHubView<DarkTheme, *>,
    override val volumeOn: KoneMutableAsynchronousHubView<Boolean, *>,
    override val language: KoneMutableAsynchronousHubView<Language, *>,
    
    override val pageVariants: KoneAsynchronousHub<ChildrenVariants<MainWindowComponent.Child.Kind, MainWindowComponent.Child, UIComponentContext>>,
    override val openPage: (page: MainWindowComponent.Child.Kind) -> Unit,
    
    override val menuList: KoneAsynchronousHub<KoneList<MainWindowComponent.MenuItem>>,
): MainWindowComponent {
    sealed interface MenuItemByKind {
        data object Separator: MenuItemByKind
        data class Child(val child: MainWindowComponent.Child.Kind): MenuItemByKind
    }
}

data class SettingDescription<T>(
    val key: RegistrySerializableKey<T>,
    val value: T,
)

val settingsDefaults: Map<String, SettingDescription<*>> = mapOf(
    "DarkTheme" to SettingDescription(DarkTheme.Key, DarkTheme.System),
    "VolumeOn" to SettingDescription(VolumeOnKey, true),
    "Language" to SettingDescription(LanguageKey, Language.English),
)

val settingsSerializer = SettingsSerializer(settingsDefaults.mapValues { it.value.key })

suspend fun RealMainWindowComponent(
    deviceGameWordsProviderRegistry: DeviceGameWordsProviderRegistry,
    
    windowState: WindowState = WindowState(),
    onWindowCloseRequest: () -> Unit = {},
    
    initialSelectedPage: MainWindowComponent.Child.Kind = MainWindowComponent.Child.Kind.Primary.Game /* TODO: Page.Primary.Home */,
): RealMainWindowComponent {
    val globalLifecycle: MutableUIComponentLifecycle = newMutableUIComponentLifecycle()
    val navigationRoot = NavigationRoot { state, path -> /* TODO */ }
    val settings = KoneMutableAsynchronousHub(
        Settings {
//            setFrom(...)
            @Suppress("UNCHECKED_CAST")
            for ((key, value) in settingsDefaults.values) if (key !in this) (key as RegistryKey<Any?>) correspondsTo value
        }
    )
    val globalComponentContext = UIComponentContext {
        UIComponentLifecycleKey correspondsTo globalLifecycle
        LoggerKey correspondsTo logger
        @Suppress("JSON_FORMAT_REDUNDANT_DEFAULT")
        setUpNavigationControl(
            navigationRoot = navigationRoot,
            stringFormat = Json,
        )
        
        DeviceGameWordsProviderRegistryKey correspondsTo deviceGameWordsProviderRegistry
        Settings.Key correspondsTo settings
        // TODO: Move the following to settings.
        DeviceGameDefaultSettingsKey correspondsTo KoneMutableAsynchronousHub(
            GameStateMachine.GameSettings.Builder<DeviceGameWordsProviderID>(
                preparationTimeSeconds = 3u,
                explanationTimeSeconds = 20u,
                finalGuessTimeSeconds = 3u,
                strictMode = false,
                cachedEndConditionWordsNumber = 100u,
                cachedEndConditionCyclesNumber = 10u,
                gameEndConditionType = GameStateMachine.GameEndCondition.Type.Words,
                wordsSource = GameStateMachine.WordsSource.Custom(DeviceGameWordsProviderID.Local("medium"))
            )
        )
    }
    
    val pageVariants =
        globalComponentContext.uiChildrenDefaultVariantsNode(
            navigationControllerSpec = NavigationControllerSpec(
                key = "page",
                configurationSerializer = MainWindowComponent.Child.Kind.serializer(),
            ),
            allVariants = KoneSet.build {
                +MainWindowComponent.Child.Kind.Primary.entries.toKoneList()
                +MainWindowComponent.Child.Kind.Secondary.entries.toKoneList()
            },
            initialVariant = initialSelectedPage,
        ) { configuration, componentContext, navigation ->
            when (configuration) {
                MainWindowComponent.Child.Kind.Primary.Home ->
                    MainWindowComponent.Child.Primary.Home(
                        RealHomePageComponent()
                    )
                MainWindowComponent.Child.Kind.Primary.Game ->
                    MainWindowComponent.Child.Primary.Game(
                        RealGamePageComponent(
                            componentContext = componentContext,
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
                            onFeedbackLinkClick = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigation.set(MainWindowComponent.Child.Kind.Secondary.FAQ)
                                }
                            }
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
    
    val openPage: (page: MainWindowComponent.Child.Kind) -> Unit = { page ->
        CoroutineScope(Dispatchers.Default).launch {
            pageVariants.set(page)
        }
    }
    
    val menuListByKinds: KoneList<RealMainWindowComponent.MenuItemByKind> = KoneList.build {
        this += MainWindowComponent.Child.Kind.Primary.entries.toKoneList().map { RealMainWindowComponent.MenuItemByKind.Child(it) }
        +RealMainWindowComponent.MenuItemByKind.Separator
        this += MainWindowComponent.Child.Kind.Secondary.entries.toKoneList().map { RealMainWindowComponent.MenuItemByKind.Child(it) }
    }
    val menuList: KoneAsynchronousHub<KoneList<MainWindowComponent.MenuItem>> =
        pageVariants.hub.map { childrenVariants ->
            menuListByKinds.map {
                when (it) {
                    is RealMainWindowComponent.MenuItemByKind.Child -> MainWindowComponent.MenuItem.Child(childrenVariants.allVariants[it.child])
                    RealMainWindowComponent.MenuItemByKind.Separator -> MainWindowComponent.MenuItem.Separator
                }
            }
        }
    
    return RealMainWindowComponent(
        globalLifecycle = globalLifecycle,
        
        windowState = windowState,
        onWindowCloseRequest = onWindowCloseRequest,
        
        darkTheme = settings.darkTheme,
        volumeOn = settings.volumeOn,
        language = settings.language,
        
        pageVariants = pageVariants.hub,
        openPage = openPage,
        
        menuList = menuList,
    )
}