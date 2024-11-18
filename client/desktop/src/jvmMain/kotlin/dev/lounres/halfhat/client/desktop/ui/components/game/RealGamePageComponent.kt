@file:OptIn(DelicateDecomposeApi::class)

package dev.lounres.halfhat.client.desktop.ui.components.game

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.RealDeviceGamePageComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.localGame.RealLocalGamePageComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.modeSelection.RealModeSelectionPageComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.onlineGame.RealOnlineGamePageComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.timer.RealTimerPageComponent
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable


class RealGamePageComponent(
    componentContext: ComponentContext,
    volumeOn: StateFlow<Boolean>,
): GamePageComponent {
    private val navigation = StackNavigation<Configuration>()
    
    override val childStack: Value<ChildStack<*, GamePageComponent.Child>> =
        componentContext.childStack(
            source = navigation,
            serializer = Configuration.serializer(),
            initialConfiguration = Configuration.ModeSelection,
            childFactory = { configuration: Configuration, componentContext: ComponentContext ->
                when (configuration) {
                    Configuration.ModeSelection ->
                        GamePageComponent.Child.ModeSelection(
                            RealModeSelectionPageComponent(
                                onOnlineGameSelect = { navigation.push(Configuration.OnlineGame) },
                                onLocalGameSelect = { navigation.push(Configuration.LocalGame) },
                                onDeviceGameSelect = { navigation.push(Configuration.DeviceGame) },
                                onGameTimerSelect = { navigation.push(Configuration.GameTimer) },
                                
                                onOnlineGameInfo = { }, // TODO: Implement online game info
                                onLocalGameInfo = { }, // TODO: Implement online game info
                                onDeviceGameInfo = { }, // TODO: Implement online game info
                                onGameTimerInfo = { }, // TODO: Implement online game info
                            )
                        )
                    Configuration.OnlineGame ->
                        GamePageComponent.Child.OnlineGame(
                            RealOnlineGamePageComponent(
                                componentContext = componentContext,
                                onExitOnlineGame = { navigation.pop() },
                            )
                        )
                    Configuration.LocalGame ->
                        GamePageComponent.Child.LocalGame(
                            RealLocalGamePageComponent(
                                onExitLocalGame = { navigation.pop() }
                            )
                        )
                    Configuration.DeviceGame ->
                        GamePageComponent.Child.DeviceGame(
                            RealDeviceGamePageComponent(
                                componentContext = componentContext,
                                onExitDeviceGame = { navigation.pop() },
                            )
                        )
                    Configuration.GameTimer ->
                        GamePageComponent.Child.GameTimer(
                            RealTimerPageComponent(
                                componentContext = componentContext,
                                onExitTimer = { navigation.pop() },
                                volumeOn = volumeOn,
                            )
                        )
                }
            }
        )
    
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