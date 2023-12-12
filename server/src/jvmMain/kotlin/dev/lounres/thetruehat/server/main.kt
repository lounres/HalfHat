package dev.lounres.thetruehat.server

import dev.lounres.thetruehat.api.*
import dev.lounres.thetruehat.server.model.Room
import dev.lounres.thetruehat.server.model.getOrCreatePlayerByNickname
import dev.lounres.thetruehat.server.model.state
import io.ktor.serialization.deserialize
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap


val rooms: MutableMap<String, Room> = ConcurrentHashMap()

class Connection(
    val socketSession: WebSocketServerSession,
//    val timeZoneOffset: Long,
) {
    var player: Room.Player? = null
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
                println("New connection: $connection")
                try {
                    for (incomingFrame in incoming) {
                        val converter = converter!!

                        if (!converter.isApplicable(incomingFrame)) continue

                        when (val clientSignal = converter.deserialize<ClientSignal>(content = incomingFrame).also { println("Receive: $it") }) {
                            is ClientSignal.JoinRoom -> {
                                if (connection.player != null) {
                                    ServerSignal.RequestError(
                                        userGameState = connection.player!!.state,
                                        errorMessage = "Вы уже находитесь в комнате"
                                    ).send()
                                    continue
                                }

                                // TODO: Room IDs and nicknames checks

                                val room = rooms.getOrPut(clientSignal.roomId) { Room.Waiting(id = clientSignal.roomId) }
                                val newPlayer =
                                    if (room is Room.Waiting) room.getOrCreatePlayerByNickname(clientSignal.nickname)
                                    else {
                                        val player = room.players.find { it.username == clientSignal.nickname }
                                        if (player == null) {
                                            ServerSignal.RequestError(
                                                userGameState = connection.player?.state,
                                                errorMessage = "Игра уже идёт, возможен только вход"
                                            )
                                            continue
                                        }
                                        player
                                    }
                                if (newPlayer.connection != null) {
                                    ServerSignal.RequestError(
                                        userGameState = null,
                                        errorMessage = "Это имя уже использовано"
                                    ).send()
                                    continue
                                }
                                newPlayer.connection = connection
                                connection.player = newPlayer
                                for (player in room.players) {
                                    println("player Player(username = ${player.username}, room = ${player.room}, connection = ${player.connection})")
                                    val playerConnection = player.connection ?: continue
                                    with(playerConnection.socketSession) {
                                        ServerSignal.StatusUpdate(userGameState = player.state).send()
                                    }
                                }
                            }
                            ClientSignal.LeaveRoom -> {
                                if (connection.player == null) continue

                                val thePlayer = connection.player!!
                                thePlayer.connection = null
                                connection.player = null
                                ServerSignal.StatusUpdate(userGameState = null).send()
                                for (player in thePlayer.room.players) {
                                    val playerConnection = player.connection ?: continue
                                    with(playerConnection.socketSession) {
                                        ServerSignal.StatusUpdate(userGameState = player.state).send()
                                    }
                                }
                            }
                            ClientSignal.RequestFreeRoomId -> {
                                val freeRoomId: String = generateRandomRoomId()
                                ServerSignal.ProvideFreeRoomId(userGameState = connection.player?.state, freeRoomId = freeRoomId).send()
                            }
                            ClientSignal.StartGame -> {
                                TODO()
                            }
                        }
                    }
                } catch (e: Exception) {
                    println(e.localizedMessage)
                } finally {
                    val player = connection.player
                    if (player != null) {
                        player.connection = null
                    }
                }
            }
        }
    }.start(wait = true)
}