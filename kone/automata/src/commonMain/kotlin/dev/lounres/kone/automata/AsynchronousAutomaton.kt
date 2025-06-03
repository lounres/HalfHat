package dev.lounres.kone.automata

import dev.lounres.kone.maybe.Maybe
import dev.lounres.kone.maybe.None
import dev.lounres.kone.maybe.Some
import dev.lounres.kone.maybe.orElse
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


public class AsynchronousAutomaton<State, Transition>(
    initialState: State,
    @PublishedApi
    internal val checkTransition: suspend (State, Transition) -> Maybe<State>,
    @PublishedApi
    internal val onTransition: suspend (previousState: State, transition: Transition, nextState: State) -> Unit,
) {
    public val state: State get() = _state
    @PublishedApi
    internal var _state: State = initialState
    @PublishedApi
    internal val mutex: Mutex = Mutex()
}

public suspend inline fun <State, Transition> AsynchronousAutomaton<State, Transition>.applyMaybe(transition: suspend (State) -> Maybe<Transition>): Boolean =
    mutex.withLock {
        val previousState = _state
        val transition = transition(previousState).orElse { return false }
        val nextState = checkTransition(previousState, transition).orElse { return false }
        onTransition(previousState, transition, nextState)
        _state = nextState
        true
    }

public suspend inline fun <State, Transition> AsynchronousAutomaton<State, Transition>.apply(transition: suspend (State) -> Transition): Boolean =
    mutex.withLock {
        val previousState = _state
        val transition = transition(previousState)
        val nextState = checkTransition(previousState, transition).orElse { return false }
        onTransition(previousState, transition, nextState)
        _state = nextState
        true
    }

public suspend fun <State, Transition> AsynchronousAutomaton<State, Transition>.applyMaybe(transition: Maybe<Transition>): Boolean =
    applyMaybe { transition }

public suspend fun <State, Transition> AsynchronousAutomaton<State, Transition>.apply(transition: Transition): Boolean =
    apply { transition }