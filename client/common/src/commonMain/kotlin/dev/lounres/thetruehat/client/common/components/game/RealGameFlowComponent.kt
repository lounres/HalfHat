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
import dev.lounres.thetruehat.api.models.Settings
import dev.lounres.thetruehat.api.models.UserGameState
import dev.lounres.thetruehat.api.signals.ClientSignal
import dev.lounres.thetruehat.api.signals.ServerSignal
import dev.lounres.thetruehat.client.common.components.game.roomEnter.RealRoomEnterPageComponent
import dev.lounres.thetruehat.client.common.components.game.roomFlow.RealRoomFlowComponent
import dev.lounres.thetruehat.client.common.components.game.roundBreak.RealRoundBreakPageComponent
import dev.lounres.thetruehat.client.common.components.game.roundBreak.RoundBreakPageComponent
import dev.lounres.thetruehat.client.common.components.game.roundCountdown.RealRoundCountdownPageComponent
import dev.lounres.thetruehat.client.common.components.game.roundEditing.RealRoundEditingPageComponent
import dev.lounres.thetruehat.client.common.components.game.roundInProgress.RealRoundInProgressPageComponent
import dev.lounres.thetruehat.client.common.components.game.roundInProgress.RoundInProgressPageComponent
import dev.lounres.thetruehat.client.common.logger
import dev.lounres.thetruehat.client.common.utils.defaultHttpClient
import dev.lounres.thetruehat.client.common.utils.runOnUiThread
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Contextual
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
    private fun activate(childToActivate: ChildConfiguration) {
        runOnUiThread {
            navigation.activate(childToActivate)
        }
    }

    init {
        componentContext.lifecycle.doOnDestroy(coroutineScope::cancel)
    }

    private val serverSignalStateFlow: MutableStateFlow<ServerSignal?> = MutableStateFlow(null)
    private val gameStateFlow: MutableStateFlow<UserGameState?> = MutableStateFlow(null)

    private val roomId: MutableValue<String> = MutableValue("")
    private val settings: MutableValue<Settings> = MutableValue(defaultSettings)
    private val playersList: MutableValue<List<RoomDescription.Player>> = MutableValue(emptyList())
    private val playerIndex: MutableValue<Int> = MutableValue(0)
    private val unitsUntilEnd: MutableValue<RoomDescription.UnitsUntilEnd> = MutableValue(RoomDescription.UnitsUntilEnd.Words(0))
    private val volumeOn: MutableValue<Boolean> = MutableValue(true)
    private val showFinishButton: MutableValue<Boolean> = MutableValue(false)
    private val speakerNickname: MutableValue<String> = MutableValue("")
    private val listenerNickname: MutableValue<String> = MutableValue("")
    private val roundBreakUserRole: MutableValue<RoundBreakPageComponent.UserRole> = MutableValue(RoundBreakPageComponent.UserRole.ListenerWaiting)
    private val roundInProgressUserRole: MutableValue<RoundInProgressPageComponent.UserRole> = MutableValue(RoundInProgressPageComponent.UserRole.Listener)
    private val wordToExplain: MutableValue<String> = MutableValue("")
    private val countsUntilStart: MutableValue<Int> = MutableValue(0)
    private var countsUntilStartJob: Job? = null
    private val countsUntilEnd: MutableValue<Int> = MutableValue(0) // TODO: Actually implement timer
    private var countsUntilEndJob: Job? = null

    private val gameConnection = GameConnection(
        coroutineScope = coroutineScope,
        httpClient = defaultHttpClient,
        host = "localhost",
        port = 3000,
        path = "/ws",
        retryPeriod = 1000,
        onConnect = {
            val currentGameState = gameStateFlow.value
            if (currentGameState != null)
                sendSerialized<ClientSignal>(
                    ClientSignal.JoinRoom(
                        roomId = currentGameState.roomDescription.id,
                        nickname = currentGameState.username
                    )
                )
        },
        onConnectionFailure = {
            // TODO: Add error badge
        },
    )

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
                        logger.warn { serverSignal.errorMessage }
                        // TODO: Add error badge
                    }
                    is ServerSignal.ProvideFreeRoomId -> roomEnterPageComponent.roomIdField.update { serverSignal.freeRoomId }
                    is ServerSignal.ProvideRoomResults -> TODO()
                }
            }
        }
        coroutineScope.launch {
            gameStateFlow.collect { gameState ->
                // TODO: Refactor child activation
                val childToActivate = run {
                    if (gameState == null) return@run ChildConfiguration.RoomEnterPageConfiguration

                    roomId.update { gameState.roomDescription.id }
                    settings.update { gameState.roomDescription.settings }
                    playerIndex.update { gameState.userIndex }

                    when (val gamePhase = gameState.roomDescription.phase) {
                        is RoomDescription.Phase.WaitingForPlayers -> {
                            playersList.update { gamePhase.currentPlayersList }
                            ChildConfiguration.RoomFlowConfiguration(
                                roomId = gameState.roomDescription.id,
                                playerIndex = playerIndex,
                            )
                        }
                        is RoomDescription.Phase.GameInProgress -> {
                            unitsUntilEnd.update { gamePhase.unitsUntilEnd }
                            showFinishButton.update { gamePhase.playersList.indexOfFirst { it.online } == gameState.userIndex }
                            speakerNickname.update { gamePhase.playersList[gamePhase.speaker].username }
                            listenerNickname.update { gamePhase.playersList[gamePhase.listener].username }

                            when (val roundPhase = gamePhase.roundPhase) {
                                is RoomDescription.RoundPhase.WaitingForPlayersToBeReady -> {
                                    roundBreakUserRole.update {
                                        when(gameState.userIndex) {
                                            gamePhase.speaker ->
                                                if (roundPhase.speakerReady) RoundBreakPageComponent.UserRole.SpeakerReady
                                                else RoundBreakPageComponent.UserRole.SpeakerWaiting
                                            gamePhase.listener ->
                                                if (roundPhase.listenerReady) RoundBreakPageComponent.UserRole.ListenerReady
                                                else RoundBreakPageComponent.UserRole.ListenerWaiting
                                            else -> RoundBreakPageComponent.UserRole.SpeakerIn(0u) // TODO: Implement schedule
                                        }
                                    }
                                    ChildConfiguration.RoundBreakConfiguration
                                }
                                is RoomDescription.RoundPhase.Countdown -> {
                                    val millisecondsUntilStart = roundPhase.millisecondsUntilStart
                                    val secondsUntilStart = millisecondsUntilStart / 1000
                                    countsUntilStart.update { (secondsUntilStart + 1).toInt() }
                                    countsUntilStartJob?.cancel()
                                    countsUntilStartJob = coroutineScope.launch {
                                        delay(millisecondsUntilStart % 1000)
                                        countsUntilStart.update { it-1 }
                                        for (i in 0 ..< secondsUntilStart) {
                                            delay(1000)
                                            countsUntilStart.update { it-1 }
                                        }
                                        countsUntilStartJob = null
                                    }
                                    ChildConfiguration.RoundCountdownConfiguration
                                }
                                is RoomDescription.RoundPhase.ExplanationInProgress -> {
                                    roundInProgressUserRole.update {
                                        when(gameState.userIndex) {
                                            gamePhase.speaker -> {
                                                wordToExplain.update { roundPhase.word!! }
                                                RoundInProgressPageComponent.UserRole.Speaker(wordToExplain)
                                            }
                                            gamePhase.listener ->
                                                RoundInProgressPageComponent.UserRole.Listener
                                            else -> RoundInProgressPageComponent.UserRole.SpeakerIn(0u) // TODO: Implement schedule
                                        }
                                    }
                                    val millisecondsUntilEnd = roundPhase.millisecondsUntilEnd
                                    val secondsUntilEnd = millisecondsUntilEnd / 1000
                                    countsUntilEnd.update { (secondsUntilEnd + 1).toInt() }
                                    countsUntilEndJob?.cancel()
                                    countsUntilEndJob = coroutineScope.launch {
                                        delay(millisecondsUntilEnd % 1000)
                                        countsUntilEnd.update { it-1 }
                                        for (i in 0 ..< secondsUntilEnd) {
                                            delay(1000)
                                            countsUntilEnd.update { it-1 }
                                        }
                                        countsUntilEndJob = null
                                    }
                                    ChildConfiguration.RoundInProgressConfiguration
                                }
                                is RoomDescription.RoundPhase.EditingInProgress ->
                                    ChildConfiguration.RoundEditingConfiguration(
                                        resultsToEdit = roundPhase.wordsToEdit
                                    )
                            }
                        }
                        is RoomDescription.Phase.GameEnded -> ChildConfiguration.GameResultsConfiguration
                    }
                }
                activate(childToActivate)
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
                    gameConnection.outgoing.send(ClientSignal.JoinRoom(roomId = roomId, nickname = nickname))
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
        logger.info { "New child slot configuration in GameFlowComponent: $configuration" }
        when(configuration) {
            is ChildConfiguration.RoomEnterPageConfiguration ->
                GameFlowComponent.Child.RoomEnter(
                    component = roomEnterPageComponent
                )
            is ChildConfiguration.RoomFlowConfiguration -> {
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
                            userList = playersList,
                            playerIndex = configuration.playerIndex,
                            settings = settings,
                            onApplySettings = {
                                coroutineScope.launch { gameConnection.outgoing.send(ClientSignal.UpdateSettings(it)) }
                            },
                            onStartGame = {
                                coroutineScope.launch { gameConnection.outgoing.send(ClientSignal.StartGame) }
                            },
                        )
                    }
                )
            }
            is ChildConfiguration.RoundBreakConfiguration ->
                GameFlowComponent.Child.RoundBreak(
                    component = RealRoundBreakPageComponent(
                        backButtonEnabled = true,
                        unitsUntilEnd = unitsUntilEnd,
                        volumeOn = volumeOn,
                        showFinishButton = showFinishButton,
                        speakerNickname = speakerNickname,
                        listenerNickname = listenerNickname,
                        userRole = roundBreakUserRole,
                        onBackButtonClick = {
                            navigation.activate(ChildConfiguration.RoomEnterPageConfiguration)
                            coroutineScope.launch {
                                gameConnection.outgoing.send(ClientSignal.LeaveRoom)
                            }
                        },
                        onLanguageChange = onLanguageChange,
                        onFeedbackButtonClick = onFeedbackButtonClick,
                        onHatButtonClick = onHatButtonClick,
                        onExitButtonClick = {
                            navigation.activate(ChildConfiguration.RoomEnterPageConfiguration)
                            coroutineScope.launch {
                                gameConnection.outgoing.send(ClientSignal.LeaveRoom)
                            }
                        },
                        onVolumeButtonClick = { volumeOn.update { !it } },
                        onFinishButtonClick = {
                            coroutineScope.launch { gameConnection.outgoing.send(ClientSignal.EndGame) }
                        },
                        onReadyButtonClick = {
                            coroutineScope.launch { gameConnection.outgoing.send(ClientSignal.ReadyForTheRound) }
                        },
                    )
                )
            ChildConfiguration.RoundCountdownConfiguration ->
                GameFlowComponent.Child.RoundCountdown(
                    component = RealRoundCountdownPageComponent(
                        backButtonEnabled = true,
                        unitsUntilEnd = unitsUntilEnd,
                        showFinishButton = showFinishButton,
                        volumeOn = volumeOn,
                        countsUntilStart = countsUntilStart,
                        onBackButtonClick = {
                            navigation.activate(ChildConfiguration.RoomEnterPageConfiguration)
                            coroutineScope.launch {
                                gameConnection.outgoing.send(ClientSignal.LeaveRoom)
                            }
                        },
                        onLanguageChange = onLanguageChange,
                        onFeedbackButtonClick = onFeedbackButtonClick,
                        onHatButtonClick = onHatButtonClick,
                        onVolumeButtonClick = { volumeOn.update { !it } },
                        onFinishButtonClick = {
                            coroutineScope.launch { gameConnection.outgoing.send(ClientSignal.EndGame) }
                        },
                        onExitButtonClick = {
                            navigation.activate(ChildConfiguration.RoomEnterPageConfiguration)
                            coroutineScope.launch {
                                gameConnection.outgoing.send(ClientSignal.LeaveRoom)
                            }
                        },
                    )
                )
            ChildConfiguration.RoundInProgressConfiguration ->
                GameFlowComponent.Child.RoundInProgress(
                    component = RealRoundInProgressPageComponent(
                        backButtonEnabled = true,
                        unitsUntilEnd = unitsUntilEnd,
                        showFinishButton = showFinishButton,
                        volumeOn = volumeOn,
                        speakerNickname = speakerNickname,
                        listenerNickname = listenerNickname,
                        userRole = roundInProgressUserRole,
                        countsUntilEnd = countsUntilEnd,
                        onBackButtonClick = {
                            navigation.activate(ChildConfiguration.RoomEnterPageConfiguration)
                            coroutineScope.launch {
                                gameConnection.outgoing.send(ClientSignal.LeaveRoom)
                            }
                        },
                        onLanguageChange = onLanguageChange,
                        onFeedbackButtonClick = onFeedbackButtonClick,
                        onHatButtonClick = onHatButtonClick,
                        onVolumeButtonClick = { volumeOn.update { !it } },
                        onFinishButtonClick = {
                            coroutineScope.launch { gameConnection.outgoing.send(ClientSignal.EndGame) }
                        },
                        onExitButtonClick = {
                            navigation.activate(ChildConfiguration.RoomEnterPageConfiguration)
                            coroutineScope.launch {
                                gameConnection.outgoing.send(ClientSignal.LeaveRoom)
                            }
                        },
                        onExplainedButtonClick = {
                            coroutineScope.launch {
                                gameConnection.outgoing.send(
                                    ClientSignal.ExplanationResult(
                                        result = RoomDescription.WordExplanationResult.State.Explained
                                    )
                                )
                            }
                        },
                        onNotExplainedButtonClick = {
                            coroutineScope.launch {
                                gameConnection.outgoing.send(
                                    ClientSignal.ExplanationResult(
                                        result = RoomDescription.WordExplanationResult.State.NotExplained
                                    )
                                )
                            }
                        },
                        onImproperlyExplainedButtonClick = {
                            coroutineScope.launch {
                                gameConnection.outgoing.send(
                                    ClientSignal.ExplanationResult(
                                        result = RoomDescription.WordExplanationResult.State.Mistake
                                    )
                                )
                            }
                        }
                    )
                )
            is ChildConfiguration.RoundEditingConfiguration -> {
                val mutableExplanationResults = configuration.resultsToEdit?.map { MutableValue(it) }
                GameFlowComponent.Child.RoundEditing(
                    component = RealRoundEditingPageComponent(
                        backButtonEnabled = true,
                        unitsUntilEnd = unitsUntilEnd,
                        volumeOn = volumeOn,
                        showFinishButton = showFinishButton,
                        onBackButtonClick = {
                            navigation.activate(ChildConfiguration.RoomEnterPageConfiguration)
                            coroutineScope.launch {
                                gameConnection.outgoing.send(ClientSignal.LeaveRoom)
                            }
                        },
                        onLanguageChange = onLanguageChange,
                        onFeedbackButtonClick = onFeedbackButtonClick,
                        onHatButtonClick = onHatButtonClick,
                        onExitButtonClick = {
                            navigation.activate(ChildConfiguration.RoomEnterPageConfiguration)
                            coroutineScope.launch {
                                gameConnection.outgoing.send(ClientSignal.LeaveRoom)
                            }
                        },
                        onVolumeButtonClick = { volumeOn.update { !it } },
                        onFinishButtonClick = {
                            coroutineScope.launch { gameConnection.outgoing.send(ClientSignal.EndGame) }
                        },
                        explanationResults = mutableExplanationResults,
                        onSubmitButtonClick = {
                            coroutineScope.launch {
                                gameConnection.outgoing.send(ClientSignal.SubmitResults(mutableExplanationResults!!.map { it.value }))
                            }
                        }
                    )
                )
            }
            ChildConfiguration.GameResultsConfiguration -> TODO()
        }
    }

    @Serializable
    public sealed interface ChildConfiguration {
        @Serializable
        public data object RoomEnterPageConfiguration: ChildConfiguration
        @Serializable
        public data class RoomFlowConfiguration(
            val roomId: String,
            val playerIndex: @Contextual Value<Int>,
        ): ChildConfiguration
        @Serializable
        public data object RoundBreakConfiguration: ChildConfiguration
        @Serializable
        public data object RoundCountdownConfiguration: ChildConfiguration
        @Serializable
        public data object RoundInProgressConfiguration: ChildConfiguration
        @Serializable
        public data class RoundEditingConfiguration(
            val resultsToEdit: List<RoomDescription.WordExplanationResult>?
        ): ChildConfiguration
        @Serializable
        public data object GameResultsConfiguration: ChildConfiguration
    }
}