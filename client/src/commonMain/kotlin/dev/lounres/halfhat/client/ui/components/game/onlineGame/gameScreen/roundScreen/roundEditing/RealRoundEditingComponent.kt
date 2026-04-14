package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.navigation.ChildrenSlot
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultSlotNode
import dev.lounres.halfhat.client.storage.settings.settings
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.listener.RealRoundEditingListenerContentComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.player.RealRoundEditingPlayerContentComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.speaker.RealRoundEditingSpeakerContentComponent
import dev.lounres.halfhat.client.ui.theming.darkTheme
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.hub.KoneAsynchronousHub
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


public class RealRoundEditingComponent(
    override val childSlot: KoneAsynchronousHub<ChildrenSlot<*, RoundEditingComponent.Child, UIComponentContext>>
) : RoundEditingComponent {
    public sealed interface Configuration {
        public data class SpeakerContent(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.Role.Round.Editing.RoundRole.Speaker>,
        ) : Configuration
        public data class ListenerContent(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.Role.Round.Editing.RoundRole.Listener>,
        ) : Configuration
        public data class PlayerContent(
            val stateFlow: MutableStateFlow<ServerApi.OnlineGame.Role.Round.Editing.RoundRole.Player>,
        ) : Configuration
    }
}

public suspend fun RealRoundEditingComponent(
    componentContext: UIComponentContext,
    
    gameState: MutableStateFlow<ServerApi.OnlineGame.State.Round.Editing>,
    
    onUpdateExplanationResults: (KoneList<GameStateMachine.WordExplanation>) -> Unit,
    onConfirmExplanationResults: () -> Unit,
): RealRoundEditingComponent {
    val childSlot =
        componentContext.uiChildrenDefaultSlotNode(
            initialConfiguration = when(val roundRole = gameState.value.role.roundRole) {
                is ServerApi.OnlineGame.Role.Round.Editing.RoundRole.Speaker -> RealRoundEditingComponent.Configuration.SpeakerContent(MutableStateFlow(roundRole))
                is ServerApi.OnlineGame.Role.Round.Editing.RoundRole.Listener -> RealRoundEditingComponent.Configuration.ListenerContent(MutableStateFlow(roundRole))
                is ServerApi.OnlineGame.Role.Round.Editing.RoundRole.Player -> RealRoundEditingComponent.Configuration.PlayerContent(MutableStateFlow(roundRole))
            },
        ) { configuration, componentContext, _ ->
            when(configuration) {
                is RealRoundEditingComponent.Configuration.SpeakerContent ->
                    RoundEditingComponent.Child.Speaker(
                        component = RealRoundEditingSpeakerContentComponent(
                            userRole = configuration.stateFlow,
                            
                            darkTheme = componentContext.settings.darkTheme,
                            
                            onUpdateExplanationResults = onUpdateExplanationResults,
                            onConfirm = onConfirmExplanationResults,
                        )
                    )
                is RealRoundEditingComponent.Configuration.ListenerContent ->
                    RoundEditingComponent.Child.Listener(
                        component = RealRoundEditingListenerContentComponent()
                    )
                is RealRoundEditingComponent.Configuration.PlayerContent ->
                    RoundEditingComponent.Child.Player(
                        component = RealRoundEditingPlayerContentComponent()
                    )
            }
        }
    
    componentContext.coroutineScope(Dispatchers.Default).launch {
        gameState.collect { newState ->
            childSlot.navigate { currentConfiguration ->
                when (val roundRole = newState.role.roundRole) {
                    is ServerApi.OnlineGame.Role.Round.Editing.RoundRole.Speaker ->
                        when (currentConfiguration) {
                            is RealRoundEditingComponent.Configuration.SpeakerContent ->
                                currentConfiguration.apply {
                                    stateFlow.value = roundRole
                                }
                            else -> RealRoundEditingComponent.Configuration.SpeakerContent(
                                stateFlow = MutableStateFlow(roundRole)
                            )
                        }
                    is ServerApi.OnlineGame.Role.Round.Editing.RoundRole.Listener ->
                        when (currentConfiguration) {
                            is RealRoundEditingComponent.Configuration.ListenerContent ->
                                currentConfiguration.apply {
                                    stateFlow.value = roundRole
                                }
                            else -> RealRoundEditingComponent.Configuration.ListenerContent(
                                stateFlow = MutableStateFlow(roundRole)
                            )
                        }
                    is ServerApi.OnlineGame.Role.Round.Editing.RoundRole.Player ->
                        when (currentConfiguration) {
                            is RealRoundEditingComponent.Configuration.PlayerContent ->
                                currentConfiguration.apply {
                                    stateFlow.value = roundRole
                                }
                            else -> RealRoundEditingComponent.Configuration.PlayerContent(
                                stateFlow = MutableStateFlow(roundRole)
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