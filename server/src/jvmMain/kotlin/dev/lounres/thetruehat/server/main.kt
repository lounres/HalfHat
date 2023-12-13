package dev.lounres.thetruehat.server

import dev.lounres.thetruehat.api.*
import dev.lounres.thetruehat.api.models.updateWith
import dev.lounres.thetruehat.server.model.Room
import dev.lounres.thetruehat.server.model.getOrCreatePlayerByNickname
import dev.lounres.thetruehat.server.model.online
import dev.lounres.thetruehat.server.model.state
import io.ktor.serialization.deserialize
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream
import java.util.concurrent.ConcurrentHashMap


val rooms: MutableMap<String, Room> = ConcurrentHashMap()

class Connection(
    val socketSession: WebSocketServerSession,
//    val timeZoneOffset: Long,
) {
    var player: Room.Player? = null
}


fun main() {
    System.setOut(PrintStream(FileOutputStream(FileDescriptor.out), true, "UTF-8"))
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
                                        userGameState = connection.state,
                                        errorMessage = "Вы уже находитесь в комнате"
                                    ).send()
                                    continue
                                }

                                // TODO: Room IDs and nicknames checks

                                val room = rooms.getOrPut(clientSignal.roomId) { Room.Waiting(id = clientSignal.roomId) }
                                when(room) {
                                    is Room.Waiting -> {
                                        val newPlayer = room.getOrCreatePlayerByNickname(clientSignal.nickname)
                                        if (newPlayer.connection != null) {
                                            ServerSignal.RequestError(
                                                userGameState = null,
                                                errorMessage = "Это имя уже использовано"
                                            ).send()
                                            continue
                                        }
                                        newPlayer.connection = connection
                                        connection.player = newPlayer
                                        room.sendStatusUpdate()
                                    }
                                    is Room.Playing -> {
                                        ServerSignal.RequestError(userGameState = connection.state, errorMessage = "Игра уже идёт, возможен только вход").send()
                                        continue
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
                                        for (player in thePlayer.room.players) {
                                            val playerConnection = player.connection ?: continue
                                            with(playerConnection.socketSession) {
                                                ServerSignal.StatusUpdate(userGameState = player.state).send()
                                            }
                                        }
                                    }
                                    is Room.Playing.Player -> {
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
                        }
                    }
                } catch (e: Exception) {
                    println(e.localizedMessage)
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