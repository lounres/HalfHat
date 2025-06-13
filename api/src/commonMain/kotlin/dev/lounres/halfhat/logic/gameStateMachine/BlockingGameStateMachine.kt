package dev.lounres.halfhat.logic.gameStateMachine

import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine.State
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine.Transition
import dev.lounres.kone.automata.BlockingAutomaton
import kotlin.jvm.JvmInline


@JvmInline
public value class BlockingGameStateMachine<P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> internal constructor(
    internal val automaton: BlockingAutomaton<State<P, WP, Metadata>, Transition<P, WP, MetadataTransition>, GameStateMachine.NoNextStateReason<NoMetadataTransitionReason>>
) {
    public companion object
}