package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen

import dev.lounres.halfhat.api.onlineGame.ClientApi
import dev.lounres.halfhat.api.onlineGame.DictionaryId
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.navigation.ChildrenSlot
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultSlotNode
import dev.lounres.halfhat.client.consts.OnlineGameSettings
import dev.lounres.halfhat.client.storage.settings.settings
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.gameResults.RealGameResultsComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.loading.RealLoadingComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roomScreen.RealRoomScreenComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.RealRoundScreenComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.wordsCollection.RealWordsCollectionComponent
import dev.lounres.halfhat.client.ui.theming.darkTheme
import dev.lounres.halfhat.client.utils.copyToClipboard
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.hub.KoneAsynchronousHub
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.thauvin.erik.urlencoder.UrlEncoderUtil


public class RealGameScreenComponent(
    override val childSlot: KoneAsynchronousHub<ChildrenSlot<*, GameScreenComponent.Child, UIComponentContext>>,
) : GameScreenComponent {
    
    public sealed interface Configuration {
        public data object Loading : Configuration
        public data class RoomScreen(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.GameInitialisation>,
        ) : Configuration
        public data class PlayersWordsCollection(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.PlayersWordsCollection>,
        ) : Configuration
        public data class RoundScreen(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.Round>,
        ) : Configuration
        public data class GameResults(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.GameResults>,
        ) : Configuration
    }
}

