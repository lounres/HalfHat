package dev.lounres.halfhat.server

import dev.lounres.halfhat.api.client.ClientApi
import dev.lounres.halfhat.api.server.ServerApi
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.KoneList
import dev.lounres.kone.collections.KoneMutableListNode
import dev.lounres.kone.collections.KoneMutableNoddedList
import dev.lounres.kone.collections.KoneSet
import dev.lounres.kone.collections.emptyKoneList
import dev.lounres.kone.collections.implementations.KoneArrayResizableLinkedNoddedList
import dev.lounres.kone.collections.koneMutableSetOf
import dev.lounres.kone.collections.toKoneList
import dev.lounres.kone.collections.utils.filter
import dev.lounres.kone.collections.utils.firstIndexOf
import dev.lounres.kone.collections.utils.firstThatOrNull
import dev.lounres.kone.collections.utils.forEach
import dev.lounres.kone.collections.utils.forEachIndexed
import dev.lounres.kone.collections.utils.map
import dev.lounres.kone.collections.utils.mapTo
import dev.lounres.kone.comparison.absoluteEquality
import dev.lounres.kone.context.invoke
import dev.lounres.logKube.core.DefaultJvmLogWriter
import dev.lounres.logKube.core.JvmLogger
import dev.lounres.logKube.core.LogAcceptor
import io.ktor.serialization.deserialize
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.server.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random
import kotlin.random.asKotlinRandom


val logger = JvmLogger(
    name = "HalfHat main server logger",
    logAcceptors = mutableListOf(
        LogAcceptor(DefaultJvmLogWriter) /*{ it.level >= Level.INFO }*/,
    )
)

sealed interface OnlineGameWordsProvider: GameStateMachine.WordsProvider {
    interface ServerDictionary: OnlineGameWordsProvider {
        val name: String
    }
}

