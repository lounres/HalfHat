package dev.lounres.kone.automata

import dev.lounres.kone.maybe.Maybe
import dev.lounres.kone.maybe.orElse
import dev.lounres.kone.maybe.useIfSome
import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic


public class ConcurrentAutomaton<State, Transition>(
    initialState: State,
    @PublishedApi
    internal val checkTransition: (State, Transition) -> Maybe<State>,
    @PublishedApi
    internal val onTransition: (previousState: State, transition: Transition, nextState: State) -> Unit,
) {
    public val state: State get() = _state
    @PublishedApi
    internal var _state: State = initialState
    @PublishedApi
    internal val isItLocked: AtomicBoolean = atomic(false)
}

public inline fun <State, Transition> ConcurrentAutomaton<State, Transition>.applyMaybe(transition: (State) -> Maybe<Transition>): Boolean {
    if (isItLocked.getAndSet(true)) return false
    val previousState = _state
    val transition = transition(previousState).orElse { return false }
    val nextState = checkTransition(previousState, transition).orElse { return false }
    onTransition(previousState, transition, nextState)
    _state = nextState
    isItLocked.value = false
    return true
}

public inline fun <State, Transition> ConcurrentAutomaton<State, Transition>.apply(transition: (State) -> Transition): Boolean {
    if (isItLocked.getAndSet(true)) return false
    val previousState = _state
    val transition = transition(previousState)
    val nextState = checkTransition(previousState, transition).orElse { return false }
    onTransition(previousState, transition, nextState)
    _state = nextState
    isItLocked.value = false
    return true
}

public fun <State, Transition> ConcurrentAutomaton<State, Transition>.applyMaybe(transition: Maybe<Transition>): Boolean = applyMaybe { transition }

public fun <State, Transition> ConcurrentAutomaton<State, Transition>.apply(transition: Transition): Boolean = apply { transition }