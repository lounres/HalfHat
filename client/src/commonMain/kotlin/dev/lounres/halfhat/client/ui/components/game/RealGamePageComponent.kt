package dev.lounres.halfhat.client.ui.components.game

import dev.lounres.halfhat.client.ui.components.game.controller.RealControllerPageComponent
import dev.lounres.halfhat.client.ui.components.game.deviceGame.RealDeviceGamePageComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.RealOnlineGamePageComponent
import dev.lounres.halfhat.client.ui.components.game.timer.RealTimerPageComponent
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.ui.components.game.localGame.RealLocalGamePageComponent
import dev.lounres.halfhat.client.ui.components.game.modeSelection.RealModeSelectionPageComponent
import dev.lounres.halfhat.client.components.navigation.ChildrenSlot
import dev.lounres.halfhat.client.components.navigation.NavigationControllerSpec
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultSlotNode
import dev.lounres.komponentual.navigation.set
import dev.lounres.kone.hub.KoneAsynchronousHub
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


public class RealGamePageComponent(
    override val currentChild: KoneAsynchronousHub<ChildrenSlot<*, GamePageComponent.Child, UIComponentContext>>,
): GamePageComponent {
    @Serializable
    public enum class Configuration {
        ModeSelection,
        OnlineGame,
        LocalGame,
        DeviceGame,
        GameController,
        GameTimer,
    }
}

public suspend fun RealGamePageComponent(
    componentContext: UIComponentContext,
    volumeOn: StateFlow<Boolean>,
): RealGamePageComponent {
    
    val currentChild =
        componentContext.uiChildrenDefaultSlotNode(
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
                                    navigationTarget.set(RealGamePageComponent.Configuration.OnlineGame)
                                }
                            },
                            onLocalGameSelect = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigationTarget.set(RealGamePageComponent.Configuration.LocalGame)
                                }
                            },
                            onDeviceGameSelect = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigationTarget.set(RealGamePageComponent.Configuration.DeviceGame)
                                }
                            },
                            onGameControllerSelect = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigationTarget.set(RealGamePageComponent.Configuration.GameController)
                                }
                            },
                            onGameTimerSelect = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigationTarget.set(RealGamePageComponent.Configuration.GameTimer)
                                }
                            },
                        )
                    )
                RealGamePageComponent.Configuration.OnlineGame ->
                    GamePageComponent.Child.OnlineGame(
                        RealOnlineGamePageComponent(
                            componentContext = componentContext,
                            volumeOn = volumeOn,
                            onExitOnlineGameMode = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigationTarget.set(RealGamePageComponent.Configuration.ModeSelection)
                                }
                            },
                        )
                    )
                RealGamePageComponent.Configuration.LocalGame ->
                    GamePageComponent.Child.LocalGame(
                        RealLocalGamePageComponent(
                            onExitLocalGame = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigationTarget.set(RealGamePageComponent.Configuration.ModeSelection)
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
                                    navigationTarget.set(RealGamePageComponent.Configuration.ModeSelection)
                                }
                            },
                        )
                    )
                RealGamePageComponent.Configuration.GameController ->
                    GamePageComponent.Child.GameController(
                        RealControllerPageComponent(
                            componentContext = componentContext,
                            volumeOn = volumeOn,
                            onExitController = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigationTarget.set(RealGamePageComponent.Configuration.ModeSelection)
                                }
                            }
                        )
                    )
                RealGamePageComponent.Configuration.GameTimer ->
                    GamePageComponent.Child.GameTimer(
                        RealTimerPageComponent(
                            componentContext = componentContext,
                            onExitTimer = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigationTarget.set(RealGamePageComponent.Configuration.ModeSelection)
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