package dev.lounres.thetruehat.server.model

import dev.lounres.thetruehat.api.models.RoomDescription
import dev.lounres.thetruehat.api.models.Settings
import dev.lounres.thetruehat.api.models.UserGameState
import dev.lounres.thetruehat.server.Connection
import dev.lounres.thetruehat.server.logger
import kotlinx.datetime.Clock
import kotlin.time.Duration


fun Room.descriptionFor(playerIndex: Int): RoomDescription =
    RoomDescription(
        id = id,
        settings = settings,
        phase = when(this) {
            is Room.Waiting ->
                RoomDescription.Phase.WaitingForPlayers(
                    currentPlayersList = players.map { RoomDescription.Player(it.username, it.online) },
                )
            is Room.Playing ->
                RoomDescription.Phase.GameInProgress(
                    playersList = players.map { RoomDescription.Player(it.username, it.online) },
                    speaker = speaker,
                    listener = listener,
                    unitsUntilEnd =
                        when(settings.gameEndCondition) {
                            Settings.GameEndCondition.Words ->
                                RoomDescription.UnitsUntilEnd.Words(
                                    freshWords.size + if (this.roundPhase is Room.Playing.RoundPhase.ExplanationInProgress) 1 else 0
                                )
                            Settings.GameEndCondition.Rounds -> RoomDescription.UnitsUntilEnd.Rounds(settings.roundsCount - numberOfLap)
                        },
                    roundPhase =
                        when(val phase = roundPhase) {
                            is Room.Playing.RoundPhase.Countdown ->
                                RoomDescription.RoundPhase.Countdown(
                                    millisecondsUntilStart = (phase.startInstant - Clock.System.now()).inWholeMilliseconds
                                )
                            Room.Playing.RoundPhase.WaitingForPlayersToBeReady ->
                                RoomDescription.RoundPhase.WaitingForPlayersToBeReady(
                                    speakerReady = speakerReady,
                                    listenerReady = listenerReady,
                                )
                            is Room.Playing.RoundPhase.ExplanationInProgress ->
                                RoomDescription.RoundPhase.ExplanationInProgress(
                                    word = if (playerIndex == speaker) phase.wordToExplain else null,
                                    millisecondsUntilEnd = (phase.endInstant - Clock.System.now()).inWholeMilliseconds,
                                )
                            Room.Playing.RoundPhase.EditingInProgress ->
                                RoomDescription.RoundPhase.EditingInProgress(
                                    wordsToEdit = if (playerIndex == speaker) wordsToEdit else null
                                )
                        },
                )
            is Room.Results ->
                RoomDescription.Phase.GameEnded(
                    results = players.map {
                        RoomDescription.GameResult(
                            username = it.username,
                            scoreGuessed = it.scoreGuessed,
                            scoreExplained = it.scoreExplained
                        )
                    }
                )
        }
    )

val Room.Waiting.Player.online: Boolean get() = connection != null
val Room.Playing.Player.online: Boolean get() = connection != null

val Room.Player.state: UserGameState
    get() = UserGameState(
        roomDescription = room.descriptionFor(playerIndex = playerIndex),
        username = username,
        userIndex = playerIndex,
    )

val Connection.state: UserGameState?
    get() = player?.state

fun Room.Waiting.getOrCreatePlayerByNickname(nickname: String): Room.Waiting.Player =
    players.find { it.username == nickname } ?: addPlayer(nickname, null)

val Room.Playing.isEnded
    get() = roundPhase == Room.Playing.RoundPhase.WaitingForPlayersToBeReady &&
            when(settings.gameEndCondition) {
                Settings.GameEndCondition.Words -> freshWords.size == 0
                Settings.GameEndCondition.Rounds -> numberOfLap == settings.roundsCount
            }

fun Room.Playing.prepareForRound() {
    speakerReady = false
    listenerReady = false

    speaker = (speaker + 1) % players.size
    listener = (listener + 1) % players.size
    if (speaker == 0) {
        listener = (listener + 1) % players.size
        if (listener == speaker) {
            listener = 1
        }
    }

    numberOfRound++
    if (speaker == 0 && listener == 1) numberOfLap++
}

fun Room.Playing.setNextWordForExplanation() {
    val randomWordIndex = freshWords.indices.random()
    val randomWord = freshWords[randomWordIndex]
    freshWords.removeAt(randomWordIndex)
    roundPhase = when(val roundPhase = roundPhase) {
        Room.Playing.RoundPhase.WaitingForPlayersToBeReady -> {
            logger.warn { "`setNextWordExplanation` is used during `WaitingForPlayersToBeReady` round phase" }
            Room.Playing.RoundPhase.ExplanationInProgress(
                endInstant = Clock.System.now(), // TODO: DI the clock
                wordToExplain = randomWord,
                strictEndJob = null
            )
        }
        is Room.Playing.RoundPhase.Countdown ->
            Room.Playing.RoundPhase.ExplanationInProgress(
                endInstant = roundPhase.startInstant + with(Duration) { settings.explanationTime.seconds },
                wordToExplain = randomWord,
                strictEndJob = roundPhase.strictEndJob
            )
        is Room.Playing.RoundPhase.ExplanationInProgress ->
            Room.Playing.RoundPhase.ExplanationInProgress(
                endInstant = roundPhase.endInstant,
                wordToExplain = randomWord,
                strictEndJob = roundPhase.strictEndJob
            )
        Room.Playing.RoundPhase.EditingInProgress -> {
            logger.warn { "`setNextWordExplanation` is used during `EditingInProgress` round phase" }
            Room.Playing.RoundPhase.ExplanationInProgress(
                endInstant = Clock.System.now(), // TODO: DI the clock
                wordToExplain = randomWord,
                strictEndJob = null
            )
        }
    }
}