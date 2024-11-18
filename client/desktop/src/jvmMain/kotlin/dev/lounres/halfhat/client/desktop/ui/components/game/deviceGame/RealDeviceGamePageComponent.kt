package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.roomScreen.RealRoomScreenComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.roomSettings.RealRoomSettingsComponent
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.KoneList
import dev.lounres.kone.collections.KoneSet
import dev.lounres.kone.collections.koneListOf
import dev.lounres.kone.collections.koneMutableSetOf
import dev.lounres.kone.collections.toKoneList
import dev.lounres.kone.collections.utils.mapTo
import kotlinx.coroutines.flow.MutableStateFlow


class RealDeviceGamePageComponent(
    componentContext: ComponentContext,
    onExitDeviceGame: () -> Unit,
    initialSettingsBuilder: GameStateMachine.GameSettingsBuilder<GameStateMachine.WordsProvider> = GameStateMachine.GameSettingsBuilder(
        preparationTimeSeconds = 3u, // TODO: Hardcoded settings!!!
        explanationTimeSeconds = 40u,
        finalGuessTimeSeconds = 3u,
        strictMode = true,
        cachedEndConditionWordsNumber = 100u,
        cachedEndConditionCyclesNumber = 3u,
        gameEndConditionType = GameStateMachine.GameEndCondition.Type.Words,
        wordsSource = GameStateMachine.WordsSource.Custom(
            object : GameStateMachine.WordsProvider {
                override fun randomWords(number: UInt): KoneSet<String> = (1u..number).toKoneList().mapTo(koneMutableSetOf()) { it.toString() }
                override fun allWords(): KoneSet<String> = (1u..100u).toKoneList().mapTo(koneMutableSetOf()) { it.toString() }
            }
        ),
    ),
) : DeviceGamePageComponent {
    
    private val playersList: MutableStateFlow<KoneList<String>> = MutableStateFlow(koneListOf("", "")) // TODO: Hardcoded settings!!!
    private val settingsBuilderState: MutableStateFlow<GameStateMachine.GameSettingsBuilder<GameStateMachine.WordsProvider>> = MutableStateFlow(initialSettingsBuilder)
    
    private val navigation = StackNavigation<Configuration>()
    
    override val childStack: Value<ChildStack<Configuration, DeviceGamePageComponent.Child>> =
        componentContext.childStack(
            source = navigation,
            serializer = null,
            initialConfiguration = Configuration.RoomScreen,
        ) { configuration: Configuration, componentContext: ComponentContext ->
            when (configuration) {
                Configuration.RoomScreen ->
                    DeviceGamePageComponent.Child.RoomScreen(
                        RealRoomScreenComponent(
                            onExitDeviceGame = onExitDeviceGame,
                            onOpenGameSettings = { navigation.replaceCurrent(Configuration.RoomSettings) },
                            onStartGame = { navigation.replaceCurrent(Configuration.GameScreen) },
                            playersList = playersList,
                        )
                    )
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
                            playersList = playersList.value,
                            settingsBuilder = settingsBuilderState.value,
                            onExitGame = { navigation.replaceCurrent(Configuration.RoomScreen) },
                        )
                    )
            }
        }
    
    sealed interface Configuration {
        data object RoomScreen : Configuration
        data object RoomSettings : Configuration
        data object GameScreen : Configuration
    }
}