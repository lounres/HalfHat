package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.player

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.navigation.ChildrenSlot
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultSlotNode
import dev.lounres.halfhat.client.storage.settings.settings
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.player.listener.RealRoundEditingListenerContentComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.player.player.RealRoundEditingPlayerContentComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.player.speaker.RealRoundEditingSpeakerContentComponent
import dev.lounres.halfhat.client.ui.theming.darkTheme
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.hub.KoneAsynchronousHub
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


public class RealRoundEditingForPlayerComponent(
    override val childSlot: KoneAsynchronousHub<ChildrenSlot<*, RoundEditingForPlayerComponent.Child, UIComponentContext>>
) : RoundEditingForPlayerComponent {
    public sealed interface Configuration {
        public data class SpeakerContent(
            val globalRoleFlow: MutableStateFlow<ServerApi.OnlineGame.SelfRole.Round.Editing.GlobalRole.Player>,
            val roundRoleFlow: MutableStateFlow<ServerApi.OnlineGame.SelfRole.Round.Editing.GlobalRole.Player.RoundRole.Speaker>,
        ) : Configuration
        public data class ListenerContent(
            val globalRoleFlow: MutableStateFlow<ServerApi.OnlineGame.SelfRole.Round.Editing.GlobalRole.Player>,
            val roundRoleFlow: MutableStateFlow<ServerApi.OnlineGame.SelfRole.Round.Editing.GlobalRole.Player.RoundRole.Listener>,
        ) : Configuration
        public data class RestingPlayerContent(
            val globalRoleFlow: MutableStateFlow<ServerApi.OnlineGame.SelfRole.Round.Editing.GlobalRole.Player>,
        ) : Configuration
    }
}

public suspend fun RealRoundEditingForPlayerComponent(
    componentContext: UIComponentContext,

    gameState: StateFlow<ServerApi.OnlineGame.SelfRole.Round.Editing.GlobalRole.Player>,

    onUpdateExplanationResults: (KoneList<GameStateMachine.WordExplanation>) -> Unit,
    onConfirmExplanationResults: () -> Unit,
): RealRoundEditingForPlayerComponent {
    val gameStateValue = gameState.value
    val childSlot =
        componentContext.uiChildrenDefaultSlotNode(
            initialConfiguration = when(val roundRole = gameStateValue.roundRole) {
                is ServerApi.OnlineGame.SelfRole.Round.Editing.GlobalRole.Player.RoundRole.Speaker ->
                    RealRoundEditingForPlayerComponent.Configuration.SpeakerContent(
                        MutableStateFlow(gameStateValue),
                        MutableStateFlow(roundRole),
                    )
                is ServerApi.OnlineGame.SelfRole.Round.Editing.GlobalRole.Player.RoundRole.Listener ->
                    RealRoundEditingForPlayerComponent.Configuration.ListenerContent(
                        MutableStateFlow(gameStateValue),
                        MutableStateFlow(roundRole),
                    )
                null ->
                    RealRoundEditingForPlayerComponent.Configuration.RestingPlayerContent(
                        MutableStateFlow(gameStateValue),
                    )
            },
        ) { configuration, componentContext, _ ->
            when(configuration) {
                is RealRoundEditingForPlayerComponent.Configuration.SpeakerContent ->
                    RoundEditingForPlayerComponent.Child.Speaker(
                        component = RealRoundEditingSpeakerContentComponent(
                            roundRole = configuration.roundRoleFlow,
                            
                            darkTheme = componentContext.settings.darkTheme,
                            
                            onUpdateExplanationResults = onUpdateExplanationResults,
                            onConfirm = onConfirmExplanationResults,
                        )
                    )
                is RealRoundEditingForPlayerComponent.Configuration.ListenerContent ->
                    RoundEditingForPlayerComponent.Child.Listener(
                        component = RealRoundEditingListenerContentComponent()
                    )
                is RealRoundEditingForPlayerComponent.Configuration.RestingPlayerContent ->
                    RoundEditingForPlayerComponent.Child.Player(
                        component = RealRoundEditingPlayerContentComponent()
                    )
            }
        }
    
    componentContext.coroutineScope(Dispatchers.Default).launch {
        gameState.collect { newState ->
            childSlot.navigate { currentConfiguration ->
                when (val roundRole = newState.roundRole) {
                    is ServerApi.OnlineGame.SelfRole.Round.Editing.GlobalRole.Player.RoundRole.Speaker ->
                        when (currentConfiguration) {
                            is RealRoundEditingForPlayerComponent.Configuration.SpeakerContent ->
                                currentConfiguration.apply {
                                    globalRoleFlow.value = newState
                                    roundRoleFlow.value = roundRole
                                }
                            else -> RealRoundEditingForPlayerComponent.Configuration.SpeakerContent(
                                globalRoleFlow = MutableStateFlow(newState),
                                roundRoleFlow = MutableStateFlow(roundRole),
                            )
                        }
                    is ServerApi.OnlineGame.SelfRole.Round.Editing.GlobalRole.Player.RoundRole.Listener ->
                        when (currentConfiguration) {
                            is RealRoundEditingForPlayerComponent.Configuration.ListenerContent ->
                                currentConfiguration.apply {
                                    globalRoleFlow.value = newState
                                    roundRoleFlow.value = roundRole
                                }
                            else -> RealRoundEditingForPlayerComponent.Configuration.ListenerContent(
                                globalRoleFlow = MutableStateFlow(newState),
                                roundRoleFlow = MutableStateFlow(roundRole),
                            )
                        }
                    null ->
                        when (currentConfiguration) {
                            is RealRoundEditingForPlayerComponent.Configuration.RestingPlayerContent ->
                                currentConfiguration.apply {
                                    globalRoleFlow.value = newState
                                }
                            else -> RealRoundEditingForPlayerComponent.Configuration.RestingPlayerContent(
                                globalRoleFlow = MutableStateFlow(newState),
                            )
                        }
                }
            }
        }
    }
    
    return RealRoundEditingForPlayerComponent(
        childSlot = childSlot.hub,
    )
}