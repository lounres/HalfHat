package dev.lounres.komponentual.lifecycle

import dev.lounres.kone.automata.SynchronousAutomaton
import dev.lounres.kone.automata.apply
import dev.lounres.kone.collections.list.KoneMutableNoddedList
import dev.lounres.kone.collections.list.implementations.KoneTwoThreeTreeList
import dev.lounres.kone.collections.list.toKoneList
import dev.lounres.kone.collections.utils.forEach
import dev.lounres.kone.maybe.None
import dev.lounres.kone.maybe.Some
import dev.lounres.kone.maybe.computeOn
import dev.lounres.kone.registry.RegistryKey
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext


public enum class LogicComponentLifecycleState {
    Destroyed, Initialized, Running
}

public enum class LogicComponentLifecycleTransition {
    Run, Stop, Destroy
}

public typealias LogicComponentLifecycleCallback = (LogicComponentLifecycleTransition) -> Unit

public typealias LogicComponentLifecycle = Lifecycle<LogicComponentLifecycleState, LogicComponentLifecycleTransition>

public typealias MutableLogicComponentLifecycle = MutableLifecycle<LogicComponentLifecycleState, LogicComponentLifecycleTransition>

public data object LogicComponentLifecycleKey : RegistryKey<LogicComponentLifecycle>

public inline fun LogicComponentLifecycle.subscribe(
    crossinline onRun: () -> Unit = {},
    crossinline onStop: () -> Unit = {},
    crossinline onDestroy: () -> Unit = {},
) {
    subscribe {
        when (it) {
            LogicComponentLifecycleTransition.Run -> onRun()
            LogicComponentLifecycleTransition.Stop -> onStop()
            LogicComponentLifecycleTransition.Destroy -> onDestroy()
        }
    }
}

public fun MutableLogicComponentLifecycle(): MutableLogicComponentLifecycle =
    MutableLifecycle(LogicComponentLifecycleState.Initialized) { previousState, transition ->
        when (previousState) {
            LogicComponentLifecycleState.Initialized ->
                when (transition) {
                    LogicComponentLifecycleTransition.Run -> Some(LogicComponentLifecycleState.Running)
                    LogicComponentLifecycleTransition.Stop -> None
                    LogicComponentLifecycleTransition.Destroy -> Some(LogicComponentLifecycleState.Destroyed)
                }
            LogicComponentLifecycleState.Running ->
                when (transition) {
                    LogicComponentLifecycleTransition.Run -> None
                    LogicComponentLifecycleTransition.Stop -> Some(LogicComponentLifecycleState.Initialized)
                    LogicComponentLifecycleTransition.Destroy -> Some(LogicComponentLifecycleState.Destroyed)
                }
            LogicComponentLifecycleState.Destroyed -> None
        }
    }

public fun mergeLogicComponentLifecycles(
    lifecycle1: LogicComponentLifecycle,
    lifecycle2: LogicComponentLifecycle,
): LogicComponentLifecycle = MergingLogicComponentLifecycleImpl(
    lifecycle1 = lifecycle1,
    lifecycle2 = lifecycle2,
)

