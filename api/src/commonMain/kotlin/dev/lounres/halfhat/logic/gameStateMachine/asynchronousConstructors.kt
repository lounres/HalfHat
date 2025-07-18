package dev.lounres.halfhat.logic.gameStateMachine

import dev.lounres.kone.automata.AsynchronousAutomaton
import dev.lounres.kone.automata.CheckResult
import dev.lounres.kone.automata.move
import dev.lounres.kone.collections.list.KoneList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlin.random.Random


public fun <P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason> AsynchronousGameStateMachine(
    coroutineScope: CoroutineScope,
    random: Random = DefaultRandom,
    mutex: Mutex = Mutex(),
    initialState: GameStateMachine.State<P, WPID, Metadata>,
    checkMetadataUpdate: suspend AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason>.(
        previousState: GameStateMachine.State<P, WPID, Metadata>,
        metadataTransition: MetadataTransition
    ) -> CheckResult<Metadata, NoMetadataTransitionReason>,
    metadataTransformer: suspend AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason>.(
        previousState: GameStateMachine.State<P, WPID, Metadata>,
        transition: GameStateMachine.Transition.UpdateGame<P, WPID, NoWordsProviderReason>
    ) -> Metadata = { previousState, _ -> previousState.metadata },
    onTransition: suspend AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason>.(
        previousState: GameStateMachine.State<P, WPID, Metadata>,
        transition: GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>,
        nextState: GameStateMachine.State<P, WPID, Metadata>
    ) -> Unit = { _, _, _ -> },
): AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason> =
    AsynchronousGameStateMachine(
        automaton = AsynchronousAutomaton(
            mutex = mutex,
            initialState = initialState,
            checkTransition = { previousState, transition ->
                checkGameStateMachineTransition(
                    coroutineScope = coroutineScope,
                    random = random,
                    moveState = { move(it) },
                    checkMetadataUpdate = { previousState, metadataTransition -> AsynchronousGameStateMachine(this).checkMetadataUpdate(previousState, metadataTransition) },
                    metadataTransformer = { previousState, transition -> AsynchronousGameStateMachine(this).metadataTransformer(previousState, transition) },
                    previousState = previousState,
                    transition = transition,
                )
            },
            onTransition = { previousState, transition, nextState -> AsynchronousGameStateMachine(this).onTransition(previousState, transition, nextState) },
        )
    )

@Suppress("FunctionName")
public fun <P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason> AsynchronousGameStateMachine.Companion.Initialization(
    coroutineScope: CoroutineScope,
    random: Random = DefaultRandom,
    mutex: Mutex = Mutex(),
    metadata: Metadata,
    playersList: KoneList<P>,
    settingsBuilder: GameStateMachine.GameSettings.Builder<WPID>,
    checkMetadataUpdate: suspend AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason>.(
        previousState: GameStateMachine.State<P, WPID, Metadata>,
        metadataTransition: MetadataTransition
    ) -> CheckResult<Metadata, NoMetadataTransitionReason>,
    metadataTransformer: suspend AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason>.(
        previousState: GameStateMachine.State<P, WPID, Metadata>,
        transition: GameStateMachine.Transition.UpdateGame<P, WPID, NoWordsProviderReason>
    ) -> Metadata = { previousState, _ -> previousState.metadata },
    onTransition: suspend AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason>.(
        previousState: GameStateMachine.State<P, WPID, Metadata>,
        transition: GameStateMachine.Transition<P, WPID, NoWordsProviderReason, MetadataTransition>,
        nextState: GameStateMachine.State<P, WPID, Metadata>
    ) -> Unit = { _, _, _ -> },
): AsynchronousGameStateMachine<P, WPID, NoWordsProviderReason, Metadata, MetadataTransition, NoMetadataTransitionReason> = AsynchronousGameStateMachine(
    coroutineScope = coroutineScope,
    random = random,
    mutex = mutex,
    initialState = GameStateMachine.State.GameInitialisation(
        metadata = metadata,
        playersList = playersList,
        settingsBuilder = settingsBuilder,
    ),
    checkMetadataUpdate = checkMetadataUpdate,
    metadataTransformer = metadataTransformer,
    onTransition = onTransition,
)