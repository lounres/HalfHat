package dev.lounres.halfhat.client.ui.components

import dev.lounres.halfhat.Language
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleKey
import dev.lounres.halfhat.client.components.logger.LoggerKey
import dev.lounres.halfhat.client.components.navigation.ChildrenVariants
import dev.lounres.halfhat.client.components.navigation.NavigationControllerSpec
import dev.lounres.halfhat.client.components.navigation.VariantsNode
import dev.lounres.halfhat.client.components.navigation.controller.NavigationRoot
import dev.lounres.halfhat.client.components.navigation.controller.setUpNavigationControl
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultVariantsNode
import dev.lounres.halfhat.client.logic.settings.DeviceGameDefaultSettingsKey
import dev.lounres.halfhat.client.logic.settings.LanguageKey
import dev.lounres.halfhat.client.logic.settings.VolumeOnKey
import dev.lounres.halfhat.client.logic.wordsProviders.DeviceGameWordsProviderID
import dev.lounres.halfhat.client.logic.wordsProviders.DeviceGameWordsProviderRegistry
import dev.lounres.halfhat.client.logic.wordsProviders.DeviceGameWordsProviderRegistryKey
import dev.lounres.halfhat.client.storage.settings.Settings
import dev.lounres.halfhat.client.storage.settings.SettingsSerializer
import dev.lounres.halfhat.client.storage.settings.settings
import dev.lounres.halfhat.client.ui.components.about.RealAboutPageComponent
import dev.lounres.halfhat.client.ui.components.faq.RealFAQPageComponent
import dev.lounres.halfhat.client.ui.components.feedback.RealFeedbackPageComponent
import dev.lounres.halfhat.client.ui.components.game.RealGamePageComponent
import dev.lounres.halfhat.client.ui.components.gameHistory.RealGameHistoryPageComponent
import dev.lounres.halfhat.client.ui.components.home.RealHomePageComponent
import dev.lounres.halfhat.client.ui.components.news.RealNewsPageComponent
import dev.lounres.halfhat.client.ui.components.rules.RealRulesPageComponent
import dev.lounres.halfhat.client.ui.components.settings.RealSettingsPageComponent
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.halfhat.client.utils.logger
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.komponentual.navigation.set
import dev.lounres.kone.collections.interop.toKoneList
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.build
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.set.build
import dev.lounres.kone.collections.utils.map
import dev.lounres.kone.collections.utils.plusAssign
import dev.lounres.kone.hub.*
import dev.lounres.kone.registry.RegistryKey
import dev.lounres.kone.registry.correspondsTo
import dev.lounres.kone.registry.serialization.RegistrySerializableKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


expect class RealMainWindowComponent: MainWindowComponent {
    override val globalLifecycle: MutableUIComponentLifecycle
    
    override val darkTheme: KoneMutableAsynchronousHubView<DarkTheme, *>
    override val volumeOn: KoneMutableAsynchronousHubView<Boolean, *>
    override val language: KoneMutableAsynchronousHubView<Language, *>
    
    override val pageVariants: KoneAsynchronousHubView<ChildrenVariants<MainWindowComponentChild.Kind, MainWindowComponentChild, UIComponentContext>, *>
    override val openPage: (page: MainWindowComponentChild.Kind) -> Unit
    
    override val menuList: KoneAsynchronousHubView<KoneList<MainWindowComponentMenuItem>, *>
}

sealed interface RealMainWindowComponentMenuItemByKind {
    data object Separator: RealMainWindowComponentMenuItemByKind
    data class Child(val child: MainWindowComponentChild.Kind): RealMainWindowComponentMenuItemByKind
}

data class SettingDescription<T>(
    val key: RegistrySerializableKey<T>,
    val value: T,
)

val settingsDefaults: Map<String, SettingDescription<*>> = mapOf(
    "DarkTheme" to SettingDescription(DarkTheme.Key, DarkTheme.System),
    "VolumeOn" to SettingDescription(VolumeOnKey, true),
    "Language" to SettingDescription(LanguageKey, Language.English),
    "InitialSelectedPage" to SettingDescription(MainWindowComponentChild.Kind.Key, MainWindowComponentChild.Kind.Primary.Game),
)

val settingsSerializer = SettingsSerializer(settingsDefaults.mapValues { it.value.key })

