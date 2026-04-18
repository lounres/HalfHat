package dev.lounres.halfhat.logic.gameStateMachine

import dev.lounres.kone.automata.AsynchronousAutomaton
import dev.lounres.kone.automata.move
import dev.lounres.kone.collections.list.KoneList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


public fun <P, WPID, NoWordsProviderReason> AsynchronousGameStateMachine(
    coroutineScope: CoroutineScope,
    random: Random = DefaultRandom,
    timerDelayDuration: Duration = 90.milliseconds,
    mutex: Mutex = Mutex(),
    initialState: GameStateMachine.State<P, WPID>,
    selfMoveState: suspend AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason>.(
        GameStateMachine.Transition<P, WPID, NoWordsProviderReason>
    ) -> Unit = { move(it) },
    onTransition: suspend AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason>.(
        previousState: GameStateMachine.State<P, WPID>,
        transition: GameStateMachine.Transition<P, WPID, NoWordsProviderReason>,
        nextState: GameStateMachine.State<P, WPID>
    ) -> Unit = { _, _, _ -> },
): AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason> =
    AsynchronousGameStateMachine(
        automaton = AsynchronousAutomaton(
            mutex = mutex,
            initialState = initialState,
            checkTransition = { previousState, transition ->
                checkGameStateMachineTransition(
                    coroutineScope = coroutineScope,
                    random = random,
                    timerDelayDuration = timerDelayDuration,
                    moveState = { AsynchronousGameStateMachine(this).selfMoveState(it) },
                    previousState = previousState,
                    transition = transition,
                )
            },
            onTransition = { previousState, transition, nextState -> AsynchronousGameStateMachine(this).onTransition(previousState, transition, nextState) },
        )
    )

@Suppress("FunctionName")
public fun <P, WPID, NoWordsProviderReason> AsynchronousGameStateMachine.Companion.Initialization(
    coroutineScope: CoroutineScope,
    random: Random = DefaultRandom,
    mutex: Mutex = Mutex(),
    playersList: KoneList<P>,
    settingsBuilder: GameStateMachine.GameSettings.Builder<WPID>,
    selfMoveState: suspend AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason>.(
        GameStateMachine.Transition<P, WPID, NoWordsProviderReason>
    ) -> Unit = { move(it) },
    onTransition: suspend AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason>.(
        previousState: GameStateMachine.State<P, WPID>,
        transition: GameStateMachine.Transition<P, WPID, NoWordsProviderReason>,
        nextState: GameStateMachine.State<P, WPID>
    ) -> Unit = { _, _, _ -> },
): AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason> = AsynchronousGameStateMachine(
    coroutineScope = coroutineScope,
    random = random,
    mutex = mutex,
    initialState = GameStateMachine.State.GameInitialisation(
        playersList = playersList,
        settingsBuilder = settingsBuilder,
    ),
    selfMoveState = selfMoveState,
    onTransition = onTransition,
)