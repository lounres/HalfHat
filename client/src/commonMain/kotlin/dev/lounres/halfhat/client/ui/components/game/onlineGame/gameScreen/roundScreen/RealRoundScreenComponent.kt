package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.navigation.ChildrenPossibility
import dev.lounres.halfhat.client.components.navigation.ChildrenSlot
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultPossibilityNode
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultSlotNode
import dev.lounres.halfhat.client.storage.settings.settings
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.RealRoundEditingComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundExplanation.RealRoundExplanationComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundLastGuess.RealRoundLastGuessComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundPreparation.RealRoundPreparationComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundWaiting.RealRoundWaitingComponent
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.halfhat.client.ui.theming.darkTheme
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import dev.lounres.kone.hub.update
import dev.lounres.kone.hub.value
import dev.lounres.kone.maybe.notNullMaybe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class RealRoundScreenComponent(
    override val onExitOnlineGame: () -> Unit,
    override val onCopyOnlineGameKey: () -> Unit,
    override val onCopyOnlineGameLink: () -> Unit,
    override val onFinishGame: () -> Unit,

    override val gameState: StateFlow<ServerApi.OnlineGame.State.Round>,

    override val roundChildSlot: KoneAsynchronousHub<ChildrenSlot<*, RoundScreenComponent.RoundChild, UIComponentContext>>,
    override val additionalCardButton: KoneAsynchronousHub<RoundScreenComponent.AdditionalCardButtonsChild>,
    override val onSelectButton: suspend (RoundScreenComponent.AdditionalCardButton) -> Unit,
    override val additionalCardChildPossibility: KoneAsynchronousHub<ChildrenPossibility<*, RoundScreenComponent.AdditionalCardChild, UIComponentContext>>,

    override val coroutineScope: CoroutineScope,
    override val darkTheme: KoneMutableAsynchronousHub<DarkTheme>,
) : RoundScreenComponent {
    override val openAdditionalCard: KoneMutableAsynchronousHub<Boolean> = KoneMutableAsynchronousHub(false)
    
    public sealed interface Configuration {
        public data class RoundWaiting(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.Round.Waiting>,
        ) : Configuration
        public data class RoundPreparation(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.Round.Preparation>,
        ) : Configuration
        public data class RoundExplanation(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.Round.Explanation>,
        ) : Configuration
        public data class RoundLastGuess(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.Round.LastGuess>,
        ) : Configuration
        public data class RoundEditing(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.State.Round.Editing>,
        ) : Configuration
    }
}

