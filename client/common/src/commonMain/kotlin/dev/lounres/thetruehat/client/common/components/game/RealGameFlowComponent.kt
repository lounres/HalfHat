package dev.lounres.thetruehat.client.common.components.game

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import dev.lounres.thetruehat.api.*
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.RoomDescription
import dev.lounres.thetruehat.api.models.UserGameState
import dev.lounres.thetruehat.client.common.components.game.roomEnter.RealRoomEnterPageComponent
import dev.lounres.thetruehat.client.common.components.game.roomFlow.RealRoomFlowComponent
import dev.lounres.thetruehat.client.common.components.game.roundBreak.RealRoundBreakPageComponent
import dev.lounres.thetruehat.client.common.utils.defaultHttpClient
import dev.lounres.thetruehat.client.common.utils.runOnUiThread
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import kotlin.coroutines.CoroutineContext


public class RealGameFlowComponent(
    public val componentContext: ComponentContext,
    public val coroutineContext: CoroutineContext,
    public val backButtonEnabled: Boolean,
    public val onBackButtonClick: () -> Unit,
    public val onLanguageChange: (language: Language) -> Unit,
    public val onFeedbackButtonClick: () -> Unit,
    public val onHatButtonClick: () -> Unit,
    generateNewRoomId: Boolean = false,
): GameFlowComponent {
    private val coroutineScope: CoroutineScope = CoroutineScope(coroutineContext + SupervisorJob()) // TODO: Understand what SupervisorJob does.

    init {
        componentContext.lifecycle.doOnDestroy(coroutineScope::cancel)
    }

    private val serverSignalStateFlow: MutableStateFlow<ServerSignal?> = MutableStateFlow(null)
    private val gameStateFlow: MutableStateFlow<UserGameState?> = MutableStateFlow(null)

    private val gameConnection = GameConnection(
        coroutineScope = coroutineScope,
        httpClient = defaultHttpClient,
        host = "localhost",
        port = 3000,
        path = "/ws",
        retryPeriod = 1000,
        onConnect = {
            println("Connected!")
            val currentGameState = gameStateFlow.value
            if (currentGameState != null)
                sendSerialized<ClientSignal>(
                    ClientSignal.JoinRoom(
                        roomId = currentGameState.roomDescription.id,
                        nickname = currentGameState.username
                    )
                )
        },
        onConnectionFailure = { println("Disconnected!") },
    )

    private var playerListStateFlow: MutableStateFlow<List<RoomDescription.Player>>? = null
    private var playerIndexStateFlow: MutableStateFlow<Int>? = null

    init {
        coroutineScope.launch {
            for (serverSignal in gameConnection.incoming) {
                serverSignalStateFlow.emit(serverSignal)
                println("new user game state: ${serverSignal.userGameState}")
                gameStateFlow.emit(serverSignal.userGameState)
            }
        }
        coroutineScope.launch {
            serverSignalStateFlow.collect { serverSignal ->
                when(serverSignal) {
                    null -> {}
                    is ServerSignal.StatusUpdate -> {}
                    is ServerSignal.RequestError -> {
                        // TODO
                        println(serverSignal.errorMessage)
                    }
                    is ServerSignal.ProvideFreeRoomId -> roomEnterPageComponent.roomIdField.update { serverSignal.freeRoomId }
                    is ServerSignal.GameStarts -> TODO()
                }
            }
        }
        coroutineScope.launch {
            gameStateFlow.collect { gameState ->
                println("New signal to process!")
                val childToActivate = run {
                    if (gameState == null) return@run ChildConfiguration.RoomEnterPageConfiguration

                    when (val gamePhase = gameState.roomDescription.phase) {
                        is RoomDescription.Phase.WaitingForPlayers -> {
                            playerListStateFlow =
                                playerListStateFlow ?: MutableStateFlow(gamePhase.currentPlayersList)
                            playerIndexStateFlow =
                                playerIndexStateFlow ?: MutableStateFlow(gameState.userIndex)
                            playerListStateFlow!!.update { gamePhase.currentPlayersList }
                            playerIndexStateFlow!!.update { gameState.userIndex }
                            ChildConfiguration.RoomFlowConfiguration(
                                roomId = gameState.roomDescription.id,
                                userList = playerListStateFlow!!,
                                playerIndex = playerIndexStateFlow!!,
                            )
                        }

                        is RoomDescription.Phase.GameInProgress ->
                            when (gamePhase.roundPhase) {
                                RoomDescription.RoundPhase.WaitingForPlayersToBeReady -> ChildConfiguration.RoundBreakConfiguration
                                is RoomDescription.RoundPhase.ExplanationInProgress -> ChildConfiguration.RoundInProgressConfiguration
                                is RoomDescription.RoundPhase.EditingInProgress -> ChildConfiguration.RoundEditingConfiguration
                            }

                        is RoomDescription.Phase.GameEnded -> ChildConfiguration.GameResultsConfiguration
                    }.also { println("Foo!") }
                }
                runOnUiThread {
                    navigation.activate(childToActivate)
                }
                println("Hello!")
                println(childSlot.value.child?.instance)
            }
        }
    }

    public val roomEnterPageComponent: RealRoomEnterPageComponent =
        RealRoomEnterPageComponent(
            backButtonEnabled = backButtonEnabled,
            onBackButtonClick = onBackButtonClick,
            onLanguageChange = onLanguageChange,
            onFeedbackButtonClick = onFeedbackButtonClick,
            onHatButtonClick = onHatButtonClick,
            generateRoomId = {
                coroutineScope.launch {
                    gameConnection.outgoing.send(ClientSignal.RequestFreeRoomId)
                }
            },
            onLetsGoAction = { roomId, nickname ->
                coroutineScope.launch(Dispatchers.Default) {
                    println("Sending JoinRoom")
                    gameConnection.outgoing.send(ClientSignal.JoinRoom(roomId = roomId, nickname = nickname))
                    println("Sent JoinRoom")
                }
            },
        )

    init {
        if (generateNewRoomId) coroutineScope.launch { gameConnection.outgoing.send(ClientSignal.RequestFreeRoomId) }
    }

    private val navigation = SlotNavigation<ChildConfiguration>()

    override val childSlot: Value<ChildSlot<ChildConfiguration, GameFlowComponent.Child>> = componentContext.childSlot(
        source = navigation,
        serializer = serializer<ChildConfiguration>(),
        initialConfiguration = { ChildConfiguration.RoomEnterPageConfiguration }
    ) { configuration, componentContext ->
        println("New child to create.")
        when(configuration) {
            is ChildConfiguration.RoomEnterPageConfiguration ->
                GameFlowComponent.Child.RoomEnter(
                    component = roomEnterPageComponent
                )
            is ChildConfiguration.RoomFlowConfiguration -> {
                val roomFlowCoroutineScope = CoroutineScope(coroutineContext)
                componentContext.lifecycle.doOnDestroy/*(roomFlowCoroutineScope::cancel)*/ {
                    roomFlowCoroutineScope.cancel()
                    println("Canceled!!!")
                }
                val settingsValue = MutableValue(gameStateFlow.value!!.roomDescription.settings)
                roomFlowCoroutineScope.launch { gameStateFlow.collect { println("!?!: $it"); it?.let { gameState -> settingsValue.update { gameState.roomDescription.settings } }; println("?!?: ${settingsValue.value}") } }
                println("It's RoomFlow creation.")
                GameFlowComponent.Child.RoomFlow(
                    component = run {
                        RealRoomFlowComponent(
                            componentContext = componentContext,
                            backButtonEnabled = true,
                            onBackButtonClick = {
                                navigation.activate(ChildConfiguration.RoomEnterPageConfiguration)
                                coroutineScope.launch {
                                    gameConnection.outgoing.send(ClientSignal.LeaveRoom)
                                }
                            },
                            onLanguageChange = onLanguageChange,
                            onFeedbackButtonClick = onFeedbackButtonClick,
                            onHatButtonClick = onHatButtonClick,
                            roomId = configuration.roomId,
                            userList = configuration.userList,
                            playerIndex = configuration.playerIndex,
                            settings = settingsValue,
                            onApplySettings = {
                                coroutineScope.launch { gameConnection.outgoing.send(ClientSignal.UpdateSettings(it)) }
                            },
                            onStartGame = {
                                coroutineScope.launch { gameConnection.outgoing.send(ClientSignal.StartGame) }
                            },
                        )
                    }
                ).also { println("It's RoomFlow returning.") }
            }
            ChildConfiguration.RoundBreakConfiguration ->
                GameFlowComponent.Child.RoundBreak(
                    component = RealRoundBreakPageComponent(
                        backButtonEnabled = backButtonEnabled,
                        wordsNumber = TODO(),
                        volumeOn = TODO(),
                        showFinishButton = TODO(),
                        speakerNickname = TODO(),
                        listenerNickname = TODO(),
                        userRole = TODO(),
                        onBackButtonClick = onBackButtonClick,
                        onLanguageChange = onLanguageChange,
                        onFeedbackButtonClick = onFeedbackButtonClick,
                        onHatButtonClick = onHatButtonClick,
                        onExitButtonClick = TODO(),
                        onVolumeButtonClick = TODO(),
                        onFinishButtonClick = TODO(),
                    )
                )
            ChildConfiguration.RoundInProgressConfiguration -> TODO()
            ChildConfiguration.RoundEditingConfiguration -> TODO()
            ChildConfiguration.GameResultsConfiguration -> TODO()
        }.also { println("New child: $it") }
    }

    @Serializable
    public sealed interface ChildConfiguration {
        @Serializable
        public data object RoomEnterPageConfiguration: ChildConfiguration
        @Serializable
        public data class RoomFlowConfiguration(
            val roomId: String,
            val userList: StateFlow<List<RoomDescription.Player>?>,
            val playerIndex: StateFlow<Int?>,
        ): ChildConfiguration
        @Serializable
        public data object RoundBreakConfiguration: ChildConfiguration
        @Serializable
        public data object RoundInProgressConfiguration: ChildConfiguration
        @Serializable
        public data object RoundEditingConfiguration: ChildConfiguration
        @Serializable
        public data object GameResultsConfiguration: ChildConfiguration
    }
}