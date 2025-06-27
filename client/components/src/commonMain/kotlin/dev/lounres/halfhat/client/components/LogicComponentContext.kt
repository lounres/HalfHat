package dev.lounres.halfhat.client.components

import dev.lounres.komponentual.lifecycle.DeferredLogicComponentLifecycle
import dev.lounres.komponentual.lifecycle.DelicateLifecycleAPI
import dev.lounres.komponentual.lifecycle.LogicComponentLifecycle
import dev.lounres.komponentual.lifecycle.LogicComponentLifecycleKey
import dev.lounres.komponentual.lifecycle.coroutineScope
import dev.lounres.komponentual.lifecycle.subscribe
import dev.lounres.kone.registry.Registry
import dev.lounres.kone.registry.RegistryBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
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

public val LogicComponentContext.lifecycle: LogicComponentLifecycle get() = this[LogicComponentLifecycleKey]

@DelicateLifecycleAPI
internal suspend fun LogicComponentContext.launch() {
    (lifecycle as DeferredLogicComponentLifecycle).launch()
}

public fun LogicComponentContext.coroutineScope(context: CoroutineContext): CoroutineScope = lifecycle.coroutineScope(context)