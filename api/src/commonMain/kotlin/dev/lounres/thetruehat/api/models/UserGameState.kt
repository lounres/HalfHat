package dev.lounres.thetruehat.api.models

import kotlinx.serialization.Serializable


@Serializable
public data class UserGameState(
    val id: String,
    val settings: Settings,
    val phase: Phase
) {
    @Serializable
    public data class ServerDictionary(
        val id: Int,
        val name: String,
        val wordsCount: Int,
    )

    @Serializable
    public data class Player(
        val username: String,
        val online: Boolean,
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
        public data class WaitingForPlayersToBeReady(
            val speakerReady: Boolean,
            val listenerReady: Boolean,
        ): RoundPhase
        @Serializable
        public data class Countdown(
            val millisecondsUntilStart: Long,
        ): RoundPhase
        @Serializable
        public data class ExplanationInProgress(
            val word: String?,
            val millisecondsUntilEnd: Long,
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
            val availableDictionaries: List<ServerDictionary>,
            val currentPlayersList: List<Player>,
            val username: String,
            val userIndex: Int,
        ): Phase
        @Serializable
        public data class GameInProgress(
            val playersList: List<Player>,
            val username: String,
            val userIndex: Int,
            val speaker: Int,
            val listener: Int,
            val unitsUntilEnd: UnitsUntilEnd,
            val roundPhase: RoundPhase,
        ): Phase
        @Serializable
        public data class GameEnded(
            val results: List<GameResult>,
            val username: String?,
            val userIndex: Int?,
        ): Phase
    }
}