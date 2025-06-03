package dev.lounres.komponentual.lifecycle

import dev.lounres.kone.automata.SynchronousAutomaton
import dev.lounres.kone.automata.apply
import dev.lounres.kone.collections.list.KoneMutableNoddedList
import dev.lounres.kone.collections.list.implementations.KoneTwoThreeTreeList
import dev.lounres.kone.collections.list.toKoneList
import dev.lounres.kone.collections.utils.forEach
import dev.lounres.kone.maybe.Maybe
import dev.lounres.kone.maybe.None
import dev.lounres.kone.maybe.Some
import dev.lounres.kone.maybe.computeOn
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock


public actual enum class UIComponentLifecycleState {
    Destroyed, Initialized, Running, Background, Foreground
}

public actual enum class UIComponentLifecycleTransition {
    Destroy, Run, Appear, Disappear, Focus, Defocus
}

public inline fun UIComponentLifecycle.subscribe(
    crossinline onRun: () -> Unit = {},
    crossinline onAppear: () -> Unit = {},
    crossinline onDisappear: () -> Unit = {},
    crossinline onFocus: () -> Unit = {},
    crossinline onDefocus: () -> Unit = {},
    crossinline onDestroy: () -> Unit = {},
) {
    subscribe {
        when (it) {
            UIComponentLifecycleTransition.Run -> onRun()
            UIComponentLifecycleTransition.Appear -> onAppear()
            UIComponentLifecycleTransition.Disappear -> onDisappear()
            UIComponentLifecycleTransition.Focus -> onFocus()
            UIComponentLifecycleTransition.Defocus -> onDefocus()
            UIComponentLifecycleTransition.Destroy -> onDestroy()
        }
    }
}

public actual fun MutableUIComponentLifecycle(): MutableUIComponentLifecycle =
    MutableLifecycle(UIComponentLifecycleState.Initialized) { previousState, transition ->
        when (previousState) {
            UIComponentLifecycleState.Initialized ->
                when (transition) {
                    UIComponentLifecycleTransition.Run -> Some(UIComponentLifecycleState.Running)
                    UIComponentLifecycleTransition.Destroy -> Some(UIComponentLifecycleState.Destroyed)
                    else -> None
                }
            
            UIComponentLifecycleState.Running ->
                when (transition) {
//                            UIComponentLifecycleTransition.Stop -> Some(UIComponentLifecycleState.Initialized)
                    UIComponentLifecycleTransition.Appear -> Some(UIComponentLifecycleState.Background)
                    UIComponentLifecycleTransition.Destroy -> Some(UIComponentLifecycleState.Destroyed)
                    else -> None
                }
            
            UIComponentLifecycleState.Background ->
                when (transition) {
                    UIComponentLifecycleTransition.Disappear -> Some(UIComponentLifecycleState.Running)
                    UIComponentLifecycleTransition.Focus -> Some(UIComponentLifecycleState.Foreground)
                    UIComponentLifecycleTransition.Destroy -> Some(UIComponentLifecycleState.Destroyed)
                    else -> None
                }
            
            UIComponentLifecycleState.Foreground ->
                when (transition) {
                    UIComponentLifecycleTransition.Defocus -> Some(UIComponentLifecycleState.Background)
                    UIComponentLifecycleTransition.Destroy -> Some(UIComponentLifecycleState.Destroyed)
                    else -> None
                }
            
            UIComponentLifecycleState.Destroyed -> None
        }
    }

public actual fun mergeUIComponentLifecycles(
    lifecycle1: UIComponentLifecycle,
    lifecycle2: UIComponentLifecycle,
): UIComponentLifecycle = MergingUIComponentLifecycleImpl(
    lifecycle1 = lifecycle1,
    lifecycle2 = lifecycle2,
)

