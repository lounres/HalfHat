package dev.lounres.halfhat.client.components

import dev.lounres.halfhat.client.components.lifecycle.coroutineScope
import dev.lounres.halfhat.client.components.lifecycle.lifecycle
import dev.lounres.kone.registry.Registry
import dev.lounres.kone.registry.RegistryBuilder
import kotlinx.coroutines.CoroutineScope
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmInline


@JvmInline
public value class LogicComponentContext(public val elements: Registry): Registry by elements

public inline fun LogicComponentContext(builder: RegistryBuilder.() -> Unit): LogicComponentContext {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }
    return LogicComponentContext(Registry(builder))
}

public fun LogicComponentContext.coroutineScope(context: CoroutineContext): CoroutineScope = lifecycle.coroutineScope(context)