suspend fun RealRoundScreenComponent(
    componentContext: UIComponentContext,
    
    onExitOnlineGame: () -> Unit,
    onCopyOnlineGameKey: () -> Unit,
    onCopyOnlineGameLink: () -> Unit,
    onFinishGame: () -> Unit,
    
    gameState: StateFlow<ServerApi.OnlineGame.State.Round>,
    
    onSpeakerReady: () -> Unit,
    onListenerReady: () -> Unit,
    
    onExplanationResult: (GameStateMachine.WordExplanation.State) -> Unit,
    
    onUpdateExplanationResults: (KoneList<GameStateMachine.WordExplanation>) -> Unit,
    onConfirmExplanationResults: () -> Unit,
): RealRoundScreenComponent {
    val roundChildSlot =
        componentContext.uiChildrenDefaultSlotNode(
            initialConfiguration = when(val gameState = gameState.value) {
                is ServerApi.OnlineGame.State.Round.Waiting -> RealRoundScreenComponent.Configuration.RoundWaiting(MutableStateFlow(gameState))
                is ServerApi.OnlineGame.State.Round.Preparation -> RealRoundScreenComponent.Configuration.RoundPreparation(MutableStateFlow(gameState))
                is ServerApi.OnlineGame.State.Round.Explanation -> RealRoundScreenComponent.Configuration.RoundExplanation(MutableStateFlow(gameState))
                is ServerApi.OnlineGame.State.Round.LastGuess -> RealRoundScreenComponent.Configuration.RoundLastGuess(MutableStateFlow(gameState))
                is ServerApi.OnlineGame.State.Round.Editing -> RealRoundScreenComponent.Configuration.RoundEditing(MutableStateFlow(gameState))
            },
        ) { configuration, componentContext, _ ->
            when(configuration) {
                is RealRoundScreenComponent.Configuration.RoundWaiting ->
                    RoundScreenComponent.RoundChild.RoundWaiting(
                        RealRoundWaitingComponent(
                            gameState = configuration.stateFlow,
                            
                            onSpeakerReady = onSpeakerReady,
                            onListenerReady = onListenerReady,
                        )
                    )
                is RealRoundScreenComponent.Configuration.RoundPreparation ->
                    RoundScreenComponent.RoundChild.RoundPreparation(
                        RealRoundPreparationComponent(
                            gameState = configuration.stateFlow,
                        )
                    )
                is RealRoundScreenComponent.Configuration.RoundExplanation ->
                    RoundScreenComponent.RoundChild.RoundExplanation(
                        RealRoundExplanationComponent(
                            gameState = configuration.stateFlow,
                            
                            darkTheme = componentContext.settings.darkTheme,
                            onExplanationResult = onExplanationResult,
                        )
                    )
                is RealRoundScreenComponent.Configuration.RoundLastGuess ->
                    RoundScreenComponent.RoundChild.RoundLastGuess(
                        RealRoundLastGuessComponent(
                            gameState = configuration.stateFlow,
                            
                            darkTheme = componentContext.settings.darkTheme,
                            
                            onExplanationResult = onExplanationResult,
                        )
                    )
                is RealRoundScreenComponent.Configuration.RoundEditing ->
                    RoundScreenComponent.RoundChild.RoundEditing(
                        RealRoundEditingComponent(
                            componentContext = componentContext,
                            
                            gameState = configuration.stateFlow,
                            
                            onUpdateExplanationResults = onUpdateExplanationResults,
                            onConfirmExplanationResults = onConfirmExplanationResults,
                        )
                    )
            }
        }

    val additionalCardButton = KoneMutableAsynchronousHub(
        RoundScreenComponent.AdditionalCardButtonsChild(
            leaderboard = gameState.value.leaderboard,
            wordsStatistic = gameState.value.wordsStatistic,
            selectedButtonType = null
        )
    )

    val additionalCardChildPossibility =
        componentContext.uiChildrenDefaultPossibilityNode(
            initialConfiguration = additionalCardButton.value.selectedButton.notNullMaybe()
        ) { configuration, _, _ ->
            when (configuration) {
                RoundScreenComponent.AdditionalCardButton.Schedule -> RoundScreenComponent.AdditionalCardChild.Schedule
                is RoundScreenComponent.AdditionalCardButton.PlayersStatistic -> RoundScreenComponent.AdditionalCardChild.PlayersStatistic(configuration.leaderboardPermutation)
                is RoundScreenComponent.AdditionalCardButton.WordsStatistic -> RoundScreenComponent.AdditionalCardChild.WordsStatistic(configuration.wordsStatistic)
                RoundScreenComponent.AdditionalCardButton.Settings -> RoundScreenComponent.AdditionalCardChild.Settings
            }
        }

    additionalCardButton.subscribe { newButtonChild ->
        additionalCardChildPossibility.navigate { newButtonChild.selectedButton.notNullMaybe() }
    }
    
    val coroutineScope = componentContext.coroutineScope(Dispatchers.Default)
    
    coroutineScope.launch {
        gameState.collect { newState ->
            coroutineScope {
                launch {
                    roundChildSlot.navigate { currentConfiguration ->
                        when (newState) {
                            is ServerApi.OnlineGame.State.Round.Waiting ->
                                when (currentConfiguration) {
                                    is RealRoundScreenComponent.Configuration.RoundWaiting ->
                                        currentConfiguration.apply {
                                            stateFlow.value = newState
                                        }

                                    else -> RealRoundScreenComponent.Configuration.RoundWaiting(
                                        stateFlow = MutableStateFlow(newState)
                                    )
                                }

                            is ServerApi.OnlineGame.State.Round.Preparation ->
                                when (currentConfiguration) {
                                    is RealRoundScreenComponent.Configuration.RoundPreparation ->
                                        currentConfiguration.apply {
                                            stateFlow.value = newState
                                        }

                                    else -> RealRoundScreenComponent.Configuration.RoundPreparation(
                                        stateFlow = MutableStateFlow(newState)
                                    )
                                }

                            is ServerApi.OnlineGame.State.Round.Explanation ->
                                when (currentConfiguration) {
                                    is RealRoundScreenComponent.Configuration.RoundExplanation ->
                                        currentConfiguration.apply {
                                            stateFlow.value = newState
                                        }

                                    else -> RealRoundScreenComponent.Configuration.RoundExplanation(
                                        stateFlow = MutableStateFlow(newState)
                                    )
                                }

                            is ServerApi.OnlineGame.State.Round.LastGuess ->
                                when (currentConfiguration) {
                                    is RealRoundScreenComponent.Configuration.RoundLastGuess ->
                                        currentConfiguration.apply {
                                            stateFlow.value = newState
                                        }

                                    else -> RealRoundScreenComponent.Configuration.RoundLastGuess(
                                        stateFlow = MutableStateFlow(newState)
                                    )
                                }

                            is ServerApi.OnlineGame.State.Round.Editing ->
                                when (currentConfiguration) {
                                    is RealRoundScreenComponent.Configuration.RoundEditing ->
                                        currentConfiguration.apply {
                                            stateFlow.value = newState
                                        }

                                    else -> RealRoundScreenComponent.Configuration.RoundEditing(
                                        stateFlow = MutableStateFlow(newState)
                                    )
                                }
                        }
                    }
                }
                launch {
                    additionalCardButton.update { oldChild ->
                        RoundScreenComponent.AdditionalCardButtonsChild(
                            leaderboard = gameState.value.leaderboard,
                            wordsStatistic = gameState.value.wordsStatistic,
                            selectedButtonType = oldChild.selectedButtonType,
                        )
                    }
                }
            }
        }
    }
    
    
    return RealRoundScreenComponent(
        onExitOnlineGame = onExitOnlineGame,
        onCopyOnlineGameKey = onCopyOnlineGameKey,
        onCopyOnlineGameLink = onCopyOnlineGameLink,
        onFinishGame = onFinishGame,
        
        gameState = gameState,
        
        roundChildSlot = roundChildSlot.hub,
        additionalCardButton = additionalCardButton,
        onSelectButton = { button ->
            additionalCardButton.update {
                RoundScreenComponent.AdditionalCardButtonsChild(
                    leaderboard = it.leaderboard,
                    wordsStatistic = it.wordsStatistic,
                    selectedButtonType = button.type,
                )
            }
        },
        additionalCardChildPossibility = additionalCardChildPossibility.hub,
        
        coroutineScope = coroutineScope,
        darkTheme = componentContext.settings.darkTheme,
    )
}