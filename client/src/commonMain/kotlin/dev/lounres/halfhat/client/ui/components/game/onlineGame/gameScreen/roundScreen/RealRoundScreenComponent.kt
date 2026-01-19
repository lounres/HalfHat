package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.navigation.ChildrenSlot
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
import dev.lounres.kone.hub.KoneAsynchronousHubView
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import dev.lounres.kone.hub.KoneMutableAsynchronousHubView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class RealRoundScreenComponent(
    override val onExitOnlineGame: () -> Unit,
    override val onCopyOnlineGameKey: () -> Unit,
    override val onCopyOnlineGameLink: () -> Unit,
    override val onFinishGame: () -> Unit,
    
    override val gameState: StateFlow<ServerApi.OnlineGame.State.Round>,
    
    override val childSlot: KoneAsynchronousHubView<ChildrenSlot<*, RoundScreenComponent.Child, UIComponentContext>, *>,
    
    override val coroutineScope: CoroutineScope,
    override val darkTheme: KoneMutableAsynchronousHubView<DarkTheme, *>,
) : RoundScreenComponent {
    override val openAdditionalCard: KoneMutableAsynchronousHubView<Boolean, *> = KoneMutableAsynchronousHub(false)
    override val additionalCard: KoneMutableAsynchronousHubView<RoundScreenComponent.AdditionalCard, *> = KoneMutableAsynchronousHub(RoundScreenComponent.AdditionalCard.Schedule)
    
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
    val childSlot =
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
                    RoundScreenComponent.Child.RoundWaiting(
                        RealRoundWaitingComponent(
                            gameState = configuration.stateFlow,
                            
                            onSpeakerReady = onSpeakerReady,
                            onListenerReady = onListenerReady,
                        )
                    )
                is RealRoundScreenComponent.Configuration.RoundPreparation ->
                    RoundScreenComponent.Child.RoundPreparation(
                        RealRoundPreparationComponent(
                            gameState = configuration.stateFlow,
                        )
                    )
                is RealRoundScreenComponent.Configuration.RoundExplanation ->
                    RoundScreenComponent.Child.RoundExplanation(
                        RealRoundExplanationComponent(
                            gameState = configuration.stateFlow,
                            
                            darkTheme = componentContext.settings.darkTheme,
                            onExplanationResult = onExplanationResult,
                        )
                    )
                is RealRoundScreenComponent.Configuration.RoundLastGuess ->
                    RoundScreenComponent.Child.RoundLastGuess(
                        RealRoundLastGuessComponent(
                            gameState = configuration.stateFlow,
                            
                            darkTheme = componentContext.settings.darkTheme,
                            
                            onExplanationResult = onExplanationResult,
                        )
                    )
                is RealRoundScreenComponent.Configuration.RoundEditing ->
                    RoundScreenComponent.Child.RoundEditing(
                        RealRoundEditingComponent(
                            componentContext = componentContext,
                            
                            gameState = configuration.stateFlow,
                            
                            onUpdateExplanationResults = onUpdateExplanationResults,
                            onConfirmExplanationResults = onConfirmExplanationResults,
                        )
                    )
            }
        }
    
    val coroutineScope = componentContext.coroutineScope(Dispatchers.Default)
    
    coroutineScope.launch {
        gameState.collect { newState ->
            childSlot.navigate { currentConfiguration ->
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
    }
    
    
    return RealRoundScreenComponent(
        onExitOnlineGame = onExitOnlineGame,
        onCopyOnlineGameKey = onCopyOnlineGameKey,
        onCopyOnlineGameLink = onCopyOnlineGameLink,
        onFinishGame = onFinishGame,
        
        gameState = gameState,
        
        childSlot = childSlot.hub,
        
        coroutineScope = coroutineScope,
        darkTheme = componentContext.settings.darkTheme,
    )
}