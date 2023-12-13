package dev.lounres.thetruehat.server

import dev.lounres.thetruehat.api.ServerSignal
import dev.lounres.thetruehat.server.model.Room
import dev.lounres.thetruehat.server.model.state
import io.ktor.server.websocket.*
import kotlinx.coroutines.launch
import kotlin.random.Random


context(WebSocketServerSession)
fun ServerSignal.send() {
    println("Send: $this")
    launch { sendSerialized<ServerSignal>(this@ServerSignal) }
}

// TODO: Replace with actual configuration
object Config {
    val minKeyLength = 6
    val maxKeyLength = 11
    val keyConsonant: List<Char> = listOf('б', 'в', 'г', 'д', 'ж', 'з', 'к', 'л', 'м', 'н', 'п', 'р', 'с', 'т', 'ф', 'х', 'ц', 'ч', 'ш', 'щ')
    val keyVowels: List<Char> = listOf('а', 'е', 'и', 'о', 'у', 'э', 'ю', 'я')
}

// TODO: Replace with more accurate implementation
fun generateRandomRoomId(): String {
    // getting the settings
    val minKeyLength = Config.minKeyLength
    val maxKeyLength = Config.maxKeyLength
    val keyConsonant = Config.keyConsonant
    val keyVowels = Config.keyVowels
    // getting the key length
    val keyLength = Random.nextInt(minKeyLength, maxKeyLength + 1)
    // generating the key
    val roomIdBuilder = StringBuilder()
    for (i in 0 ..< keyLength) {
        val charList = if (i % 2 == 0) keyConsonant else keyVowels
        roomIdBuilder.append(charList[Random.nextInt(charList.size)])
    }
    return roomIdBuilder.toString()
}

fun Room.Waiting.sendStatusUpdate() {
    for (player in this.players) {
        println("player Player(username = ${player.username}, room = ${player.room}, connection = ${player.connection})")
        val playerConnection = player.connection ?: continue
        with(playerConnection.socketSession) {
            ServerSignal.StatusUpdate(userGameState = player.state).send()
        }
    }
}

fun Room.Playing.sendStatusUpdate() {
    for (player in this.players) {
        println("player Player(username = ${player.username}, room = ${player.room}, connection = ${player.connection})")
        val playerConnection = player.connection ?: continue
        with(playerConnection.socketSession) {
            ServerSignal.StatusUpdate(userGameState = player.state).send()
        }
    }
}