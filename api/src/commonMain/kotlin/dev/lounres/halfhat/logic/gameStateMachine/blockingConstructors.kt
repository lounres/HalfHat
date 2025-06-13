package dev.lounres.halfhat.logic.gameStateMachine

import dev.lounres.kone.automata.BlockingAutomaton
import dev.lounres.kone.automata.CheckResult
import dev.lounres.kone.automata.move
import dev.lounres.kone.collections.list.KoneList
import kotlinx.coroutines.CoroutineScope
import kotlin.random.Random


public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> BlockingGameStateMachine(
    coroutineScope: CoroutineScope,
    random: Random = DefaultRandom,
    initialState: GameStateMachine.State<P, WP, Metadata>,
    checkMetadataUpdate: BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.(
        previousState: GameStateMachine.State<P, WP, Metadata>,
        metadataTransition: MetadataTransition
    ) -> CheckResult<Metadata, NoMetadataTransitionReason>,
    metadataTransformer: BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.(
        previousState: GameStateMachine.State<P, WP, Metadata>,
        transition: GameStateMachine.Transition.UpdateGame<P, WP>
    ) -> Metadata = { previousState, _ -> previousState.metadata },
    onTransition: BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.(
        previousState: GameStateMachine.State<P, WP, Metadata>,
        transition: GameStateMachine.Transition<P, WP, MetadataTransition>,
        nextState: GameStateMachine.State<P, WP, Metadata>
    ) -> Unit = { _, _, _ -> },
): BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason> =
    BlockingGameStateMachine(
        automaton = BlockingAutomaton(
            initialState = initialState,
            onTransition = { previousState, transition, nextState -> BlockingGameStateMachine(this).onTransition(previousState, transition, nextState) },
            checkTransition = { previousState, transition ->
                checkGameStateMachineTransition(
                    coroutineScope = coroutineScope,
                    random = random,
                    moveState = { move(it) },
                    checkMetadataUpdate = { previousState, metadataTransition -> BlockingGameStateMachine(this).checkMetadataUpdate(previousState, metadataTransition) },
                    metadataTransformer = { previousState, transition -> BlockingGameStateMachine(this).metadataTransformer(previousState, transition) },
                    previousState = previousState,
                    transition = transition,
                )
            }
        )
    )

@Suppress("FunctionName")
public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> BlockingGameStateMachine.Companion.Initialization(
    coroutineScope: CoroutineScope,
    random: Random = DefaultRandom,
    metadata: Metadata,
    playersList: KoneList<P>,
    settingsBuilder: GameStateMachine.GameSettings.Builder<WP>,
    checkMetadataUpdate: BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.(
        previousState: GameStateMachine.State<P, WP, Metadata>,
        metadataTransition: MetadataTransition
    ) -> CheckResult<Metadata, NoMetadataTransitionReason>,
    metadataTransformer: BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.(
        previousState: GameStateMachine.State<P, WP, Metadata>,
        transition: GameStateMachine.Transition.UpdateGame<P, WP>
    ) -> Metadata = { previousState, _ -> previousState.metadata },
    onTransition: BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.(
        previousState: GameStateMachine.State<P, WP, Metadata>,
        transition: GameStateMachine.Transition<P, WP, MetadataTransition>,
        nextState: GameStateMachine.State<P, WP, Metadata>
    ) -> Unit = { _, _, _ -> },
): BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason> = BlockingGameStateMachine(
    coroutineScope = coroutineScope,
    random = random,
    initialState = GameStateMachine.State.GameInitialisation(
        metadata = metadata,
        playersList = playersList,
        settingsBuilder = settingsBuilder,
    ),
    checkMetadataUpdate = checkMetadataUpdate,
    metadataTransformer = metadataTransformer,
    onTransition = onTransition,
)

@Suppress("FunctionName")
public fun <P, WP: GameStateMachine.WordsProvider, Metadata, MetadataTransition, NoMetadataTransitionReason> BlockingGameStateMachine.Companion.Initialized(
    coroutineScope: CoroutineScope,
    random: Random = DefaultRandom,
    metadata: Metadata,
    playersList: KoneList<P>,
    settingsBuilder: GameStateMachine.GameSettings.Builder<WP>,
    checkMetadataUpdate: BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.(
        previousState: GameStateMachine.State<P, WP, Metadata>,
        metadataTransition: MetadataTransition
    ) -> CheckResult<Metadata, NoMetadataTransitionReason>,
    metadataTransformer: BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.(
        previousState: GameStateMachine.State<P, WP, Metadata>,
        transition: GameStateMachine.Transition.UpdateGame<P, WP>
    ) -> Metadata = { previousState, _ -> previousState.metadata },
    onTransition: BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason>.(
        previousState: GameStateMachine.State<P, WP, Metadata>,
        transition: GameStateMachine.Transition<P, WP, MetadataTransition>,
        nextState: GameStateMachine.State<P, WP, Metadata>
    ) -> Unit = { _, _, _ -> },
): BlockingGameStateMachine<P, WP, Metadata, MetadataTransition, NoMetadataTransitionReason> = BlockingGameStateMachine(
    coroutineScope = coroutineScope,
    random = random,
    initialState = GameStateMachine.State.RoundWaiting(
        metadata = metadata,
        playersList = playersList,
        settings = settingsBuilder.build(),
        roundNumber = 0u,
        cycleNumber = 0u,
        speakerIndex = 0u,
        listenerIndex = 1u,
        restWords = when (val wordsSource = settingsBuilder.wordsSource) {
            GameStateMachine.WordsSource.Players -> TODO()
            is GameStateMachine.WordsSource.Custom<*> ->
                when (settingsBuilder.gameEndConditionType) {
                    GameStateMachine.GameEndCondition.Type.Words -> wordsSource.provider.randomWords(settingsBuilder.cachedEndConditionWordsNumber)
                    GameStateMachine.GameEndCondition.Type.Cycles -> wordsSource.provider.allWords()
                }
        },
        explanationScores = KoneList(playersList.size) { 0u },
        guessingScores = KoneList(playersList.size) { 0u },
        speakerReady = false,
        listenerReady = false,
    ),
    checkMetadataUpdate = checkMetadataUpdate,
    metadataTransformer = metadataTransformer,
    onTransition = onTransition,
)