internal class MergingLogicComponentLifecycleImpl(
    lifecycle1: LogicComponentLifecycle,
    lifecycle2: LogicComponentLifecycle,
) : LogicComponentLifecycle {
    private val callbacksLock = ReentrantLock()
    private val callbacks: KoneMutableNoddedList<(LogicComponentLifecycleTransition) -> Unit> = KoneTwoThreeTreeList() // TODO: Replace with concurrent queue
    private val automatonLock = ReentrantLock()
    
    private sealed interface BiTransition {
        val transition: LogicComponentLifecycleTransition
        data class FirstTransition(override val transition: LogicComponentLifecycleTransition) : BiTransition
        data class SecondTransition(override val transition: LogicComponentLifecycleTransition) : BiTransition
    }
    
    private fun checkTransition(state: LogicComponentLifecycleState, transition: LogicComponentLifecycleTransition) =
        when (state) {
            LogicComponentLifecycleState.Initialized ->
                when (transition) {
                    LogicComponentLifecycleTransition.Run -> Some(LogicComponentLifecycleState.Running)
                    LogicComponentLifecycleTransition.Stop -> None
                    LogicComponentLifecycleTransition.Destroy -> Some(LogicComponentLifecycleState.Destroyed)
                }
            
            LogicComponentLifecycleState.Running ->
                when (transition) {
                    LogicComponentLifecycleTransition.Run -> None
                    LogicComponentLifecycleTransition.Stop -> Some(LogicComponentLifecycleState.Initialized)
                    LogicComponentLifecycleTransition.Destroy -> Some(LogicComponentLifecycleState.Destroyed)
                }
            
            LogicComponentLifecycleState.Destroyed -> None
        }
    
    private var automaton: SynchronousAutomaton<Pair<LogicComponentLifecycleState, LogicComponentLifecycleState>, BiTransition>? = null
    
    init {
        automatonLock.withLock {
            val subscription1 = lifecycle1.subscribe { transition ->
                automatonLock.withLock {
                    automaton!!.apply(BiTransition.FirstTransition(transition))
                }
            }
            val subscription2 = lifecycle2.subscribe { transition ->
                automatonLock.withLock {
                    automaton!!.apply(BiTransition.SecondTransition(transition))
                }
            }
            
            val state1 = lifecycle1.state
            val state2 = lifecycle2.state
            
            automaton =
                SynchronousAutomaton(
                    initialState = Pair(state1, state2),
                    checkTransition = { state, transition ->
                        when (transition) {
                            is BiTransition.FirstTransition -> checkTransition(state.first, transition.transition).computeOn { Pair(it, state.second) }
                            is BiTransition.SecondTransition -> checkTransition(state.second, transition.transition).computeOn { Pair(state.first, it) }
                        }
                    },
                    onTransition = { previousState, transition, nextState ->
                        if (minOf(previousState.first, previousState.second) != minOf(nextState.first, nextState.second))
                            callbacksLock.withLock { callbacks.toKoneList() }.forEach { it(transition.transition) }
                        if (transition.transition == LogicComponentLifecycleTransition.Destroy) {
                            subscription1.cancel()
                            subscription2.cancel()
                        }
                    },
                )
            
            if (state1 == LogicComponentLifecycleState.Destroyed || state2 == LogicComponentLifecycleState.Destroyed) {
                subscription1.cancel()
                subscription2.cancel()
            }
        }
    }
    
    override val state: LogicComponentLifecycleState get() = automaton!!.state.let { minOf(it.first, it.second) }
    override fun subscribe(callback: LogicComponentLifecycleCallback) =
        callbacksLock.withLock {
            val node = callbacks.addNode(callback)
            Lifecycle.Subscription {
                callbacksLock.withLock {
                    node.remove()
                }
            }
        }
}

public fun MutableLogicComponentLifecycle.moveTo(state: LogicComponentLifecycleState) {
    when (state) {
        LogicComponentLifecycleState.Destroyed -> apply(LogicComponentLifecycleTransition.Destroy)
        LogicComponentLifecycleState.Initialized -> apply(LogicComponentLifecycleTransition.Stop)
        LogicComponentLifecycleState.Running -> apply(LogicComponentLifecycleTransition.Run)
    }
}

public fun LogicComponentLifecycle.attach(child: MutableLogicComponentLifecycle) {
    val subscription = subscribe { child.apply(it) }
    child.subscribe(onDestroy = { subscription.cancel() })
    if (child.state == LogicComponentLifecycleState.Destroyed) subscription.cancel()
}

public fun LogicComponentLifecycle.child(controllingLifecycle: LogicComponentLifecycle? = null): LogicComponentLifecycle {
    val directChild = MutableLogicComponentLifecycle().also { child -> this.attach(child) }
    return if (controllingLifecycle == null) directChild else mergeLogicComponentLifecycles(directChild, controllingLifecycle)
}

public fun CoroutineScope.attachTo(lifecycle: LogicComponentLifecycle) {
    lifecycle.subscribe(onStop = { this.cancel() }, onDestroy = { this.cancel() })
}

public fun LogicComponentLifecycle.coroutineScope(context: CoroutineContext): CoroutineScope =
    CoroutineScope(context).apply { attachTo(this@coroutineScope) }