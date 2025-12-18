package dev.lounres.halfhat.client.common.utils


public actual fun <T> runOnUiThread(block: () -> T): T = block()