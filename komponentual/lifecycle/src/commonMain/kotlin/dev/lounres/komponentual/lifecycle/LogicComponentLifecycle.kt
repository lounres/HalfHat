package dev.lounres.komponentual.lifecycle

import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.of
import dev.lounres.kone.registry.RegistryKey
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
public typealias DeferredLogicComponentLifecycle = DeferredLifecycle<LogicComponentLifecycleState, LogicComponentLifecycleTransition>

public data object LogicComponentLifecycleKey : RegistryKey<LogicComponentLifecycle>

public inline fun LogicComponentLifecycle.subscribe(
    crossinline onRun: suspend () -> Unit = {},
    crossinline onStop: suspend () -> Unit = {},
    crossinline onDestroy: suspend () -> Unit = {},
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
                LogicComponentLifecycleState.Initialized -> false
                LogicComponentLifecycleState.Running -> true
                LogicComponentLifecycleState.Destroyed -> true
            }
        
        LogicComponentLifecycleState.Running ->
            when (nextState) {
                LogicComponentLifecycleState.Initialized -> true
                LogicComponentLifecycleState.Running -> false
                LogicComponentLifecycleState.Destroyed -> true
            }
        
        LogicComponentLifecycleState.Destroyed -> false
    }

internal fun decomposeTransition(previousState: LogicComponentLifecycleState, nextState: LogicComponentLifecycleState): KoneList<LogicComponentLifecycleTransition> =
    when (previousState) {
        LogicComponentLifecycleState.Initialized ->
            when (nextState) {
                LogicComponentLifecycleState.Initialized -> error("Unexpected logic component lifecycle transition")
                LogicComponentLifecycleState.Running -> KoneList.of(LogicComponentLifecycleTransition.Run)
                LogicComponentLifecycleState.Destroyed -> KoneList.of(LogicComponentLifecycleTransition.Destroy)
            }
        
        LogicComponentLifecycleState.Running ->
            when (nextState) {
                LogicComponentLifecycleState.Initialized -> KoneList.of(LogicComponentLifecycleTransition.Stop)
                LogicComponentLifecycleState.Running -> error("Unexpected logic component lifecycle transition")
                LogicComponentLifecycleState.Destroyed -> KoneList.of(LogicComponentLifecycleTransition.Destroy)
            }
        
        LogicComponentLifecycleState.Destroyed -> error("Unexpected logic component lifecycle transition")
    }

public fun MutableLogicComponentLifecycle(coroutineScope: CoroutineScope): MutableLogicComponentLifecycle =
    MutableLifecycle(coroutineScope, LogicComponentLifecycleState.Initialized, ::checkNextState, ::decomposeTransition)

@DelicateLifecycleAPI
public fun LogicComponentLifecycle.childDeferring(coroutineScope: CoroutineScope): DeferredLogicComponentLifecycle =
    childDeferring(
        coroutineScope = coroutineScope,
        initialState = LogicComponentLifecycleState.Initialized,
        mapState = { it },
        mapTransition = { _, transition -> transition.target },
        checkNextState = ::checkNextState,
        decomposeTransition = ::decomposeTransition,
        outputState = { it },
    )

@DelicateLifecycleAPI
public fun Lifecycle.Companion.mergeLogicComponentLifecyclesDeferring(
    lifecycle1: LogicComponentLifecycle,
    lifecycle2: LogicComponentLifecycle,
    coroutineScope: CoroutineScope,
): DeferredLogicComponentLifecycle =
    mergeDeferring(
        lifecycle1 = lifecycle1,
        lifecycle2 = lifecycle2,
        coroutineScope = coroutineScope,
        initialState = Pair(LogicComponentLifecycleState.Initialized, LogicComponentLifecycleState.Initialized),
        mergeStates = { state1, state2 -> Pair(state1, state2) },
        mapTransition1 = { state, transition1 -> Pair(transition1.target, state.second) },
        mapTransition2 = { state, transition2 -> Pair(state.first, transition2.target) },
        checkNextState = { previousState, nextState -> checkNextState(minOf(previousState.first, previousState.second), minOf(nextState.first, nextState.second)) },
        decomposeTransition = { previousState, nextState -> decomposeTransition(minOf(previousState.first, previousState.second), minOf(nextState.first, nextState.second)) },
        outputState = { minOf(it.first, it.second) }
    )

public fun CoroutineScope.attachTo(lifecycle: LogicComponentLifecycle) {
    lifecycle.subscribe(onStop = { this.cancel() }, onDestroy = { this.cancel() })
}

public fun LogicComponentLifecycle.coroutineScope(context: CoroutineContext): CoroutineScope =
    CoroutineScope(context).apply { attachTo(this@coroutineScope) }