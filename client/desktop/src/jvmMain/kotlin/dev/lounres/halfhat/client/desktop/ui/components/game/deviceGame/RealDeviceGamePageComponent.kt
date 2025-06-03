package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.desktop.storage.dictionaries.LocalDictionariesRegistry
import dev.lounres.halfhat.client.desktop.storage.dictionaries.LocalDictionary
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.roomScreen.RealRoomScreenComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.roomSettings.RealRoomSettingsComponent
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.of
import dev.lounres.kone.collections.set.KoneMutableSet
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.set.of
import dev.lounres.kone.collections.set.toKoneMutableSet
import dev.lounres.kone.collections.utils.any
import dev.lounres.kone.collections.utils.random
import dev.lounres.komponentual.lifecycle.UIComponentLifecycle
import dev.lounres.komponentual.lifecycle.UIComponentLifecycleKey
import dev.lounres.komponentual.navigation.ChildrenStack
import dev.lounres.komponentual.navigation.MutableStackNavigation
import dev.lounres.komponentual.navigation.replaceCurrent
import dev.lounres.kone.misc.router.uiChildrenFromRunningToForegroundStack
import dev.lounres.kone.repeat
import dev.lounres.kone.state.KoneState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random


class WordsProviderViaLocalDictionary(
    private val localDictionary: LocalDictionary
) : GameStateMachine.WordsProvider {
    val name: String get() = localDictionary.name
    override fun allWords(): KoneSet<String> = localDictionary.allWords
    override fun randomWords(number: UInt): KoneSet<String> {
        val allWords = localDictionary.allWords.toKoneMutableSet()
        if (allWords.size < number) error("Not enough words in the dictionary")
        val result = KoneMutableSet.of<String>()
        repeat(number) {
            val newWord = allWords.random(Random)
            allWords.remove(newWord)
            result.add(newWord)
        }
        return result
    }
}

class RealDeviceGamePageComponent(
    componentContext: UIComponentContext,
    localDictionariesRegistry: LocalDictionariesRegistry,
    onExitDeviceGame: () -> Unit,
    initialSettingsBuilder: GameStateMachine.GameSettingsBuilder<GameStateMachine.WordsProvider> = GameStateMachine.GameSettingsBuilder(
        preparationTimeSeconds = 3u, // TODO: Hardcoded settings!!!
        explanationTimeSeconds = 40u,
        finalGuessTimeSeconds = 3u,
        strictMode = true,
        cachedEndConditionWordsNumber = 3u,
        cachedEndConditionCyclesNumber = 3u,
        gameEndConditionType = GameStateMachine.GameEndCondition.Type.Words,
        wordsSource = GameStateMachine.WordsSource.Custom(WordsProviderViaLocalDictionary(localDictionariesRegistry.getDictionaryByName("Простые русские слова"))),
    ),
) : DeviceGamePageComponent {
    
    private val playersList: MutableStateFlow<KoneList<String>> = MutableStateFlow(KoneList.of("", "")) // TODO: Hardcoded settings!!!
    private val settingsBuilderState: MutableStateFlow<GameStateMachine.GameSettingsBuilder<GameStateMachine.WordsProvider>> = MutableStateFlow(initialSettingsBuilder)
    
    private val navigation = MutableStackNavigation<Configuration>()
    
    override val childStack: KoneState<ChildrenStack<Configuration, DeviceGamePageComponent.Child>> =
        componentContext.uiChildrenFromRunningToForegroundStack(
            source = navigation,
            initialStack = { KoneList.of(Configuration.RoomScreen) },
        ) { configuration: Configuration, lifecycle: UIComponentLifecycle ->
            when (configuration) {
                is Configuration.RoomScreen -> {
                    val showErrorForEmptyPlayerNames = MutableStateFlow(false)
                    DeviceGamePageComponent.Child.RoomScreen(
                        RealRoomScreenComponent(
                            onExitDeviceGame = onExitDeviceGame,
                            onOpenGameSettings = { navigation.replaceCurrent(Configuration.RoomSettings) },
                            onStartGame = {
                                var playerListIsValid = true
                                
                                if (playersList.value.any { it.isBlank() }) {
                                    showErrorForEmptyPlayerNames.value = true
                                    playerListIsValid = false
                                }
                                
                                if (playersList.value.size < 2u) {
                                    // TODO: Добавить другую индикацию малого числа участников
                                    playerListIsValid = false
                                }
                                
                                if (playerListIsValid) navigation.replaceCurrent(Configuration.GameScreen)
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
                            componentContext = UIComponentContext {
                                UIComponentLifecycleKey correspondsTo lifecycle
                            },
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