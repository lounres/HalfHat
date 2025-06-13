package dev.lounres.halfhat.server

import dev.lounres.halfhat.api.onlineGame.ClientApi
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.halfhat.logic.server.Room
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.empty
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.set.of
import dev.lounres.kone.collections.set.toKoneSet
import dev.lounres.kone.collections.utils.map
import dev.lounres.kone.collections.utils.take
import dev.lounres.logKube.core.DefaultJvmLogWriter
import dev.lounres.logKube.core.JvmLogger
import dev.lounres.logKube.core.LogAcceptor
import dev.lounres.logKube.core.debug
import dev.lounres.logKube.core.info
import dev.lounres.logKube.core.warn
import io.ktor.serialization.deserialize
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.server.websocket.*
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid


val logger = JvmLogger(
    name = "HalfHat main server logger",
    logAcceptors = listOf(
        LogAcceptor(DefaultJvmLogWriter) /*{ it.logLevel >= LogLevel.INFO }*/,
    )
)

data class RoomMetadata(
    val name: String,
)

data class PlayerMetadata(
    val name: String
) : Room.Player.Metadata<String> {
    override val id: String get() = name
}

typealias WordsProviderID = String

sealed interface OnlineGameWordsProvider: Room.WordsProvider<WordsProviderID> {
    interface ServerDictionary: OnlineGameWordsProvider {
        val name: String
    }
}

object OnlineGameWordsProviderRegistry : Room.WordProviderRegistry<WordsProviderID, OnlineGameWordsProvider> {
    override fun contains(id: String): Boolean = id == "kek"
    
    override fun get(id: String): OnlineGameWordsProvider = if (id == "kek") TemporaryDictionary else TODO()
    
    private object TemporaryDictionary: OnlineGameWordsProvider.ServerDictionary {
        override val name: String = "kek"
        override val id: String = "kek"
        
        private val words = KoneSet.of("картина", "корзина", "картонка", "собачонка")
        
        override val size: UInt get() = words.size
        override fun randomWords(number: UInt): KoneSet<String> = words.take(number).toKoneSet()
        override fun allWords(): KoneSet<String> = words
    }
}

typealias ServerRoom = Room<RoomMetadata, String, PlayerMetadata, String, OnlineGameWordsProvider, Connection>

val rooms = ConcurrentHashMap<String, ServerRoom>()

fun getRoomByIdOrCreate(id: String): ServerRoom = rooms.computeIfAbsent(id) {
    Room(
        metadata = RoomMetadata(id),
        wordsProviderRegistry = OnlineGameWordsProviderRegistry,
        initialSettingsBuilder = Room.GameSettings.Builder( // TODO: Захардкоженные константы
            preparationTimeSeconds = 3u,
            explanationTimeSeconds = 40u,
            finalGuessTimeSeconds = 3u,
            strictMode = false,
            cachedEndConditionWordsNumber = 100u,
            cachedEndConditionCyclesNumber = 4u,
            gameEndConditionType = GameStateMachine.GameEndCondition.Type.Words,
            wordsSource = Room.WordsSource.ServerDictionary("kek")
        ),
        initialMetadataFactory = { PlayerMetadata(it) },
        checkConnectionAttachment = { metadata, isOnline, connection -> !isOnline }
    )
}

