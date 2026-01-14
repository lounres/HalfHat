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


public suspend inline fun <P, WPID, NoWordsProviderReason, Metadata, MetadataTransition : Any, NoTransitionReason, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason>.moveMaybe(
    transition: suspend (GameStateMachine.State<P, WPID, Metadata>) -> TransitionOrReason<GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>, NoTransitionReason>
): MovementMaybeResult<GameStateMachine.State<P, WPID, Metadata>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>, NoTransitionReason, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason, NoWordsProviderReason>> = automaton.moveMaybe(transition)

public suspend inline fun <P, WPID, NoWordsProviderReason, Metadata, MetadataTransition : Any, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason>.move(
    transition: suspend (GameStateMachine.State<P, WPID, Metadata>) -> GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>
): MovementResult<GameStateMachine.State<P, WPID, Metadata>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason, NoWordsProviderReason>> = automaton.move(transition)

public suspend fun <P, WPID, NoWordsProviderReason, Metadata, MetadataTransition : Any, NoTransitionReason, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason>.moveMaybe(
    transition: TransitionOrReason<GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>, NoTransitionReason>
): MovementMaybeResult<GameStateMachine.State<P, WPID, Metadata>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>, NoTransitionReason, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason, NoWordsProviderReason>> = automaton.moveMaybe(transition)

public suspend fun <P, WPID, NoWordsProviderReason, Metadata, MetadataTransition : Any, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason>.move(
    transition: GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>
): MovementResult<GameStateMachine.State<P, WPID, Metadata>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason, NoWordsProviderReason>> = automaton.move(transition)

public suspend inline fun <P, WPID, NoWordsProviderReason, Metadata, MetadataTransition : Any, NoTransitionReason, NoMetadataTransitionReason, Computation> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason>.moveMaybeAndCompute(
    transition: suspend (GameStateMachine.State<P, WPID, Metadata>) -> TransitionOrReasonAndComputation<GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>, NoTransitionReason, Computation>
): MovementMaybeAndComputationResult<GameStateMachine.State<P, WPID, Metadata>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>, NoTransitionReason, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason, NoWordsProviderReason>, Computation> = automaton.moveMaybeAndCompute(transition)

public suspend inline fun <P, WPID, NoWordsProviderReason, Metadata, MetadataTransition : Any, NoMetadataTransitionReason, Computation> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason>.moveAndCompute(
    transition: suspend (GameStateMachine.State<P, WPID, Metadata>) -> TransitionAndComputation<GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>, Computation>
): MovementAndComputationResult<GameStateMachine.State<P, WPID, Metadata>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason, NoWordsProviderReason>, Computation> = automaton.moveAndCompute(transition)

public suspend fun <P, WPID, NoWordsProviderReason, Metadata, MetadataTransition : Any, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason>.updateGameSettings(
    playersList: KoneList<P>,
    settingsBuilder: GameStateMachine.GameSettings.Builder<WPID>,
    metadataTransition: MetadataTransition? = null,
): MovementResult<GameStateMachine.State<P, WPID, Metadata>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason, NoWordsProviderReason>> = automaton.move(
    GameStateMachine.Transition.UpdateGameSettings(
        playersList = playersList,
        settingsBuilder = settingsBuilder,
        metadataTransition = metadataTransition,
    )
)

public suspend fun <P, WPID, NoWordsProviderReason, Metadata, MetadataTransition : Any, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason>.initialiseGame(
    wordsProviderRegistry: GameStateMachine.WordsProviderRegistry<WPID, NoWordsProviderReason>,
    metadataTransition: MetadataTransition? = null,
): MovementResult<GameStateMachine.State<P, WPID, Metadata>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason, NoWordsProviderReason>> =
    automaton.move(GameStateMachine.Transition.InitialiseGame(wordsProviderRegistry, metadataTransition))

public suspend fun <P, WPID, NoWordsProviderReason, Metadata, MetadataTransition : Any, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason>.speakerReady(
    metadataTransition: MetadataTransition? = null
): MovementResult<GameStateMachine.State<P, WPID, Metadata>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason, NoWordsProviderReason>> =
    automaton.move(GameStateMachine.Transition.SpeakerReady(metadataTransition))

public suspend fun <P, WPID, NoWordsProviderReason, Metadata, MetadataTransition : Any, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason>.listenerReady(
    metadataTransition: MetadataTransition? = null
): MovementResult<GameStateMachine.State<P, WPID, Metadata>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason, NoWordsProviderReason>> =
    automaton.move(GameStateMachine.Transition.ListenerReady(metadataTransition))

public suspend fun <P, WPID, NoWordsProviderReason, Metadata, MetadataTransition : Any, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason>.speakerAndListenerReady(
    metadataTransition: MetadataTransition? = null
): MovementResult<GameStateMachine.State<P, WPID, Metadata>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason, NoWordsProviderReason>> =
    automaton.move(GameStateMachine.Transition.SpeakerAndListenerReady(metadataTransition))

public suspend fun <P, WPID, NoWordsProviderReason, Metadata, MetadataTransition : Any, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason>.wordExplanationState(
    wordState: GameStateMachine.WordExplanation.State,
    metadataTransition: MetadataTransition? = null
): MovementResult<GameStateMachine.State<P, WPID, Metadata>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason, NoWordsProviderReason>> = automaton.move(
    GameStateMachine.Transition.WordExplanationState(
        wordState = wordState,
        metadataTransition = metadataTransition,
    )
)

public suspend fun <P, WPID, NoWordsProviderReason, Metadata, MetadataTransition : Any, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason>.updateWordsExplanationResults(
    newExplanationResults: KoneList<GameStateMachine.WordExplanation>,
    metadataTransition: MetadataTransition? = null
): MovementResult<GameStateMachine.State<P, WPID, Metadata>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason, NoWordsProviderReason>> = automaton.move(
    GameStateMachine.Transition.UpdateWordsExplanationResults(
        newExplanationResults = newExplanationResults,
        metadataTransition = metadataTransition,
    )
)

public suspend fun <P, WPID, NoWordsProviderReason, Metadata, MetadataTransition : Any, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason>.confirmWordsExplanationResults(
    metadataTransition: MetadataTransition? = null
): MovementResult<GameStateMachine.State<P, WPID, Metadata>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason, NoWordsProviderReason>> =
    automaton.move(GameStateMachine.Transition.ConfirmWordsExplanationResults(metadataTransition))

public suspend fun <P, WPID, NoWordsProviderReason, Metadata, MetadataTransition : Any, NoMetadataTransitionReason> AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason>.finishGame(
    metadataTransition: MetadataTransition? = null
): MovementResult<GameStateMachine.State<P, WPID, Metadata>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason, NoWordsProviderReason>> =
    automaton.move(GameStateMachine.Transition.FinishGame(metadataTransition))