internal class MergingUIComponentLifecycleImpl(
    lifecycle1: UIComponentLifecycle,
    lifecycle2: UIComponentLifecycle,
) : UIComponentLifecycle {
    private val callbacksLock = ReentrantLock()
    private val callbacks: KoneMutableNoddedList<(UIComponentLifecycleTransition) -> Unit> = KoneTwoThreeTreeList() // TODO: Replace with concurrent queue
    private val automatonLock = ReentrantLock()
    
    private sealed interface BiTransition {
        val transition: UIComponentLifecycleTransition
        data class FirstTransition(override val transition: UIComponentLifecycleTransition) : BiTransition
        data class SecondTransition(override val transition: UIComponentLifecycleTransition) : BiTransition
    }
    
    private fun checkTransition(state: UIComponentLifecycleState, transition: UIComponentLifecycleTransition): Maybe<UIComponentLifecycleState> =
        when (state) {
            UIComponentLifecycleState.Initialized ->
                when (transition) {
                    UIComponentLifecycleTransition.Run -> Some(UIComponentLifecycleState.Running)
                    UIComponentLifecycleTransition.Destroy -> Some(UIComponentLifecycleState.Destroyed)
                    else -> None
                }
            UIComponentLifecycleState.Running ->
                when (transition) {
//                    UIComponentLifecycleTransition.Stop -> Some(UIComponentLifecycleState.Initialized)
                    UIComponentLifecycleTransition.Appear -> Some(UIComponentLifecycleState.Background)
                    UIComponentLifecycleTransition.Destroy -> Some(UIComponentLifecycleState.Destroyed)
                    else -> None
                }
            UIComponentLifecycleState.Background ->
                when (transition) {
                    UIComponentLifecycleTransition.Disappear -> Some(UIComponentLifecycleState.Running)
                    UIComponentLifecycleTransition.Focus -> Some(UIComponentLifecycleState.Foreground)
                    UIComponentLifecycleTransition.Destroy -> Some(UIComponentLifecycleState.Destroyed)
                    else -> None
                }
            UIComponentLifecycleState.Foreground ->
                when (transition) {
                    UIComponentLifecycleTransition.Defocus -> Some(UIComponentLifecycleState.Background)
                    UIComponentLifecycleTransition.Destroy -> Some(UIComponentLifecycleState.Destroyed)
                    else -> None
                }
            UIComponentLifecycleState.Destroyed -> None
        }
    
    private var automaton: SynchronousAutomaton<Pair<UIComponentLifecycleState, UIComponentLifecycleState>, BiTransition>? = null
    
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
                        if (transition.transition == UIComponentLifecycleTransition.Destroy) {
                            subscription1.cancel()
                            subscription2.cancel()
                        }
                    },
                )
            
            if (state1 == UIComponentLifecycleState.Destroyed || state2 == UIComponentLifecycleState.Destroyed) {
                subscription1.cancel()
                subscription2.cancel()
            }
        }
    }
    
    override val state: UIComponentLifecycleState get() = automaton!!.state.let { minOf(it.first, it.second) }
    override fun subscribe(callback: UIComponentLifecycleCallback) =
        callbacksLock.withLock {
            val node = callbacks.addNode(callback)
            Lifecycle.Subscription {
                callbacksLock.withLock {
                    node.remove()
                }
            }
        }
}

public actual fun MutableUIComponentLifecycle.moveTo(state: UIComponentLifecycleState) {
    when (state) {
        UIComponentLifecycleState.Destroyed -> {
            apply(UIComponentLifecycleTransition.Destroy)
        }
        UIComponentLifecycleState.Initialized -> {}
        UIComponentLifecycleState.Running -> {
            apply(UIComponentLifecycleTransition.Run)
            apply(UIComponentLifecycleTransition.Defocus)
            apply(UIComponentLifecycleTransition.Disappear)
        }
        UIComponentLifecycleState.Background -> {
            apply(UIComponentLifecycleTransition.Run)
            apply(UIComponentLifecycleTransition.Appear)
            apply(UIComponentLifecycleTransition.Defocus)
        }
        UIComponentLifecycleState.Foreground -> {
            apply(UIComponentLifecycleTransition.Run)
            apply(UIComponentLifecycleTransition.Appear)
            apply(UIComponentLifecycleTransition.Focus)
        }
    }
}