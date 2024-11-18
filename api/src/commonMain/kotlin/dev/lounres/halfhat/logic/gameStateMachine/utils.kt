package dev.lounres.halfhat.logic.gameStateMachine

import dev.lounres.kone.collections.KoneList
import dev.lounres.kone.collections.utils.map


public val <P> GameStateMachine.State.RoundWaiting<P>.speaker: P get() = playersList[speakerIndex].also { println("Speaker index: $speakerIndex") }
public val <P> GameStateMachine.State.RoundWaiting<P>.listener: P get() = playersList[listenerIndex].also { println("Listener index: $listenerIndex") }

public val <P> GameStateMachine.State.RoundPreparation<P>.speaker: P get() = playersList[speakerIndex]
public val <P> GameStateMachine.State.RoundPreparation<P>.listener: P get() = playersList[listenerIndex]

public val <P> GameStateMachine.State.RoundExplanation<P>.speaker: P get() = playersList[speakerIndex]
public val <P> GameStateMachine.State.RoundExplanation<P>.listener: P get() = playersList[listenerIndex]

public val <P> GameStateMachine.State.RoundLastGuess<P>.speaker: P get() = playersList[speakerIndex]
public val <P> GameStateMachine.State.RoundLastGuess<P>.listener: P get() = playersList[listenerIndex]

public val <P> GameStateMachine.State.RoundEditing<P>.speaker: P get() = playersList[speakerIndex]
public val <P> GameStateMachine.State.RoundEditing<P>.listener: P get() = playersList[listenerIndex]

public val <P> GameStateMachine.State.GameResults<P>.personalResults: KoneList<GameStateMachine.PersonalResult<P>>
    get() = results.map { GameStateMachine.PersonalResult(playersList[it.player], it.scoreExplained, it.scoreGuessed, it.sum) }