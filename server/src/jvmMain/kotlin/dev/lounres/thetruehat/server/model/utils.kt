package dev.lounres.thetruehat.server.model

import dev.lounres.thetruehat.api.models.Settings
import dev.lounres.thetruehat.api.models.UserGameState
import dev.lounres.thetruehat.server.Connection
import dev.lounres.thetruehat.server.availableDictionaries
import dev.lounres.thetruehat.server.logger
import kotlinx.datetime.Clock
import kotlin.random.Random
import kotlin.time.Duration


fun Room.Waiting.descriptionFor(playerIndex: Int): UserGameState =
    UserGameState(
        id = id,
        settings = settings,
        phase = UserGameState.Phase.WaitingForPlayers(
            availableDictionaries = availableDictionaries.map { UserGameState.ServerDictionary(id = it.id, name = it.name, wordsCount = it.words.size) },
            currentPlayersList = players.map { UserGameState.Player(it.username, it.online) },
            username = players[playerIndex].username,
            userIndex = playerIndex
        )
    )

fun Room.Playing.descriptionFor(playerIndex: Int): UserGameState =
    UserGameState(
        id = id,
        settings = settings,
        phase = UserGameState.Phase.GameInProgress(
            playersList = players.map { UserGameState.Player(it.username, it.online) },
            username = players[playerIndex].username,
            userIndex = playerIndex,
            speaker = speaker,
            listener = listener,
            unitsUntilEnd =
            when(settings.gameEndCondition) {
                Settings.GameEndCondition.Words ->
                    UserGameState.UnitsUntilEnd.Words(
                        freshWords.size + if (roundPhase is Room.Playing.RoundPhase.ExplanationInProgress) 1 else 0
                    )

                Settings.GameEndCondition.Rounds -> UserGameState.UnitsUntilEnd.Rounds(settings.roundsCount - numberOfLap)
            },
            roundPhase =
            when(val phase = roundPhase) {
                is Room.Playing.RoundPhase.Countdown ->
                    UserGameState.RoundPhase.Countdown(
                        millisecondsUntilStart = (phase.startInstant - Clock.System.now()).inWholeMilliseconds
                    )

                Room.Playing.RoundPhase.WaitingForPlayersToBeReady ->
                    UserGameState.RoundPhase.WaitingForPlayersToBeReady(
                        speakerReady = speakerReady,
                        listenerReady = listenerReady,
                    )

                is Room.Playing.RoundPhase.ExplanationInProgress ->
                    UserGameState.RoundPhase.ExplanationInProgress(
                        word = if (playerIndex == speaker) phase.wordToExplain else null,
                        millisecondsUntilEnd = (phase.endInstant - Clock.System.now()).inWholeMilliseconds,
                    )

                Room.Playing.RoundPhase.EditingInProgress ->
                    UserGameState.RoundPhase.EditingInProgress(
                        wordsToEdit = if (playerIndex == speaker) wordsToEdit else null
                    )
            },
        )
    )

fun Room.Results.descriptionFor(playerIndex: Int?): UserGameState =
    UserGameState(
        id = id,
        settings = settings,
        phase = UserGameState.Phase.GameEnded(
            username = playerIndex?.let { players[it].username },
            userIndex = playerIndex,
            results = players.map {
                UserGameState.GameResult(
                    username = it.username,
                    scoreGuessed = it.scoreGuessed,
                    scoreExplained = it.scoreExplained
                )
            }
        )
    )

fun Room.descriptionFor(playerIndex: Int): UserGameState =
    when(this) {
        is Room.Waiting -> descriptionFor(playerIndex)
        is Room.Playing -> descriptionFor(playerIndex)
        is Room.Results -> descriptionFor(playerIndex as Int?)
    }

val Room.Waiting.Player.online: Boolean get() = connection != null
val Room.Playing.Player.online: Boolean get() = connection != null

val Room.Player.state
    get() = when(this) {
        is Room.Waiting.Player -> room.descriptionFor(playerIndex)
        is Room.Playing.Player -> room.descriptionFor(playerIndex)
        is Room.Results.Player -> room.descriptionFor(playerIndex as Int?)
        is Room.Results.Spectator -> room.descriptionFor(null)
    }

val Connection.state: UserGameState?
    get() = player?.state

fun Room.Waiting.getOrCreatePlayerByNickname(nickname: String): Room.Waiting.Player =
    players.find { it.username == nickname } ?: addPlayer(nickname, null)

context(Random)
fun List<String>.generateWords(wordsCount: Int): MutableList<String> {
    val usedIndices = IntArray(wordsCount) { -1 }
    val result = ArrayList<String>(wordsCount)

    while (result.size < wordsCount) {
        val randomIndex = nextInt(this@generateWords.size)
        if (randomIndex in usedIndices) continue
        usedIndices[result.size] = randomIndex
        result.add(this@generateWords[randomIndex])
    }

    return result
}

val Room.Playing.hasNoWords: Boolean
    get() = freshWords.size == 0

val Room.Playing.isEnded: Boolean
    get() = hasNoWords || (settings.gameEndCondition == Settings.GameEndCondition.Rounds && numberOfLap == settings.roundsCount)

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