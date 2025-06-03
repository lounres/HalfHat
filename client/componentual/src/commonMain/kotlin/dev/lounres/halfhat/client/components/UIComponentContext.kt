package dev.lounres.halfhat.client.components

import dev.lounres.komponentual.lifecycle.UIComponentLifecycle
import dev.lounres.komponentual.lifecycle.UIComponentLifecycleKey
import dev.lounres.komponentual.lifecycle.UIComponentLifecycleTransition
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
public value class UIComponentContext(public val elements: Registry) : Registry by elements

public inline fun UIComponentContext(builder: RegistryBuilder.() -> Unit): UIComponentContext {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }
    return UIComponentContext(Registry(builder))
}

public val UIComponentContext.lifecycle: UIComponentLifecycle get() = this[UIComponentLifecycleKey]

public fun UIComponentContext.coroutineScope(context: CoroutineContext): CoroutineScope = lifecycle.coroutineScope(context)