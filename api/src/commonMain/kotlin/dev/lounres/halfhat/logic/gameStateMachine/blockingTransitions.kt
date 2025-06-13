package dev.lounres.halfhat.logic.gameStateMachine

import dev.lounres.kone.automata.MovementMaybeResult
import dev.lounres.kone.automata.MovementResult
import dev.lounres.kone.automata.TransitionOrReason
import dev.lounres.kone.automata.move
import dev.lounres.kone.automata.moveMaybe
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.maybe.Maybe


public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoTransitionReason, NoMetadataTransitionReason> BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.moveMaybe(
    transition: (GameStateMachine.State<P, WP, Metadata>) -> TransitionOrReason<GameStateMachine.Transition<P, WP, MetadataTransition>, NoTransitionReason>
): MovementMaybeResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, NoTransitionReason, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> = automaton.moveMaybe(transition)

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.move(
    transition: (GameStateMachine.State<P, WP, Metadata>) -> GameStateMachine.Transition<P, WP, MetadataTransition>
): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> = automaton.move(transition)

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoTransitionReason, NoMetadataTransitionReason> BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.moveMaybe(
    transition: TransitionOrReason<GameStateMachine.Transition<P, WP, MetadataTransition>, NoTransitionReason>
): MovementMaybeResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, NoTransitionReason, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> = automaton.moveMaybe(transition)

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.move(
    transition: GameStateMachine.Transition<P, WP, MetadataTransition>
): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> = automaton.move(transition)

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.updateGameSettings(
    playersList: KoneList<P>,
    settingsBuilder: GameStateMachine.GameSettings.Builder<WP>
): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> = automaton.move(
    GameStateMachine.Transition.UpdateGame.UpdateGameSettings(
        playersList = playersList,
        settingsBuilder = settingsBuilder,
    )
)

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.initialiseGame(): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> =
    automaton.move(GameStateMachine.Transition.UpdateGame.InitialiseGame)

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.speakerReady(): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> =
    automaton.move(GameStateMachine.Transition.UpdateGame.SpeakerReady)

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.listenerReady(): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> =
    automaton.move(GameStateMachine.Transition.UpdateGame.ListenerReady)

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.speakerAndListenerReady(): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> =
    automaton.move(GameStateMachine.Transition.UpdateGame.SpeakerAndListenerReady)

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.wordExplanationState(
    wordState: GameStateMachine.WordExplanation.State
): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> = automaton.move(
    GameStateMachine.Transition.UpdateGame.WordExplanationState(
        wordState = wordState,
    )
)

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.updateWordsExplanationResults(
    newExplanationResults: KoneList<GameStateMachine.WordExplanation>,
): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> = automaton.move(
    GameStateMachine.Transition.UpdateGame.UpdateWordsExplanationResults(
        newExplanationResults = newExplanationResults,
    )
)

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.confirmWordsExplanationResults(): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> =
    automaton.move(GameStateMachine.Transition.UpdateGame.ConfirmWordsExplanationResults)

public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.finishGame(): MovementResult<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>> =
    automaton.move(GameStateMachine.Transition.UpdateGame.FinishGame)