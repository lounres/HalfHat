package dev.lounres.halfhat.client.ui.components.game.deviceGame

import dev.lounres.halfhat.client.logic.settings.deviceGameDefaultSettings
import dev.lounres.halfhat.client.logic.wordsProviders.deviceGameWordsProviderRegistry
import dev.lounres.halfhat.client.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent
import dev.lounres.halfhat.client.ui.components.game.deviceGame.roomScreen.Player
import dev.lounres.halfhat.client.ui.components.game.deviceGame.roomScreen.RealRoomScreenComponent
import dev.lounres.halfhat.client.ui.components.game.deviceGame.roomSettings.RealRoomSettingsComponent
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.navigation.ChildrenStack
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultStackNode
import dev.lounres.halfhat.client.storage.settings.settings
import dev.lounres.komponentual.navigation.replaceCurrent
import dev.lounres.kone.collections.iterables.isNotEmpty
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.of
import dev.lounres.kone.collections.set.KoneMutableSet
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.set.empty
import dev.lounres.kone.collections.set.of
import dev.lounres.kone.collections.utils.*
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.map
import dev.lounres.kone.hub.set
import dev.lounres.kone.hub.value
import dev.lounres.kone.relations.Equality
import dev.lounres.kone.relations.Hashing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


public class RealDeviceGamePageComponent(
    override val childStack: KoneAsynchronousHub<ChildrenStack<*, DeviceGamePageComponent.Child, UIComponentContext>>,
) : DeviceGamePageComponent {
    public sealed interface Configuration {
        public data object RoomScreen : Configuration
        public data object RoomSettings : Configuration
        public data object GameScreen : Configuration
    }
}

public suspend fun RealDeviceGamePageComponent(
    componentContext: UIComponentContext,
    onExitDeviceGame: () -> Unit,
): RealDeviceGamePageComponent {
    val coroutineScope = componentContext.coroutineScope(Dispatchers.Default)
    
    val playersList: MutableStateFlow<KoneList<Player>> = MutableStateFlow(KoneList.of(Player(""), Player(""))) // TODO: Hardcoded settings!!!
    val defaultSettingsBuilder = componentContext.settings.deviceGameDefaultSettings
    val settingsBuilderState = defaultSettingsBuilder.map { it }
    
    val possibleWordsSources = componentContext.deviceGameWordsProviderRegistry.list()
    
    val childStack =
        componentContext.uiChildrenDefaultStackNode<RealDeviceGamePageComponent.Configuration, _>(
            initialStack = KoneList.of(RealDeviceGamePageComponent.Configuration.RoomScreen),
        ) { configuration, componentContext, navigationTarget ->
            when (configuration) {
                is RealDeviceGamePageComponent.Configuration.RoomScreen -> {
                    val showErrorForPlayers = MutableStateFlow(KoneSet.empty<Player>())
                    DeviceGamePageComponent.Child.RoomScreen(
                        RealRoomScreenComponent(
                            onExitDeviceGame = onExitDeviceGame,
                            onOpenGameSettings = {
                                coroutineScope.launch {
                                    navigationTarget.replaceCurrent(RealDeviceGamePageComponent.Configuration.RoomSettings)
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
                                    error("Unreachable state: cannot start game with less than two players.")
                                }
                                
                                coroutineScope.launch {
                                    navigationTarget.replaceCurrent(
                                        RealDeviceGamePageComponent.Configuration.GameScreen
                                    )
                                }
                                coroutineScope.launch {
                                    defaultSettingsBuilder.set(settingsBuilderState.value)
                                }
                            },
                            playersList = playersList,
                            showErrorForPlayers = showErrorForPlayers,
                        )
                    )
                }
                RealDeviceGamePageComponent.Configuration.RoomSettings ->
                    DeviceGamePageComponent.Child.RoomSettings(
                        RealRoomSettingsComponent(
                            initialSettingsBuilder = settingsBuilderState.value,
                            possibleWordsSources = possibleWordsSources,
                            onUpdateSettingsBuilder = {
                                coroutineScope.launch {
                                    settingsBuilderState.set(it)
                                }
                            },
                            onExitSettings = {
                                coroutineScope.launch {
                                    navigationTarget.replaceCurrent(RealDeviceGamePageComponent.Configuration.RoomScreen)
                                }
                            },
                        )
                    )
                is RealDeviceGamePageComponent.Configuration.GameScreen ->
                    DeviceGamePageComponent.Child.GameScreen(
                        RealGameScreenComponent(
                            componentContext = componentContext,
                            playersList = playersList.value.map { it.name },
                            settingsBuilder = settingsBuilderState.value,
                            onExitGame = {
                                coroutineScope.launch {
                                    navigationTarget.replaceCurrent(RealDeviceGamePageComponent.Configuration.RoomScreen)
                                }
                            },
                        )
                    )
            }
        }
    
    return RealDeviceGamePageComponent(
        childStack = childStack.hub,
    )
}