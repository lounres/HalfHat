package dev.lounres.komponentual.lifecycle

import kotlinx.coroutines.CoroutineScope


@DelicateLifecycleAPI
public fun UIComponentLifecycle.logicChildDeferringOnRunning(coroutineScope: CoroutineScope): LogicComponentLifecycle =
    childDeferring(
        coroutineScope = coroutineScope,
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
public fun Lifecycle.Companion.mergeLogicAndUILifecyclesDeferringOnRunning(
    lifecycle1: LogicComponentLifecycle,
    lifecycle2: UIComponentLifecycle,
    coroutineScope: CoroutineScope
): LogicComponentLifecycle =
    mergeDeferring(
        lifecycle1 = lifecycle1,
        lifecycle2 = lifecycle2,
        coroutineScope = coroutineScope,
        initialState = Pair(LogicComponentLifecycleState.Initialized, UIComponentLifecycleState.Initialized),
        mergeStates = { state1, state2 -> Pair(state1, state2) },
        mapTransition1 = { state, transition1 -> Pair(transition1.target, state.second) },
        mapTransition2 = { state, transition2 -> Pair(state.first, transition2.target) },
        checkNextState = { previousState, nextState -> checkNextState(minOf(previousState.first, previousState.second), minOf(nextState.first, nextState.second)) },
        decomposeTransition = { previousState, nextState -> decomposeTransition(minOf(previousState.first, previousState.second), minOf(nextState.first, nextState.second)) },
        outputState = { minOf(it.first, it.second) }
    )