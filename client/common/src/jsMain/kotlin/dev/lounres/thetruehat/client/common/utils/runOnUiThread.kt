package dev.lounres.thetruehat.client.common.utils


public actual fun <T> runOnUiThread(block: () -> T): T {
    // TODO: Implement it adequately
    return block()
}