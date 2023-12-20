package dev.lounres.thetruehat.server

import dev.lounres.thetruehat.api.signals.ServerSignal
import dev.lounres.thetruehat.server.model.Room
import dev.lounres.thetruehat.server.model.state
import io.ktor.server.websocket.*
import kotlinx.coroutines.launch
import kotlin.random.Random


context(WebSocketServerSession)
fun ServerSignal.send() {
    logger.info { "Outgoing signal: $this" }
    launch { sendSerialized<ServerSignal>(this@ServerSignal) }
}

// TODO: Replace with actual configuration
object Config {
    val minKeyLength = 6
    val maxKeyLength = 11
    val keyConsonant: List<Char> = listOf('Б', 'В', 'Г', 'Д', 'Ж', 'З', 'К', 'Л', 'М', 'Н', 'П', 'Р', 'С', 'Т', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ')
    val keyVowels: List<Char> = listOf('А', 'Е', 'И', 'О', 'У', 'Э', 'Ю', 'Я')
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
        val playerConnection = player.connection ?: continue
        with(playerConnection.socketSession) {
            ServerSignal.StatusUpdate(userGameState = player.state).send()
        }
    }
}

fun Room.Playing.sendStatusUpdate() {
    for (player in this.players) {
        val playerConnection = player.connection ?: continue
        with(playerConnection.socketSession) {
            ServerSignal.StatusUpdate(userGameState = player.state).send()
        }
    }
}