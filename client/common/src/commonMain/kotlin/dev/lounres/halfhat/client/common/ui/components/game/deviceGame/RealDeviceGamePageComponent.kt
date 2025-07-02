package dev.lounres.halfhat.client.common.ui.components.game.deviceGame

import dev.lounres.halfhat.client.common.logic.settings.deviceGameDefaultSettings
import dev.lounres.halfhat.client.common.logic.wordsProviders.DeviceGameWordsProviderID
import dev.lounres.halfhat.client.common.logic.wordsProviders.deviceGameWordsProviderRegistry
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.roomScreen.Player
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.roomScreen.RealRoomScreenComponent
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.roomSettings.RealRoomSettingsComponent
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultStack
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.komponentual.navigation.ChildrenStack
import dev.lounres.komponentual.navigation.MutableStackNavigation
import dev.lounres.komponentual.navigation.replaceCurrent
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.of
import dev.lounres.kone.collections.utils.any
import dev.lounres.kone.collections.utils.map
import dev.lounres.kone.state.KoneAsynchronousState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


public class RealDeviceGamePageComponent(
    override val childStack: KoneAsynchronousState<ChildrenStack<*, DeviceGamePageComponent.Child>>,
) : DeviceGamePageComponent {
    public sealed interface Configuration {
        public data object RoomScreen : Configuration
        public data object RoomSettings : Configuration
        public data object GameScreen : Configuration
    }
}

public suspend fun RealDeviceGamePageComponent(
    componentContext: UIComponentContext,
    onExitDeviceGame: () -> Unit,
): RealDeviceGamePageComponent {
    val playersList: MutableStateFlow<KoneList<Player>> = MutableStateFlow(KoneList.of(Player(""), Player(""))) // TODO: Hardcoded settings!!!
    val settingsBuilderState: MutableStateFlow<GameStateMachine.GameSettings.Builder<DeviceGameWordsProviderID>> = MutableStateFlow(componentContext.deviceGameDefaultSettings.value)
    
    val navigation = MutableStackNavigation<RealDeviceGamePageComponent.Configuration>(CoroutineScope(Dispatchers.Default))
    
    val possibleWordsSources = componentContext.deviceGameWordsProviderRegistry.list()
    
    val childStack: KoneAsynchronousState<ChildrenStack<RealDeviceGamePageComponent.Configuration, DeviceGamePageComponent.Child>> =
        componentContext.uiChildrenDefaultStack(
            source = navigation,
            initialStack = KoneList.of(RealDeviceGamePageComponent.Configuration.RoomScreen),
        ) { configuration: RealDeviceGamePageComponent.Configuration, componentContext: UIComponentContext ->
            when (configuration) {
                is RealDeviceGamePageComponent.Configuration.RoomScreen -> {
                    val showErrorForEmptyPlayerNames = MutableStateFlow(false)
                    DeviceGamePageComponent.Child.RoomScreen(
                        RealRoomScreenComponent(
                            onExitDeviceGame = onExitDeviceGame,
                            onOpenGameSettings = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigation.replaceCurrent(RealDeviceGamePageComponent.Configuration.RoomSettings)
                                }
                            },
                            onStartGame = {
                                var playersListIsValid = true
                                
                                if (playersList.value.any { it.name.isBlank() }) {
                                    showErrorForEmptyPlayerNames.value = true
                                    playersListIsValid = false
                                }
                                
                                if (playersList.value.size < 2u) {
                                    // TODO: Добавить другую индикацию малого числа участников
                                    playersListIsValid = false
                                }
                                
                                if (playersListIsValid) CoroutineScope(Dispatchers.Default).launch {
                                    navigation.replaceCurrent(
                                        RealDeviceGamePageComponent.Configuration.GameScreen
                                    )
                                }
                            },
                            playersList = playersList,
                            showErrorForEmptyPlayerNames = showErrorForEmptyPlayerNames,
                        )
                    )
                }
                RealDeviceGamePageComponent.Configuration.RoomSettings ->
                    DeviceGamePageComponent.Child.RoomSettings(
                        RealRoomSettingsComponent(
                            initialSettingsBuilder = settingsBuilderState.value,
                            possibleWordsSources = possibleWordsSources,
                            onUpdateSettingsBuilder = { settingsBuilderState.value = it },
                            onExitSettings = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigation.replaceCurrent(RealDeviceGamePageComponent.Configuration.RoomScreen)
                                }
                            },
                        )
                    )
                is RealDeviceGamePageComponent.Configuration.GameScreen ->
                    DeviceGamePageComponent.Child.GameScreen(
                        RealGameScreenComponent(
                            componentContext = componentContext,
                            playersList = playersList.value.map { it.name },
                            settingsBuilder = settingsBuilderState.value,
                            onExitGame = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigation.replaceCurrent(RealDeviceGamePageComponent.Configuration.RoomScreen)
                                }
                            },
                        )
                    )
            }
        }
    
    return RealDeviceGamePageComponent(
        childStack = childStack,
    )
}