fun globalComponentContext(
    globalLifecycle: MutableUIComponentLifecycle,
    navigationRoot: NavigationRoot,
    deviceGameWordsProviderRegistry: DeviceGameWordsProviderRegistry,
    gameStateMachineWordsSource: GameStateMachine.WordsSource<DeviceGameWordsProviderID>,
): UIComponentContext = UIComponentContext {
    UIComponentLifecycleKey correspondsTo globalLifecycle
    
    LoggerKey correspondsTo logger
    
    setUpNavigationControl(
        navigationRoot = navigationRoot,
        stringFormat = Json,
    )
    
    val settings = KoneMutableAsynchronousHub(
        Settings {
//            setFrom(...)
            @Suppress("UNCHECKED_CAST")
            for ((key, value) in settingsDefaults.values) if (key !in this) (key as RegistryKey<Any?>) correspondsTo value
        }
    )
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
            wordsSource = gameStateMachineWordsSource
        )
    )
    
    DeviceGameWordsProviderRegistryKey correspondsTo deviceGameWordsProviderRegistry
}

data class PagesDescription(
    val pageVariants: VariantsNode<MainWindowComponentChild.Kind, MainWindowComponentChild, UIComponentContext>,
    val openPage: (page: MainWindowComponentChild.Kind) -> Unit,
    val menuList: KoneAsynchronousHubView<KoneList<MainWindowComponentMenuItem>, *>,
)

suspend fun UIComponentContext.pagesDescription(): PagesDescription {
    val pageVariants =
        uiChildrenDefaultVariantsNode(
            navigationControllerSpec = NavigationControllerSpec(
                key = "page",
                configurationSerializer = MainWindowComponentChild.Kind.serializer(),
            ),
            allVariants = KoneSet.build {
                +MainWindowComponentChild.Kind.Primary.entries.toKoneList()
                +MainWindowComponentChild.Kind.Secondary.entries.toKoneList()
            },
            initialVariant = this.settings.value[MainWindowComponentChild.Kind.Key],
        ) { configuration, componentContext, navigation ->
            when (configuration) {
                MainWindowComponentChild.Kind.Primary.Home ->
                    MainWindowComponentChild.Primary.Home(
                        RealHomePageComponent()
                    )
                MainWindowComponentChild.Kind.Primary.Game ->
                    MainWindowComponentChild.Primary.Game(
                        RealGamePageComponent(
                            componentContext = componentContext,
                        )
                    )
                MainWindowComponentChild.Kind.Secondary.News ->
                    MainWindowComponentChild.Secondary.News(
                        RealNewsPageComponent()
                    )
                MainWindowComponentChild.Kind.Secondary.Rules ->
                    MainWindowComponentChild.Secondary.Rules(
                        RealRulesPageComponent()
                    )
                MainWindowComponentChild.Kind.Secondary.FAQ ->
                    MainWindowComponentChild.Secondary.FAQ(
                        RealFAQPageComponent(
                            onFeedbackLinkClick = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigation.set(MainWindowComponentChild.Kind.Secondary.FAQ)
                                }
                            }
                        )
                    )
                MainWindowComponentChild.Kind.Secondary.GameHistory ->
                    MainWindowComponentChild.Secondary.GameHistory(
                        RealGameHistoryPageComponent()
                    )
                MainWindowComponentChild.Kind.Secondary.Settings ->
                    MainWindowComponentChild.Secondary.Settings(
                        RealSettingsPageComponent()
                    )
                MainWindowComponentChild.Kind.Secondary.Feedback ->
                    MainWindowComponentChild.Secondary.Feedback(
                        RealFeedbackPageComponent()
                    )
                MainWindowComponentChild.Kind.Secondary.About ->
                    MainWindowComponentChild.Secondary.About(
                        RealAboutPageComponent()
                    )
            }
        }
    
    val coroutineScope = this.coroutineScope(Dispatchers.Default)
    val openPage: (page: MainWindowComponentChild.Kind) -> Unit = { page ->
        coroutineScope.launch {
            pageVariants.set(page)
        }
    }
    
    val menuListByKinds: KoneList<RealMainWindowComponentMenuItemByKind> = KoneList.build {
        this += MainWindowComponentChild.Kind.Primary.entries.toKoneList().map { RealMainWindowComponentMenuItemByKind.Child(it) }
        +RealMainWindowComponentMenuItemByKind.Separator
        this += MainWindowComponentChild.Kind.Secondary.entries.toKoneList().map { RealMainWindowComponentMenuItemByKind.Child(it) }
    }
    val menuList: KoneAsynchronousHub<KoneList<MainWindowComponentMenuItem>> =
        pageVariants.hub.map { childrenVariants ->
            menuListByKinds.map {
                when (it) {
                    is RealMainWindowComponentMenuItemByKind.Child -> MainWindowComponentMenuItem.Child(childrenVariants.allVariants[it.child])
                    RealMainWindowComponentMenuItemByKind.Separator -> MainWindowComponentMenuItem.Separator
                }
            }
        }
    
    return PagesDescription(
        pageVariants = pageVariants,
        openPage = openPage,
        menuList = menuList,
    )
}