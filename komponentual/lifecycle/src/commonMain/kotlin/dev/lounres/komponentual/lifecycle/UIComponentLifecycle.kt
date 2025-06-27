package dev.lounres.komponentual.lifecycle

import dev.lounres.komponentual.lifecycle.DelicateLifecycleAPI
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.registry.RegistryKey
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext


public expect enum class UIComponentLifecycleState {
    Destroyed, Initialized,
}

public expect enum class UIComponentLifecycleTransition {
    Destroy, Run;
    public val target: UIComponentLifecycleState
}

public typealias UIComponentLifecycleCallback = suspend (UIComponentLifecycleTransition) -> Unit

public typealias UIComponentLifecycle = Lifecycle<UIComponentLifecycleState, UIComponentLifecycleTransition>

public data object UIComponentLifecycleKey : RegistryKey<UIComponentLifecycle>

public val UIComponentLifecycle.isRun: Boolean get() = state != UIComponentLifecycleState.Initialized
public val UIComponentLifecycle.isDestroyed: Boolean get() = state == UIComponentLifecycleState.Destroyed

public typealias MutableUIComponentLifecycle = MutableLifecycle<UIComponentLifecycleState, UIComponentLifecycleTransition>

@DelicateLifecycleAPI
public typealias DeferredUIComponentLifecycle = DeferredLifecycle<UIComponentLifecycleState, UIComponentLifecycleTransition>

internal expect fun checkNextState(previousState: UIComponentLifecycleState, nextState: UIComponentLifecycleState): Boolean

internal expect fun decomposeTransition(previousState: UIComponentLifecycleState, nextState: UIComponentLifecycleState): KoneList<UIComponentLifecycleTransition>

public fun MutableUIComponentLifecycle(coroutineScope: CoroutineScope): MutableUIComponentLifecycle =
    MutableLifecycle(
        coroutineScope = coroutineScope,
        initialState = UIComponentLifecycleState.Initialized,
        checkNextState = ::checkNextState,
        decomposeTransition = ::decomposeTransition,
    )

@DelicateLifecycleAPI
public fun UIComponentLifecycle.childDeferring(coroutineScope: CoroutineScope): DeferredUIComponentLifecycle =
    childDeferring(
        coroutineScope = coroutineScope,
        initialState = UIComponentLifecycleState.Initialized,
        mapState = { it },
        mapTransition = { _, transition -> transition.target },
        checkNextState = ::checkNextState,
        decomposeTransition = ::decomposeTransition,
        outputState = { it },
    )

@DelicateLifecycleAPI
public fun Lifecycle.Companion.mergeUIComponentLifecyclesDeferring(
    lifecycle1: UIComponentLifecycle,
    lifecycle2: UIComponentLifecycle,
    coroutineScope: CoroutineScope,
): DeferredUIComponentLifecycle =
    mergeDeferring(
        lifecycle1 = lifecycle1,
        lifecycle2 = lifecycle2,
        coroutineScope = coroutineScope,
        initialState = Pair(UIComponentLifecycleState.Initialized, UIComponentLifecycleState.Initialized),
        mergeStates = { state1, state2 -> Pair(state1, state2) },
        mapTransition1 = { state, transition1 -> Pair(transition1.target, state.second) },
        mapTransition2 = { state, transition2 -> Pair(state.first, transition2.target) },
        checkNextState = { previousState, nextState -> checkNextState(minOf(previousState.first, previousState.second), minOf(nextState.first, nextState.second)) },
        decomposeTransition = { previousState, nextState -> decomposeTransition(minOf(previousState.first, previousState.second), minOf(nextState.first, nextState.second)) },
        outputState = { minOf(it.first, it.second) }
    )

public fun CoroutineScope.attachTo(lifecycle: UIComponentLifecycle) {
    lifecycle.subscribe { if (it == UIComponentLifecycleTransition.Destroy) cancel() }
}

public fun UIComponentLifecycle.coroutineScope(context: CoroutineContext): CoroutineScope =
    CoroutineScope(context).apply { attachTo(this@coroutineScope) }