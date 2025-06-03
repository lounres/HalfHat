package dev.lounres.komponentual.lifecycle

import dev.lounres.kone.automata.LockingAutomaton
import dev.lounres.kone.automata.apply
import dev.lounres.kone.collections.list.KoneMutableNoddedList
import dev.lounres.kone.collections.list.implementations.KoneTwoThreeTreeList
import dev.lounres.kone.collections.list.toKoneList
import dev.lounres.kone.collections.utils.forEach
import dev.lounres.kone.maybe.Maybe
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock


public interface Lifecycle<out State, out Transition> {
    public val state: State
    
    public fun subscribe(callback: (Transition) -> Unit): Subscription
    
    public fun interface Subscription {
        public fun cancel()
    }
}

public interface MutableLifecycle<out State, Transition> : Lifecycle<State, Transition> {
    public fun apply(transition: Transition)
}

public fun <State, Transition> MutableLifecycle(
    initialState: State,
    checkTransition: (previousState: State, transition: Transition) -> Maybe<State>
): MutableLifecycle<State, Transition> = MutableLifecycleImpl(initialState, checkTransition)

internal class MutableLifecycleImpl<State, Transition>(
    initialState: State,
    checkTransition: (previousState: State, transition: Transition) -> Maybe<State>
) : MutableLifecycle<State, Transition> {
    private val callbacksLock = ReentrantLock()
    private val callbacks: KoneMutableNoddedList<(Transition) -> Unit> = KoneTwoThreeTreeList() // TODO: Replace with concurrent queue
    private val automaton =
        LockingAutomaton<State, Transition>(
            initialState = initialState,
            checkTransition = checkTransition,
            onTransition = { _, transition, _ ->
                callbacksLock.withLock { callbacks.toKoneList() }.forEach { it(transition) }
            },
        )
    
    override val state: State get() = automaton.state
        override fun subscribe(callback: (Transition) -> Unit): Lifecycle.Subscription =
            callbacksLock.withLock {
                val node = callbacks.addNode(callback)
                Lifecycle.Subscription {
                    callbacksLock.withLock {
                        node.remove()
                    }
                }
            }
    
    override fun apply(transition: Transition) {
        automaton.apply(transition)
    }
}