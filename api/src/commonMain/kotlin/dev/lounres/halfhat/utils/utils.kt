package dev.lounres.halfhat.utils

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


public inline fun <T> scope(block: () -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return block()
}