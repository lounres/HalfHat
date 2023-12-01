package dev.lounres.thetruehat.server

import dev.lounres.thetruehat.api.*
import io.ktor.serialization.deserialize
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


class Room(
    val key: String,
) {
    val players: MutableList<User> = mutableListOf()
    var settings: Settings =
}

class User(
    val username: String,
    var connection: Connection?,
    val timeZoneOffset: Long,
) {
    val online: Boolean get() = connection != null
    var scoreExplained = 0u
    var scoreGuessed = 0u
}

val rooms = mutableMapOf<String, Room>()

class Connection(
    val socketSession: WebSocketServerSession
) {
    var room: Room? = null
}

suspend inline fun <E> ReceiveChannel<E>.forEach(block: (E) -> Unit): Unit {
    for (e in this) {
        block(e)
    }
}
inline fun <E> MutableCollection<E>.firstOrPut(default: () -> E, block: (E) -> Boolean): E {
    for (e in this) if (block(e)) return e
    return default().also { add(it) }
}

context(WebSocketServerSession)
fun ServerSignal.send() {
    launch { sendSerialized<ServerSignal>(this@ServerSignal) }
}

context(WebSocketServerSession)
fun sendError(message: String) = SFailureSignal(message = message, log = "/* TODO */").send()

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(WebSockets) {
            pingPeriodMillis = 1000
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        routing {
            webSocket("/ws") {
                val connection = Connection(this)
                try {
                    incoming.forEach {
                        val converter = converter!!

                        if (!converter.isApplicable(it)) return@forEach

                        val signal = converter.deserialize<ClientSignal>(content = it)

                        when (signal) {
                            is CJoinRoomSignal -> {
                                if (connection.room != null) {
                                    sendError("Вы уже находитесь в комнате")
                                    return@forEach
                                }

                                val (key, username, timeZoneOffset) = signal
                                if (key.isEmpty()) {
                                    sendError("Неверный ключ комнаты")
                                    return@forEach
                                }
                                if (username.isEmpty()) {
                                    sendError("Неверное имя игрока")
                                    return@forEach
                                }
                                val normalizedKey = key.uppercase().replace(Regex("\\s+"), "")
                                val normalizedUsername = username.trim().replace(Regex("\\s+"), " ")
                                val room = rooms.getOrPut(normalizedKey) { Room(normalizedKey) }
                                val playerIndex = room.players.indexOfFirst { it.username == normalizedUsername }
                                val player: User
                                if (playerIndex == -1) {
                                    @Suppress("UNUSED_VALUE")
                                    player = User(username = normalizedUsername, connection = connection, timeZoneOffset = timeZoneOffset).also { room.players.add(it) }
                                } else {
                                    player = room.players[playerIndex]
                                    if (player.connection != null) {
                                        sendError("Это имя уже использовано")
                                        return@forEach
                                    }
                                    player.connection = connection
                                }
                                val playerList = room.players.map { Player(username = it.username, online = it.online) }
                                SYouJoinedSignal( // TODO
                                    room = RoomState(
                                        key = normalizedKey,
                                        stage = RoomState.Stage.Wait(
                                            playerList = playerList,
                                            host = 0,
                                            settings = Settings(
                                                delayTime = 0,
                                                explanationTime = 0,
                                                aftermathTime = 0,
                                                strictMode = false,
                                                terminationCondition = Settings.TerminationCondition.WORDS,
                                                wordsNumber = 0,
                                                roundsNumber = 0,
                                                wordset = Settings.Wordset.PlayerWords,
                                            )
                                        )
                                    )
                                ).send()
                                room.players.forEach sendCycle@{
                                    val playerConnection = it.connection ?: return@sendCycle
                                    with(playerConnection.socketSession) {
                                        SPlayerJoinedSignal(
                                            username = normalizedUsername,
                                            playersList = playerList,
                                            host = 0,
                                        ).send()
                                    }
                                }
                            }
                            CLeaveRoomSignal -> TODO()
                            is CApplySettingsSignal -> TODO()

                            CEndGameSignal -> TODO()
                            is CEndWordExplanationSignal -> TODO()
                            CListenerReadySignal -> TODO()
                            CSpeakerReadySignal -> TODO()
                            CStartGameSignal -> TODO()
                            CStartWordCollectionSignal -> TODO()
                            is CWordsEditedSignal -> TODO()
                            is CWordsReadySignal -> TODO()
                        }
                    }
                } catch (e: Exception) {
                    println(e.localizedMessage)
                } finally {

                }
            }
        }
    }.start(wait = true)
}