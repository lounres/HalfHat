package dev.lounres.halfhat.client

import org.jetbrains.skiko.wasm.onWasmReady


fun main() {
    onWasmReady {
        application()
    }
}