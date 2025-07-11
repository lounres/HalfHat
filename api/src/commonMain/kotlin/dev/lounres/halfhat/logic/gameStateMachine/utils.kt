package dev.lounres.halfhat.logic.gameStateMachine

import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.utils.map
import kotlin.random.Random


@PublishedApi
internal val DefaultRandom: Random = Random

public val <P, WPID, Metadata> AsynchronousGameStateMachine<P, WPID, *, Metadata, *, *>.state: GameStateMachine.State<P, WPID, Metadata> get() = automaton.state

public val <P> GameStateMachine.State.RoundWaiting<P, *, *>.speaker: P get() = playersList[speakerIndex]
public val <P> GameStateMachine.State.RoundWaiting<P, *, *>.listener: P get() = playersList[listenerIndex]

public val <P> GameStateMachine.State.RoundPreparation<P, *, *>.speaker: P get() = playersList[speakerIndex]
public val <P> GameStateMachine.State.RoundPreparation<P, *, *>.listener: P get() = playersList[listenerIndex]

public val <P> GameStateMachine.State.RoundExplanation<P, *, *>.speaker: P get() = playersList[speakerIndex]
public val <P> GameStateMachine.State.RoundExplanation<P, *, *>.listener: P get() = playersList[listenerIndex]

public val <P> GameStateMachine.State.RoundLastGuess<P, *, *>.speaker: P get() = playersList[speakerIndex]
public val <P> GameStateMachine.State.RoundLastGuess<P, *, *>.listener: P get() = playersList[listenerIndex]

public val <P> GameStateMachine.State.RoundEditing<P, *, *>.speaker: P get() = playersList[speakerIndex]
public val <P> GameStateMachine.State.RoundEditing<P, *, *>.listener: P get() = playersList[listenerIndex]

public val <P> GameStateMachine.State.GameResults<P, *>.personalResults: KoneList<GameStateMachine.PersonalResult<P>>
    get() = results.map { GameStateMachine.PersonalResult(playersList[it.player], it.scoreExplained, it.scoreGuessed, it.sum) }