class Connection(
    val socketSession: WebSocketServerSession,
) : Room.Connection<RoomMetadata, PlayerMetadata, WordsProviderID> {
    val id: Uuid = Uuid.random()
    override fun toString(): String = "Connection#${id.toHexString()}"
    
    var playerAttachment : Room.Player.Attachment<RoomMetadata, String, PlayerMetadata, String, OnlineGameWordsProvider, Connection>? = null
    val playerAttachmentMutex = Mutex()
    
    override suspend fun sendNewState(state: Room.Outgoing.State<RoomMetadata, PlayerMetadata, WordsProviderID>) {
        fun Room.Player.Description<PlayerMetadata>.toServerApi(): ServerApi.PlayerDescription =
            ServerApi.PlayerDescription(
                name = this.metadata.name,
                isOnline = this.isOnline,
            )
        fun KoneList<Room.Player.Description<PlayerMetadata>>.toServerApi(): KoneList<ServerApi.PlayerDescription> =
            map { it.toServerApi() }
        fun Room.GameSettings.Builder<WordsProviderID>.toServerApi(): ServerApi.SettingsBuilder = 
            ServerApi.SettingsBuilder(
                preparationTimeSeconds = this.preparationTimeSeconds,
                explanationTimeSeconds = this.explanationTimeSeconds,
                finalGuessTimeSeconds = this.finalGuessTimeSeconds,
                strictMode = this.strictMode,
                cachedEndConditionWordsNumber = this.cachedEndConditionWordsNumber,
                cachedEndConditionCyclesNumber = this.cachedEndConditionCyclesNumber,
                gameEndConditionType = this.gameEndConditionType,
                wordsSource = when (val wordsSource = this.wordsSource) {
                    Room.WordsSource.Players -> ServerApi.WordsSource.Players
                    is Room.WordsSource.ServerDictionary -> ServerApi.WordsSource.ServerDictionary(wordsSource.id)
                },
            )
        fun Room.GameSettings<WordsProviderID>.toServerApi(): ServerApi.Settings =
            ServerApi.Settings(
                preparationTimeSeconds = this.preparationTimeSeconds,
                explanationTimeSeconds = this.explanationTimeSeconds,
                finalGuessTimeSeconds = this.finalGuessTimeSeconds,
                strictMode = this.strictMode,
                gameEndCondition = this.gameEndCondition,
                wordsSource = when (val wordsSource = this.wordsSource) {
                    Room.WordsSource.Players -> ServerApi.WordsSource.Players
                    is Room.WordsSource.ServerDictionary -> ServerApi.WordsSource.ServerDictionary(wordsSource.id)
                },
            )
        
        socketSession.sendSerialized<ServerApi.Signal>(
            ServerApi.Signal.OnlineGameStateUpdate(
                when (state) {
                    is Room.Outgoing.State.GameInitialisation<RoomMetadata, PlayerMetadata, WordsProviderID> ->
                        ServerApi.OnlineGame.State.GameInitialisation(
                            role = ServerApi.OnlineGame.Role.GameInitialisation(
                                name = state.role.metadata.name,
                                userIndex = state.role.userIndex,
                                isHost = state.role.isHost
                            ),
                            playersList = state.playersList.toServerApi(),
                            settingsBuilder = state.settingsBuilder.toServerApi(),
                        )
                    is Room.Outgoing.State.RoundWaiting<RoomMetadata, PlayerMetadata, WordsProviderID> ->
                        ServerApi.OnlineGame.State.RoundWaiting(
                            role = ServerApi.OnlineGame.Role.RoundWaiting(
                                name = state.role.metadata.name,
                                userIndex = state.role.userIndex,
                                isHost = state.role.isHost,
                                roundRole = when (state.role.roundRole) {
                                    Room.Outgoing.Role.RoundWaiting.RoundRole.Player -> ServerApi.OnlineGame.Role.RoundWaiting.RoundRole.Player
                                    Room.Outgoing.Role.RoundWaiting.RoundRole.Listener -> ServerApi.OnlineGame.Role.RoundWaiting.RoundRole.Listener
                                    Room.Outgoing.Role.RoundWaiting.RoundRole.Speaker -> ServerApi.OnlineGame.Role.RoundWaiting.RoundRole.Speaker
                                },
                            ),
                            playersList = state.playersList.toServerApi(),
                            settings = state.settings.toServerApi(),
                            roundNumber = state.roundNumber,
                            cycleNumber = state.cycleNumber,
                            speakerIndex = state.speakerIndex,
                            listenerIndex = state.listenerIndex,
                            explanationScores = state.explanationScores,
                            guessingScores = state.guessingScores,
                            speakerReady = state.speakerReady,
                            listenerReady = state.listenerReady,
                        )
                    is Room.Outgoing.State.RoundPreparation<RoomMetadata, PlayerMetadata, WordsProviderID> ->
                        ServerApi.OnlineGame.State.RoundPreparation(
                            role = ServerApi.OnlineGame.Role.RoundPreparation(
                                name = state.role.metadata.name,
                                userIndex = state.role.userIndex,
                                isHost = state.role.isHost,
                                roundRole = when (state.role.roundRole) {
                                    Room.Outgoing.Role.RoundPreparation.RoundRole.Player -> ServerApi.OnlineGame.Role.RoundPreparation.RoundRole.Player
                                    Room.Outgoing.Role.RoundPreparation.RoundRole.Listener -> ServerApi.OnlineGame.Role.RoundPreparation.RoundRole.Listener
                                    Room.Outgoing.Role.RoundPreparation.RoundRole.Speaker -> ServerApi.OnlineGame.Role.RoundPreparation.RoundRole.Speaker
                                },
                            ),
                            playersList = state.playersList.toServerApi(),
                            settings = state.settings.toServerApi(),
                            roundNumber = state.roundNumber,
                            cycleNumber = state.cycleNumber,
                            speakerIndex = state.speakerIndex,
                            listenerIndex = state.listenerIndex,
                            millisecondsLeft = state.millisecondsLeft,
                            explanationScores = state.explanationScores,
                            guessingScores = state.guessingScores,
                        )
                    is Room.Outgoing.State.RoundExplanation<RoomMetadata, PlayerMetadata, WordsProviderID> ->
                        ServerApi.OnlineGame.State.RoundExplanation(
                            role = ServerApi.OnlineGame.Role.RoundExplanation(
                                name = state.role.metadata.name,
                                userIndex = state.role.userIndex,
                                isHost = state.role.isHost,
                                roundRole = when (val role = state.role.roundRole) {
                                    Room.Outgoing.Role.RoundExplanation.RoundRole.Player -> ServerApi.OnlineGame.Role.RoundExplanation.RoundRole.Player
                                    Room.Outgoing.Role.RoundExplanation.RoundRole.Listener -> ServerApi.OnlineGame.Role.RoundExplanation.RoundRole.Listener
                                    is Room.Outgoing.Role.RoundExplanation.RoundRole.Speaker -> ServerApi.OnlineGame.Role.RoundExplanation.RoundRole.Speaker(role.currentWord)
                                },
                            ),
                            playersList = state.playersList.toServerApi(),
                            settings = state.settings.toServerApi(),
                            roundNumber = state.roundNumber,
                            cycleNumber = state.cycleNumber,
                            speakerIndex = state.speakerIndex,
                            listenerIndex = state.listenerIndex,
                            millisecondsLeft = state.millisecondsLeft,
                            explanationScores = state.explanationScores,
                            guessingScores = state.guessingScores,
                        )
                    is Room.Outgoing.State.RoundLastGuess<RoomMetadata, PlayerMetadata, WordsProviderID> ->
                        ServerApi.OnlineGame.State.RoundLastGuess(
                            role = ServerApi.OnlineGame.Role.RoundLastGuess(
                                name = state.role.metadata.name,
                                userIndex = state.role.userIndex,
                                isHost = state.role.isHost,
                                roundRole = when (val role = state.role.roundRole) {
                                    Room.Outgoing.Role.RoundLastGuess.RoundRole.Player -> ServerApi.OnlineGame.Role.RoundLastGuess.RoundRole.Player
                                    Room.Outgoing.Role.RoundLastGuess.RoundRole.Listener -> ServerApi.OnlineGame.Role.RoundLastGuess.RoundRole.Listener
                                    is Room.Outgoing.Role.RoundLastGuess.RoundRole.Speaker -> ServerApi.OnlineGame.Role.RoundLastGuess.RoundRole.Speaker(role.currentWord)
                                },
                            ),
                            playersList = state.playersList.toServerApi(),
                            settings = state.settings.toServerApi(),
                            roundNumber = state.roundNumber,
                            cycleNumber = state.cycleNumber,
                            speakerIndex = state.speakerIndex,
                            listenerIndex = state.listenerIndex,
                            millisecondsLeft = state.millisecondsLeft,
                            explanationScores = state.explanationScores,
                            guessingScores = state.guessingScores,
                        )
                    is Room.Outgoing.State.RoundEditing<RoomMetadata, PlayerMetadata, WordsProviderID> ->
                        ServerApi.OnlineGame.State.RoundEditing(
                            role = ServerApi.OnlineGame.Role.RoundEditing(
                                name = state.role.metadata.name,
                                userIndex = state.role.userIndex,
                                isHost = state.role.isHost,
                                roundRole = when (val role = state.role.roundRole) {
                                    Room.Outgoing.Role.RoundEditing.RoundRole.Player -> ServerApi.OnlineGame.Role.RoundEditing.RoundRole.Player
                                    Room.Outgoing.Role.RoundEditing.RoundRole.Listener -> ServerApi.OnlineGame.Role.RoundEditing.RoundRole.Listener
                                    is Room.Outgoing.Role.RoundEditing.RoundRole.Speaker -> ServerApi.OnlineGame.Role.RoundEditing.RoundRole.Speaker(role.wordsToEdit)
                                },
                            ),
                            playersList = state.playersList.toServerApi(),
                            settings = state.settings.toServerApi(),
                            roundNumber = state.roundNumber,
                            cycleNumber = state.cycleNumber,
                            speakerIndex = state.speakerIndex,
                            listenerIndex = state.listenerIndex,
                            explanationScores = state.explanationScores,
                            guessingScores = state.guessingScores,
                        )
                    is Room.Outgoing.State.GameResults<RoomMetadata, PlayerMetadata, WordsProviderID> ->
                        ServerApi.OnlineGame.State.GameResults(
                            role = ServerApi.OnlineGame.Role.GameResults(
                                name = state.role.metadata.name,
                                userIndex = state.role.userIndex,
                                isHost = state.role.isHost,
                            ),
                            playersList = state.playersList.map { it.metadata.name },
                            results = state.results,
                        )
                }
            )
        )
    }
    
    override suspend fun sendError(error: Room.Outgoing.Error) {
        val errorToSend = when (error) {
            Room.Outgoing.Error.AttachmentIsDenied -> ServerApi.OnlineGame.Error.AttachmentIsDenied
            Room.Outgoing.Error.AttachmentIsAlreadySevered -> ServerApi.OnlineGame.Error.AttachmentIsAlreadySevered
            Room.Outgoing.Error.NotHostChangingGameSettings -> ServerApi.OnlineGame.Error.NotHostChangingGameSettings
            Room.Outgoing.Error.CannotUpdateGameSettingsAfterInitialization -> ServerApi.OnlineGame.Error.CannotUpdateGameSettingsAfterInitialization
            Room.Outgoing.Error.NotEnoughPlayersForInitialization -> ServerApi.OnlineGame.Error.NotEnoughPlayersForInitialization
            Room.Outgoing.Error.CannotInitializationGameSettingsAfterInitialization -> ServerApi.OnlineGame.Error.CannotInitializationGameSettingsAfterInitialization
            Room.Outgoing.Error.CannotSetSpeakerReadinessNotDuringRoundWaiting -> ServerApi.OnlineGame.Error.CannotSetSpeakerReadinessNotDuringRoundWaiting
            Room.Outgoing.Error.CannotSetListenerReadinessNotDuringRoundWaiting -> ServerApi.OnlineGame.Error.CannotSetListenerReadinessNotDuringRoundWaiting
            Room.Outgoing.Error.CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting -> ServerApi.OnlineGame.Error.CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting
            Room.Outgoing.Error.CannotUpdateRoundInfoNotDuringTheRound -> ServerApi.OnlineGame.Error.CannotUpdateRoundInfoNotDuringTheRound
            Room.Outgoing.Error.CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess -> ServerApi.OnlineGame.Error.CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess
            Room.Outgoing.Error.CannotUpdateWordExplanationResultsNotDuringRoundEditing -> ServerApi.OnlineGame.Error.CannotUpdateWordExplanationResultsNotDuringRoundEditing
            Room.Outgoing.Error.CannotConfirmWordExplanationResultsNotDuringRoundEditing -> ServerApi.OnlineGame.Error.CannotConfirmWordExplanationResultsNotDuringRoundEditing
            Room.Outgoing.Error.CannotFinishGameNotDuringRoundWaiting -> ServerApi.OnlineGame.Error.CannotFinishGameNotDuringRoundWaiting
        }
        
        socketSession.sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(errorToSend))
    }
}

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
                        
                        when (signal) {
                            is ClientApi.Signal.FetchFreeRoomId -> {
                                // TODO
                            }
                            is ClientApi.Signal.FetchRoomInfo -> {
                                val room = rooms[signal.roomId]
                                
                                sendSerialized<ServerApi.Signal>(
                                    ServerApi.Signal.RoomInfo(
                                        room?.description
                                            ?.let {
                                                ServerApi.RoomDescription(
                                                    name = it.metadata.name,
                                                    playersList = KoneList.empty(),
                                                    state = when (it.stateType) {
                                                        Room.StateType.GameInitialisation -> ServerApi.RoomStateType.GameInitialisation
                                                        Room.StateType.RoundWaiting -> ServerApi.RoomStateType.RoundWaiting
                                                        Room.StateType.RoundPreparation -> ServerApi.RoomStateType.RoundPreparation
                                                        Room.StateType.RoundExplanation -> ServerApi.RoomStateType.RoundExplanation
                                                        Room.StateType.RoundLastGuess -> ServerApi.RoomStateType.RoundLastGuess
                                                        Room.StateType.RoundEditing -> ServerApi.RoomStateType.RoundEditing
                                                        Room.StateType.GameResults -> ServerApi.RoomStateType.GameResults
                                                    }
                                                )
                                            }
                                            ?: ServerApi.RoomDescription(
                                                name = signal.roomId,
                                                playersList = KoneList.empty(),
                                                state = ServerApi.RoomStateType.GameInitialisation
                                            )
                                    )
                                )
                            }
                            is ClientApi.Signal.OnlineGame.JoinRoom -> connection.playerAttachmentMutex.withLock {
                                if (connection.playerAttachment != null) {
                                    sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(ServerApi.OnlineGame.Error.UnspecifiedError))
                                    return@withLock
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
                                
                                val attachmentResult = getRoomByIdOrCreate(signal.roomId).attachConnectionToPlayer(connection, signal.playerName)
                                
                                if (attachmentResult == null) {
                                    logger.debug(
                                        items = {
                                            mapOf(
                                                "connection" to connection.toString(),
                                                "room" to signal.roomId,
                                                "player" to signal.playerName,
                                                "result" to attachmentResult.toString(),
                                            )
                                        }
                                    ) { "Connection was not attached" }
                                } else {
                                    logger.debug(
                                        items = {
                                            mapOf(
                                                "connection" to connection.toString(),
                                                "room" to signal.roomId,
                                                "player" to signal.playerName,
                                                "result" to attachmentResult.toString(),
                                            )
                                        }
                                    ) { "Successfully attached connection to player" }
                                    
                                    connection.playerAttachment = attachmentResult
                                }
                            }
                            
                            ClientApi.Signal.OnlineGame.LeaveRoom -> connection.playerAttachmentMutex.withLock {
                                val attachment = connection.playerAttachment
                                
                                if (attachment == null) {
                                    sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(ServerApi.OnlineGame.Error.UnspecifiedError))
                                    return@withLock
                                }
                                
                                attachment.sever()
                            }
                            
                            is ClientApi.Signal.OnlineGame.UpdateSettings -> connection.playerAttachmentMutex.withLock {
                                val attachment = connection.playerAttachment
                                
                                if (attachment == null) {
                                    sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(ServerApi.OnlineGame.Error.UnspecifiedError))
                                    return@withLock
                                }
                                
                                val settingsBuilder = signal.settingsBuilder
                                
                                attachment.updateGameSettings(
                                    Room.GameSettings.Builder(
                                        preparationTimeSeconds = settingsBuilder.preparationTimeSeconds,
                                        explanationTimeSeconds = settingsBuilder.explanationTimeSeconds,
                                        finalGuessTimeSeconds = settingsBuilder.finalGuessTimeSeconds,
                                        strictMode = settingsBuilder.strictMode,
                                        cachedEndConditionWordsNumber = settingsBuilder.cachedEndConditionWordsNumber,
                                        cachedEndConditionCyclesNumber = settingsBuilder.cachedEndConditionCyclesNumber,
                                        gameEndConditionType = settingsBuilder.gameEndConditionType,
                                        wordsSource = when (val wordsSource = settingsBuilder.wordsSource) {
                                            ClientApi.WordsSource.Players -> Room.WordsSource.Players
                                            is ClientApi.WordsSource.ServerDictionary -> Room.WordsSource.ServerDictionary(wordsSource.id)
                                        },
                                    )
                                )
                            }
                            ClientApi.Signal.OnlineGame.InitializeGame -> connection.playerAttachmentMutex.withLock {
                                val attachment = connection.playerAttachment
                                
                                if (attachment == null) {
                                    sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(ServerApi.OnlineGame.Error.UnspecifiedError))
                                    return@withLock
                                }
                                
                                attachment.initializeGame()
                            }
                            ClientApi.Signal.OnlineGame.SpeakerReady -> connection.playerAttachmentMutex.withLock {
                                val attachment = connection.playerAttachment
                                
                                if (attachment == null) {
                                    sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(ServerApi.OnlineGame.Error.UnspecifiedError))
                                    return@withLock
                                }
                                
                                attachment.speakerReady()
                            }
                            ClientApi.Signal.OnlineGame.ListenerReady -> connection.playerAttachmentMutex.withLock {
                                val attachment = connection.playerAttachment
                                
                                if (attachment == null) {
                                    sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(ServerApi.OnlineGame.Error.UnspecifiedError))
                                    return@withLock
                                }
                                
                                attachment.listenerReady()
                            }
                            is ClientApi.Signal.OnlineGame.WordExplanationState -> connection.playerAttachmentMutex.withLock {
                                val attachment = connection.playerAttachment
                                
                                if (attachment == null) {
                                    sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(ServerApi.OnlineGame.Error.UnspecifiedError))
                                    return@withLock
                                }
                                
                                attachment.wordExplanationState(signal.state)
                            }
                            is ClientApi.Signal.OnlineGame.UpdateWordsExplanationResults -> connection.playerAttachmentMutex.withLock {
                                val attachment = connection.playerAttachment
                                
                                if (attachment == null) {
                                    sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(ServerApi.OnlineGame.Error.UnspecifiedError))
                                    return@withLock
                                }
                                
                                attachment.updateWordsExplanationResults(signal.newExplanationResults)
                            }
                            ClientApi.Signal.OnlineGame.ConfirmWordsExplanationResults -> connection.playerAttachmentMutex.withLock {
                                val attachment = connection.playerAttachment
                                
                                if (attachment == null) {
                                    sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(ServerApi.OnlineGame.Error.UnspecifiedError))
                                    return@withLock
                                }
                                
                                attachment.confirmWordsExplanationResults()
                            }
                            ClientApi.Signal.OnlineGame.FinishGame -> connection.playerAttachmentMutex.withLock {
                                val attachment = connection.playerAttachment
                                
                                if (attachment == null) {
                                    sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(ServerApi.OnlineGame.Error.UnspecifiedError))
                                    return@withLock
                                }
                                
                                attachment.finishGame()
                            }
                        }
                    }
                } catch (exception: Exception) {
                    logger.warn(
                        throwable = exception,
                        items = {
                            mapOf("connection" to connection.toString())
                        }
                    ) { "Caught exception during WebSocket connection" }
                    throw exception
                } catch (exception: Error) {
                    logger.warn(
                        throwable = exception,
                        items = {
                            mapOf("connection" to connection.toString())
                        }
                    ) { "Caught error during WebSocket connection" }
                    throw exception
                } finally {
                    logger.info(
                        items = {
                            mapOf("connection" to connection.toString())
                        }
                    ) { "Closed connection" }
                    withContext(NonCancellable) {
                        connection.playerAttachmentMutex.withLock {
                            connection.playerAttachment?.sever()
                        }
                    }
                }
            }
        }
    }.start(wait = true)
}