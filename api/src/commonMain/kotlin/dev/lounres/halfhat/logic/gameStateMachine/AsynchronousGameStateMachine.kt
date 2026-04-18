package dev.lounres.halfhat.logic.gameStateMachine

import dev.lounres.kone.automata.AsynchronousAutomaton
import dev.lounres.kone.automata.CheckResult
import dev.lounres.kone.automata.InternalAutomatonApi
import kotlin.jvm.JvmInline


@JvmInline
public value class AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason> @PublishedApi internal constructor(
    @PublishedApi
    internal val automaton: AsynchronousAutomaton<
        GameStateMachine.State<P, WPID>,
        GameStateMachine.Transition<P, WPID, NoWordsProviderReason>,
        GameStateMachine.NoNextStateReason<NoWordsProviderReason>
    >,
) {
    @OptIn(InternalAutomatonApi::class)
    public suspend fun checkTransition(
        transition: GameStateMachine.Transition<P, WPID, NoWordsProviderReason>
    ): CheckResult<GameStateMachine.State<P, WPID>, GameStateMachine.NoNextStateReason<NoWordsProviderReason>> =
        automaton.checkTransition(automaton.state, transition)

    public companion object
}