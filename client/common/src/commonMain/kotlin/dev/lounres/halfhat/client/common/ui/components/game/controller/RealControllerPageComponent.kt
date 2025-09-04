package dev.lounres.halfhat.client.common.ui.components.game.controller

import dev.lounres.halfhat.client.common.ui.components.game.controller.gameScreen.RealGameScreenComponent
import dev.lounres.halfhat.client.common.ui.components.game.controller.roomScreen.Player
import dev.lounres.halfhat.client.common.ui.components.game.controller.roomScreen.RealRoomScreenComponent
import dev.lounres.halfhat.client.common.ui.components.game.controller.roomSettings.RealRoomSettingsComponent
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.navigation.ChildrenStack
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultStackItem
import dev.lounres.komponentual.navigation.replaceCurrent
import dev.lounres.kone.collections.iterables.isNotEmpty
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.of
import dev.lounres.kone.collections.set.KoneMutableSet
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.set.empty
import dev.lounres.kone.collections.set.of
import dev.lounres.kone.collections.utils.filter
import dev.lounres.kone.collections.utils.flatMap
import dev.lounres.kone.collections.utils.groupBy
import dev.lounres.kone.collections.utils.map
import dev.lounres.kone.collections.utils.mapTo
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.relations.Equality
import dev.lounres.kone.relations.Hashing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


public class RealControllerPageComponent(
    override val childStack: KoneAsynchronousHub<ChildrenStack<*, ControllerPageComponent.Child>>,
) : ControllerPageComponent {
    public sealed interface Configuration {
        public data object RoomScreen : Configuration
        public data object RoomSettings : Configuration
        public data object GameScreen : Configuration
    }
}

public suspend fun RealControllerPageComponent(
    componentContext: UIComponentContext,
    volumeOn: StateFlow<Boolean>,
    onExitController: () -> Unit,
): RealControllerPageComponent {
    val playersList: MutableStateFlow<KoneList<Player>> = MutableStateFlow(KoneList.of(Player(""), Player(""))) // TODO: Hardcoded settings!!!
    val preparationTimeSeconds: MutableStateFlow<UInt> = MutableStateFlow(3u) // TODO: Hardcoded settings!!!
    val explanationTimeSeconds: MutableStateFlow<UInt> = MutableStateFlow(20u) // TODO: Hardcoded settings!!!
    val finalGuessTimeSeconds: MutableStateFlow<UInt> = MutableStateFlow(3u) // TODO: Hardcoded settings!!!
    
    val childStack =
        componentContext.uiChildrenDefaultStackItem<RealControllerPageComponent.Configuration, _>(
            initialStack = KoneList.of(RealControllerPageComponent.Configuration.RoomScreen),
        ) { configuration, componentContext, navigationTarget ->
            when (configuration) {
                is RealControllerPageComponent.Configuration.RoomScreen -> {
                    val showErrorForPlayers = MutableStateFlow(KoneSet.empty<Player>())
                    ControllerPageComponent.Child.RoomScreen(
                        RealRoomScreenComponent(
                            onExitGameController = onExitController,
                            onOpenGameSettings = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigationTarget.replaceCurrent(RealControllerPageComponent.Configuration.RoomSettings)
                                }
                            },
                            onStartGame = onStartGame@{
                                val errorPlayers = playersList.value
                                    .groupBy { it.name }
                                    .nodesView
                                    .filter { it.value.size > 1u || it.key.isEmpty() }
                                    .flatMap { it.value }
                                    .mapTo(
                                        KoneMutableSet.of(
                                            elementEquality = Equality { left, right -> left.id == right.id },
                                            elementHashing = Hashing { it.id.hashCode() },
                                        )
                                    ) {
                                        it
                                    }
                                
                                if (errorPlayers.isNotEmpty()) {
                                    showErrorForPlayers.value = errorPlayers
                                    return@onStartGame
                                }
                                
                                if (playersList.value.size < 2u) {
                                    error { "Cannot remove player when there are no more than two of them" }
                                }
                                
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigationTarget.replaceCurrent(
                                        RealControllerPageComponent.Configuration.GameScreen
                                    )
                                }
                            },
                            playersList = playersList,
                            showErrorForPlayers = showErrorForPlayers,
                        )
                    )
                }
                RealControllerPageComponent.Configuration.RoomSettings ->
                    ControllerPageComponent.Child.RoomSettings(
                        RealRoomSettingsComponent(
                            initialPreparationTimeSeconds = preparationTimeSeconds.value,
                            initialExplanationTimeSeconds = explanationTimeSeconds.value,
                            initialFinalGuessTimeSeconds = finalGuessTimeSeconds.value,
                            onUpdateSettingsBuilder = { preparationTimeSecondsValue, explanationTimeSecondsValue, finalGuessTimeSecondsValue ->
                                preparationTimeSeconds.value = preparationTimeSecondsValue
                                explanationTimeSeconds.value = explanationTimeSecondsValue
                                finalGuessTimeSeconds.value = finalGuessTimeSecondsValue
                            },
                            onExitSettings = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigationTarget.replaceCurrent(RealControllerPageComponent.Configuration.RoomScreen)
                                }
                            },
                        )
                    )
                is RealControllerPageComponent.Configuration.GameScreen ->
                    ControllerPageComponent.Child.GameScreen(
                        RealGameScreenComponent(
                            componentContext = componentContext,
                            volumeOn = volumeOn,
                            playersList = playersList.value.map { it.name },
                            preparationTimeSeconds = preparationTimeSeconds.value,
                            explanationTimeSeconds = explanationTimeSeconds.value,
                            finalGuessTimeSeconds = finalGuessTimeSeconds.value,
                            onExitGameController = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigationTarget.replaceCurrent(RealControllerPageComponent.Configuration.RoomScreen)
                                }
                            },
                        )
                    )
            }
        }
    
    return RealControllerPageComponent(
        childStack = childStack.hub,
    )
}