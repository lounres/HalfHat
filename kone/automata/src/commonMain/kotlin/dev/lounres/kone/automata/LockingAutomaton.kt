package dev.lounres.kone.automata

import dev.lounres.kone.maybe.Maybe
import dev.lounres.kone.maybe.None
import dev.lounres.kone.maybe.Some
import dev.lounres.kone.maybe.orElse
import dev.lounres.kone.maybe.useIfSome
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized


public class LockingAutomaton<State, Transition>(
    initialState: State,
    @PublishedApi
    internal val checkTransition: (previousState: State, transition: Transition) -> Maybe<State>,
    @PublishedApi
    internal val onTransition: (previousState: State, transition: Transition, nextState: State) -> Unit,
) : SynchronizedObject() {
    public val state: State get() = _state
    @PublishedApi
    internal var _state: State = initialState
}

public inline fun <State, Transition> LockingAutomaton<State, Transition>.applyMaybe(transition: (State) -> Maybe<Transition>): Boolean =
    synchronized(this) {
        val previousState = _state
        val transition = transition(previousState).orElse { return false }
        val nextState = checkTransition(previousState, transition).orElse { return false }
        onTransition(previousState, transition, nextState)
        _state = nextState
        true
    }

public inline fun <State, Transition> LockingAutomaton<State, Transition>.apply(transition: (State) -> Transition): Boolean =
    synchronized(this) {
        val previousState = _state
        val transition = transition(previousState)
        val nextState = checkTransition(previousState, transition).orElse { return false }
        onTransition(previousState, transition, nextState)
        _state = nextState
        true
    }

public fun <State, Transition> LockingAutomaton<State, Transition>.applyMaybe(transition: Maybe<Transition>): Boolean = applyMaybe { transition }

public fun <State, Transition> LockingAutomaton<State, Transition>.apply(transition: Transition): Boolean = apply { transition }