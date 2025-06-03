package dev.lounres.halfhat.client.components

import dev.lounres.komponentual.lifecycle.LogicComponentLifecycle
import dev.lounres.komponentual.lifecycle.LogicComponentLifecycleKey
import dev.lounres.komponentual.lifecycle.UIComponentLifecycle
import dev.lounres.komponentual.lifecycle.UIComponentLifecycleKey
import dev.lounres.komponentual.lifecycle.coroutineScope
import dev.lounres.komponentual.lifecycle.child
import dev.lounres.komponentual.lifecycle.runningLogicChild
import dev.lounres.kone.registry.Registry
import dev.lounres.kone.registry.RegistryBuilder
import kotlinx.coroutines.CoroutineScope
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

public fun UIComponentContext.uiChild(
    controllingLifecycle: UIComponentLifecycle? = null,
): UIComponentContext =
    UIComponentContext {
        val base = this@uiChild
        
        setFrom(base)
        
        val childLifecycle = base.lifecycle.child(controllingLifecycle)
        UIComponentLifecycleKey correspondsTo childLifecycle
    }

public fun UIComponentContext.logicChildOnRunning(
    controllingLifecycle: LogicComponentLifecycle? = null,
): LogicComponentContext =
    LogicComponentContext {
        val base = this@logicChildOnRunning
        
        setFrom(base)
        
        val childLifecycle = base.lifecycle.runningLogicChild(controllingLifecycle)
        LogicComponentLifecycleKey correspondsTo childLifecycle
    }