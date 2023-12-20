package dev.lounres.thetruehat.server.model

import dev.lounres.thetruehat.api.defaultSettings
import dev.lounres.thetruehat.api.models.RoomDescription
import dev.lounres.thetruehat.api.models.Settings
import dev.lounres.thetruehat.server.Connection
import kotlinx.coroutines.Job
import kotlinx.datetime.Instant
import java.util.concurrent.CopyOnWriteArrayList


sealed interface Room {
    val id: String
    val settings: Settings
    val players: List<Player>

    sealed interface Player {
        val room: Room
        val username: String
        val playerIndex: Int
    }

    class Waiting(
        override val id: String,
        override var settings: Settings = defaultSettings
    ): Room {
        private val _players: MutableList<Player> = CopyOnWriteArrayList()
        override val players: List<Player> = _players

        fun addPlayer(username: String, connection: Connection?): Player {
            val player = Player(room = this, username = username, playerIndex = _players.size, connection = connection)
            _players.add(player)
            return player
        }

        class Player(
            override val room: Room.Waiting,
            override val username: String,
            override val playerIndex: Int,
            var connection: Connection?,
        ): Room.Player
    }

    class Playing(
        override val id: String,
        override val settings: Settings,
        playerListToProcess: List<Waiting.Player>,
    ): Room {
        override val players: List<Player>

        init {
            var index = 0
            players = playerListToProcess
                .mapNotNull {
                    val connection = it.connection
                    if (connection != null) {
                        val newPlayer = Player(
                            room = this,
                            username = it.username,
                            playerIndex = index++,
                            connection = connection
                        )
                        connection.player = newPlayer
                        newPlayer
                    } else null
                }
        }

        val freshWords = mutableListOf(
            "кошка",
            "собака",
            "утка",
            "гусь",
            "воробей",
            "голубь",
            "жираф",
            "пантера",
            "тигр",
            "лев",
            "бегемот",
            "коршун",
            "ястреб",
        )
        val wordsToEdit = mutableListOf<RoomDescription.WordExplanationResult>()
        val usedWords = mutableListOf<RoomDescription.WordExplanationResult>()

        var roundPhase: RoundPhase = RoundPhase.WaitingForPlayersToBeReady

        var speaker: Int = 0
        var listener: Int = 1

        var speakerReady = false
        var listenerReady = false

        var numberOfRound = 0
        var numberOfLap = 0

        sealed interface RoundPhase {
            data object WaitingForPlayersToBeReady: RoundPhase
            data class Countdown(
                val startInstant: Instant,
                val roundStartJob: Job,
                val strictEndJob: Job?
            ): RoundPhase
            data class ExplanationInProgress(
                val endInstant: Instant,
                val wordToExplain: String,
                val strictEndJob: Job?
            ): RoundPhase
            data object EditingInProgress: RoundPhase
        }

        class Player(
            override val room: Room.Playing,
            override val username: String,
            override val playerIndex: Int,
            var connection: Connection?,
        ): Room.Player {
            var scoreExplained = 0
            var scoreGuessed = 0
        }
    }

    class Results(
        override val id: String,
        override val settings: Settings,
        playerListToProcess: List<Playing.Player>, // TODO: Add other explanation results (history of explanation, etc.)
    ): Room {
        override val players: List<Player> =
            playerListToProcess.map {
                it.connection?.let { connection -> connection.player = null }
                Player(
                    room = this,
                    username = it.username,
                    playerIndex = it.playerIndex,
                    scoreExplained = it.scoreExplained,
                    scoreGuessed = it.scoreGuessed
                )
            }

        class Player(
            override val room: Room.Results,
            override val username: String,
            override val playerIndex: Int,
            val scoreExplained: Int,
            val scoreGuessed: Int,
        ): Room.Player
    }
}