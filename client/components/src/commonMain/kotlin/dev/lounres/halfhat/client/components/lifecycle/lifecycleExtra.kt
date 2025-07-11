package dev.lounres.halfhat.client.components.lifecycle

import dev.lounres.komponentual.lifecycle.DelicateLifecycleAPI
import dev.lounres.komponentual.lifecycle.Lifecycle
import dev.lounres.komponentual.lifecycle.childDeferring
import dev.lounres.komponentual.lifecycle.mergeDeferring


@DelicateLifecycleAPI
internal fun UIComponentLifecycle.logicChildDeferringOnRunning(): LogicComponentLifecycle =
    childDeferring(
        initialState = LogicComponentLifecycleState.Initialized,
        mapState = {
            when (it) {
                UIComponentLifecycleState.Destroyed -> LogicComponentLifecycleState.Destroyed
                UIComponentLifecycleState.Initialized -> LogicComponentLifecycleState.Initialized
                else -> LogicComponentLifecycleState.Running
            }
        },
        mapTransition = { _, transition ->
            when (transition.target) {
                UIComponentLifecycleState.Destroyed -> LogicComponentLifecycleState.Destroyed
                UIComponentLifecycleState.Initialized -> LogicComponentLifecycleState.Initialized
                else -> LogicComponentLifecycleState.Running
            }
        },
        checkNextState = ::checkNextState,
        decomposeTransition = ::decomposeTransition,
        outputState = { it },
    )

internal fun minOf(logic: LogicComponentLifecycleState, ui: UIComponentLifecycleState): LogicComponentLifecycleState =
    when {
        logic == LogicComponentLifecycleState.Destroyed || ui == UIComponentLifecycleState.Destroyed  -> LogicComponentLifecycleState.Destroyed
        logic == LogicComponentLifecycleState.Initialized || ui == UIComponentLifecycleState.Initialized  -> LogicComponentLifecycleState.Initialized
        else -> LogicComponentLifecycleState.Running
    }

@DelicateLifecycleAPI
internal fun Lifecycle.Companion.mergeLogicAndUILifecyclesDeferringOnRunning(
    lifecycle1: LogicComponentLifecycle,
    lifecycle2: UIComponentLifecycle,
): LogicComponentLifecycle =
    mergeDeferring(
        lifecycle1 = lifecycle1,
        lifecycle2 = lifecycle2,
        initialState = Pair(LogicComponentLifecycleState.Initialized, UIComponentLifecycleState.Initialized),
        mergeStates = { state1, state2 -> Pair(state1, state2) },
        mapTransition1 = { state, transition1 -> Pair(transition1.target, state.second) },
        mapTransition2 = { state, transition2 -> Pair(state.first, transition2.target) },
        checkNextState = { previousState, nextState -> checkNextState(minOf(previousState.first, previousState.second), minOf(nextState.first, nextState.second)) },
        decomposeTransition = { previousState, nextState -> decomposeTransition(minOf(previousState.first, previousState.second), minOf(nextState.first, nextState.second)) },
        outputState = { minOf(it.first, it.second) }
    )