package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.navigation.ChildrenSlot
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultSlotNode
import dev.lounres.halfhat.client.storage.settings.settings
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.player.RealRoundEditingForPlayerComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.player.listener.RealRoundEditingListenerContentComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.player.player.RealRoundEditingPlayerContentComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.player.speaker.RealRoundEditingSpeakerContentComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.spectator.RealRoundEditingForSpectatorComponent
import dev.lounres.halfhat.client.ui.theming.darkTheme
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.hub.KoneAsynchronousHub
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


public class RealRoundEditingComponent(
    override val childSlot: KoneAsynchronousHub<ChildrenSlot<*, RoundEditingComponent.Child, UIComponentContext>>
) : RoundEditingComponent {
    public sealed interface Configuration {
        public data class PlayerContent(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.SelfRole.Round.Editing.GlobalRole.Player>,
        ) : Configuration
        public data class SpectatorContent(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.SelfRole.Round.Editing.GlobalRole.Spectator>,
        ) : Configuration
    }
}

public suspend fun RealRoundEditingComponent(
    componentContext: UIComponentContext,
    
    gameState: StateFlow<ServerApi.OnlineGame.State.Round.Editing>,
    
    onUpdateExplanationResults: (KoneList<GameStateMachine.WordExplanation>) -> Unit,
    onConfirmExplanationResults: () -> Unit,
): RealRoundEditingComponent {
    val childSlot =
        componentContext.uiChildrenDefaultSlotNode(
            initialConfiguration = when(val roundRole = gameState.value.selfRole.globalRole) {
                is ServerApi.OnlineGame.SelfRole.Round.Editing.GlobalRole.Player -> RealRoundEditingComponent.Configuration.PlayerContent(MutableStateFlow(roundRole))
                is ServerApi.OnlineGame.SelfRole.Round.Editing.GlobalRole.Spectator -> RealRoundEditingComponent.Configuration.SpectatorContent(MutableStateFlow(roundRole))
            },
        ) { configuration, componentContext, _ ->
            when(configuration) {
                is RealRoundEditingComponent.Configuration.PlayerContent ->
                    RoundEditingComponent.Child.Player(
                        component = RealRoundEditingForPlayerComponent(
                            componentContext = componentContext,

                            gameState = configuration.stateFlow,

                            onUpdateExplanationResults = onUpdateExplanationResults,
                            onConfirmExplanationResults = onConfirmExplanationResults,
                        )
                    )
                is RealRoundEditingComponent.Configuration.SpectatorContent ->
                    RoundEditingComponent.Child.Spectator(
                        component = RealRoundEditingForSpectatorComponent()
                    )
            }
        }
    
    componentContext.coroutineScope(Dispatchers.Default).launch {
        gameState.collect { newState ->
            childSlot.navigate { currentConfiguration ->
                when (val globalRole = newState.selfRole.globalRole) {
                    is ServerApi.OnlineGame.SelfRole.Round.Editing.GlobalRole.Player ->
                        when (currentConfiguration) {
                            is RealRoundEditingComponent.Configuration.PlayerContent ->
                                currentConfiguration.apply {
                                    stateFlow.value = globalRole
                                }
                            else -> RealRoundEditingComponent.Configuration.PlayerContent(
                                stateFlow = MutableStateFlow(globalRole)
                            )
                        }
                    is ServerApi.OnlineGame.SelfRole.Round.Editing.GlobalRole.Spectator ->
                        when (currentConfiguration) {
                            is RealRoundEditingComponent.Configuration.SpectatorContent ->
                                currentConfiguration.apply {
                                    stateFlow.value = globalRole
                                }
                            else -> RealRoundEditingComponent.Configuration.SpectatorContent(
                                stateFlow = MutableStateFlow(globalRole)
                            )
                        }
                }
            }
        }
    }
    
    return RealRoundEditingComponent(
        childSlot = childSlot.hub,
    )
}