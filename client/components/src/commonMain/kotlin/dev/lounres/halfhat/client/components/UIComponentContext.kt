package dev.lounres.halfhat.client.components

import dev.lounres.komponentual.lifecycle.DelicateLifecycleAPI
import dev.lounres.komponentual.lifecycle.Lifecycle
import dev.lounres.halfhat.client.components.lifecycle.LogicComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.LogicComponentLifecycleKey
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleKey
import dev.lounres.halfhat.client.components.lifecycle.coroutineScope
import dev.lounres.halfhat.client.components.lifecycle.childDeferring
import dev.lounres.halfhat.client.components.lifecycle.launch
import dev.lounres.halfhat.client.components.lifecycle.lifecycle
import dev.lounres.halfhat.client.components.lifecycle.logicChildDeferringOnRunning
import dev.lounres.halfhat.client.components.lifecycle.mergeLogicAndUILifecyclesDeferringOnRunning
import dev.lounres.halfhat.client.components.lifecycle.mergeUIComponentLifecyclesDeferring
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

public fun UIComponentContext.coroutineScope(context: CoroutineContext): CoroutineScope = lifecycle.coroutineScope(context)

@DelicateLifecycleAPI
@PublishedApi
internal fun UIComponentContext.uiChildDeferring(
    controllingLifecycle: UIComponentLifecycle? = null,
): UIComponentContext =
    UIComponentContext {
        val base = this@uiChildDeferring
        
        setFrom(base)
        
        val childLifecycle =
            if (controllingLifecycle != null)
                Lifecycle.mergeUIComponentLifecyclesDeferring(base.lifecycle, controllingLifecycle, CoroutineScope(Dispatchers.Default))
            else
                base.lifecycle.childDeferring(CoroutineScope(Dispatchers.Default))
        UIComponentLifecycleKey correspondsTo childLifecycle
    }

@OptIn(DelicateLifecycleAPI::class)
public suspend inline fun <Result> UIComponentContext.buildUiChild(
    controllingLifecycle: UIComponentLifecycle? = null,
    provider: (UIComponentContext) -> Result,
): Result {
    val childContext = uiChildDeferring(controllingLifecycle)
    val result = provider(childContext)
    childContext.launch()
    return result
}

@DelicateLifecycleAPI
@PublishedApi
internal fun UIComponentContext.logicChildDeferringOnRunning(
    controllingLifecycle: LogicComponentLifecycle? = null,
): LogicComponentContext =
    LogicComponentContext {
        val base = this@logicChildDeferringOnRunning
        
        setFrom(base)
        
        val childLifecycle =
            if (controllingLifecycle != null)
                Lifecycle.mergeLogicAndUILifecyclesDeferringOnRunning(controllingLifecycle, base.lifecycle, CoroutineScope(Dispatchers.Default))
            else
                base.lifecycle.logicChildDeferringOnRunning(CoroutineScope(Dispatchers.Default))
        LogicComponentLifecycleKey correspondsTo childLifecycle
    }

@OptIn(DelicateLifecycleAPI::class)
public suspend inline fun <Result> UIComponentContext.buildLogicChildOnRunning(
    controllingLifecycle: LogicComponentLifecycle? = null,
    provider: (LogicComponentContext) -> Result,
): Result {
    val childContext = logicChildDeferringOnRunning(controllingLifecycle)
    val result = provider(childContext)
    childContext.launch()
    return result
}