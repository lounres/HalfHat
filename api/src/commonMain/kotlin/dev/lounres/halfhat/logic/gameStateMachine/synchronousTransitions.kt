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
import dev.lounres.kone.maybe.Maybe
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoTransitionReason, NoMetadataTransitionReason> SynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.moveMaybe(
    transition: (GameStateMachine.State<P, WP, Metadata>) -> TransitionOrReason<GameStateMachine.Transition<P, WP, MetadataTransition>, NoTransitionReason>
): MovementMaybeResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, NoTransitionReason, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> {
    contract {
        callsInPlace(transition, InvocationKind.EXACTLY_ONCE)
    }
    return automaton.moveMaybe(transition)
}

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> SynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.move(
    transition: (GameStateMachine.State<P, WP, Metadata>) -> GameStateMachine.Transition<P, WP, MetadataTransition>
): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> {
    contract {
        callsInPlace(transition, InvocationKind.EXACTLY_ONCE)
    }
    return automaton.move(transition)
}

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoTransitionReason, NoMetadataTransitionReason> SynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.moveMaybe(
    transition: TransitionOrReason<GameStateMachine.Transition<P, WP, MetadataTransition>, NoTransitionReason>
): MovementMaybeResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, NoTransitionReason, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> = automaton.moveMaybe(transition)

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> SynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.move(
    transition: GameStateMachine.Transition<P, WP, MetadataTransition>
): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> = automaton.move(transition)

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoTransitionReason, NoMetadataTransitionReason, Computation> SynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.moveMaybeAndCompute(
    transition: (GameStateMachine.State<P, WP, Metadata>) -> TransitionOrReasonAndComputation<GameStateMachine.Transition<P, WP, MetadataTransition>, NoTransitionReason, Computation>
): MovementMaybeAndComputationResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, NoTransitionReason, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>, Computation> {
    contract {
        callsInPlace(transition, InvocationKind.EXACTLY_ONCE)
    }
    return automaton.moveMaybeAndCompute(transition)
}

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason, Computation> SynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.moveAndCompute(
    transition: (GameStateMachine.State<P, WP, Metadata>) -> TransitionAndComputation<GameStateMachine.Transition<P, WP, MetadataTransition>, Computation>
): MovementAndComputationResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>, Computation> {
    contract {
        callsInPlace(transition, InvocationKind.EXACTLY_ONCE)
    }
    return automaton.moveAndCompute(transition)
}

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> SynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.updateGameSettings(
    playersList: KoneList<P>,
    settingsBuilder: GameStateMachine.GameSettings.Builder<WP>
): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> = automaton.move(
    GameStateMachine.Transition.UpdateGame.UpdateGameSettings(
        playersList = playersList,
        settingsBuilder = settingsBuilder,
    )
)

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> SynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.initialiseGame(): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> =
    automaton.move(GameStateMachine.Transition.UpdateGame.InitialiseGame)

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> SynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.speakerReady(): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> =
    automaton.move(GameStateMachine.Transition.UpdateGame.SpeakerReady)

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> SynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.listenerReady(): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> =
    automaton.move(GameStateMachine.Transition.UpdateGame.ListenerReady)

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> SynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.wordExplanationState(
    wordState: GameStateMachine.WordExplanation.State
): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> = automaton.move(
    GameStateMachine.Transition.UpdateGame.WordExplanationState(
        wordState = wordState,
    )
)

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> SynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.updateWordsExplanationResults(
    newExplanationResults: KoneList<GameStateMachine.WordExplanation>,
): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> = automaton.move(
    GameStateMachine.Transition.UpdateGame.UpdateWordsExplanationResults(
        newExplanationResults = newExplanationResults,
    )
)

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> SynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.confirmWordsExplanationResults(): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> =
    automaton.move(GameStateMachine.Transition.UpdateGame.ConfirmWordsExplanationResults)

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> SynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.finishGame(): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> =
    automaton.move(GameStateMachine.Transition.UpdateGame.FinishGame)