package dev.lounres.thetruehat.client.common.components.onlineGame

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.api.models.Settings
import dev.lounres.thetruehat.api.models.UserGameState
import dev.lounres.thetruehat.api.signals.ClientSignal
import dev.lounres.thetruehat.api.signals.ServerSignal
import dev.lounres.thetruehat.client.common.components.onlineGame.gameResults.RealGameResultsPageComponent
import dev.lounres.thetruehat.client.common.components.onlineGame.roomEnter.RealRoomEnterPageComponent
import dev.lounres.thetruehat.client.common.components.onlineGame.roomFlow.RealRoomFlowComponent
import dev.lounres.thetruehat.client.common.components.onlineGame.roundBreak.RealRoundBreakPageComponent
import dev.lounres.thetruehat.client.common.components.onlineGame.roundBreak.RoundBreakPageComponent
import dev.lounres.thetruehat.client.common.components.onlineGame.roundCountdown.RealRoundCountdownPageComponent
import dev.lounres.thetruehat.client.common.components.onlineGame.roundEditing.RealRoundEditingPageComponent
import dev.lounres.thetruehat.client.common.components.onlineGame.roundInProgress.RealRoundInProgressPageComponent
import dev.lounres.thetruehat.client.common.components.onlineGame.roundInProgress.RoundInProgressPageComponent
import dev.lounres.thetruehat.client.common.logger
import dev.lounres.thetruehat.client.common.utils.*
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import kotlin.coroutines.CoroutineContext


