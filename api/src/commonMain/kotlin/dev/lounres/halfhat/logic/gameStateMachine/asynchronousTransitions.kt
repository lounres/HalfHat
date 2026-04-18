package dev.lounres.halfhat.logic.gameStateMachine

import dev.lounres.kone.automata.MovementAndComputationResult
import dev.lounres.kone.automata.MovementMaybeAndComputationResult
import dev.lounres.kone.automata.MovementMaybeResult
import dev.lounres.kone.automata.MovementResult
import dev.lounres.kone.automata.TransitionAndComputation
import dev.lounres.kone.automata.TransitionOrReason
import dev.lounres.kone.automata.TransitionOrReasonAndComputation
import dev.lounres.kone.automata.move
import dev.lounres.kone.automata.moveAndCompute
import dev.lounres.kone.automata.moveMaybe
import dev.lounres.kone.automata.moveMaybeAndCompute
import dev.lounres.kone.collections.list.KoneList


@IgnorableReturnValue
public suspend inline fun <P, WPID, NoWordsProviderReason, NoTransitionReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason>.moveMaybe(
    transition: suspend (GameStateMachine.State<P, WPID>) -> TransitionOrReason<GameStateMachine.Transition<P, WPID, NoWordsProviderReason>, NoTransitionReason>
): MovementMaybeResult<GameStateMachine.State<P, WPID>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason>, NoTransitionReason, GameStateMachine.NoNextStateReason<NoWordsProviderReason>> = automaton.moveMaybe(transition)

@IgnorableReturnValue
public suspend inline fun <P, WPID, NoWordsProviderReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason>.move(
    transition: suspend (GameStateMachine.State<P, WPID>) -> GameStateMachine.Transition<P, WPID, NoWordsProviderReason>
): MovementResult<GameStateMachine.State<P, WPID>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason>, GameStateMachine.NoNextStateReason<NoWordsProviderReason>> = automaton.move(transition)

@IgnorableReturnValue
public suspend fun <P, WPID, NoWordsProviderReason, NoTransitionReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason>.moveMaybe(
    transition: TransitionOrReason<GameStateMachine.Transition<P, WPID, NoWordsProviderReason>, NoTransitionReason>
): MovementMaybeResult<GameStateMachine.State<P, WPID>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason>, NoTransitionReason, GameStateMachine.NoNextStateReason<NoWordsProviderReason>> = automaton.moveMaybe(transition)

@IgnorableReturnValue
public suspend fun <P, WPID, NoWordsProviderReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason>.move(
    transition: GameStateMachine.Transition<P, WPID, NoWordsProviderReason>
): MovementResult<GameStateMachine.State<P, WPID>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason>, GameStateMachine.NoNextStateReason<NoWordsProviderReason>> = automaton.move(transition)

@IgnorableReturnValue
public suspend inline fun <P, WPID, NoWordsProviderReason, NoTransitionReason, Computation> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason>.moveMaybeAndCompute(
    transition: suspend (GameStateMachine.State<P, WPID>) -> TransitionOrReasonAndComputation<GameStateMachine.Transition<P, WPID, NoWordsProviderReason>, NoTransitionReason, Computation>
): MovementMaybeAndComputationResult<GameStateMachine.State<P, WPID>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason>, NoTransitionReason, GameStateMachine.NoNextStateReason<NoWordsProviderReason>, Computation> = automaton.moveMaybeAndCompute(transition)

@IgnorableReturnValue
public suspend inline fun <P, WPID, NoWordsProviderReason, Computation> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason>.moveAndCompute(
    transition: suspend (GameStateMachine.State<P, WPID>) -> TransitionAndComputation<GameStateMachine.Transition<P, WPID, NoWordsProviderReason>, Computation>
): MovementAndComputationResult<GameStateMachine.State<P, WPID>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason>, GameStateMachine.NoNextStateReason<NoWordsProviderReason>, Computation> = automaton.moveAndCompute(transition)

@IgnorableReturnValue
public suspend fun <P, WPID, NoWordsProviderReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason>.updateGameSettings(
    playersList: KoneList<P>,
    settingsBuilder: GameStateMachine.GameSettings.Builder<WPID>,
): MovementResult<GameStateMachine.State<P, WPID>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason>, GameStateMachine.NoNextStateReason<NoWordsProviderReason>> = automaton.move(
    GameStateMachine.Transition.UpdateGameSettings(
        playersList = playersList,
        settingsBuilder = settingsBuilder,
    )
)

@IgnorableReturnValue
public suspend fun <P, WPID, NoWordsProviderReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason>.initialiseGame(
    wordsProviderRegistry: GameStateMachine.WordsProviderRegistry<WPID, NoWordsProviderReason>,
): MovementResult<GameStateMachine.State<P, WPID>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason>, GameStateMachine.NoNextStateReason<NoWordsProviderReason>> =
    automaton.move(GameStateMachine.Transition.InitialiseGame(wordsProviderRegistry))

@IgnorableReturnValue
public suspend fun <P, WPID, NoWordsProviderReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason>.speakerReady(): MovementResult<GameStateMachine.State<P, WPID>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason>, GameStateMachine.NoNextStateReason<NoWordsProviderReason>> =
    automaton.move(GameStateMachine.Transition.SpeakerReady)

@IgnorableReturnValue
public suspend fun <P, WPID, NoWordsProviderReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason>.listenerReady(): MovementResult<GameStateMachine.State<P, WPID>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason>, GameStateMachine.NoNextStateReason<NoWordsProviderReason>> =
    automaton.move(GameStateMachine.Transition.ListenerReady)

@IgnorableReturnValue
public suspend fun <P, WPID, NoWordsProviderReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason>.speakerAndListenerReady(): MovementResult<GameStateMachine.State<P, WPID>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason>, GameStateMachine.NoNextStateReason<NoWordsProviderReason>> =
    automaton.move(GameStateMachine.Transition.SpeakerAndListenerReady)

@IgnorableReturnValue
public suspend fun <P, WPID, NoWordsProviderReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason>.wordExplanationState(
    wordState: GameStateMachine.WordExplanation.State,
): MovementResult<GameStateMachine.State<P, WPID>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason>, GameStateMachine.NoNextStateReason<NoWordsProviderReason>> = automaton.move(
    GameStateMachine.Transition.WordExplanationState(
        wordState = wordState,
    )
)

@IgnorableReturnValue
public suspend fun <P, WPID, NoWordsProviderReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason>.updateWordsExplanationResults(
    newExplanationResults: KoneList<GameStateMachine.WordExplanation>,
): MovementResult<GameStateMachine.State<P, WPID>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason>, GameStateMachine.NoNextStateReason<NoWordsProviderReason>> = automaton.move(
    GameStateMachine.Transition.UpdateWordsExplanationResults(
        newExplanationResults = newExplanationResults,
    )
)

@IgnorableReturnValue
public suspend fun <P, WPID, NoWordsProviderReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason>.confirmWordsExplanationResults(): MovementResult<GameStateMachine.State<P, WPID>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason>, GameStateMachine.NoNextStateReason<NoWordsProviderReason>> =
    automaton.move(GameStateMachine.Transition.ConfirmWordsExplanationResults)

@IgnorableReturnValue
public suspend fun <P, WPID, NoWordsProviderReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason>.finishGame(): MovementResult<GameStateMachine.State<P, WPID>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason>, GameStateMachine.NoNextStateReason<NoWordsProviderReason>> =
    automaton.move(GameStateMachine.Transition.FinishGame)