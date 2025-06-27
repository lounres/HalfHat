package dev.lounres.halfhat.client.common.ui.components.game.deviceGame

import dev.lounres.halfhat.client.common.logic.settings.deviceGameDefaultSettings
import dev.lounres.halfhat.client.common.logic.wordsProviders.DeviceGameWordsProviderID
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
import dev.lounres.kone.state.KoneState
import kotlinx.coroutines.flow.MutableStateFlow


public class RealDeviceGamePageComponent(
    componentContext: UIComponentContext,
    onExitDeviceGame: () -> Unit,
) : DeviceGamePageComponent {
    
    private val playersList: MutableStateFlow<KoneList<Player>> = MutableStateFlow(KoneList.of(Player(""), Player(""))) // TODO: Hardcoded settings!!!
    private val settingsBuilderState: MutableStateFlow<GameStateMachine.GameSettings.Builder<DeviceGameWordsProviderID>> = MutableStateFlow(componentContext.deviceGameDefaultSettings.value)
    
    private val navigation = MutableStackNavigation<Configuration>()
    
    override val childStack: KoneState<ChildrenStack<Configuration, DeviceGamePageComponent.Child>> =
        componentContext.uiChildrenDefaultStack(
            source = navigation,
            initialStack = { KoneList.of(Configuration.RoomScreen) },
        ) { configuration: Configuration, componentContext: UIComponentContext ->
            when (configuration) {
                is Configuration.RoomScreen -> {
                    val showErrorForEmptyPlayerNames = MutableStateFlow(false)
                    DeviceGamePageComponent.Child.RoomScreen(
                        RealRoomScreenComponent(
                            onExitDeviceGame = onExitDeviceGame,
                            onOpenGameSettings = { navigation.replaceCurrent(Configuration.RoomSettings) },
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
                                
                                if (playersListIsValid) navigation.replaceCurrent(Configuration.GameScreen)
                            },
                            playersList = playersList,
                            showErrorForEmptyPlayerNames = showErrorForEmptyPlayerNames,
                        )
                    )
                }
                Configuration.RoomSettings ->
                    DeviceGamePageComponent.Child.RoomSettings(
                        RealRoomSettingsComponent(
                            initialSettingsBuilder = settingsBuilderState.value,
                            onUpdateSettingsBuilder = { settingsBuilderState.value = it },
                            onExitSettings = { navigation.replaceCurrent(Configuration.RoomScreen) },
                        )
                    )
                is Configuration.GameScreen ->
                    DeviceGamePageComponent.Child.GameScreen(
                        RealGameScreenComponent(
                            componentContext = componentContext,
                            playersList = playersList.value.map { it.name },
                            settingsBuilder = settingsBuilderState.value,
                            onExitGame = { navigation.replaceCurrent(Configuration.RoomScreen) },
                        )
                    )
            }
        }
    
    public sealed interface Configuration {
        public data object RoomScreen : Configuration
        public data object RoomSettings : Configuration
        public data object GameScreen : Configuration
    }
}