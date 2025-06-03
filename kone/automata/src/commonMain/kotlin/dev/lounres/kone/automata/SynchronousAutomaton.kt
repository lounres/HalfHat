package dev.lounres.kone.automata

import dev.lounres.kone.maybe.Maybe
import dev.lounres.kone.maybe.orElse
import dev.lounres.kone.maybe.useIfSome


public class SynchronousAutomaton<State, Transition>(
    initialState: State,
    @PublishedApi
    internal val checkTransition: (State, Transition) -> Maybe<State>,
    @PublishedApi
    internal val onTransition: (previousState: State, transition: Transition, nextState: State) -> Unit,
) {
    public val state: State get() = _state
    @PublishedApi
    internal var _state: State = initialState
}

public inline fun <State, Transition> SynchronousAutomaton<State, Transition>.applyMaybe(transition: (State) -> Maybe<Transition>): Boolean {
    val previousState = _state
    val transition = transition(previousState).orElse { return false }
    val nextState = checkTransition(previousState, transition).orElse { return false }
    onTransition(previousState, transition, nextState)
    _state = nextState
    return true
}

public inline fun <State, Transition> SynchronousAutomaton<State, Transition>.apply(transition: (State) -> Transition): Boolean {
    val previousState = _state
    val transition = transition(previousState)
    val nextState = checkTransition(previousState, transition).orElse { return false }
    onTransition(previousState, transition, nextState)
    _state = nextState
    return true
}

public fun <State, Transition> SynchronousAutomaton<State, Transition>.applyMaybe(transition: Maybe<Transition>): Boolean =
    applyMaybe { transition }

public fun <State, Transition> SynchronousAutomaton<State, Transition>.apply(transition: Transition): Boolean =
    apply { transition }