public class RealOnlineGameFlowComponent(
    public val componentContext: ComponentContext,
    public val coroutineContext: CoroutineContext,
    public val backButtonEnabled: Boolean,
    public val onBackButtonClick: () -> Unit,
    public val onLanguageChange: (language: Language) -> Unit,
    public val onFeedbackButtonClick: () -> Unit,
    public val onHatButtonClick: () -> Unit,
    generateNewRoomId: Boolean = false,
): OnlineGameFlowComponent {
    private val coroutineScope: CoroutineScope = CoroutineScope(coroutineContext + SupervisorJob()) // TODO: Understand what SupervisorJob does.

    init {
        componentContext.lifecycle.doOnDestroy(coroutineScope::cancel)
    }

    private val gameStateFlow: MutableStateFlow<UserGameState?> = MutableStateFlow(null)

    private val volumeOn: MutableValue<Boolean> = MutableValue(true)

    private val gameConnection = OnlineGameConnection(
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
                        roomId = currentGameState.id,
                        nickname = when(val gamePhase = currentGameState.phase) {
                            is UserGameState.Phase.WaitingForPlayers -> gamePhase.username
                            is UserGameState.Phase.GameInProgress -> gamePhase.username
                            is UserGameState.Phase.GameEnded -> TODO()
                        }
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
                gameStateFlow.emit(serverSignal.userGameState)
                when(serverSignal) {
                    is ServerSignal.StatusUpdate -> {}
                    is ServerSignal.RequestError -> {
                        logger.warn { serverSignal.errorMessage }
                        // TODO: Add error badge
                    }
                    is ServerSignal.ProvideFreeRoomId -> roomEnterPageComponent.roomIdField.update { serverSignal.freeRoomId }
                }
            }
        }
        coroutineScope.launch {
            gameStateFlow.collect { gameState ->
                if (gameState == null) {
                    activate(ChildConfiguration.RoomEnterPageConfiguration)
                    return@collect
                }

                val childConfiguration = childSlot.child!!.configuration
                when (val gamePhase = gameState.phase) {
                    is UserGameState.Phase.WaitingForPlayers -> {
                        if (childConfiguration is ChildConfiguration.RoomFlowConfiguration) {
                            with(childConfiguration) {
                                roomId.update { gameState.id }
                                availableDictionaries.update { gamePhase.availableDictionaries }
                                playersList.update { gamePhase.currentPlayersList }
                                playerIndex.update { gamePhase.userIndex }
                                settings.update { gameState.settings }
                            }
                        } else activate(
                            ChildConfiguration.RoomFlowConfiguration(
                                roomId = MutableValue(gameState.id),
                                availableDictionaries = MutableValue(gamePhase.availableDictionaries),
                                playersList = MutableValue(gamePhase.currentPlayersList),
                                playerIndex = MutableValue(gamePhase.userIndex),
                                settings = MutableValue(gameState.settings),
                            )
                        )
                    }
                    is UserGameState.Phase.GameInProgress -> {
                        val unitsUntilEnd = gamePhase.unitsUntilEnd
                        val showFinishButton = gamePhase.playersList.indexOfFirst { it.online } == gamePhase.userIndex
                        println("???${gamePhase.playersList.indexOfFirst { it.online }}, ${gamePhase.userIndex}, $showFinishButton")
                        val speakerNickname = gamePhase.playersList[gamePhase.speaker].username
                        val listenerNickname = gamePhase.playersList[gamePhase.listener].username

                        when (val roundPhase = gamePhase.roundPhase) {
                            is UserGameState.RoundPhase.WaitingForPlayersToBeReady -> {
                                val userRole =
                                    when(gamePhase.userIndex) {
                                        gamePhase.speaker ->
                                            if (roundPhase.speakerReady) RoundBreakPageComponent.UserRole.SpeakerReady
                                            else RoundBreakPageComponent.UserRole.SpeakerWaiting
                                        gamePhase.listener ->
                                            if (roundPhase.listenerReady) RoundBreakPageComponent.UserRole.ListenerReady
                                            else RoundBreakPageComponent.UserRole.ListenerWaiting
                                        else -> RoundBreakPageComponent.UserRole.SpeakerIn(0u) // TODO: Implement schedule
                                    }
                                if (childConfiguration is ChildConfiguration.RoundBreakConfiguration) {
                                    childConfiguration.unitsUntilEnd.update { unitsUntilEnd }
                                    childConfiguration.showFinishButton.update { showFinishButton }
                                    childConfiguration.speakerNickname.update { speakerNickname }
                                    childConfiguration.listenerNickname.update { listenerNickname }
                                    childConfiguration.userRole.update { userRole }
                                } else activate(
                                    ChildConfiguration.RoundBreakConfiguration(
                                        unitsUntilEnd = MutableValue(unitsUntilEnd),
                                        showFinishButton = MutableValue(showFinishButton),
                                        speakerNickname = MutableValue(speakerNickname),
                                        listenerNickname = MutableValue(listenerNickname),
                                        userRole = MutableValue(userRole),
                                    )
                                )
                            }
                            is UserGameState.RoundPhase.Countdown -> {
                                if (childConfiguration is ChildConfiguration.RoundCountdownConfiguration) {
                                    childConfiguration.unitsUntilEnd.update { unitsUntilEnd }
                                    childConfiguration.showFinishButton.update { showFinishButton }
                                    childConfiguration.millisecondsUntilStart.update { roundPhase.millisecondsUntilStart }
                                } else activate(
                                    ChildConfiguration.RoundCountdownConfiguration(
                                        unitsUntilEnd = MutableValue(unitsUntilEnd),
                                        showFinishButton = MutableValue(showFinishButton),
                                        millisecondsUntilStart = MutableValue(roundPhase.millisecondsUntilStart),
                                    )
                                )
                            }
                            is UserGameState.RoundPhase.ExplanationInProgress -> {
                                val userRole =
                                    when(gamePhase.userIndex) {
                                        gamePhase.speaker -> {
                                            RoundInProgressPageComponent.UserRole.Speaker(roundPhase.word!!)
                                        }
                                        gamePhase.listener ->
                                            RoundInProgressPageComponent.UserRole.Listener
                                        else -> RoundInProgressPageComponent.UserRole.SpeakerIn(0u) // TODO: Implement schedule
                                    }
                                if (childConfiguration is ChildConfiguration.RoundInProgressConfiguration) {
                                    childConfiguration.unitsUntilEnd.update { unitsUntilEnd }
                                    childConfiguration.showFinishButton.update { showFinishButton }
                                    childConfiguration.speakerNickname.update { speakerNickname }
                                    childConfiguration.listenerNickname.update { listenerNickname }
                                    childConfiguration.userRole.update { userRole }
                                    childConfiguration.millisecondsUntilEnd.update { roundPhase.millisecondsUntilEnd }
                                } else activate(
                                    ChildConfiguration.RoundInProgressConfiguration(
                                        unitsUntilEnd = MutableValue(unitsUntilEnd),
                                        showFinishButton = MutableValue(showFinishButton),
                                        speakerNickname = MutableValue(speakerNickname),
                                        listenerNickname = MutableValue(listenerNickname),
                                        userRole = MutableValue(userRole),
                                        millisecondsUntilEnd = MutableValue(roundPhase.millisecondsUntilEnd)
                                    )
                                )
                            }
                            is UserGameState.RoundPhase.EditingInProgress ->
                                activate(
                                    ChildConfiguration.RoundEditingConfiguration(
                                        unitsUntilEnd = MutableValue(unitsUntilEnd),
                                        showFinishButton = MutableValue(showFinishButton),
                                        resultsToEdit = roundPhase.wordsToEdit,
                                    )
                                )
                        }
                    }
                    is UserGameState.Phase.GameEnded ->
                        activate(
                            ChildConfiguration.GameResultsConfiguration(
                                resultList = gamePhase.results,
                            )
                        )
                }
            }
        }
    }

    private val roomEnterPageComponent: RealRoomEnterPageComponent =
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
    private fun activate(childToActivate: ChildConfiguration) {
        runOnUiThread {
            navigation.activate(childToActivate)
        }
    }

    override val childSlot: Value<ChildSlot<ChildConfiguration, OnlineGameFlowComponent.Child>> = componentContext.childSlot(
        source = navigation,
        serializer = serializer<ChildConfiguration>(),
        initialConfiguration = { ChildConfiguration.RoomEnterPageConfiguration }
    ) { configuration, componentContext ->
        logger.info { "New child slot configuration in GameFlowComponent: $configuration" }
        when(configuration) {
            is ChildConfiguration.RoomEnterPageConfiguration ->
                OnlineGameFlowComponent.Child.RoomEnter(
                    component = roomEnterPageComponent
                )
            is ChildConfiguration.RoomFlowConfiguration ->
                OnlineGameFlowComponent.Child.RoomFlow(
                    component = RealRoomFlowComponent(
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
                        availableDictionaries = configuration.availableDictionaries,
                        userList = configuration.playersList,
                        playerIndex = configuration.playerIndex,
                        settings = configuration.settings,
                        onApplySettings = {
                            coroutineScope.launch { gameConnection.outgoing.send(ClientSignal.UpdateSettings(it)) }
                        },
                        onStartGame = {
                            coroutineScope.launch { gameConnection.outgoing.send(ClientSignal.StartGame) }
                        },
                    )
                )
            is ChildConfiguration.RoundBreakConfiguration ->
                OnlineGameFlowComponent.Child.RoundBreak(
                    component = RealRoundBreakPageComponent(
                        backButtonEnabled = true,
                        unitsUntilEnd = configuration.unitsUntilEnd,
                        volumeOn = volumeOn,
                        showFinishButton = configuration.showFinishButton,
                        speakerNickname = configuration.speakerNickname,
                        listenerNickname = configuration.listenerNickname,
                        userRole = configuration.userRole,
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
            is ChildConfiguration.RoundCountdownConfiguration ->
                OnlineGameFlowComponent.Child.RoundCountdown(
                    component = RealRoundCountdownPageComponent(
                        componentContext = componentContext,
                        coroutineContext = coroutineContext,
                        backButtonEnabled = true,
                        unitsUntilEnd = configuration.unitsUntilEnd,
                        showFinishButton = configuration.showFinishButton,
                        volumeOn = volumeOn,
                        millisecondsUntilStart = configuration.millisecondsUntilStart,
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
            is ChildConfiguration.RoundInProgressConfiguration ->
                OnlineGameFlowComponent.Child.RoundInProgress(
                    component = RealRoundInProgressPageComponent(
                        componentContext = componentContext,
                        coroutineContext = coroutineContext,
                        backButtonEnabled = true,
                        unitsUntilEnd = configuration.unitsUntilEnd,
                        showFinishButton = configuration.showFinishButton,
                        volumeOn = volumeOn,
                        speakerNickname = configuration.speakerNickname,
                        listenerNickname = configuration.listenerNickname,
                        userRole = configuration.userRole,
                        millisecondsUntilEnd = configuration.millisecondsUntilEnd,
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
                                        result = UserGameState.WordExplanationResult.State.Explained
                                    )
                                )
                            }
                        },
                        onNotExplainedButtonClick = {
                            coroutineScope.launch {
                                gameConnection.outgoing.send(
                                    ClientSignal.ExplanationResult(
                                        result = UserGameState.WordExplanationResult.State.NotExplained
                                    )
                                )
                            }
                        },
                        onImproperlyExplainedButtonClick = {
                            coroutineScope.launch {
                                gameConnection.outgoing.send(
                                    ClientSignal.ExplanationResult(
                                        result = UserGameState.WordExplanationResult.State.Mistake
                                    )
                                )
                            }
                        }
                    )
                )
            is ChildConfiguration.RoundEditingConfiguration -> {
                val mutableExplanationResults = configuration.resultsToEdit?.map { MutableValue(it) }
                OnlineGameFlowComponent.Child.RoundEditing(
                    component = RealRoundEditingPageComponent(
                        backButtonEnabled = true,
                        unitsUntilEnd = configuration.unitsUntilEnd,
                        volumeOn = volumeOn,
                        showFinishButton = configuration.showFinishButton,
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
            is ChildConfiguration.GameResultsConfiguration ->
                OnlineGameFlowComponent.Child.GameResults(
                    component = RealGameResultsPageComponent(
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
                        resultList = configuration.resultList,
                    )
                )
        }
    }

    @Serializable
    public sealed interface ChildConfiguration {
        @Serializable
        public data object RoomEnterPageConfiguration: ChildConfiguration
        @Serializable
        public data class RoomFlowConfiguration(
            val roomId: @Contextual MutableValue<String>,
            val availableDictionaries: @Contextual MutableValue<List<UserGameState.ServerDictionary>>,
            val playersList: @Contextual MutableValue<List<UserGameState.Player>>,
            val playerIndex: @Contextual MutableValue<Int>,
            val settings: @Contextual MutableValue<Settings>,
        ): ChildConfiguration
        @Serializable
        public data class RoundBreakConfiguration(
            val unitsUntilEnd: @Contextual MutableValue<UserGameState.UnitsUntilEnd>,
            val showFinishButton: @Contextual MutableValue<Boolean>,
            val speakerNickname: @Contextual MutableValue<String>,
            val listenerNickname: @Contextual MutableValue<String>,
            val userRole: @Contextual MutableValue<RoundBreakPageComponent.UserRole>,
        ): ChildConfiguration
        @Serializable
        public data class RoundCountdownConfiguration(
            val unitsUntilEnd: @Contextual MutableValue<UserGameState.UnitsUntilEnd>,
            val showFinishButton: @Contextual MutableValue<Boolean>,
            val millisecondsUntilStart: @Contextual MutableValue<Long>,
        ): ChildConfiguration
        @Serializable
        public data class RoundInProgressConfiguration(
            val unitsUntilEnd: @Contextual MutableValue<UserGameState.UnitsUntilEnd>,
            val showFinishButton: @Contextual MutableValue<Boolean>,
            val speakerNickname: @Contextual MutableValue<String>,
            val listenerNickname: @Contextual MutableValue<String>,
            val userRole: @Contextual MutableValue<RoundInProgressPageComponent.UserRole>,
            val millisecondsUntilEnd: @Contextual MutableValue<Long>,
        ): ChildConfiguration
        @Serializable
        public data class RoundEditingConfiguration(
            val unitsUntilEnd: @Contextual MutableValue<UserGameState.UnitsUntilEnd>,
            val showFinishButton: @Contextual MutableValue<Boolean>,
            val resultsToEdit: List<UserGameState.WordExplanationResult>?
        ): ChildConfiguration
        @Serializable
        public data class GameResultsConfiguration(
            val resultList: List<UserGameState.GameResult>
        ): ChildConfiguration
    }
}