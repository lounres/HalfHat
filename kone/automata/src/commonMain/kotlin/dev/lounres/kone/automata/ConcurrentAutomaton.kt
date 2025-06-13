package dev.lounres.kone.automata

import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic


//public class ConcurrentAutomaton<State, Transition>(
//    initialState: State,
//    @PublishedApi
//    internal val checkTransition: ConcurrentAutomaton<State, Transition>.(State, Transition) -> Maybe<State>,
//    @PublishedApi
//    internal val onTransition: ConcurrentAutomaton<State, Transition>.(previousState: State, transition: Transition, nextState: State) -> Unit = { _, _, _ -> },
//) {
//    public val state: State get() = _state
//    @PublishedApi
//    internal var _state: State = initialState
//    @PublishedApi
//    internal val isItLocked: AtomicBoolean = atomic(false)
//}
//
//public inline fun <State, Transition> ConcurrentAutomaton<State, Transition>.moveMaybe(transition: (State) -> Maybe<Transition>): ConcurrentMovementMaybeResult<State, Transition> {
//    if (isItLocked.getAndSet(true)) return ConcurrentMovementMaybeResult.Blocked
//    val previousState = _state
//    val transition = transition(previousState).orElse { return ConcurrentMovementMaybeResult.NoTransition(previousState) }
//    val nextState = checkTransition(previousState, transition).orElse { return ConcurrentMovementMaybeResult.NoNextState(previousState, transition) }
//    onTransition(previousState, transition, nextState)
//    _state = nextState
//    isItLocked.value = false
//    return ConcurrentMovementMaybeResult.Success(previousState, transition, nextState)
//}
//
//public inline fun <State, Transition> ConcurrentAutomaton<State, Transition>.move(transition: (State) -> Transition): ConcurrentMovementResult<State, Transition> {
//    if (isItLocked.getAndSet(true)) return ConcurrentMovementResult.Blocked
//    val previousState = _state
//    val transition = transition(previousState)
//    val nextState = checkTransition(previousState, transition).orElse { return ConcurrentMovementResult.NoNextState(previousState, transition) }
//    onTransition(previousState, transition, nextState)
//    _state = nextState
//    isItLocked.value = false
//    return ConcurrentMovementResult.Success(previousState, transition, nextState)
//}
//
//public fun <State, Transition> ConcurrentAutomaton<State, Transition>.moveMaybe(transition: Maybe<Transition>): ConcurrentMovementMaybeResult<State, Transition> = moveMaybe { transition }
//
//public fun <State, Transition> ConcurrentAutomaton<State, Transition>.move(transition: Transition): ConcurrentMovementResult<State, Transition> = move { transition }