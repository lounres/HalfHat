package dev.lounres.komponentual.lifecycle

import dev.lounres.kone.automata.BlockingAutomaton
import dev.lounres.kone.automata.CheckResult
import dev.lounres.kone.automata.move
import dev.lounres.kone.collections.list.KoneMutableNoddedList
import dev.lounres.kone.collections.list.implementations.KoneGCLinkedList
import dev.lounres.kone.collections.list.toKoneList
import dev.lounres.kone.collections.utils.forEach
import dev.lounres.kone.maybe.Maybe
import dev.lounres.kone.maybe.None
import dev.lounres.kone.maybe.Some
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
    public fun move(transition: Transition)
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
    private val callbacks: KoneMutableNoddedList<(Transition) -> Unit> = KoneGCLinkedList() // TODO: Replace with concurrent queue
    private val automaton =
        BlockingAutomaton<State, Transition, Nothing?>(
            initialState = initialState,
            checkTransition = { previousState, transition ->
                when (val result = checkTransition(previousState, transition)) {
                    None -> CheckResult.Failure(null)
                    is Some<State> -> CheckResult.Success(result.value)
                }
            },
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
    
    override fun move(transition: Transition) {
        automaton.move(transition)
    }
}