class Room(
    val name: String,
) {
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    
    private val structuralMutex: Mutex = Mutex()
    
    class Player(
        val room: Room,
        val name: String,
    ) {
        val connectionRegistry: KoneMutableNoddedList<Connection> = KoneArrayResizableLinkedNoddedList()
        
        class AttachmentHandle(val player: Player, private val registration: KoneMutableListNode<Connection>) {
            suspend fun detachConnection() {
                with(player.room) {
                    structuralMutex.withLock {
                        registration.remove()
                        
                        when (val currentGameState = gameStateMachine.state.value) {
                            is GameStateMachine.State.GameInitialisation -> {
                                val result = gameStateMachine.updateGameSettings(
                                    playersList = playersRegistry.filter { it.isOnline },
                                    settingsBuilder = currentGameState.settingsBuilder,
                                )
                                
                                when (result) {
                                    GameStateMachine.Result.GameSettingsUpdateResult.InvalidState -> {
                                        logger.warn { "Game state machine in state 'GameInitialisation' refused to update settings" }
                                    }
                                    GameStateMachine.Result.GameSettingsUpdateResult.Success -> {}
                                }
                            }
                            is GameStateMachine.State.RoundWaiting,
                            is GameStateMachine.State.RoundPreparation,
                            is GameStateMachine.State.RoundExplanation,
                            is GameStateMachine.State.RoundLastGuess,
                            is GameStateMachine.State.RoundEditing,
                            is GameStateMachine.State.GameResults, -> {}
                        }
                        
                        updateGamePlayers()
                        requestStatusUpdate()
                    }
                }
            }
        }
    }
    
    private val Player.isOnline: Boolean get() = connectionRegistry.size != 0u
    
    private val playersRegistry: KoneMutableNoddedList<Player> = KoneArrayResizableLinkedNoddedList()
    
    private val gameStateMachine = GameStateMachine.FromInitialization(
        playersList = emptyKoneList<Player>(),
        settingsBuilder = GameStateMachine.GameSettingsBuilder<OnlineGameWordsProvider>( // TODO: Hardcoded settings!
            preparationTimeSeconds = 3u,
            explanationTimeSeconds = 40u,
            finalGuessTimeSeconds = 3u,
            strictMode = true,
            cachedEndConditionWordsNumber = 100u,
            cachedEndConditionCyclesNumber = 3u,
            gameEndConditionType = GameStateMachine.GameEndCondition.Type.Words,
            wordsSource = GameStateMachine.WordsSource.Custom (
                object : OnlineGameWordsProvider.ServerDictionary {
                    override val name: String = "Default dictionary"
                    override fun randomWords(number: UInt): KoneSet<String> = (1u..number).toKoneList().mapTo(koneMutableSetOf()) { it.toString() }
                    override fun allWords(): KoneSet<String> = randomWords(100u)
                }
            )
        ),
        coroutineScope = coroutineScope,
        structuralMutex = structuralMutex,
        random = random,
    )
    
    private fun updateGamePlayers() {
        // TODO: Add checks and logging
        when(val gameState = gameStateMachine.state.value) {
            is GameStateMachine.State.GameInitialisation<Player, OnlineGameWordsProvider> -> {
                val result = gameStateMachine.updateGameSettings(
                    playersList = playersRegistry.filter { it.isOnline },
                    settingsBuilder = gameState.settingsBuilder,
                )
                
                when (result) {
                    GameStateMachine.Result.GameSettingsUpdateResult.InvalidState -> {
                        logger.warn { "Game state machine in state 'GameInitialisation' refused to update settings" }
                    }
                    GameStateMachine.Result.GameSettingsUpdateResult.Success -> {}
                }
            }
            else -> {}
        }
    }
    
    private val statusUpdateRequestChannel = Channel<Nothing?>(Channel.CONFLATED)
    private fun requestStatusUpdate() {
        statusUpdateRequestChannel.trySend(null)
    }
    
    private suspend fun sendUpdateSignal() {
        logger.info(
            items = {
                mapOf(
                    "room name" to this.name,
                )
            }
        ) { "Sending update signals to room" }
        playersRegistry.forEachIndexed { playerIndex, player ->
            val gameStateToSend = when (val gameState = gameStateMachine.state.value) {
                is GameStateMachine.State.GameInitialisation ->
                    ServerApi.OnlineGame.State.GameInitialisation(
                        role = ServerApi.OnlineGame.Role.GameInitialisation(
                            name = player.name,
                            isHost = player.isOnline,
                        ),
                        playersList = gameState.playersList.map { ServerApi.PlayerDescription(name = it.name, isOnline = it.isOnline) },
                        userIndex = (absoluteEquality<Player>()) { gameState.playersList.firstIndexOf(player) }, // TODO: Add check on -1
                        settingsBuilder = ServerApi.SettingsBuilder(
                            preparationTimeSeconds = gameState.settingsBuilder.preparationTimeSeconds,
                            explanationTimeSeconds = gameState.settingsBuilder.explanationTimeSeconds,
                            finalGuessTimeSeconds = gameState.settingsBuilder.finalGuessTimeSeconds,
                            strictMode = gameState.settingsBuilder.strictMode,
                            cachedEndConditionWordsNumber = gameState.settingsBuilder.cachedEndConditionWordsNumber,
                            cachedEndConditionCyclesNumber = gameState.settingsBuilder.cachedEndConditionCyclesNumber,
                            gameEndConditionType = gameState.settingsBuilder.gameEndConditionType,
                            wordsSource = when (val wordSource = gameState.settingsBuilder.wordsSource) {
                                GameStateMachine.WordsSource.Players -> ServerApi.WordsSource.Players
                                is GameStateMachine.WordsSource.Custom -> when (val provider = wordSource.provider) {
                                    is OnlineGameWordsProvider.ServerDictionary -> ServerApi.WordsSource.ServerDictionary(name = provider.name)
                                }
                            },
                        )
                    )
                is GameStateMachine.State.RoundWaiting ->
                    ServerApi.OnlineGame.State.RoundWaiting(
                        role = ServerApi.OnlineGame.Role.RoundWaiting(
                            name = player.name,
                            isHost = player.isOnline,
                            roundRole = when((absoluteEquality<Player>()) { gameState.playersList.firstIndexOf(player) /* TODO: Add check on -1 */ }) {
                                gameState.speakerIndex -> ServerApi.OnlineGame.Role.RoundRole.Speaker
                                gameState.listenerIndex -> ServerApi.OnlineGame.Role.RoundRole.Listener
                                else -> ServerApi.OnlineGame.Role.RoundRole.Player
                            }
                        ),
                        playersList = gameState.playersList.map { ServerApi.PlayerDescription(name = it.name, isOnline = it.isOnline) },
                        userIndex = (absoluteEquality<Player>()) { gameState.playersList.firstIndexOf(player) }, // TODO: Add check on -1
                        settings = ServerApi.Settings(
                            preparationTimeSeconds = gameState.settings.preparationTimeSeconds,
                            explanationTimeSeconds = gameState.settings.explanationTimeSeconds,
                            finalGuessTimeSeconds = gameState.settings.finalGuessTimeSeconds,
                            strictMode = gameState.settings.strictMode,
                            gameEndCondition = gameState.settings.gameEndCondition,
                        ),
                        roundNumber = gameState.roundNumber,
                        cycleNumber = gameState.cycleNumber,
                        speakerIndex = gameState.speakerIndex,
                        listenerIndex = gameState.listenerIndex,
                        explanationScores = gameState.explanationScores,
                        guessingScores = gameState.guessingScores,
                        speakerReady = gameState.speakerReady,
                        listenerReady = gameState.listenerReady,
                    )
                is GameStateMachine.State.RoundPreparation ->
                    ServerApi.OnlineGame.State.RoundPreparation(
                        role = ServerApi.OnlineGame.Role.RoundPreparation(
                            name = player.name,
                            isHost = player.isOnline,
                            roundRole = when((absoluteEquality<Player>()) { gameState.playersList.firstIndexOf(player) /* TODO: Add check on -1 */ }) {
                                gameState.speakerIndex -> ServerApi.OnlineGame.Role.RoundRole.Speaker
                                gameState.listenerIndex -> ServerApi.OnlineGame.Role.RoundRole.Listener
                                else -> ServerApi.OnlineGame.Role.RoundRole.Player
                            }
                        ),
                        playersList = gameState.playersList.map { ServerApi.PlayerDescription(name = it.name, isOnline = it.isOnline) },
                        userIndex = (absoluteEquality<Player>()) { gameState.playersList.firstIndexOf(player) }, // TODO: Add check on -1
                        settings = ServerApi.Settings(
                            preparationTimeSeconds = gameState.settings.preparationTimeSeconds,
                            explanationTimeSeconds = gameState.settings.explanationTimeSeconds,
                            finalGuessTimeSeconds = gameState.settings.finalGuessTimeSeconds,
                            strictMode = gameState.settings.strictMode,
                            gameEndCondition = gameState.settings.gameEndCondition,
                        ),
                        roundNumber = gameState.roundNumber,
                        cycleNumber = gameState.cycleNumber,
                        speakerIndex = gameState.speakerIndex,
                        listenerIndex = gameState.listenerIndex,
                        millisecondsLeft = gameState.millisecondsLeft,
                        explanationScores = gameState.explanationScores,
                        guessingScores = gameState.guessingScores,
                    )
                is GameStateMachine.State.RoundExplanation ->
                    ServerApi.OnlineGame.State.RoundExplanation(
                        role = ServerApi.OnlineGame.Role.RoundExplanation(
                            name = player.name,
                            isHost = player.isOnline,
                            roundRole = when((absoluteEquality<Player>()) { gameState.playersList.firstIndexOf(player) /* TODO: Add check on -1 */ }) {
                                gameState.speakerIndex -> ServerApi.OnlineGame.Role.RoundRole.Speaker
                                gameState.listenerIndex -> ServerApi.OnlineGame.Role.RoundRole.Listener
                                else -> ServerApi.OnlineGame.Role.RoundRole.Player
                            }
                        ),
                        playersList = gameState.playersList.map { ServerApi.PlayerDescription(name = it.name, isOnline = it.isOnline) },
                        userIndex = (absoluteEquality<Player>()) { gameState.playersList.firstIndexOf(player) }, // TODO: Add check on -1
                        settings = ServerApi.Settings(
                            preparationTimeSeconds = gameState.settings.preparationTimeSeconds,
                            explanationTimeSeconds = gameState.settings.explanationTimeSeconds,
                            finalGuessTimeSeconds = gameState.settings.finalGuessTimeSeconds,
                            strictMode = gameState.settings.strictMode,
                            gameEndCondition = gameState.settings.gameEndCondition,
                        ),
                        roundNumber = gameState.roundNumber,
                        cycleNumber = gameState.cycleNumber,
                        speakerIndex = gameState.speakerIndex,
                        listenerIndex = gameState.listenerIndex,
                        millisecondsLeft = gameState.millisecondsLeft,
                        explanationScores = gameState.explanationScores,
                        guessingScores = gameState.guessingScores,
                    )
                is GameStateMachine.State.RoundLastGuess ->
                    ServerApi.OnlineGame.State.RoundLastGuess(
                        role = ServerApi.OnlineGame.Role.RoundLastGuess(
                            name = player.name,
                            isHost = player.isOnline,
                            roundRole = when((absoluteEquality<Player>()) { gameState.playersList.firstIndexOf(player) /* TODO: Add check on -1 */ }) {
                                gameState.speakerIndex -> ServerApi.OnlineGame.Role.RoundRole.Speaker
                                gameState.listenerIndex -> ServerApi.OnlineGame.Role.RoundRole.Listener
                                else -> ServerApi.OnlineGame.Role.RoundRole.Player
                            }
                        ),
                        playersList = gameState.playersList.map { ServerApi.PlayerDescription(name = it.name, isOnline = it.isOnline) },
                        userIndex = (absoluteEquality<Player>()) { gameState.playersList.firstIndexOf(player) }, // TODO: Add check on -1
                        settings = ServerApi.Settings(
                            preparationTimeSeconds = gameState.settings.preparationTimeSeconds,
                            explanationTimeSeconds = gameState.settings.explanationTimeSeconds,
                            finalGuessTimeSeconds = gameState.settings.finalGuessTimeSeconds,
                            strictMode = gameState.settings.strictMode,
                            gameEndCondition = gameState.settings.gameEndCondition,
                        ),
                        roundNumber = gameState.roundNumber,
                        cycleNumber = gameState.cycleNumber,
                        speakerIndex = gameState.speakerIndex,
                        listenerIndex = gameState.listenerIndex,
                        millisecondsLeft = gameState.millisecondsLeft,
                        explanationScores = gameState.explanationScores,
                        guessingScores = gameState.guessingScores,
                    )
                is GameStateMachine.State.RoundEditing ->
                    ServerApi.OnlineGame.State.RoundEditing(
                        role = ServerApi.OnlineGame.Role.RoundEditing(
                            name = player.name,
                            isHost = player.isOnline,
                            roundRole = when((absoluteEquality<Player>()) { gameState.playersList.firstIndexOf(player) } /* TODO: Add check on -1 */) {
                                gameState.speakerIndex -> ServerApi.OnlineGame.Role.RoundRole.Speaker
                                gameState.listenerIndex -> ServerApi.OnlineGame.Role.RoundRole.Listener
                                else -> ServerApi.OnlineGame.Role.RoundRole.Player
                            }
                        ),
                        playersList = gameState.playersList.map { ServerApi.PlayerDescription(name = it.name, isOnline = it.isOnline) },
                        userIndex = (absoluteEquality<Player>()) { gameState.playersList.firstIndexOf(player) }, // TODO: Add check on -1
                        settings = ServerApi.Settings(
                            preparationTimeSeconds = gameState.settings.preparationTimeSeconds,
                            explanationTimeSeconds = gameState.settings.explanationTimeSeconds,
                            finalGuessTimeSeconds = gameState.settings.finalGuessTimeSeconds,
                            strictMode = gameState.settings.strictMode,
                            gameEndCondition = gameState.settings.gameEndCondition,
                        ),
                        roundNumber = gameState.roundNumber,
                        cycleNumber = gameState.cycleNumber,
                        speakerIndex = gameState.speakerIndex,
                        listenerIndex = gameState.listenerIndex,
                        explanationScores = gameState.explanationScores,
                        guessingScores = gameState.guessingScores,
                        wordsToEdit = gameState.currentExplanationResults, // TODO: Remove from non-speaking players data
                    )
                is GameStateMachine.State.GameResults ->
                    ServerApi.OnlineGame.State.GameResults(
                        role = ServerApi.OnlineGame.Role.GameResults(
                            name = player.name,
                            isHost = player.isOnline,
                        ),
                        playersList = gameState.playersList.map { it.name },
                        userIndex = (absoluteEquality<Player>()) { gameState.playersList.firstIndexOf(player) }, // TODO: Add check on -1
                        results = gameState.results,
                    )
            }
            logger.info(
                items = {
                    mapOf(
                        "room name" to this@Room.name,
                        "player" to player.name,
                        "signal" to gameStateToSend.toString(),
                    )
                }
            ) { "Sending signal to player" }
            player.connectionRegistry.forEach {
                logger.info(
                    items = {
                        mapOf(
                            "room name" to this@Room.name,
                            "player" to player.name,
                            "connection" to it.toString(),
                            "signal" to gameStateToSend.toString(),
                        )
                    }
                ) { "Sending signal to connection" }
                it.socketSession.sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameStateUpdate(gameStateToSend))
            }
        }
    }
    
    init {
        coroutineScope.launch {
            gameStateMachine.state.collect { requestStatusUpdate() }
        }
        coroutineScope.launch {
            for (request in statusUpdateRequestChannel) sendUpdateSignal()
        }
    }
    
    val description: ServerApi.RoomDescription
        get() = when (val state = gameStateMachine.state.value) {
            is GameStateMachine.State.GameInitialisation ->
                ServerApi.RoomDescription(
                    name = this.name,
                    playersList = state.playersList.map { ServerApi.PlayerDescription(name = it.name, isOnline = it.isOnline) },
                    state = ServerApi.RoomStateType.GameInitialisation
                )
            is GameStateMachine.State.RoundWaiting ->
                ServerApi.RoomDescription(
                    name = this.name,
                    playersList = state.playersList.map { ServerApi.PlayerDescription(name = it.name, isOnline = it.isOnline) },
                    state = ServerApi.RoomStateType.GameInitialisation
                )
            is GameStateMachine.State.RoundPreparation ->
                ServerApi.RoomDescription(
                    name = this.name,
                    playersList = state.playersList.map { ServerApi.PlayerDescription(name = it.name, isOnline = it.isOnline) },
                    state = ServerApi.RoomStateType.GameInitialisation
                )
            is GameStateMachine.State.RoundExplanation ->
                ServerApi.RoomDescription(
                    name = this.name,
                    playersList = state.playersList.map { ServerApi.PlayerDescription(name = it.name, isOnline = it.isOnline) },
                    state = ServerApi.RoomStateType.GameInitialisation
                )
            is GameStateMachine.State.RoundLastGuess ->
                ServerApi.RoomDescription(
                    name = this.name,
                    playersList = state.playersList.map { ServerApi.PlayerDescription(name = it.name, isOnline = it.isOnline) },
                    state = ServerApi.RoomStateType.GameInitialisation
                )
            is GameStateMachine.State.RoundEditing ->
                ServerApi.RoomDescription(
                    name = this.name,
                    playersList = state.playersList.map { ServerApi.PlayerDescription(name = it.name, isOnline = it.isOnline) },
                    state = ServerApi.RoomStateType.GameInitialisation
                )
            is GameStateMachine.State.GameResults ->
                ServerApi.RoomDescription(
                    name = this.name,
                    playersList = state.playersList.map { ServerApi.PlayerDescription(name = it.name, isOnline = it.isOnline) },
                    state = ServerApi.RoomStateType.GameInitialisation
                )
        }
    
    suspend fun attachConnectionToPlayer(connection: Connection, playerName: String): ConnectionToPlayerAttachmentResult {
        structuralMutex.withLock {
            logger.debug(
                items = {
                    mapOf(
                        "connection" to connection.toString(),
                        "room" to this.name,
                        "player" to playerName,
                    )
                }
            ) { "Attaching connection to player" }
            val possiblePlayer = playersRegistry.firstThatOrNull { it.name == playerName }
            val currentGameState = gameStateMachine.state.value
            
            val result = when (currentGameState) {
                is GameStateMachine.State.GameInitialisation -> {
                    val player = possiblePlayer ?: Player(room = this, name = playerName).also { playersRegistry.addNode(it) }
                    
                    if (player.isOnline) return ConnectionToPlayerAttachmentResult.SomeOtherConnectionIsAlreadyAttached
                    
                    val connectionRegistration = player.connectionRegistry.addNode(connection)
                    
                    val result = gameStateMachine.updateGameSettings(
                        playersList = playersRegistry.filter { it.isOnline },
                        settingsBuilder = currentGameState.settingsBuilder,
                    )
                    
                    when (result) {
                        GameStateMachine.Result.GameSettingsUpdateResult.InvalidState -> {
                            logger.warn { "Game state machine in state 'GameInitialisation' refused to update settings" }
                            ConnectionToPlayerAttachmentResult.GameStartedWithoutThePlayer
                        }
                        GameStateMachine.Result.GameSettingsUpdateResult.Success -> ConnectionToPlayerAttachmentResult.Success(Player.AttachmentHandle(player, connectionRegistration))
                    }
                }
                is GameStateMachine.State.RoundWaiting,
                is GameStateMachine.State.RoundPreparation,
                is GameStateMachine.State.RoundExplanation,
                is GameStateMachine.State.RoundLastGuess,
                is GameStateMachine.State.RoundEditing,
                is GameStateMachine.State.GameResults, -> {
                    if (possiblePlayer == null) return ConnectionToPlayerAttachmentResult.GameStartedWithoutThePlayer
                    
                    val connectionRegistration = possiblePlayer.connectionRegistry.addNode(connection)
                    
                    ConnectionToPlayerAttachmentResult.Success(Player.AttachmentHandle(possiblePlayer, connectionRegistration))
                }
            }
            
            updateGamePlayers()
            requestStatusUpdate()
            
            return result
        }
    }
    
    suspend fun updateSettings(settingsBuilder: ClientApi.SettingsBuilder): SettingsUpdateResult =
        structuralMutex.withLock {
            when (val currentState = gameStateMachine.state.value) {
                is GameStateMachine.State.GameInitialisation -> {
                    val result = gameStateMachine.updateGameSettings(
                        playersList = currentState.playersList,
                        GameStateMachine.GameSettingsBuilder(
                            preparationTimeSeconds = settingsBuilder.preparationTimeSeconds,
                            explanationTimeSeconds = settingsBuilder.explanationTimeSeconds,
                            finalGuessTimeSeconds = settingsBuilder.finalGuessTimeSeconds,
                            strictMode = settingsBuilder.strictMode,
                            cachedEndConditionWordsNumber = settingsBuilder.cachedEndConditionWordsNumber,
                            cachedEndConditionCyclesNumber = settingsBuilder.cachedEndConditionCyclesNumber,
                            gameEndConditionType = settingsBuilder.gameEndConditionType,
                            wordsSource = when (val clientWordsSource = settingsBuilder.wordsSource) {
                                ClientApi.WordsSource.Players -> GameStateMachine.WordsSource.Players
                                is ClientApi.WordsSource.ServerDictionary ->
                                    GameStateMachine.WordsSource.Custom(
                                        object : OnlineGameWordsProvider.ServerDictionary {
                                            override val name: String = clientWordsSource.name
                                            override fun randomWords(number: UInt): KoneSet<String> = (1u..number).toKoneList().mapTo(koneMutableSetOf()) { it.toString() }
                                            override fun allWords(): KoneSet<String> = randomWords(100u)
                                        }
                                    )
                            }
                        )
                    )
                    when (result) {
                        GameStateMachine.Result.GameSettingsUpdateResult.InvalidState -> {
                            logger.warn { "Game state machine in state 'GameInitialisation' refused to update settings" }
                            SettingsUpdateResult.GameAlreadyStarted
                        }
                        GameStateMachine.Result.GameSettingsUpdateResult.Success -> SettingsUpdateResult.Success
                    }
                }
                is GameStateMachine.State.RoundWaiting,
                is GameStateMachine.State.RoundPreparation,
                is GameStateMachine.State.RoundExplanation,
                is GameStateMachine.State.RoundLastGuess,
                is GameStateMachine.State.RoundEditing,
                is GameStateMachine.State.GameResults,
                    -> SettingsUpdateResult.GameAlreadyStarted
            }
        }
    
    suspend fun initialiseGame(): GameInitialisationResult =
        structuralMutex.withLock {
            val result = gameStateMachine.initialiseGame()
            when (result) {
                GameStateMachine.Result.GameInitialisationResult.InvalidState -> GameInitialisationResult.InvalidState
                GameStateMachine.Result.GameInitialisationResult.Success -> GameInitialisationResult.Success
            }
        }
    
    suspend fun speakerReady(): SpeakerReadinessResult =
        structuralMutex.withLock {
            val result = gameStateMachine.speakerReady()
            when (result) {
                GameStateMachine.Result.SpeakerReadinessResult.InvalidState -> SpeakerReadinessResult.InvalidState
                GameStateMachine.Result.SpeakerReadinessResult.Success -> SpeakerReadinessResult.Success
            }
        }
    
    suspend fun listenerReady(): ListenerReadinessResult =
        structuralMutex.withLock {
            val result = gameStateMachine.listenerReady()
            when (result) {
                GameStateMachine.Result.ListenerReadinessResult.InvalidState -> ListenerReadinessResult.InvalidState
                GameStateMachine.Result.ListenerReadinessResult.Success -> ListenerReadinessResult.Success
            }
        }
    
    suspend fun wordExplanationState(wordsState: GameStateMachine.WordExplanation.State): WordExplanationStatementResult =
        structuralMutex.withLock {
            val result = gameStateMachine.wordExplanationState(wordsState)
            
            when (result) {
                GameStateMachine.Result.WordExplanationStatementResult.InvalidState -> WordExplanationStatementResult.InvalidState
                GameStateMachine.Result.WordExplanationStatementResult.Success -> WordExplanationStatementResult.Success
            }
        }
    
    suspend fun updateWordsExplanationResults(newExplanationResults: KoneList<GameStateMachine.WordExplanation>): WordsExplanationResultsUpdateResult =
        structuralMutex.withLock {
            val result = gameStateMachine.updateWordsExplanationResults(newExplanationResults)
            
            when (result) {
                GameStateMachine.Result.WordsExplanationResultsUpdateResult.InvalidState -> WordsExplanationResultsUpdateResult.InvalidState
                GameStateMachine.Result.WordsExplanationResultsUpdateResult.Success -> WordsExplanationResultsUpdateResult.Success
            }
        }
    
    suspend fun confirmWordsExplanationResults(): WordsExplanationResultsConfirmationResult =
        structuralMutex.withLock {
            val result = gameStateMachine.confirmWordsExplanationResults()
            
            when (result) {
                GameStateMachine.Result.WordsExplanationResultsConfirmationResult.InvalidState -> WordsExplanationResultsConfirmationResult.InvalidState
                GameStateMachine.Result.WordsExplanationResultsConfirmationResult.Success -> WordsExplanationResultsConfirmationResult.Success
            }
        }
    
    suspend fun finishGame(): GameFinishingResult =
        structuralMutex.withLock {
            val result = gameStateMachine.finishGame()
            
            when (result) {
                GameStateMachine.Result.GameFinishingResult.InvalidState -> GameFinishingResult.InvalidState
                GameStateMachine.Result.GameFinishingResult.Success -> GameFinishingResult.Success
            }
        }

    companion object {
        val random: Random = ThreadLocalRandom.current().asKotlinRandom()
    }
}

