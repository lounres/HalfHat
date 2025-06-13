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


public suspend inline fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoTransitionReason, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.moveMaybe(
    transition: suspend (GameStateMachine.State<P, WP, Metadata>) -> TransitionOrReason<GameStateMachine.Transition<P, WP, MetadataTransition>, NoTransitionReason>
): MovementMaybeResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, NoTransitionReason, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> = automaton.moveMaybe(transition)

public suspend inline fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.move(
    transition: suspend (GameStateMachine.State<P, WP, Metadata>) -> GameStateMachine.Transition<P, WP, MetadataTransition>
): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> = automaton.move(transition)

public suspend fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoTransitionReason, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.moveMaybe(
    transition: TransitionOrReason<GameStateMachine.Transition<P, WP, MetadataTransition>, NoTransitionReason>
): MovementMaybeResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, NoTransitionReason, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> = automaton.moveMaybe(transition)

public suspend fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.move(
    transition: GameStateMachine.Transition<P, WP, MetadataTransition>
): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> = automaton.move(transition)

public suspend inline fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoTransitionReason, NoMetadataTransitionReason, Computation> AsynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.moveMaybeAndCompute(
    transition: suspend (GameStateMachine.State<P, WP, Metadata>) -> TransitionOrReasonAndComputation<GameStateMachine.Transition<P, WP, MetadataTransition>, NoTransitionReason, Computation>
): MovementMaybeAndComputationResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, NoTransitionReason, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>, Computation> = automaton.moveMaybeAndCompute(transition)

public suspend inline fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason, Computation> AsynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.moveAndCompute(
    transition: suspend (GameStateMachine.State<P, WP, Metadata>) -> TransitionAndComputation<GameStateMachine.Transition<P, WP, MetadataTransition>, Computation>
): MovementAndComputationResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>, Computation> = automaton.moveAndCompute(transition)

public suspend fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.updateGameSettings(
    playersList: KoneList<P>,
    settingsBuilder: GameStateMachine.GameSettings.Builder<WP>
): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> = automaton.move(
    GameStateMachine.Transition.UpdateGame.UpdateGameSettings(
        playersList = playersList,
        settingsBuilder = settingsBuilder,
    )
)

public suspend fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.initialiseGame(): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> =
    automaton.move(GameStateMachine.Transition.UpdateGame.InitialiseGame)

public suspend fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.speakerReady(): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> =
    automaton.move(GameStateMachine.Transition.UpdateGame.SpeakerReady)

public suspend fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.listenerReady(): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> =
    automaton.move(GameStateMachine.Transition.UpdateGame.ListenerReady)

public suspend fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.wordExplanationState(
    wordState: GameStateMachine.WordExplanation.State
): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> = automaton.move(
    GameStateMachine.Transition.UpdateGame.WordExplanationState(
        wordState = wordState,
    )
)

public suspend fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.updateWordsExplanationResults(
    newExplanationResults: KoneList<GameStateMachine.WordExplanation>,
): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> = automaton.move(
    GameStateMachine.Transition.UpdateGame.UpdateWordsExplanationResults(
        newExplanationResults = newExplanationResults,
    )
)

public suspend fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.confirmWordsExplanationResults(): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> =
    automaton.move(GameStateMachine.Transition.UpdateGame.ConfirmWordsExplanationResults)

public suspend fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.finishGame(): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> =
    automaton.move(GameStateMachine.Transition.UpdateGame.FinishGame)