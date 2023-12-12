package dev.lounres.thetruehat.client.desktop.components.game

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import dev.lounres.thetruehat.api.*
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.RoomDescription
import dev.lounres.thetruehat.api.models.UserGameState
import dev.lounres.thetruehat.client.common.utils.runOnUiThread
import dev.lounres.thetruehat.client.desktop.components.game.roomEnter.RealRoomEnterPageComponent
import dev.lounres.thetruehat.client.desktop.components.game.roomFlow.RealRoomFlowComponent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.coroutines.CoroutineContext


class RealGameFlowComponent(
    val componentContext: ComponentContext,
    val coroutineContext: CoroutineContext,
    val backButtonEnabled: Boolean,
    val onBackButtonClick: () -> Unit,
    val onLanguageChange: (language: Language) -> Unit,
    val onFeedbackButtonClick: () -> Unit,
    val onHatButtonClick: () -> Unit,
    generateNewRoomId: Boolean = false,
): GameFlowComponent {
    private val coroutineScope: CoroutineScope = CoroutineScope(coroutineContext + SupervisorJob()) // TODO: Understand what SupervisorJob does.

    init {
        componentContext.lifecycle.doOnDestroy(coroutineScope::cancel)
    }

    private val gameConnection = GameConnection(
        coroutineScope = coroutineScope,
        httpClient = HttpClient(CIO) {
            WebSockets {
                pingInterval = 1000
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
        },
        host = "localhost",
        port = 3000,
        path = "/ws",
        retryPeriod = 1000,
        onConnect = { println("Connected!") },
        onConnectionFailure = { println("Disconnected!") },
    )

    private val serverSignalStateFlow: MutableSharedFlow<ServerSignal?> = MutableStateFlow(null)
    private val gameStateFlow: MutableStateFlow<UserGameState?> = MutableStateFlow(null)

    private var playerListStateFlow: MutableStateFlow<List<RoomDescription.Player>>? = null
    private var playerIndexStateFlow: MutableStateFlow<Int>? = null

    init {
        coroutineScope.launch {
            for (serverSignal in gameConnection.incoming) {
                serverSignalStateFlow.emit(serverSignal)
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
                            when (gamePhase.roundState) {
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

    val roomEnterPageComponent: RealRoomEnterPageComponent =
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
            is ChildConfiguration.RoomFlowConfiguration ->
                GameFlowComponent.Child.RoomFlow(
                    component = run {
                        println("It's RoomFlow creation.")
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
                        ).also { println("It's RoomFlow returning.") }
                    }
                )
            ChildConfiguration.RoundBreakConfiguration ->
                GameFlowComponent.Child.RoundBreak(
                    component = TODO() /*RealRoundBreakPageComponent(
                        backButtonEnabled = true,

                    )*/
                )
            ChildConfiguration.RoundInProgressConfiguration -> TODO()
            ChildConfiguration.RoundEditingConfiguration -> TODO()
            ChildConfiguration.GameResultsConfiguration -> TODO()
        }.also { println("New child: $it") }
    }

    @Serializable
    sealed interface ChildConfiguration {
        @Serializable
        data object RoomEnterPageConfiguration: ChildConfiguration
        @Serializable
        data class RoomFlowConfiguration(
            val roomId: String,
            val userList: StateFlow<List<RoomDescription.Player>?>,
            val playerIndex: StateFlow<Int?>,
        ): ChildConfiguration
        @Serializable
        data object RoundBreakConfiguration: ChildConfiguration
        @Serializable
        data object RoundInProgressConfiguration: ChildConfiguration
        @Serializable
        data object RoundEditingConfiguration: ChildConfiguration
        @Serializable
        data object GameResultsConfiguration: ChildConfiguration
    }
}