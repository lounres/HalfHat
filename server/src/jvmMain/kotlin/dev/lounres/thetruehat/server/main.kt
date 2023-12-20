package dev.lounres.thetruehat.server

import dev.lounres.thetruehat.api.models.RoomDescription
import dev.lounres.thetruehat.api.models.updateWith
import dev.lounres.thetruehat.api.signals.ClientSignal
import dev.lounres.thetruehat.api.signals.ServerSignal
import dev.lounres.thetruehat.server.model.*
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.serialization.deserialize
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration


val logger = KotlinLogging.logger {}

val rooms: MutableMap<String, Room> = ConcurrentHashMap()

class Connection(
    val socketSession: WebSocketServerSession,
//    val timeZoneOffset: Long, // TODO
) {
    var player: Room.Player? = null
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
                logger.info { "New connection: $connection" }
                try {
                    for (incomingFrame in incoming) {
                        val converter = converter!!

                        if (!converter.isApplicable(incomingFrame)) {
                            logger.warn { "Received unexpected websocket frame: $incoming" }
                            continue
                        }

                        val clientSignal = converter.deserialize<ClientSignal>(content = incomingFrame)
                        logger.info { "Incoming signal: $clientSignal" }
                        when (clientSignal) {
                            is ClientSignal.JoinRoom -> {
                                if (connection.player != null) {
                                    ServerSignal.RequestError(userGameState = connection.state, errorMessage = "Вы уже находитесь в комнате").send()
                                    continue
                                }

                                // TODO: Room IDs and nicknames checks

                                val room = rooms.getOrPut(clientSignal.roomId) { Room.Waiting(id = clientSignal.roomId) }
                                when(room) {
                                    is Room.Waiting -> {
                                        val newPlayer = room.getOrCreatePlayerByNickname(clientSignal.nickname)
                                        if (newPlayer.connection != null) {
                                            ServerSignal.RequestError(userGameState = null, errorMessage = "Это имя уже использовано").send()
                                            continue
                                        }
                                        newPlayer.connection = connection
                                        connection.player = newPlayer
                                        room.sendStatusUpdate()
                                    }
                                    is Room.Playing -> {
                                        val player = room.players.find { it.username == clientSignal.nickname }
                                        if (player == null) {
                                            ServerSignal.RequestError(userGameState = connection.state, errorMessage = "Игра уже идёт, возможен только вход").send()
                                            continue
                                        }
                                        if (player.connection != null) {
                                            ServerSignal.RequestError(userGameState = null, errorMessage = "Это имя уже использовано").send()
                                            continue
                                        }
                                        player.connection = connection
                                        connection.player = player
                                        room.sendStatusUpdate()
                                    }
                                    is Room.Results -> {
                                        ServerSignal.RequestError(userGameState = connection.state, errorMessage = "Игра уже закончена, вход невозможен").send()
                                        continue
                                    }
                                }
                            }
                            ClientSignal.LeaveRoom -> {
                                val thePlayer = connection.player ?: continue
                                when(thePlayer) {
                                    is Room.Waiting.Player -> {
                                        thePlayer.connection = null
                                        connection.player = null
                                        ServerSignal.StatusUpdate(userGameState = null).send()
                                        thePlayer.room.sendStatusUpdate()
                                    }
                                    is Room.Playing.Player -> {
                                        thePlayer.connection = null
                                        connection.player = null
                                        ServerSignal.StatusUpdate(userGameState = null).send()
                                        thePlayer.room.sendStatusUpdate()
                                    }
                                    is Room.Results.Player -> {
                                        // TODO: Logs that something went wrong
                                        connection.player = null
                                        ServerSignal.RequestError(
                                            userGameState = null,
                                            errorMessage = "Вы не в комнате",
                                        ).send()
                                    }
                                }
                            }
                            ClientSignal.RequestFreeRoomId -> {
                                val freeRoomId: String = generateRandomRoomId()
                                ServerSignal.ProvideFreeRoomId(userGameState = connection.state, freeRoomId = freeRoomId).send()
                            }
                            is ClientSignal.UpdateSettings -> {
                                val player = connection.player
                                if (player == null) {
                                    ServerSignal.RequestError(userGameState = null, errorMessage = "Вы не в комнате").send()
                                    continue
                                }
                                when(val room = player.room) {
                                    is Room.Waiting -> {
                                        if (room.players.first { it.online } != player) {
                                            ServerSignal.RequestError(userGameState = player.state, errorMessage = "Только хост может изменить настройки").send()
                                            continue
                                        }
                                        room.settings = room.settings.updateWith(clientSignal.settingsUpdate)
                                        room.sendStatusUpdate()
                                    }
                                    is Room.Playing -> {
                                        ServerSignal.RequestError(userGameState = player.state, errorMessage = "Игра уже начата").send()
                                        continue
                                    }
                                    is Room.Results -> {
                                        ServerSignal.RequestError(userGameState = player.state, errorMessage = "Игра закончена").send()
                                        continue
                                    }
                                }
                            }
                            ClientSignal.StartGame -> {
                                val player = connection.player
                                if (player == null) {
                                    ServerSignal.RequestError(userGameState = null, errorMessage = "Вы не в комнате").send()
                                    continue
                                }
                                when(val room = player.room) {
                                    is Room.Waiting -> {
                                        if (room.players.first { it.online } != player) {
                                            ServerSignal.RequestError(userGameState = player.state, errorMessage = "Только хост может начать игру").send()
                                            continue
                                        }
                                        if (room.players.count { it.online } < 2) {
                                            ServerSignal.RequestError(userGameState = player.state, errorMessage = "Недостаточно игроков онлайн в комнате, чтобы начать игру (необходимо хотя бы два)").send()
                                            continue
                                        }
                                        val newRoom = Room.Playing(id = room.id, settings = room.settings, playerListToProcess = room.players)
                                        rooms[newRoom.id] = newRoom
                                        newRoom.sendStatusUpdate()
                                    }
                                    is Room.Playing -> {
                                        ServerSignal.RequestError(userGameState = player.state, errorMessage = "Игра уже начата").send()
                                        continue
                                    }
                                    is Room.Results -> {
                                        ServerSignal.RequestError(userGameState = player.state, errorMessage = "Игра закончена").send()
                                        continue
                                    }
                                }
                            }
                            ClientSignal.EndGame -> {
                                val player = connection.player
                                if (player == null) {
                                    ServerSignal.RequestError(userGameState = null, errorMessage = "Вы не в комнате").send()
                                    continue
                                }
                                when(val room = player.room) {
                                    is Room.Waiting -> {
                                        ServerSignal.RequestError(userGameState = player.state, errorMessage = "Игра ещё не начата").send()
                                        continue
                                    }
                                    is Room.Playing -> {
                                        // TODO: Check round phase
                                        val otherConnections = room.players.map { it.connection }
                                        val newRoom = Room.Results(id = room.id, settings = room.settings, playerListToProcess = room.players)
                                        rooms[newRoom.id] = newRoom
                                        for (otherConnection in otherConnections) if (otherConnection != null) with(otherConnection.socketSession) {
                                            ServerSignal.StatusUpdate(userGameState = null).send()
                                            // TODO: Send something different to show the results
                                        }
                                    }
                                    is Room.Results -> {
                                        ServerSignal.RequestError(userGameState = player.state, errorMessage = "Игра закончена").send()
                                        continue
                                    }
                                }
                            }
                            ClientSignal.ReadyForTheRound -> {
                                val player = connection.player
                                if (player == null) {
                                    ServerSignal.RequestError(userGameState = null, errorMessage = "Вы не в комнате").send()
                                    continue
                                }
                                when(val room = player.room) {
                                    is Room.Waiting -> {
                                        ServerSignal.RequestError(userGameState = player.state, errorMessage = "Игра ещё не начата").send()
                                        continue
                                    }
                                    is Room.Playing -> {
                                        when(room.roundPhase) {
                                            is Room.Playing.RoundPhase.Countdown -> {
                                                ServerSignal.RequestError(userGameState = player.state, errorMessage = "Игра не на стадии подготовки к раунду").send()
                                                continue
                                            }
                                            Room.Playing.RoundPhase.WaitingForPlayersToBeReady -> {
                                                when(player.playerIndex) {
                                                    room.speaker -> {
                                                        if (!room.speakerReady) {
                                                            room.speakerReady = true
                                                        } else {
                                                            ServerSignal.RequestError(userGameState = player.state, errorMessage = "Объясняющий уже и так готов").send()
                                                            continue
                                                        }
                                                    }
                                                    room.listener -> {
                                                        if (!room.listenerReady) {
                                                            room.listenerReady = true
                                                        } else {
                                                            ServerSignal.RequestError(userGameState = player.state, errorMessage = "Отгадывающий уже и так готов").send()
                                                            continue
                                                        }
                                                    }
                                                    else -> {
                                                        ServerSignal.RequestError(userGameState = player.state, errorMessage = "Вы не объясняющий и не отгадывающий").send()
                                                        continue
                                                    }
                                                }
                                                if (room.speakerReady && room.listenerReady) {
                                                    val transferTime = 1 // TODO: Move the constant `1` somewhere appropriate
                                                    val countdownTime = room.settings.countdownTime
                                                    val explanationTime = room.settings.explanationTime
                                                    val finalGuessTime = room.settings.finalGuessTime
                                                    val startInstant = Clock.System.now() + with(Duration) { (countdownTime + transferTime).seconds }

                                                    val roundStartJob = GlobalScope.launch {
                                                        delay(with(Duration) { (countdownTime + transferTime).seconds })
                                                        if (room.isEnded) {
                                                            logger.warn { "Round started without any words" }
                                                            val newRoom = Room.Results(
                                                                id = room.id,
                                                                settings = room.settings,
                                                                playerListToProcess = room.players,
                                                            )
                                                            rooms[newRoom.id] = newRoom
                                                        } else {
                                                            room.setNextWordForExplanation()
                                                        }
                                                        room.sendStatusUpdate()
                                                    }
                                                    val strictEndJob =
                                                        if (room.settings.strictMode)
                                                            GlobalScope.launch {
                                                                delay(with(Duration) { (countdownTime + explanationTime + finalGuessTime + transferTime).seconds })
                                                                val roundPhase = room.roundPhase
                                                                if (roundPhase is Room.Playing.RoundPhase.ExplanationInProgress) {
                                                                    room.wordsToEdit.add(
                                                                        RoomDescription.WordExplanationResult(
                                                                            word = roundPhase.wordToExplain,
                                                                            state = RoomDescription.WordExplanationResult.State.NotExplained,
                                                                        )
                                                                    )
                                                                } else {
                                                                    logger.warn { "Unexpected room phase during strict game ending" }
                                                                }
                                                                room.roundPhase = Room.Playing.RoundPhase.EditingInProgress
                                                                room.sendStatusUpdate()
                                                            }
                                                        else null

                                                    room.roundPhase = Room.Playing.RoundPhase.Countdown(
                                                        startInstant = startInstant,
                                                        roundStartJob = roundStartJob,
                                                        strictEndJob = strictEndJob
                                                    )
                                                }
                                                room.sendStatusUpdate()
                                            }
                                            is Room.Playing.RoundPhase.ExplanationInProgress -> {
                                                ServerSignal.RequestError(userGameState = player.state, errorMessage = "Игра не на стадии подготовки к раунду").send()
                                                continue
                                            }
                                            Room.Playing.RoundPhase.EditingInProgress -> {
                                                ServerSignal.RequestError(userGameState = player.state, errorMessage = "Игра не на стадии подготовки к раунду").send()
                                                continue
                                            }
                                        }
                                    }
                                    is Room.Results -> {
                                        ServerSignal.RequestError(userGameState = player.state, errorMessage = "Игра закончена").send()
                                        continue
                                    }
                                }
                            }
                            is ClientSignal.ExplanationResult -> {
                                val player = connection.player
                                if (player == null) {
                                    ServerSignal.RequestError(userGameState = null, errorMessage = "Вы не в комнате").send()
                                    continue
                                }
                                when(val room = player.room) {
                                    is Room.Waiting -> {
                                        ServerSignal.RequestError(userGameState = player.state, errorMessage = "Игра ещё не начата").send()
                                        continue
                                    }
                                    is Room.Playing -> {
                                        when(val roundPhase = room.roundPhase) {
                                            is Room.Playing.RoundPhase.Countdown -> {
                                                ServerSignal.RequestError(userGameState = player.state, errorMessage = "Игра не на стадии объяснения слов").send()
                                                continue
                                            }
                                            Room.Playing.RoundPhase.WaitingForPlayersToBeReady -> {
                                                ServerSignal.RequestError(userGameState = player.state, errorMessage = "Игра не на стадии объяснения слов").send()
                                                continue
                                            }
                                            is Room.Playing.RoundPhase.ExplanationInProgress -> {
                                                if (player.playerIndex == room.speaker) {
                                                    room.wordsToEdit.add(
                                                        RoomDescription.WordExplanationResult(
                                                            word = roundPhase.wordToExplain,
                                                            state = clientSignal.result,
                                                        )
                                                    )
                                                    when (clientSignal.result) {
                                                        RoomDescription.WordExplanationResult.State.Explained -> {
                                                            if (room.isEnded || Clock.System.now() > roundPhase.endInstant) {
                                                                roundPhase.strictEndJob?.cancel()
                                                                room.roundPhase = Room.Playing.RoundPhase.EditingInProgress
                                                            } else {
                                                                room.setNextWordForExplanation()
                                                            }
                                                        }
                                                        RoomDescription.WordExplanationResult.State.NotExplained,
                                                        RoomDescription.WordExplanationResult.State.Mistake -> {
                                                            roundPhase.strictEndJob?.cancel()
                                                            room.roundPhase = Room.Playing.RoundPhase.EditingInProgress
                                                        }
                                                    }
                                                    room.sendStatusUpdate()
                                                } else {
                                                    ServerSignal.RequestError(userGameState = player.state, errorMessage = "Вы не объясняющий").send()
                                                    continue
                                                }
                                            }
                                            Room.Playing.RoundPhase.EditingInProgress -> {
                                                ServerSignal.RequestError(userGameState = player.state, errorMessage = "Игра не на стадии объяснения слов").send()
                                                continue
                                            }
                                        }
                                    }
                                    is Room.Results -> {
                                        ServerSignal.RequestError(userGameState = player.state, errorMessage = "Игра закончена").send()
                                        continue
                                    }
                                }
                            }
                            is ClientSignal.SubmitResults -> {
                                val player = connection.player
                                if (player == null) {
                                    ServerSignal.RequestError(userGameState = null, errorMessage = "Вы не в комнате").send()
                                    continue
                                }
                                when(val room = player.room) {
                                    is Room.Waiting -> {
                                        ServerSignal.RequestError(userGameState = player.state, errorMessage = "Игра ещё не начата").send()
                                        continue
                                    }
                                    is Room.Playing -> {
                                        when(room.roundPhase) {
                                            is Room.Playing.RoundPhase.Countdown -> {
                                                ServerSignal.RequestError(userGameState = player.state, errorMessage = "Игра не на стадии редактирования раунда").send()
                                                continue
                                            }
                                            Room.Playing.RoundPhase.WaitingForPlayersToBeReady -> {
                                                ServerSignal.RequestError(userGameState = player.state, errorMessage = "Игра не на стадии редактирования раунда").send()
                                                continue
                                            }
                                            is Room.Playing.RoundPhase.ExplanationInProgress -> {
                                                ServerSignal.RequestError(userGameState = player.state, errorMessage = "Игра не на стадии редактирования раунда").send()
                                                continue
                                            }
                                            Room.Playing.RoundPhase.EditingInProgress -> {
                                                if (room.wordsToEdit.map { it.word } != clientSignal.results.map { it.word }) {
                                                    ServerSignal.RequestError(userGameState = player.state, errorMessage = "Несовпадение слов редактирования раунда").send()
                                                    continue
                                                }
                                                val (notExplained, others) = clientSignal.results.partition { it.state == RoomDescription.WordExplanationResult.State.NotExplained }
                                                room.usedWords.addAll(others)
                                                notExplained.mapTo(room.freshWords) { it.word }
                                                room.wordsToEdit.clear()
                                                val points = others.count { it.state == RoomDescription.WordExplanationResult.State.Explained }
                                                room.players[room.speaker].scoreExplained += points
                                                room.players[room.listener].scoreGuessed += points
                                                room.prepareForRound()
                                                room.roundPhase = Room.Playing.RoundPhase.WaitingForPlayersToBeReady
                                                room.sendStatusUpdate()
                                            }
                                        }
                                    }
                                    is Room.Results -> {
                                        // TODO: Log as warning
                                        ServerSignal.RequestError(userGameState = player.state, errorMessage = "Игра закончена").send()
                                        continue
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.warn { e.localizedMessage }
                } finally {
                    val player = connection.player
                    if (player != null) {
                        when(player) {
                            is Room.Waiting.Player -> player.connection = null
                            is Room.Playing.Player -> player.connection = null
                            is Room.Results.Player -> {}
                        }
                    }
                }
            }
        }
    }.start(wait = true)
}