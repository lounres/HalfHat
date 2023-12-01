package dev.lounres.thetruehat.api

import kotlinx.serialization.Serializable


@Serializable
public data class Player(
    val username: String,
    val online: Boolean
)

@Serializable
public data object DictionaryFileInfo

@Serializable
public data class Settings(
    val delayTime: Int,
    val explanationTime: Int,
    val aftermathTime: Int,
    val strictMode: Boolean,
    val terminationCondition: TerminationCondition,
    val wordsNumber: Int,
    val roundsNumber: Int,
    val wordset: Wordset,
) {
    public enum class TerminationCondition {
        WORDS, ROUNDS;
    }
    public sealed interface Wordset {
        public data class ServerDictionary(
            val dictionaryId: Int,
        ): Wordset
        public data class HostDictionary(
            val dictionaryFileInfo: DictionaryFileInfo,
        ): Wordset
        public data object PlayerWords: Wordset
    }
}

@Serializable
public data class SettingsUpdate(//TODO
    val delayTime: Int?,
    val explanationTime: Int?,
    val aftermathTime: Int?,
    val strictMode: Boolean?,
    val terminationCondition: Settings.TerminationCondition?,
    val wordsNumber: Int?,
    val roundsNumber: Int?,
    val wordset: Settings.Wordset?,
)

@Serializable
public data class TimetableEntry(
    val speaker: Int,
    val listener: Int,
)

@Serializable
public data class WordExplanation(
    val word: String,
    val state: State
) {
    public enum class State {
        EXPLAINED, NOT_EXPLAINED, MISTAKE;
    }
}

@Serializable
public data class RoomState(
    val key: String,
    val stage: Stage
) {
    @Serializable
    public sealed interface Stage {
        @Serializable
        public data class Wait(
            val playerList: List<Player>,
            val host: Int,
            val settings: Settings,
        ): Stage
        @Serializable
        public data class Play(
            val playerList: List<Player>,
            val host: Int,
            val settings: Settings,
            val timetable: List<TimetableEntry>,
            val speaker: Int,
            val listener: Int,
            val wordsLeft: Int,
            val roundsLeft: Int,
            val stage: Stage
        ): Stage {
            @Serializable
            public sealed interface Stage {
                public data object Wait: Stage
                public data class Explanation(
                    val word: String?,
                    val endTime: Long,
                ): Stage
                public data class Edit(
                    val editWords: List<WordExplanation>?
                ): Stage
            }
        }
    }
}

@Serializable
public sealed interface Termination {
    @Serializable
    public data class Words(
        val wordsLeft: Int,
    ): Termination
    @Serializable
    public data class Rounds(
        val roundsLeft: Int,
    ): Termination
}

@Serializable
public data class GameResult(
    val username: String,
    val scoreExplained: Int,
    val scoreGuessed: Int,
)