package dev.lounres.kone.automata



//public class SuspendAutomaton<State, Transition>(
//    initialState: State,
//    @PublishedApi
//    internal val checkTransition: suspend SuspendAutomaton<State, Transition>.(State, Transition) -> Maybe<State>,
//    @PublishedApi
//    internal val onTransition: suspend SuspendAutomaton<State, Transition>.(previousState: State, transition: Transition, nextState: State) -> Unit = { _, _, _ -> },
//) {
//    public val state: State get() = _state
//    @PublishedApi
//    internal var _state: State = initialState
//}
//
//public suspend inline fun <State, Transition> SuspendAutomaton<State, Transition>.moveMaybe(transition: suspend /* TODO: Add receiver */ (State) -> Maybe<Transition>): MovementMaybeResult<State, Transition> {
//    val previousState = _state
//    val transition = transition(previousState).orElse { return MovementMaybeResult.NoTransition(previousState) }
//    val nextState = checkTransition(previousState, transition).orElse { return MovementMaybeResult.NoNextState(previousState, transition) }
//    onTransition(previousState, transition, nextState)
//    _state = nextState
//    return MovementMaybeResult.Success(previousState, transition, nextState)
//}
//
//public suspend inline fun <State, Transition> SuspendAutomaton<State, Transition>.move(transition: suspend (State) -> Transition): MovementResult<State, Transition> {
//    val previousState = _state
//    val transition = transition(previousState)
//    val nextState = checkTransition(previousState, transition).orElse { return MovementResult.NoNextState(previousState, transition) }
//    onTransition(previousState, transition, nextState)
//    _state = nextState
//    return MovementResult.Success(previousState, transition, nextState)
//}
//
//public suspend fun <State, Transition> SuspendAutomaton<State, Transition>.moveMaybe(transition: Maybe<Transition>): MovementMaybeResult<State, Transition> =
//    moveMaybe { transition }
//
//public suspend fun <State, Transition> SuspendAutomaton<State, Transition>.move(transition: Transition): MovementResult<State, Transition> =
//    move { transition }