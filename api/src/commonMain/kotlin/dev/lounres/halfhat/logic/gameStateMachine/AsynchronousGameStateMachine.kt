package dev.lounres.halfhat.logic.gameStateMachine

import dev.lounres.kone.automata.AsynchronousAutomaton
import kotlin.jvm.JvmInline


@JvmInline
public value class AsynchronousGameStateMachine<P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> @PublishedApi internal constructor(
    @PublishedApi
    internal val automaton: AsynchronousAutomaton<GameStateMachine.State<P, WP, Metadata>, GameStateMachine.Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>>
) {
    public companion object
}