val rooms = ConcurrentHashMap<String, Room>()

fun getRoomByIdOrCreate(id: String): Room = rooms.computeIfAbsent(id) { Room(id) }

class Connection(
    val socketSession: WebSocketServerSession,
) {
    var playerAttachmentHandle : Room.Player.AttachmentHandle? = null
    var playerAttachmentHandleMutex = Mutex()
}

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    embeddedServer(Netty, port = 3000) {
        install(WebSockets) {
            pingPeriodMillis = 1000
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        routing {
            webSocket(path = "/ws") {
                val connection = Connection(this)
                logger.info(
                    items = {
                        mapOf("connection" to connection.toString())
                    }
                ) { "Opened connection" }
                try {
                    val converter = converter!!
                    for (frame in incoming) {
                        if (!converter.isApplicable(frame)) {
                            logger.info(
                                items = {
                                    mapOf(
                                        "connection" to connection.toString(),
                                        "frame" to frame.toString(),
                                    )
                                }
                            ) { "Received inconvertible frame" }
                            continue
                        }
                        val signal = converter.deserialize<ClientApi.Signal>(frame)
                        
                        logger.info(
                            items = {
                                mapOf(
                                    "connection" to connection.toString(),
                                    "signal" to signal.toString(),
                                )
                            }
                        ) { "Received signal" }
                        
                        connection.playerAttachmentHandleMutex.withLock {
                            when (signal) {
                                is ClientApi.Signal.FetchFreeRoomId -> {
                                    TODO()
                                }
                                is ClientApi.Signal.FetchRoomInfo -> {
                                    val room = rooms[signal.roomId]
                                    
                                    sendSerialized<ServerApi.Signal>(
                                        ServerApi.Signal.RoomInfo(
                                            room?.description
                                                ?: ServerApi.RoomDescription(
                                                    name = signal.roomId,
                                                    playersList = emptyKoneList(),
                                                    state = ServerApi.RoomStateType.GameInitialisation
                                                )
                                        )
                                    )
                                }
                                is ClientApi.Signal.OnlineGame.JoinRoom -> {
                                    if (connection.playerAttachmentHandle != null) {
                                        sendSerialized<ServerApi.Signal>(ServerApi.Signal.UnspecifiedError)
                                        return@withLock // TODO: Replace with `continue`
                                    }
                                    
                                    logger.debug(
                                        items = {
                                            mapOf(
                                                "connection" to connection.toString(),
                                                "room" to signal.roomId,
                                                "player" to signal.playerName,
                                            )
                                        }
                                    ) { "Trying to attach connection to player" }
                                    
                                    val attachmentResult = getRoomByIdOrCreate(signal.roomId)
                                        .attachConnectionToPlayer(connection, signal.playerName)
                                    
                                    logger.debug(
                                        items = {
                                            mapOf(
                                                "connection" to connection.toString(),
                                                "room" to signal.roomId,
                                                "player" to signal.playerName,
                                                "result" to attachmentResult.toString(),
                                            )
                                        }
                                    ) { "Got connection to player attachment result" }
                                    
                                    when (attachmentResult) {
                                        ConnectionToPlayerAttachmentResult.GameStartedWithoutThePlayer -> {
                                            sendSerialized<ServerApi.Signal>(ServerApi.Signal.UnspecifiedError)
                                            return@withLock // TODO: Replace with `continue`
                                        }
                                        ConnectionToPlayerAttachmentResult.SomeOtherConnectionIsAlreadyAttached -> {
                                            sendSerialized<ServerApi.Signal>(ServerApi.Signal.UnspecifiedError)
                                            return@withLock // TODO: Replace with `continue`
                                        }
                                        is ConnectionToPlayerAttachmentResult.Success -> {
                                            connection.playerAttachmentHandle = attachmentResult.attachment
                                        }
                                    }
                                }
                                
                                ClientApi.Signal.OnlineGame.LeaveRoom -> {
                                    val handle = connection.playerAttachmentHandle
                                    
                                    if (handle == null) {
                                        sendSerialized<ServerApi.Signal>(ServerApi.Signal.UnspecifiedError)
                                        return@withLock // TODO: Replace with `continue`
                                    }
                                    
                                    handle.detachConnection()
                                }
                                
                                is ClientApi.Signal.OnlineGame.UpdateSettings -> {
                                    val handle = connection.playerAttachmentHandle
                                    
                                    if (handle == null) {
                                        sendSerialized<ServerApi.Signal>(ServerApi.Signal.UnspecifiedError)
                                        return@withLock // TODO: Replace with `continue`
                                    }
                                    
                                    // TODO: Add a check on host
                                    val result = handle.player.room.updateSettings(signal.settingsBuilder)
                                    
                                    when (result) {
                                        SettingsUpdateResult.GameAlreadyStarted -> {
                                            sendSerialized<ServerApi.Signal>(ServerApi.Signal.UnspecifiedError)
                                            return@withLock // TODO: Replace with `continue`
                                        }
                                        SettingsUpdateResult.Success -> {}
                                    }
                                }
                                ClientApi.Signal.OnlineGame.InitializeGame -> {
                                    val handle = connection.playerAttachmentHandle
                                    
                                    if (handle == null) {
                                        sendSerialized<ServerApi.Signal>(ServerApi.Signal.UnspecifiedError)
                                        return@withLock // TODO: Replace with `continue`
                                    }
                                    
                                    // TODO: Add a check on host
                                    val result = handle.player.room.initialiseGame()
                                    
                                    when (result) {
                                        GameInitialisationResult.InvalidState -> {
                                            sendSerialized<ServerApi.Signal>(ServerApi.Signal.UnspecifiedError)
                                            return@withLock // TODO: Replace with `continue`
                                        }
                                        GameInitialisationResult.Success -> {}
                                    }
                                }
                                ClientApi.Signal.OnlineGame.SpeakerReady -> {
                                    val handle = connection.playerAttachmentHandle
                                    
                                    if (handle == null) {
                                        sendSerialized<ServerApi.Signal>(ServerApi.Signal.UnspecifiedError)
                                        return@withLock // TODO: Replace with `continue`
                                    }
                                    
                                    // TODO: Add a check on speaker
                                    val result = handle.player.room.speakerReady()
                                    
                                    when (result) {
                                        SpeakerReadinessResult.InvalidState -> {
                                            sendSerialized<ServerApi.Signal>(ServerApi.Signal.UnspecifiedError)
                                            return@withLock // TODO: Replace with `continue`
                                        }
                                        SpeakerReadinessResult.Success -> {}
                                    }
                                }
                                ClientApi.Signal.OnlineGame.ListenerReady -> {
                                    val handle = connection.playerAttachmentHandle
                                    
                                    if (handle == null) {
                                        sendSerialized<ServerApi.Signal>(ServerApi.Signal.UnspecifiedError)
                                        return@withLock // TODO: Replace with `continue`
                                    }
                                    
                                    // TODO: Add a check on listener
                                    val result = handle.player.room.listenerReady()
                                    
                                    when (result) {
                                        ListenerReadinessResult.InvalidState -> {
                                            sendSerialized<ServerApi.Signal>(ServerApi.Signal.UnspecifiedError)
                                            return@withLock // TODO: Replace with `continue`
                                        }
                                        ListenerReadinessResult.Success -> {}
                                    }
                                }
                                is ClientApi.Signal.OnlineGame.WordExplanationState -> {
                                    val handle = connection.playerAttachmentHandle
                                    
                                    if (handle == null) {
                                        sendSerialized<ServerApi.Signal>(ServerApi.Signal.UnspecifiedError)
                                        return@withLock // TODO: Replace with `continue`
                                    }
                                    
                                    // TODO: Add a check on speaker
                                    val result = handle.player.room.wordExplanationState(signal.state)
                                    
                                    when (result) {
                                        WordExplanationStatementResult.InvalidState -> {
                                            sendSerialized<ServerApi.Signal>(ServerApi.Signal.UnspecifiedError)
                                            return@withLock // TODO: Replace with `continue`
                                        }
                                        WordExplanationStatementResult.Success -> {}
                                    }
                                }
                                is ClientApi.Signal.OnlineGame.UpdateWordsExplanationResults -> {
                                    val handle = connection.playerAttachmentHandle
                                    
                                    if (handle == null) {
                                        sendSerialized<ServerApi.Signal>(ServerApi.Signal.UnspecifiedError)
                                        return@withLock // TODO: Replace with `continue`
                                    }
                                    
                                    // TODO: Add a check on speaker
                                    val result = handle.player.room.updateWordsExplanationResults(signal.newExplanationResults)
                                    
                                    when (result) {
                                        WordsExplanationResultsUpdateResult.InvalidState -> {
                                            sendSerialized<ServerApi.Signal>(ServerApi.Signal.UnspecifiedError)
                                            return@withLock // TODO: Replace with `continue`
                                        }
                                        WordsExplanationResultsUpdateResult.Success -> {}
                                    }
                                }
                                ClientApi.Signal.OnlineGame.ConfirmWordsExplanationResults -> {
                                    val handle = connection.playerAttachmentHandle
                                    
                                    if (handle == null) {
                                        sendSerialized<ServerApi.Signal>(ServerApi.Signal.UnspecifiedError)
                                        return@withLock // TODO: Replace with `continue`
                                    }
                                    
                                    // TODO: Add a check on speaker
                                    val result = handle.player.room.confirmWordsExplanationResults()
                                    
                                    when (result) {
                                        WordsExplanationResultsConfirmationResult.InvalidState -> {
                                            sendSerialized<ServerApi.Signal>(ServerApi.Signal.UnspecifiedError)
                                            return@withLock // TODO: Replace with `continue`
                                        }
                                        WordsExplanationResultsConfirmationResult.Success -> {}
                                    }
                                }
                                ClientApi.Signal.OnlineGame.FinishGame -> {
                                    val handle = connection.playerAttachmentHandle
                                    
                                    if (handle == null) {
                                        sendSerialized<ServerApi.Signal>(ServerApi.Signal.UnspecifiedError)
                                        return@withLock // TODO: Replace with `continue`
                                    }
                                    
                                    // TODO: Add a check on host
                                    val result = handle.player.room.finishGame()
                                    
                                    when (result) {
                                        GameFinishingResult.InvalidState -> {
                                            sendSerialized<ServerApi.Signal>(ServerApi.Signal.UnspecifiedError)
                                            return@withLock // TODO: Replace with `continue`
                                        }
                                        GameFinishingResult.Success -> {}
                                    }
                                }
                            }
                        }
                    }
                } finally {
                    logger.info(
                        items = {
                            mapOf("connection" to connection.toString())
                        }
                    ) { "Closed connection" }
                    withContext(NonCancellable) {
                        connection.playerAttachmentHandleMutex.withLock {
                            connection.playerAttachmentHandle?.detachConnection()
                        }
                    }
                }
            }
        }
    }.start(wait = true)
}