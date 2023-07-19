package site.m20sch57.thetruehat.feedback

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.channel.TextChannel
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * Feedback server.
 */
fun main() {
    val environmentVariables: Map<String, String> = System.getenv()
    val discordBotToken = environmentVariables["token"]!!
    val channelId = environmentVariables["channelId"]!!
    val logFile = File(environmentVariables["logFile"]!!)
    val pidFile = File(environmentVariables["pidFile"]!!)

    pidFile.writeText(ProcessHandle.current().pid().toString())

    val theChannel = runBlocking {
        val discordBot = Kord(discordBotToken)
        discordBot.getChannelOf<TextChannel>(Snowflake(channelId))!!
    }

    embeddedServer(Netty, port = 8080) {
        routing {
            post("/feedback") {
                call.respond(HttpStatusCode.OK)
                val feedbackText = call.receiveText()
                logFile.appendText("""
                    ===
                    $feedbackText
                    ===
                    
                    
                    """.trimIndent())
                theChannel.createMessage("```$feedbackText```")
            }
        }
    }.start(wait = true)
}