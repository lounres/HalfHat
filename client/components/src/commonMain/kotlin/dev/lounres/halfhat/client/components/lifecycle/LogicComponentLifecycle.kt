package dev.lounres.halfhat.client.components.lifecycle

import dev.lounres.halfhat.client.components.LogicComponentContext
import dev.lounres.komponentual.lifecycle.DeferredLifecycle
import dev.lounres.komponentual.lifecycle.DelicateLifecycleAPI
import dev.lounres.komponentual.lifecycle.Lifecycle
import dev.lounres.komponentual.lifecycle.MutableLifecycle
import dev.lounres.komponentual.lifecycle.childDeferring
import dev.lounres.komponentual.lifecycle.mergeDeferring
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.of
import dev.lounres.kone.registry.RegistryKey
import dev.lounres.kone.registry.getOrElse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext


public enum class LogicComponentLifecycleState {
    Destroyed, Initialized, Running
}

public enum class LogicComponentLifecycleTransition(public val target: LogicComponentLifecycleState) {
    Run(target = LogicComponentLifecycleState.Running),
    Stop(target = LogicComponentLifecycleState.Initialized),
    Destroy(target = LogicComponentLifecycleState.Destroyed)
}

public typealias LogicComponentLifecycleCallback = suspend (LogicComponentLifecycleTransition) -> Unit

public typealias LogicComponentLifecycleSubscription = Lifecycle.Subscription<LogicComponentLifecycleState>

public typealias LogicComponentLifecycle = Lifecycle<LogicComponentLifecycleState, LogicComponentLifecycleTransition>

public typealias MutableLogicComponentLifecycle = MutableLifecycle<LogicComponentLifecycleState, LogicComponentLifecycleTransition>

@DelicateLifecycleAPI
internal typealias DeferredLogicComponentLifecycle = DeferredLifecycle<LogicComponentLifecycleState, LogicComponentLifecycleTransition>

// FIXME
public /*inline*/ fun LogicComponentLifecycle.subscribe(
    /*crossinline*/ onRun: suspend () -> Unit = {},
    /*crossinline*/ onStop: suspend () -> Unit = {},
    /*crossinline*/ onDestroy: suspend () -> Unit = {},
): LogicComponentLifecycleSubscription = subscribe {
    when (it) {
        LogicComponentLifecycleTransition.Run -> onRun()
        LogicComponentLifecycleTransition.Stop -> onStop()
        LogicComponentLifecycleTransition.Destroy -> onDestroy()
    }
}

internal fun checkNextState(previousState: LogicComponentLifecycleState, nextState: LogicComponentLifecycleState): Boolean =
    when (previousState) {
        LogicComponentLifecycleState.Initialized ->
            when (nextState) {
                LogicComponentLifecycleState.Initialized -> true
                LogicComponentLifecycleState.Running -> true
                LogicComponentLifecycleState.Destroyed -> true
            }
        
        LogicComponentLifecycleState.Running ->
            when (nextState) {
                LogicComponentLifecycleState.Initialized -> true
                LogicComponentLifecycleState.Running -> true
                LogicComponentLifecycleState.Destroyed -> true
            }
        
        LogicComponentLifecycleState.Destroyed ->
            when (nextState) {
                LogicComponentLifecycleState.Initialized -> false
                LogicComponentLifecycleState.Running -> false
                LogicComponentLifecycleState.Destroyed -> true
            }
    }

internal fun decomposeTransition(previousState: LogicComponentLifecycleState, nextState: LogicComponentLifecycleState): KoneList<LogicComponentLifecycleTransition> =
    when (previousState) {
        LogicComponentLifecycleState.Initialized ->
            when (nextState) {
                LogicComponentLifecycleState.Initialized -> KoneList.of()
                LogicComponentLifecycleState.Running -> KoneList.of(LogicComponentLifecycleTransition.Run)
                LogicComponentLifecycleState.Destroyed -> KoneList.of(LogicComponentLifecycleTransition.Destroy)
            }
        
        LogicComponentLifecycleState.Running ->
            when (nextState) {
                LogicComponentLifecycleState.Initialized -> KoneList.of(LogicComponentLifecycleTransition.Stop)
                LogicComponentLifecycleState.Running -> KoneList.of()
                LogicComponentLifecycleState.Destroyed -> KoneList.of(LogicComponentLifecycleTransition.Destroy)
            }
        
        LogicComponentLifecycleState.Destroyed ->
            when (nextState) {
                LogicComponentLifecycleState.Initialized -> error("Unexpected logic component lifecycle transition")
                LogicComponentLifecycleState.Running -> error("Unexpected logic component lifecycle transition")
                LogicComponentLifecycleState.Destroyed -> KoneList.of()
            }
    }

public fun MutableLogicComponentLifecycle(): MutableLogicComponentLifecycle =
    MutableLifecycle(LogicComponentLifecycleState.Initialized, ::checkNextState, ::decomposeTransition)

@DelicateLifecycleAPI
internal fun LogicComponentLifecycle.childDeferring(): DeferredLogicComponentLifecycle =
    childDeferring(
        initialState = LogicComponentLifecycleState.Initialized,
        mapState = { it },
        mapTransition = { _, transition -> transition.target },
        checkNextState = ::checkNextState,
        decomposeTransition = ::decomposeTransition,
        outputState = { it },
    )

@DelicateLifecycleAPI
internal fun Lifecycle.Companion.mergeLogicComponentLifecyclesDeferring(
    lifecycle1: LogicComponentLifecycle,
    lifecycle2: LogicComponentLifecycle,
): DeferredLogicComponentLifecycle =
    mergeDeferring(
        lifecycle1 = lifecycle1,
        lifecycle2 = lifecycle2,
        initialState = Pair(LogicComponentLifecycleState.Initialized, LogicComponentLifecycleState.Initialized),
        mergeStates = { state1, state2 -> Pair(state1, state2) },
        mapTransition1 = { state, transition1 -> Pair(transition1.target, state.second) },
        mapTransition2 = { state, transition2 -> Pair(state.first, transition2.target) },
        checkNextState = { previousState, nextState -> checkNextState(minOf(previousState.first, previousState.second), minOf(nextState.first, nextState.second)) },
        decomposeTransition = { previousState, nextState -> decomposeTransition(minOf(previousState.first, previousState.second), minOf(nextState.first, nextState.second)) },
        outputState = { minOf(it.first, it.second) }
    )

public fun CoroutineScope.attachTo(lifecycle: LogicComponentLifecycle) {
    val subscription = lifecycle.subscribe(onStop = { this.cancel() }, onDestroy = { this.cancel() })
    if (subscription.initialState == LogicComponentLifecycleState.Destroyed) cancel()
}

public fun LogicComponentLifecycle.coroutineScope(context: CoroutineContext): CoroutineScope =
    CoroutineScope(context).apply { attachTo(this@coroutineScope) }

public data object LogicComponentLifecycleKey : RegistryKey<LogicComponentLifecycle>

public val LogicComponentContext.lifecycle: LogicComponentLifecycle
    get() = this.getOrElse(LogicComponentLifecycleKey) { error("No logic component lifecycle registered") }

@DelicateLifecycleAPI
@PublishedApi
internal suspend fun LogicComponentContext.launch() {
    val lifecycle = lifecycle
    if (lifecycle is DeferredLogicComponentLifecycle) lifecycle.launch()
    else error("Cannot launch non-deferred lifecycle")
}