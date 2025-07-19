package dev.lounres.halfhat.client.common.ui.components.game

import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.RealDeviceGamePageComponent
import dev.lounres.halfhat.client.common.ui.components.game.onlineGame.RealOnlineGamePageComponent
import dev.lounres.halfhat.client.common.ui.components.game.timer.RealTimerPageComponent
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.common.ui.components.game.localGame.RealLocalGamePageComponent
import dev.lounres.halfhat.client.common.ui.components.game.modeSelection.RealModeSelectionPageComponent
import dev.lounres.halfhat.client.components.navigation.ChildrenSlot
import dev.lounres.halfhat.client.components.navigation.NavigationControllerSpec
import dev.lounres.halfhat.client.components.navigation.controller.navigationContext
import dev.lounres.halfhat.client.components.navigation.controller.doStoringNavigation
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultSlotItem
import dev.lounres.komponentual.navigation.set
import dev.lounres.kone.hub.KoneAsynchronousHub
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


public class RealGamePageComponent(
    override val currentChild: KoneAsynchronousHub<ChildrenSlot<*, GamePageComponent.Child>>,
): GamePageComponent {
    @Serializable
    public enum class Configuration {
        ModeSelection,
        OnlineGame,
        LocalGame,
        DeviceGame,
        GameTimer,
    }
}

public suspend fun RealGamePageComponent(
    componentContext: UIComponentContext,
    volumeOn: StateFlow<Boolean>,
): RealGamePageComponent {
    
    val currentChild =
        componentContext.uiChildrenDefaultSlotItem<RealGamePageComponent.Configuration, _>(
            navigationControllerSpec = NavigationControllerSpec(
                key = "mode",
                configurationSerializer = RealGamePageComponent.Configuration.serializer(),
            ),
            initialConfiguration = RealGamePageComponent.Configuration.ModeSelection,
        ) { configuration, componentContext, navigationTarget ->
            when (configuration) {
                RealGamePageComponent.Configuration.ModeSelection ->
                    GamePageComponent.Child.ModeSelection(
                        RealModeSelectionPageComponent(
                            componentContext = componentContext,
                            onOnlineGameSelect = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    componentContext.navigationContext.doStoringNavigation {
                                        navigationTarget.set(RealGamePageComponent.Configuration.OnlineGame)
                                    }
                                }
                            },
                            onLocalGameSelect = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    componentContext.navigationContext.doStoringNavigation {
                                        navigationTarget.set(RealGamePageComponent.Configuration.LocalGame)
                                    }
                                }
                            },
                            onDeviceGameSelect = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    componentContext.navigationContext.doStoringNavigation {
                                        navigationTarget.set(RealGamePageComponent.Configuration.DeviceGame)
                                    }
                                }
                            },
                            onGameTimerSelect = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    componentContext.navigationContext.doStoringNavigation {
                                        navigationTarget.set(RealGamePageComponent.Configuration.GameTimer)
                                    }
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
                                    componentContext.navigationContext.doStoringNavigation {
                                        navigationTarget.set(RealGamePageComponent.Configuration.ModeSelection)
                                    }
                                }
                            },
                        )
                    )
                RealGamePageComponent.Configuration.LocalGame ->
                    GamePageComponent.Child.LocalGame(
                        RealLocalGamePageComponent(
                            onExitLocalGame = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    componentContext.navigationContext.doStoringNavigation {
                                        navigationTarget.set(RealGamePageComponent.Configuration.ModeSelection)
                                    }
                                }
                            }
                        )
                    )
                RealGamePageComponent.Configuration.DeviceGame ->
                    GamePageComponent.Child.DeviceGame(
                        RealDeviceGamePageComponent(
                            componentContext = componentContext,
                            volumeOn = volumeOn,
                            onExitDeviceGame = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    componentContext.navigationContext.doStoringNavigation {
                                        navigationTarget.set(RealGamePageComponent.Configuration.ModeSelection)
                                    }
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
                                    componentContext.navigationContext.doStoringNavigation {
                                        navigationTarget.set(RealGamePageComponent.Configuration.ModeSelection)
                                    }
                                }
                            },
                            volumeOn = volumeOn,
                            // TODO: Hardcoded constants!!!
                            initialPreparationTimeSetting = 3u,
                            initialExplanationTimeSetting = 20u,
                            initialLastGuessTimeSetting = 3u
                        )
                    )
            }
        }
    
    return RealGamePageComponent(
        currentChild = currentChild.hub,
    )
}