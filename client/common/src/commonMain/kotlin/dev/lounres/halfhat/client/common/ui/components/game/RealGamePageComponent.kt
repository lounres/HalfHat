package dev.lounres.halfhat.client.common.ui.components.game

import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.RealDeviceGamePageComponent
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.RealOnlineGamePageComponent
import dev.lounres.halfhat.client.common.ui.components.game.timer.RealTimerPageComponent
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultSlot
import dev.lounres.halfhat.client.common.ui.components.game.localGame.RealLocalGamePageComponent
import dev.lounres.halfhat.client.common.ui.components.game.modeSelection.RealModeSelectionPageComponent
import dev.lounres.komponentual.navigation.ChildrenSlot
import dev.lounres.komponentual.navigation.MutableSlotNavigation
import dev.lounres.komponentual.navigation.set
import dev.lounres.kone.state.KoneAsynchronousState
import dev.lounres.kone.state.KoneState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


public class RealGamePageComponent(
    override val currentChild: KoneAsynchronousState<ChildrenSlot<*, GamePageComponent.Child>>,
): GamePageComponent {
    @Serializable
    public sealed interface Configuration {
        @Serializable
        public data object ModeSelection : Configuration
        @Serializable
        public data object OnlineGame : Configuration
        @Serializable
        public data object LocalGame : Configuration
        @Serializable
        public data object DeviceGame : Configuration
        @Serializable
        public data object GameTimer: Configuration
    }
}

public suspend fun RealGamePageComponent(
    componentContext: UIComponentContext,
//    localDictionariesRegistry: LocalDictionariesRegistry,
    volumeOn: StateFlow<Boolean>,
): RealGamePageComponent {
    
    val slotNavigation = MutableSlotNavigation<RealGamePageComponent.Configuration>(CoroutineScope(Dispatchers.Default))
    val currentChild: KoneAsynchronousState<ChildrenSlot<RealGamePageComponent.Configuration, GamePageComponent.Child>> =
        componentContext.uiChildrenDefaultSlot(
            source = slotNavigation,
            initialConfiguration = RealGamePageComponent.Configuration.ModeSelection,
        ) { configuration, componentContext ->
            when (configuration) {
                RealGamePageComponent.Configuration.ModeSelection ->
                    GamePageComponent.Child.ModeSelection(
                        RealModeSelectionPageComponent(
                            componentContext = componentContext,
                            onOnlineGameSelect = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    slotNavigation.set(RealGamePageComponent.Configuration.OnlineGame)
                                }
                            },
                            onLocalGameSelect = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    slotNavigation.set(RealGamePageComponent.Configuration.LocalGame)
                                }
                            },
                            onDeviceGameSelect = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    slotNavigation.set(RealGamePageComponent.Configuration.DeviceGame)
                                }
                            },
                            onGameTimerSelect = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    slotNavigation.set(RealGamePageComponent.Configuration.GameTimer)
                                }
                            },
                        )
                    )
                RealGamePageComponent.Configuration.OnlineGame ->
                    GamePageComponent.Child.OnlineGame(
                        RealOnlineGamePageComponent(
                            componentContext = componentContext,
                            onExitOnlineGameMode = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    slotNavigation.set(RealGamePageComponent.Configuration.ModeSelection)
                                }
                            },
                        )
                    )
                RealGamePageComponent.Configuration.LocalGame ->
                    GamePageComponent.Child.LocalGame(
                        RealLocalGamePageComponent(
                            onExitLocalGame = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    slotNavigation.set(RealGamePageComponent.Configuration.ModeSelection)
                                }
                            }
                        )
                    )
                RealGamePageComponent.Configuration.DeviceGame ->
                    GamePageComponent.Child.DeviceGame(
                        RealDeviceGamePageComponent(
                            componentContext = componentContext,
//                            localDictionariesRegistry = localDictionariesRegistry,
                            onExitDeviceGame = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    slotNavigation.set(RealGamePageComponent.Configuration.ModeSelection)
                                }
                            },
                        )
                    )
                RealGamePageComponent.Configuration.GameTimer ->
                    GamePageComponent.Child.GameTimer(
                        RealTimerPageComponent(
                            componentContext = componentContext,
                            onExitTimer = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    slotNavigation.set(RealGamePageComponent.Configuration.ModeSelection)
                                }
                            },
                            volumeOn = volumeOn,
                        )
                    )
            }
        }
    
    return RealGamePageComponent(
        currentChild = currentChild,
    )
}