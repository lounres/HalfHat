package dev.lounres.halfhat.client.utils

import web.clipboard.readText
import web.clipboard.writeText
import web.navigator.navigator


public actual suspend fun copyToClipboard(content: String) {
    navigator.clipboard.writeText(content)
}

public actual suspend fun copyFromClipboard(): String = navigator.clipboard.readText()