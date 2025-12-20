package dev.lounres.halfhat.client.utils


public actual fun <T> runOnUiThread(block: () -> T): T = block()