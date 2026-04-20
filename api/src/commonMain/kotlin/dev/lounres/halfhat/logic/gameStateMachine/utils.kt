package dev.lounres.halfhat.logic.gameStateMachine

import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.utils.mapIndexed
import dev.lounres.kone.collections.utils.sortedBy
import kotlin.random.Random


@PublishedApi
internal val DefaultRandom: Random = Random

public val <P, WPID> AsynchronousGameStateMachine<P, WPID, *>.state: GameStateMachine.State<P, WPID> get() = automaton.state

public val <P> GameStateMachine.State.GameInitialised.Round.RoundWaiting<P, *>.speaker: P get() = playersList[speakerIndex]
public val <P> GameStateMachine.State.GameInitialised.Round.RoundWaiting<P, *>.listener: P get() = playersList[listenerIndex]

public val <P> GameStateMachine.State.GameInitialised.Round.RoundPreparation<P, *>.speaker: P get() = playersList[speakerIndex]
public val <P> GameStateMachine.State.GameInitialised.Round.RoundPreparation<P, *>.listener: P get() = playersList[listenerIndex]

public val <P> GameStateMachine.State.GameInitialised.Round.RoundExplanation<P, *>.speaker: P get() = playersList[speakerIndex]
public val <P> GameStateMachine.State.GameInitialised.Round.RoundExplanation<P, *>.listener: P get() = playersList[listenerIndex]

public val <P> GameStateMachine.State.GameInitialised.Round.RoundLastGuess<P, *>.speaker: P get() = playersList[speakerIndex]
public val <P> GameStateMachine.State.GameInitialised.Round.RoundLastGuess<P, *>.listener: P get() = playersList[listenerIndex]

public val <P> GameStateMachine.State.GameInitialised.Round.RoundEditing<P, *>.speaker: P get() = playersList[speakerIndex]
public val <P> GameStateMachine.State.GameInitialised.Round.RoundEditing<P, *>.listener: P get() = playersList[listenerIndex]

public val <P> GameStateMachine.State.GameInitialised.GameResults<P, *>.personalResults: KoneList<GameStateMachine.PersonalResult<P>>
    get() = playersList
        .mapIndexed { index, player -> GameStateMachine.PersonalResult(player, explanationScores[index], guessingScores[index], explanationScores[index] + guessingScores[index]) }
        .sortedBy { it.scoreSum }