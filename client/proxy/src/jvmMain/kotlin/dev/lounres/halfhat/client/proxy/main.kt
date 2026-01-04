package dev.lounres.halfhat.client.proxy

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.get
import io.ktor.server.routing.routing


fun main(args: Array<String>) {
    check(args.size == 3) { "There are ${args.size} arguments instead of 3 for client reverse proxy." }
    
    val proxyPrefix = args[0]
    val proxyPort = args[1].toInt()
    val webpackPort = args[2].toInt()
    
    val client = HttpClient(CIO)

    embeddedServer(Netty, port = proxyPort) {
        routing {
            get("${proxyPrefix}{path...}") {
                val path = call.parameters.getAll("path")!!

                val webpackResponse = client.get("http://localhost:${webpackPort}/${path.joinToString(separator = "/")}")

                if (webpackResponse.status.isSuccess()) {
                    call.respondBytes(
                        webpackResponse.bodyAsBytes(),
                        contentType = webpackResponse.contentType(),
                        status = webpackResponse.status,
                    )
                } else {
                    val indexWebpackResponse = client.get("http://localhost:${webpackPort}/")
                    call.respondBytes(
                        indexWebpackResponse.bodyAsBytes(),
                        contentType = indexWebpackResponse.contentType(),
                        status = indexWebpackResponse.status,
                    )
                }
            }
        }
    }.start(wait = true)
}