package dev.lounres.halfhat.client.ui.components.game

import dev.lounres.halfhat.client.ui.components.game.controller.RealControllerPageComponent
import dev.lounres.halfhat.client.ui.components.game.deviceGame.RealDeviceGamePageComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.RealOnlineGamePageComponent
import dev.lounres.halfhat.client.ui.components.game.timer.RealTimerPageComponent
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.ui.components.game.localGame.RealLocalGamePageComponent
import dev.lounres.halfhat.client.ui.components.game.modeSelection.RealModeSelectionPageComponent
import dev.lounres.halfhat.client.components.navigation.ChildrenSlot
import dev.lounres.halfhat.client.components.navigation.NavigationControllerSpec
import dev.lounres.halfhat.client.components.navigation.controller.NavigationNodePath
import dev.lounres.halfhat.client.components.navigation.controller.navigationController
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultSlotNode
import dev.lounres.komponentual.navigation.set
import dev.lounres.kone.collections.iterables.isEmpty
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.addAllFrom
import dev.lounres.kone.collections.list.build
import dev.lounres.kone.collections.list.empty
import dev.lounres.kone.collections.list.of
import dev.lounres.kone.collections.map.KoneMap
import dev.lounres.kone.collections.map.empty
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.utils.drop
import dev.lounres.kone.hub.KoneAsynchronousHubView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.nullable


public class RealGamePageComponent(
    override val currentChild: KoneAsynchronousHubView<ChildrenSlot<*, GamePageComponent.Child, UIComponentContext>, *>,
): GamePageComponent {
    @Serializable
    public enum class Configuration(
        val path: String,
    ) {
        OnlineGame("online"),
        LocalGame("local"),
        DeviceGame("device"),
        GameController("controller"),
        GameTimer("timer"),
    }
}

public suspend fun RealGamePageComponent(
    componentContext: UIComponentContext,
): RealGamePageComponent {
    val coroutineScope = componentContext.coroutineScope(Dispatchers.Default)
    
    val currentChild =
        componentContext.uiChildrenDefaultSlotNode(
            navigationControllerSpec = NavigationControllerSpec(
                key = "mode",
                configurationSerializer = RealGamePageComponent.Configuration.serializer().nullable,
                pathBuilder = pathBuilder@ { navigationState, children ->
                    if (navigationState == null)
                        return@pathBuilder NavigationNodePath(
                            path = KoneList.empty(),
                            arguments = KoneMap.empty(),
                        )
                    val prefix = navigationState.path
                    val subPath = children[navigationState].context.navigationController?.pathBuilder?.invoke()
                    if (subPath != null)
                        NavigationNodePath(
                            path = KoneList.build {
                                +prefix
                                addAllFrom(subPath.path)
                            },
                            arguments = subPath.arguments
                        )
                    else
                        NavigationNodePath(
                            path = KoneList.of(prefix),
                            arguments = KoneMap.empty(),
                        )
                },
                restorationByPath = restorationByPath@ { path, childrenNode ->
                    if (path.path.isEmpty()) {
                        childrenNode.set(null)
                        return@restorationByPath
                    }
                    val configuration = RealGamePageComponent.Configuration.entries.firstOrNull { it.path == path.path[0u] }
                    if (configuration == null) return@restorationByPath
                    childrenNode.set(configuration)
                    childrenNode.hub.value.componentContext.navigationController?.restorationByPath?.invoke(
                        NavigationNodePath(
                            path = path.path.drop(1u),
                            arguments = path.arguments,
                        )
                    )
                },
            ),
            initialConfiguration = null,
        ) { configuration, componentContext, navigationTarget ->
            when (configuration) {
                null ->
                    GamePageComponent.Child.ModeSelection(
                        RealModeSelectionPageComponent(
                            componentContext = componentContext,
                            onOnlineGameSelect = {
                                coroutineScope.launch {
                                    navigationTarget.set(RealGamePageComponent.Configuration.OnlineGame)
                                }
                            },
                            onLocalGameSelect = {
                                coroutineScope.launch {
                                    navigationTarget.set(RealGamePageComponent.Configuration.LocalGame)
                                }
                            },
                            onDeviceGameSelect = {
                                coroutineScope.launch {
                                    navigationTarget.set(RealGamePageComponent.Configuration.DeviceGame)
                                }
                            },
                            onGameControllerSelect = {
                                coroutineScope.launch {
                                    navigationTarget.set(RealGamePageComponent.Configuration.GameController)
                                }
                            },
                            onGameTimerSelect = {
                                coroutineScope.launch {
                                    navigationTarget.set(RealGamePageComponent.Configuration.GameTimer)
                                }
                            },
                        )
                    )
                RealGamePageComponent.Configuration.OnlineGame ->
                    GamePageComponent.Child.OnlineGame(
                        RealOnlineGamePageComponent(
                            componentContext = componentContext,
                            onExitOnlineGameMode = {
                                coroutineScope.launch {
                                    navigationTarget.set(null)
                                }
                            },
                        )
                    )
                RealGamePageComponent.Configuration.LocalGame ->
                    GamePageComponent.Child.LocalGame(
                        RealLocalGamePageComponent(
                            onExitLocalGame = {
                                coroutineScope.launch {
                                    navigationTarget.set(null)
                                }
                            }
                        )
                    )
                RealGamePageComponent.Configuration.DeviceGame ->
                    GamePageComponent.Child.DeviceGame(
                        RealDeviceGamePageComponent(
                            componentContext = componentContext,
                            onExitDeviceGame = {
                                coroutineScope.launch {
                                    navigationTarget.set(null)
                                }
                            },
                        )
                    )
                RealGamePageComponent.Configuration.GameController ->
                    GamePageComponent.Child.GameController(
                        RealControllerPageComponent(
                            componentContext = componentContext,
                            onExitController = {
                                coroutineScope.launch {
                                    navigationTarget.set(null)
                                }
                            }
                        )
                    )
                RealGamePageComponent.Configuration.GameTimer ->
                    GamePageComponent.Child.GameTimer(
                        RealTimerPageComponent(
                            componentContext = componentContext,
                            onExitTimer = {
                                coroutineScope.launch {
                                    navigationTarget.set(null)
                                }
                            },
                            // TODO: Hardcoded constants!!!
                            initialPreparationTimeSetting = 3u,
                            initialExplanationTimeSetting = 20u,
                            initialLastGuessTimeSetting = 3u
                        )
                    )
            }
        }
    
    componentContext.navigationController?.setPathBuilder {
        currentChild.context.navigationController?.pathBuilder?.invoke() ?: NavigationNodePath(
            path = KoneList.empty(),
            arguments = KoneMap.empty(),
        )
    }
    
    componentContext.navigationController?.setRestorationByPath {
        currentChild.context.navigationController?.restorationByPath?.invoke(it)
    }
    
    return RealGamePageComponent(
        currentChild = currentChild.hub,
    )
}