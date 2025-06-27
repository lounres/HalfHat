package dev.lounres.halfhat.logic.gameStateMachine

import dev.lounres.kone.automata.AsynchronousAutomaton
import kotlin.jvm.JvmInline


@JvmInline
public value class AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason> @PublishedApi internal constructor(
    @PublishedApi
    internal val automaton: AsynchronousAutomaton<GameStateMachine.State<P, WPID, Metadata>, GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason, NoWordsProviderReason>>
) {
    public companion object
}