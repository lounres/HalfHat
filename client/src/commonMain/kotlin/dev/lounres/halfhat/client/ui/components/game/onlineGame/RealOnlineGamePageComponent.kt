package dev.lounres.halfhat.client.ui.components.game.onlineGame

import dev.lounres.halfhat.api.onlineGame.ClientApi
import dev.lounres.halfhat.client.logic.components.game.onlineGame.ConnectionStatus
import dev.lounres.halfhat.client.logic.components.game.onlineGame.OnlineGameComponent
import dev.lounres.halfhat.client.logic.components.game.onlineGame.RealOnlineGameComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.RealGameScreenComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.previewScreen.RealPreviewScreenComponent
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.buildLogicChildOnRunning
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.navigation.ChildrenVariants
import dev.lounres.halfhat.client.components.navigation.NavigationControllerSpec
import dev.lounres.halfhat.client.components.navigation.controller.NavigationAction
import dev.lounres.halfhat.client.components.navigation.controller.NavigationNodePath
import dev.lounres.halfhat.client.components.navigation.controller.doStoringNavigation
import dev.lounres.halfhat.client.components.navigation.controller.navigationContext
import dev.lounres.halfhat.client.components.navigation.controller.navigationController
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultVariantsNode
import dev.lounres.komponentual.navigation.set
import dev.lounres.kone.collections.interop.toKoneList
import dev.lounres.kone.collections.iterables.isNotEmpty
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.addAllFrom
import dev.lounres.kone.collections.list.build
import dev.lounres.kone.collections.list.empty
import dev.lounres.kone.collections.map.KoneMap
import dev.lounres.kone.collections.map.build
import dev.lounres.kone.collections.map.contains
import dev.lounres.kone.collections.map.getOrNull
import dev.lounres.kone.collections.map.setAllFrom
import dev.lounres.kone.collections.set.toKoneSet
import dev.lounres.kone.collections.utils.drop
import dev.lounres.kone.hub.KoneAsynchronousHubView
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import dev.lounres.kone.hub.set
import dev.lounres.kone.hub.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json


public class RealOnlineGamePageComponent(
    override val onExitOnlineGameMode: () -> Unit,
    private val onlineGameComponent: OnlineGameComponent,
    override val childSlot: KoneAsynchronousHubView<ChildrenVariants<*, OnlineGamePageComponent.Child, UIComponentContext>, *>,
) : OnlineGamePageComponent {
    override val connectionStatus: StateFlow<ConnectionStatus> get() = onlineGameComponent.connectionStatus
    
    @Serializable
    public enum class Configuration {
        PreviewScreen, GameScreen,
    }
    
    @Serializable
    public data class State(
        val currentRoomSearchEntry: String,
        val currentEnterName: String,
    )
}

