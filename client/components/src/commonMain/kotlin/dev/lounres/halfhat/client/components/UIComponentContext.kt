package dev.lounres.halfhat.client.components

import dev.lounres.komponentual.lifecycle.DeferredUIComponentLifecycle
import dev.lounres.komponentual.lifecycle.DelicateLifecycleAPI
import dev.lounres.komponentual.lifecycle.Lifecycle
import dev.lounres.komponentual.lifecycle.LogicComponentLifecycle
import dev.lounres.komponentual.lifecycle.LogicComponentLifecycleKey
import dev.lounres.komponentual.lifecycle.UIComponentLifecycle
import dev.lounres.komponentual.lifecycle.UIComponentLifecycleKey
import dev.lounres.komponentual.lifecycle.coroutineScope
import dev.lounres.komponentual.lifecycle.childDeferring
import dev.lounres.komponentual.lifecycle.logicChildDeferringOnRunning
import dev.lounres.komponentual.lifecycle.mergeLogicAndUILifecyclesDeferringOnRunning
import dev.lounres.komponentual.lifecycle.mergeUIComponentLifecyclesDeferring
import dev.lounres.kone.registry.Registry
import dev.lounres.kone.registry.RegistryBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

@DelicateLifecycleAPI
internal suspend fun UIComponentContext.launch() {
    (lifecycle as DeferredUIComponentLifecycle).launch()
}

public fun UIComponentContext.coroutineScope(context: CoroutineContext): CoroutineScope = lifecycle.coroutineScope(context)

@OptIn(DelicateLifecycleAPI::class)
public fun UIComponentContext.uiChild(
    controllingLifecycle: UIComponentLifecycle? = null,
): UIComponentContext =
    UIComponentContext {
        val base = this@uiChild
        
        setFrom(base)
        
        val childLifecycle =
            if (controllingLifecycle != null)
                Lifecycle.mergeUIComponentLifecyclesDeferring(base.lifecycle, controllingLifecycle, CoroutineScope(Dispatchers.Default))
            else
                base.lifecycle.childDeferring(CoroutineScope(Dispatchers.Default))
        UIComponentLifecycleKey correspondsTo childLifecycle
    }

@OptIn(DelicateLifecycleAPI::class)
public fun UIComponentContext.logicChildOnRunning(
    controllingLifecycle: LogicComponentLifecycle? = null,
): LogicComponentContext =
    LogicComponentContext {
        val base = this@logicChildOnRunning
        
        setFrom(base)
        
        val childLifecycle =
            if (controllingLifecycle != null)
                Lifecycle.mergeLogicAndUILifecyclesDeferringOnRunning(controllingLifecycle, base.lifecycle, CoroutineScope(Dispatchers.Default))
            else
                base.lifecycle.logicChildDeferringOnRunning(CoroutineScope(Dispatchers.Default))
        LogicComponentLifecycleKey correspondsTo childLifecycle
    }