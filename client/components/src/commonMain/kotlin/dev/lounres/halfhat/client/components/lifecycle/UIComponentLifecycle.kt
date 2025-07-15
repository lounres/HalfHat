package dev.lounres.halfhat.client.components.lifecycle

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.komponentual.lifecycle.DeferredLifecycle
import dev.lounres.komponentual.lifecycle.DelicateLifecycleAPI
import dev.lounres.komponentual.lifecycle.Lifecycle
import dev.lounres.komponentual.lifecycle.MutableLifecycle
import dev.lounres.komponentual.lifecycle.buildSubscription
import dev.lounres.komponentual.lifecycle.childDeferring
import dev.lounres.komponentual.lifecycle.mergeDeferring
import dev.lounres.komponentual.lifecycle.subscribe
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.registry.RegistryKey
import dev.lounres.kone.registry.getOrElse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
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

public val UIComponentLifecycle.isRun: Boolean get() = state != UIComponentLifecycleState.Initialized
public val UIComponentLifecycle.isDestroyed: Boolean get() = state == UIComponentLifecycleState.Destroyed

public typealias MutableUIComponentLifecycle = MutableLifecycle<UIComponentLifecycleState, UIComponentLifecycleTransition>

@DelicateLifecycleAPI
internal typealias DeferredUIComponentLifecycle = DeferredLifecycle<UIComponentLifecycleState, UIComponentLifecycleTransition>

internal expect fun checkNextState(previousState: UIComponentLifecycleState, nextState: UIComponentLifecycleState): Boolean

internal expect fun decomposeTransition(previousState: UIComponentLifecycleState, nextState: UIComponentLifecycleState): KoneList<UIComponentLifecycleTransition>

// TODO: Rename
public fun newMutableUIComponentLifecycle(): MutableUIComponentLifecycle =
    MutableLifecycle(
        initialState = UIComponentLifecycleState.Initialized,
        checkNextState = ::checkNextState,
        decomposeTransition = ::decomposeTransition,
    )

@DelicateLifecycleAPI
internal fun UIComponentLifecycle.childDeferring(): DeferredUIComponentLifecycle =
    childDeferring(
        initialState = UIComponentLifecycleState.Initialized,
        mapState = { it },
        mapTransition = { _, transition -> transition.target },
        checkNextState = ::checkNextState,
        decomposeTransition = ::decomposeTransition,
        outputState = { it },
    )

@DelicateLifecycleAPI
internal fun Lifecycle.Companion.mergeUIComponentLifecyclesDeferring(
    lifecycle1: UIComponentLifecycle,
    lifecycle2: UIComponentLifecycle,
): DeferredUIComponentLifecycle =
    mergeDeferring(
        lifecycle1 = lifecycle1,
        lifecycle2 = lifecycle2,
        initialState = Pair(UIComponentLifecycleState.Initialized, UIComponentLifecycleState.Initialized),
        mergeStates = { state1, state2 -> Pair(state1, state2) },
        mapTransition1 = { state, transition1 -> Pair(transition1.target, state.second) },
        mapTransition2 = { state, transition2 -> Pair(state.first, transition2.target) },
        checkNextState = { previousState, nextState -> checkNextState(minOf(previousState.first, previousState.second), minOf(nextState.first, nextState.second)) },
        decomposeTransition = { previousState, nextState -> decomposeTransition(minOf(previousState.first, previousState.second), minOf(nextState.first, nextState.second)) },
        outputState = { minOf(it.first, it.second) }
    )

public fun CoroutineScope.attachTo(lifecycle: UIComponentLifecycle) {
    lifecycle.buildSubscription {
        if (it == UIComponentLifecycleState.Destroyed) cancel()
        else subscribe { transition -> if (transition == UIComponentLifecycleTransition.Destroy) cancel() }
    }
}

public fun UIComponentLifecycle.coroutineScope(context: CoroutineContext): CoroutineScope =
    CoroutineScope(context).apply { attachTo(this@coroutineScope) }

public data object UIComponentLifecycleKey : RegistryKey<UIComponentLifecycle>

public val UIComponentContext.lifecycle: UIComponentLifecycle
    get() = this.getOrElse(UIComponentLifecycleKey) { error("No UI component lifecycle registered") }

@DelicateLifecycleAPI
@PublishedApi
internal suspend fun UIComponentContext.launch() {
    val lifecycle = lifecycle
    if (lifecycle is DeferredUIComponentLifecycle) lifecycle.launch()
    else error("Cannot launch non-deferred lifecycle")
}