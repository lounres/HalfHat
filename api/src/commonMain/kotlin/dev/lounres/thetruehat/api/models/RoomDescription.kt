package dev.lounres.thetruehat.api.models

import kotlinx.serialization.Serializable


@Serializable
public data class RoomDescription(
    val id: String,
    val settings: Settings,
    val phase: Phase
) {
    @Serializable
    public data class Player(
        val username: String,
        val online: Boolean,
    )

    @Serializable
    public data class TimetableEntry(
        val speaker: Int,
        val listener: Int,
    )

    @Serializable
    public sealed interface UnitsUntilEnd {
        @Serializable
        public data class Words(
            val wordsLeft: Int,
        ): UnitsUntilEnd
        @Serializable
        public data class Rounds(
            val roundsLeft: Int,
        ): UnitsUntilEnd
    }

    @Serializable
    public data class WordExplanationResult(
        val word: String,
        val state: State
    ) {
        @Serializable
        public enum class State {
            Explained, NotExplained, Mistake;
        }
    }

    @Serializable
    public data class GameResult(
        val username: String,
        val scoreExplained: Int,
        val scoreGuessed: Int,
    )

    @Serializable
    public sealed interface RoundPhase {
        @Serializable
        public data object WaitingForPlayersToBeReady: RoundPhase
        @Serializable
        public data class ExplanationInProgress(
            val word: String?,
            val endTime: Long,
        ): RoundPhase
        @Serializable
        public data class EditingInProgress(
            val wordsToEdit: List<WordExplanationResult>?
        ): RoundPhase
    }

    @Serializable
    public sealed interface Phase {

        @Serializable
        public data class WaitingForPlayers(
            val currentPlayersList: List<Player>,
        ): Phase
        @Serializable
        public data class GameInProgress(
            val palyersList: List<Player>,
            val timetable: List<TimetableEntry>,
            val speaker: Int,
            val listener: Int,
            val unitsUntilEnd: UnitsUntilEnd,
            val roundPhase: RoundPhase,
        ): Phase
        @Serializable
        public data class GameEnded(
            val results: List<GameResult>,
        ): Phase
    }
}