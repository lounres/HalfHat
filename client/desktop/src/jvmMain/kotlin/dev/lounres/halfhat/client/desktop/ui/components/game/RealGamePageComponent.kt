package dev.lounres.halfhat.client.desktop.ui.components.game

import dev.lounres.halfhat.client.common.ui.components.game.timer.RealTimerPageComponent
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.desktop.storage.dictionaries.LocalDictionariesRegistry
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.RealDeviceGamePageComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.localGame.RealLocalGamePageComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.modeSelection.RealModeSelectionPageComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.RealOnlineGamePageComponent
import dev.lounres.komponentual.lifecycle.UIComponentLifecycleKey
import dev.lounres.komponentual.navigation.ChildrenSlot
import dev.lounres.komponentual.navigation.MutableSlotNavigation
import dev.lounres.komponentual.navigation.set
import dev.lounres.kone.misc.router.uiChildrenToForegroundSlot
import dev.lounres.kone.state.KoneState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable


class RealGamePageComponent(
    componentContext: UIComponentContext,
    localDictionariesRegistry: LocalDictionariesRegistry,
    volumeOn: StateFlow<Boolean>,
): GamePageComponent {
    private val slotNavigation = MutableSlotNavigation<Configuration>()
    
    override val currentChild: KoneState<ChildrenSlot<Configuration, GamePageComponent.Child>> =
        componentContext.uiChildrenToForegroundSlot(
            source = slotNavigation,
            initialConfiguration = { Configuration.ModeSelection },
        ) { configuration, lifecycle ->
            val componentContext = UIComponentContext {
                UIComponentLifecycleKey correspondsTo lifecycle
            }
            when (configuration) {
                Configuration.ModeSelection ->
                    GamePageComponent.Child.ModeSelection(
                        RealModeSelectionPageComponent(
                            componentContext = componentContext,
                            onOnlineGameSelect = { slotNavigation.set(Configuration.OnlineGame) },
                            onLocalGameSelect = { slotNavigation.set(Configuration.LocalGame) },
                            onDeviceGameSelect = { slotNavigation.set(Configuration.DeviceGame) },
                            onGameTimerSelect = { slotNavigation.set(Configuration.GameTimer) },
                        )
                    )
                Configuration.OnlineGame ->
                    GamePageComponent.Child.OnlineGame(
                        RealOnlineGamePageComponent(
                            componentContext = componentContext,
                            onExitOnlineGame = { slotNavigation.set(Configuration.ModeSelection) },
                        )
                    )
                Configuration.LocalGame ->
                    GamePageComponent.Child.LocalGame(
                        RealLocalGamePageComponent(
                            onExitLocalGame = { slotNavigation.set(Configuration.ModeSelection) }
                        )
                    )
                Configuration.DeviceGame ->
                    GamePageComponent.Child.DeviceGame(
                        RealDeviceGamePageComponent(
                            componentContext = componentContext,
                            localDictionariesRegistry = localDictionariesRegistry,
                            onExitDeviceGame = { slotNavigation.set(Configuration.ModeSelection) },
                        )
                    )
                Configuration.GameTimer ->
                    GamePageComponent.Child.GameTimer(
                        RealTimerPageComponent(
                            componentContext = componentContext,
                            onExitTimer = { slotNavigation.set(Configuration.ModeSelection) },
                            volumeOn = volumeOn,
                        )
                    )
            }
        }
    
    @Serializable
    sealed interface Configuration {
        @Serializable
        data object ModeSelection : Configuration
        @Serializable
        data object OnlineGame : Configuration
        @Serializable
        data object LocalGame : Configuration
        @Serializable
        data object DeviceGame : Configuration
        @Serializable
        data object GameTimer: Configuration
    }
}