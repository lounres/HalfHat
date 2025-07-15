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
import dev.lounres.halfhat.client.components.logger.logger
import dev.lounres.komponentual.lifecycle.subscribe
import dev.lounres.kone.registry.Registry
import dev.lounres.kone.registry.RegistryBuilder
import dev.lounres.logKube.core.debug
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
                Lifecycle.mergeUIComponentLifecyclesDeferring(base.lifecycle, controllingLifecycle)
            else
                base.lifecycle.childDeferring()
        UIComponentLifecycleKey correspondsTo childLifecycle
        childLifecycle.subscribe {
            logger.debug(
                source = "dev.lounres.halfhat.client.components.uiChildDeferring",
                items = {
                    mapOf(
                        "lifecycle" to childLifecycle.toString(),
                        "transition" to it.toString(),
                    )
                }
            ) { "Lifecycle transition in progress" }
        }
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
                Lifecycle.mergeLogicAndUILifecyclesDeferringOnRunning(controllingLifecycle, base.lifecycle)
            else
                base.lifecycle.logicChildDeferringOnRunning()
        LogicComponentLifecycleKey correspondsTo childLifecycle
        childLifecycle.subscribe {
            logger.debug(
                source = "dev.lounres.halfhat.client.components.logicChildDeferringOnRunning",
                items = {
                    mapOf(
                        "lifecycle" to childLifecycle.toString(),
                        "transition" to it.toString(),
                    )
                }
            ) { "Lifecycle transition in progress" }
        }
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