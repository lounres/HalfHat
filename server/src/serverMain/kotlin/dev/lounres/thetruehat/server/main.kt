package dev.lounres.thetruehat.server

import io.ktor.serialization.deserialize
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import dev.lounres.thetruehat.api.CJoinRoomSignal
import dev.lounres.thetruehat.api.ClientSignal
import dev.lounres.thetruehat.api.SFailureSignal
import dev.lounres.thetruehat.api.ServerSignal


fun main() {
    embeddedServer(Netty, port = 8080) {
        install(WebSockets) {
            pingPeriodMillis = 1000
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        routing {
            webSocket("/ws") {
//                sendSerialized<ClientSignal>(CJoinRoomSignal(key = "", username = "", timeZoneOffset = 0))
//                for (incoming)
            }
        }
    }.start(wait = true)
}