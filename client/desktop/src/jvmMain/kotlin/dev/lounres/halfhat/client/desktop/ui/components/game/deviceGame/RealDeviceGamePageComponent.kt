package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import dev.lounres.halfhat.client.desktop.storage.dictionaries.LocalDictionariesRegistry
import dev.lounres.halfhat.client.desktop.storage.dictionaries.LocalDictionary
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.roomScreen.RealRoomScreenComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.roomSettings.RealRoomSettingsComponent
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.koneListOf
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.set.koneMutableSetOf
import dev.lounres.kone.collections.set.toKoneMutableSet
import dev.lounres.kone.collections.utils.any
import dev.lounres.kone.collections.utils.random
import dev.lounres.kone.repeat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.random.Random


class WordsProviderViaLocalDictionary(
    private val localDictionary: LocalDictionary
) : GameStateMachine.WordsProvider {
    val name: String get() = localDictionary.name
    override fun allWords(): KoneSet<String> = localDictionary.allWords
    override fun randomWords(number: UInt): KoneSet<String> {
        val allWords = localDictionary.allWords.toKoneMutableSet()
        if (allWords.size < number) error("Not enough words in the dictionary")
        val result = koneMutableSetOf<String>()
        repeat(number) {
            val newWord = allWords.random(Random)
            allWords.remove(newWord)
            result.add(newWord)
        }
        return result
    }
}

class RealDeviceGamePageComponent(
    componentContext: ComponentContext,
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
    
    private val playersList: MutableStateFlow<KoneList<String>> = MutableStateFlow(koneListOf("", "")) // TODO: Hardcoded settings!!!
    private val settingsBuilderState: MutableStateFlow<GameStateMachine.GameSettingsBuilder<GameStateMachine.WordsProvider>> = MutableStateFlow(initialSettingsBuilder)
    
    private val navigation = StackNavigation<Configuration>()
    
    override val childStack: Value<ChildStack<Configuration, DeviceGamePageComponent.Child>> =
        componentContext.childStack(
            source = navigation,
            serializer = null,
            initialConfiguration = Configuration.RoomScreen(),
        ) { configuration: Configuration, componentContext: ComponentContext ->
            when (configuration) {
                is Configuration.RoomScreen ->
                    DeviceGamePageComponent.Child.RoomScreen(
                        RealRoomScreenComponent(
                            onExitDeviceGame = onExitDeviceGame,
                            onOpenGameSettings = { navigation.replaceCurrent(Configuration.RoomSettings) },
                            onStartGame = {
                                if (playersList.value.any { it.isBlank() }) configuration.showErrorForEmptyPlayerNames.value = true
                                else navigation.replaceCurrent(Configuration.GameScreen)
                            },
                            playersList = playersList,
                            showErrorForEmptyPlayerNames = configuration.showErrorForEmptyPlayerNames,
                        )
                    )
                Configuration.RoomSettings ->
                    DeviceGamePageComponent.Child.RoomSettings(
                        RealRoomSettingsComponent(
                            initialSettingsBuilder = settingsBuilderState.value,
                            onUpdateSettingsBuilder = { settingsBuilderState.value = it },
                            onExitSettings = { navigation.replaceCurrent(Configuration.RoomScreen()) },
                        )
                    )
                is Configuration.GameScreen ->
                    DeviceGamePageComponent.Child.GameScreen(
                        RealGameScreenComponent(
                            componentContext = componentContext,
                            playersList = playersList.value,
                            settingsBuilder = settingsBuilderState.value,
                            onExitGame = { navigation.replaceCurrent(Configuration.RoomScreen()) },
                        )
                    )
            }
        }
    
    sealed interface Configuration {
        data class RoomScreen(
            val showErrorForEmptyPlayerNames: MutableStateFlow<Boolean> = MutableStateFlow(false),
        ) : Configuration
        data object RoomSettings : Configuration
        data object GameScreen : Configuration
    }
}