package dev.lounres.halfhat.client.common.ui.components.game.deviceGame

import dev.lounres.halfhat.client.common.logic.settings.deviceGameDefaultSettings
import dev.lounres.halfhat.client.common.logic.wordsProviders.DeviceGameWordsProviderID
import dev.lounres.halfhat.client.common.logic.wordsProviders.deviceGameWordsProviderRegistry
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.gameScreen.RealGameScreenComponent
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.roomScreen.Player
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.roomScreen.RealRoomScreenComponent
import dev.lounres.halfhat.client.common.ui.components.game.deviceGame.roomSettings.RealRoomSettingsComponent
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultStack
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.komponentual.navigation.ChildrenStack
import dev.lounres.komponentual.navigation.MutableStackNavigation
import dev.lounres.komponentual.navigation.replaceCurrent
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.of
import dev.lounres.kone.collections.set.KoneMutableSet
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.set.empty
import dev.lounres.kone.collections.set.of
import dev.lounres.kone.collections.utils.*
import dev.lounres.kone.relations.Equality
import dev.lounres.kone.relations.Hashing
import dev.lounres.kone.state.KoneAsynchronousState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


public class RealDeviceGamePageComponent(
    override val childStack: KoneAsynchronousState<ChildrenStack<*, DeviceGamePageComponent.Child>>,
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
    val playersList: MutableStateFlow<KoneList<Player>> = MutableStateFlow(KoneList.of(Player(""), Player(""))) // TODO: Hardcoded settings!!!
    val settingsBuilderState: MutableStateFlow<GameStateMachine.GameSettings.Builder<DeviceGameWordsProviderID>> = MutableStateFlow(componentContext.deviceGameDefaultSettings.value)
    
    val navigation = MutableStackNavigation<RealDeviceGamePageComponent.Configuration>(CoroutineScope(Dispatchers.Default))
    
    val possibleWordsSources = componentContext.deviceGameWordsProviderRegistry.list()
    
    val childStack: KoneAsynchronousState<ChildrenStack<RealDeviceGamePageComponent.Configuration, DeviceGamePageComponent.Child>> =
        componentContext.uiChildrenDefaultStack(
            source = navigation,
            initialStack = KoneList.of(RealDeviceGamePageComponent.Configuration.RoomScreen),
        ) { configuration: RealDeviceGamePageComponent.Configuration, componentContext: UIComponentContext ->
            when (configuration) {
                is RealDeviceGamePageComponent.Configuration.RoomScreen -> {
                    val showErrorForPlayers = MutableStateFlow(KoneSet.empty<Player>())
                    DeviceGamePageComponent.Child.RoomScreen(
                        RealRoomScreenComponent(
                            onExitDeviceGame = onExitDeviceGame,
                            onOpenGameSettings = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigation.replaceCurrent(RealDeviceGamePageComponent.Configuration.RoomSettings)
                                }
                            },
                            onStartGame = onStartGame@{
                                if (playersList.value.any { it.name.isBlank() }) {
                                    showErrorForPlayers.value = playersList.value
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
                                    return@onStartGame
                                }
                                
                                if (playersList.value.size < 2u) {
                                    error { "Cannot remove player when there are no more than two of them" }
                                }
                                
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigation.replaceCurrent(
                                        RealDeviceGamePageComponent.Configuration.GameScreen
                                    )
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
                            onUpdateSettingsBuilder = { settingsBuilderState.value = it },
                            onExitSettings = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigation.replaceCurrent(RealDeviceGamePageComponent.Configuration.RoomScreen)
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
                                CoroutineScope(Dispatchers.Default).launch {
                                    navigation.replaceCurrent(RealDeviceGamePageComponent.Configuration.RoomScreen)
                                }
                            },
                        )
                    )
            }
        }
    
    return RealDeviceGamePageComponent(
        childStack = childStack,
    )
}