public suspend fun RealOnlineGamePageComponent(
    componentContext: UIComponentContext,
    onExitOnlineGameMode: () -> Unit,
): RealOnlineGamePageComponent {
    val coroutineScope = componentContext.coroutineScope(Dispatchers.Default)
    val onlineGameComponent: OnlineGameComponent =
        componentContext.buildLogicChildOnRunning {
            RealOnlineGameComponent(it)
        }
    
    val currentRoomSearchEntry = KoneMutableAsynchronousHub("")
    coroutineScope.launch {
        onlineGameComponent.freeRoomIdFlow.collect {
            componentContext.navigationContext.doStoringNavigation(action = NavigationAction.ReplaceState) {
                currentRoomSearchEntry.set(it)
            }
        }
    }
    val currentEnterName = KoneMutableAsynchronousHub("")
    
    currentRoomSearchEntry.subscribe {
        onlineGameComponent.sendSignal(ClientApi.Signal.FetchRoomInfo(currentRoomSearchEntry.value))
        componentContext.navigationController?.configuration =
            Json.encodeToString(
                RealOnlineGamePageComponent.State(
                    currentRoomSearchEntry = it,
                    currentEnterName = currentEnterName.value,
                )
            )
    }
    currentEnterName.subscribe {
        componentContext.navigationController?.configuration =
            Json.encodeToString(
                RealOnlineGamePageComponent.State(
                    currentRoomSearchEntry = currentRoomSearchEntry.value,
                    currentEnterName = it,
                )
            )
    }
    componentContext.navigationController?.setRestoration {
        try {
            val state = Json.decodeFromString<RealOnlineGamePageComponent.State>(it)
            currentRoomSearchEntry.set(state.currentRoomSearchEntry)
            currentEnterName.set(state.currentEnterName)
        } catch (e: SerializationException) {}
    }
    
    val childSlot =
        componentContext.uiChildrenDefaultVariantsNode<RealOnlineGamePageComponent.Configuration, _>(
            navigationControllerSpec = NavigationControllerSpec(
                key = "screen",
                configurationSerializer = RealOnlineGamePageComponent.Configuration.serializer(),
                pathBuilder = { navigationState, _ ->
                    NavigationNodePath(
                        path = KoneList.empty(),
                        arguments = KoneMap.build {
                            when (navigationState.currentVariant) {
                                RealOnlineGamePageComponent.Configuration.GameScreen -> set("open", "")
                                RealOnlineGamePageComponent.Configuration.PreviewScreen -> {}
                            }
                        }
                    )
                },
                restorationByPath = { path, navigationTarget ->
                    navigationTarget.set(
                        if ("open" in path.arguments) RealOnlineGamePageComponent.Configuration.GameScreen
                        else RealOnlineGamePageComponent.Configuration.PreviewScreen
                    )
                }
            ),
            allVariants = RealOnlineGamePageComponent.Configuration.entries.toKoneList().toKoneSet(),
            initialVariant = RealOnlineGamePageComponent.Configuration.PreviewScreen,
        ) { configuration, componentContext, navigation ->
            when (configuration) {
                RealOnlineGamePageComponent.Configuration.PreviewScreen -> {
                    OnlineGamePageComponent.Child.PreviewScreen(
                        RealPreviewScreenComponent(
                            componentContext = componentContext,
                            currentRoomSearchEntry = currentRoomSearchEntry,
                            currentEnterName = currentEnterName,
                            onFetchFreeRoomId = { onlineGameComponent.sendSignal(ClientApi.Signal.FetchFreeRoomId) },
                            roomDescriptionFlow = onlineGameComponent.roomDescriptionFlow,
                            onJoinRoom = {
                                coroutineScope.launch {
                                    navigation.set(RealOnlineGamePageComponent.Configuration.GameScreen)
                                }
                            }
                        )
                    )
                }
                RealOnlineGamePageComponent.Configuration.GameScreen -> {
                    onlineGameComponent.resetGameState()
                    // TODO: Think about replacing with single signal like "ChangeRoom"
                    onlineGameComponent.sendSignal(
                        ClientApi.Signal.OnlineGame.LeaveRoom
                    )
                    onlineGameComponent.sendSignal(
                        ClientApi.Signal.OnlineGame.JoinRoom(
                            roomId = currentRoomSearchEntry.value,
                            playerName = currentEnterName.value,
                        )
                    )
                    
                    OnlineGamePageComponent.Child.GameScreen(
                        RealGameScreenComponent(
                            componentContext = componentContext,
                            gameStateFlow = onlineGameComponent.gameStateFlow,
                            onExitOnlineGame = {
                                onlineGameComponent.sendSignal(ClientApi.Signal.OnlineGame.LeaveRoom)
                                coroutineScope.launch {
                                    navigation.set(RealOnlineGamePageComponent.Configuration.PreviewScreen)
                                }
                            },
                            onApplySettings = {
                                onlineGameComponent.sendSignal(ClientApi.Signal.OnlineGame.UpdateSettings(it))
                            },
                            onStartGame = {
                                onlineGameComponent.sendSignal(ClientApi.Signal.OnlineGame.InitializeGame)
                            },
                            onFinishGame = {
                                onlineGameComponent.sendSignal(ClientApi.Signal.OnlineGame.FinishGame)
                            },
                            onSpeakerReady = {
                                onlineGameComponent.sendSignal(ClientApi.Signal.OnlineGame.SpeakerReady)
                            },
                            onListenerReady = {
                                onlineGameComponent.sendSignal(ClientApi.Signal.OnlineGame.ListenerReady)
                            },
                            onExplanationResult = {
                                onlineGameComponent.sendSignal(ClientApi.Signal.OnlineGame.WordExplanationState(it))
                            },
                            onUpdateExplanationResults = {
                                onlineGameComponent.sendSignal(ClientApi.Signal.OnlineGame.UpdateWordsExplanationResults(it))
                            },
                            onConfirmExplanationResults = {
                                onlineGameComponent.sendSignal(ClientApi.Signal.OnlineGame.ConfirmWordsExplanationResults)
                            }
                        )
                    )
                }
            }
        }
    
    componentContext.navigationController?.setPathBuilder {
        val subpath = childSlot.context.navigationController?.pathBuilder?.invoke()
        NavigationNodePath(
            path = KoneList.build {
                add(currentRoomSearchEntry.value)
                if (subpath != null) addAllFrom(subpath.path)
            },
            arguments = KoneMap.build {
                set("name", currentEnterName.value)
                if (subpath != null) setAllFrom(subpath.arguments)
            },
        )
    }
    
    componentContext.navigationController?.setRestorationByPath {
        val name = it.arguments.getOrNull("name")
        if (name != null) currentEnterName.set(name)
        
        if (it.path.isNotEmpty()) {
            currentRoomSearchEntry.set(it.path[0u])
            childSlot.context.navigationController?.restorationByPath?.invoke(
                NavigationNodePath(
                    path = it.path.drop(1u),
                    arguments = it.arguments,
                )
            )
        }
    }
    
    return RealOnlineGamePageComponent(
        onExitOnlineGameMode = onExitOnlineGameMode,
        onlineGameComponent = onlineGameComponent,
        childSlot = childSlot.hub,
    )
}