public suspend fun RealGameScreenComponent(
    componentContext: UIComponentContext,
    gameStateFlow: StateFlow<ServerApi.OnlineGame.State?>,
    onExitOnlineGame: () -> Unit,
    availableDictionariesFlow: StateFlow<KoneList<DictionaryId.WithDescription>?>,
    onLoadServerDictionaries: () -> Unit,
    onApplySettings: (ClientApi.SettingsBuilderPatch) -> Unit,
    onStartGame: () -> Unit,
    onFinishGame: () -> Unit,
    onSubmitWords: (KoneList<String>) -> Unit,
    onSpeakerReady: () -> Unit,
    onListenerReady: () -> Unit,
    onExplanationResult: (GameStateMachine.WordExplanation.State) -> Unit,
    onUpdateExplanationResults: (KoneList<GameStateMachine.WordExplanation>) -> Unit,
    onConfirmExplanationResults: () -> Unit,
): RealGameScreenComponent {
    val coroutineScope = componentContext.coroutineScope(Dispatchers.Default)
    
    val childSlot =
        componentContext.uiChildrenDefaultSlotNode(
            initialConfiguration = when(val gameState = gameStateFlow.value) {
                null -> RealGameScreenComponent.Configuration.Loading
                is ServerApi.OnlineGame.State.GameInitialisation -> RealGameScreenComponent.Configuration.RoomScreen(MutableStateFlow(gameState))
                is ServerApi.OnlineGame.State.PlayersWordsCollection -> RealGameScreenComponent.Configuration.PlayersWordsCollection(MutableStateFlow(gameState))
                is ServerApi.OnlineGame.State.Round -> RealGameScreenComponent.Configuration.RoundScreen(MutableStateFlow(gameState))
                is ServerApi.OnlineGame.State.GameResults -> RealGameScreenComponent.Configuration.GameResults(MutableStateFlow(gameState))
            },
        ) { configuration, componentContext, _ ->
            when(configuration) {
                RealGameScreenComponent.Configuration.Loading ->
                    GameScreenComponent.Child.Loading(
                        RealLoadingComponent(
                            onExitOnlineGame = onExitOnlineGame,
                        )
                    )
                is RealGameScreenComponent.Configuration.RoomScreen ->
                    GameScreenComponent.Child.RoomScreen(
                        RealRoomScreenComponent(
                            gameState = configuration.stateFlow,
                            
                            onExitOnlineGame = onExitOnlineGame,
                            onCopyOnlineGameKey = {
                                coroutineScope.launch {
                                    val gameState = gameStateFlow.value
                                    if (gameState != null) copyToClipboard(gameState.roomName)
                                }
                            },
                            onCopyOnlineGameLink = {
                                coroutineScope.launch {
                                    val gameState = gameStateFlow.value
                                    if (gameState != null) {
                                        copyToClipboard("${OnlineGameSettings.linkBase}game/online/${UrlEncoderUtil.encode(gameState.roomName)}")
                                    }
                                }
                            },

                            availableDictionaries = availableDictionariesFlow,
                            onLoadServerDictionaries = onLoadServerDictionaries,
                            
                            onStartGame = onStartGame,
                            
                            onApplySettings = {
                                onApplySettings(it)
                            },
                        )
                    )
                is RealGameScreenComponent.Configuration.PlayersWordsCollection ->
                    GameScreenComponent.Child.PlayersWordsCollection(
                        RealWordsCollectionComponent(
                            componentContext = componentContext,
                            
                            onSubmitWords = onSubmitWords,
                            
                            onExitOnlineGame = onExitOnlineGame,
                            onCopyOnlineGameKey = {
                                coroutineScope.launch {
                                    val gameState = gameStateFlow.value
                                    if (gameState != null) copyToClipboard(gameState.roomName)
                                }
                            },
                            onCopyOnlineGameLink = {
                                coroutineScope.launch {
                                    val gameState = gameStateFlow.value
                                    if (gameState != null) {
                                        copyToClipboard("${OnlineGameSettings.linkBase}game/online/${UrlEncoderUtil.encode(gameState.roomName)}")
                                    }
                                }
                            },
                            
                            gameState = configuration.stateFlow,
                        )
                    )
                is RealGameScreenComponent.Configuration.RoundScreen ->
                    GameScreenComponent.Child.RoundScreen(
                        RealRoundScreenComponent(
                            componentContext = componentContext,
                            
                            onExitOnlineGame = onExitOnlineGame,
                            onCopyOnlineGameKey = {
                                coroutineScope.launch {
                                    val gameState = gameStateFlow.value
                                    if (gameState != null) copyToClipboard(gameState.roomName)
                                }
                            },
                            onCopyOnlineGameLink = {
                                coroutineScope.launch {
                                    val gameState = gameStateFlow.value
                                    if (gameState != null) {
                                        copyToClipboard("${OnlineGameSettings.linkBase}game/online/${UrlEncoderUtil.encode(gameState.roomName)}")
                                    }
                                }
                            },
                            onFinishGame = onFinishGame,
                            
                            gameState = configuration.stateFlow,
                            
                            onSpeakerReady = onSpeakerReady,
                            onListenerReady = onListenerReady,
                            
                            onExplanationResult = onExplanationResult,
                            
                            onUpdateExplanationResults = onUpdateExplanationResults,
                            onConfirmExplanationResults = onConfirmExplanationResults,
                        )
                    )
                is RealGameScreenComponent.Configuration.GameResults ->
                    GameScreenComponent.Child.GameResults(
                        RealGameResultsComponent(
                            gameState = configuration.stateFlow,
                            
                            coroutineScope = componentContext.coroutineScope(Dispatchers.Default),
                            darkTheme = componentContext.settings.darkTheme,
                            
                            onLeaveGameResults = onExitOnlineGame,
                        )
                    )
            }
        }
    
    coroutineScope.launch {
        gameStateFlow.collect { newState ->
            childSlot.navigate { currentConfiguration ->
                when (newState) {
                    null -> RealGameScreenComponent.Configuration.Loading
                    is ServerApi.OnlineGame.State.GameInitialisation ->
                        when (currentConfiguration) {
                            is RealGameScreenComponent.Configuration.RoomScreen ->
                                currentConfiguration.apply {
                                    stateFlow.value = newState
                                }
                            else -> RealGameScreenComponent.Configuration.RoomScreen(
                                stateFlow = MutableStateFlow(newState),
                            )
                        }
                    is ServerApi.OnlineGame.State.PlayersWordsCollection ->
                        when (currentConfiguration) {
                            is RealGameScreenComponent.Configuration.PlayersWordsCollection ->
                                currentConfiguration.apply {
                                    stateFlow.value = newState
                                }
                            else -> RealGameScreenComponent.Configuration.PlayersWordsCollection(
                                stateFlow = MutableStateFlow(newState),
                            )
                        }
                    is ServerApi.OnlineGame.State.Round ->
                        when (currentConfiguration) {
                            is RealGameScreenComponent.Configuration.RoundScreen ->
                                currentConfiguration.apply {
                                    stateFlow.value = newState
                                }
                            else -> RealGameScreenComponent.Configuration.RoundScreen(
                                stateFlow = MutableStateFlow(newState),
                            )
                        }
                    is ServerApi.OnlineGame.State.GameResults ->
                        when (currentConfiguration) {
                            is RealGameScreenComponent.Configuration.GameResults ->
                                currentConfiguration.apply {
                                    stateFlow.value = newState
                                }
                            else -> RealGameScreenComponent.Configuration.GameResults(
                                stateFlow = MutableStateFlow(newState),
                            )
                        }
                }
            }
        }
    }
    
    return RealGameScreenComponent(
        childSlot = childSlot.hub,
    )
}