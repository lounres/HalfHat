package dev.lounres.thetruehat.server.model

import dev.lounres.thetruehat.api.defaultSettings
import dev.lounres.thetruehat.api.models.Settings
import dev.lounres.thetruehat.api.models.UserGameState
import dev.lounres.thetruehat.api.signals.ServerSignal
import dev.lounres.thetruehat.server.Connection
import dev.lounres.thetruehat.server.availableDictionaries
import dev.lounres.thetruehat.server.send
import kotlinx.coroutines.Job
import kotlinx.datetime.Instant
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random


// TODO: Refactor the rooms' constructors
sealed interface Room {
    val id: String
    val settings: Settings
    val players: List<Player>

    sealed interface Player {
        val room: Room
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
            val username: String,
            val playerIndex: Int,
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

        val freshWords: MutableList<String> =
            when(val wordsSource = settings.wordsSource) {
                is Settings.WordsSource.ServerDictionary -> availableDictionaries.first { it.id == wordsSource.id }.words.let { with(Random) { it.generateWords(settings.wordsCount) } }
            }
        val wordsToEdit = mutableListOf<UserGameState.WordExplanationResult>()
        val usedWords = mutableListOf<UserGameState.WordExplanationResult>()

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
            val username: String,
            val playerIndex: Int,
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
        private var connections: MutableList<Connection>? = mutableListOf()

        override val players: List<Player> =
            playerListToProcess
                .sortedByDescending { it.scoreExplained + it.scoreGuessed }
                .map {
                    val newPlayer = Player(
                        room = this,
                        username = it.username,
                        playerIndex = it.playerIndex,
                        scoreExplained = it.scoreExplained,
                        scoreGuessed = it.scoreGuessed
                    )
                    it.connection?.let { connection ->
                        connections?.add(connection)
                        connection.player = newPlayer
                    }
                    newPlayer
                }

        init {
            for (connection in connections!!) with(connection.socketSession) {
                ServerSignal.StatusUpdate(userGameState = connection.player!!.state).send()
            }
            connections = null
        }

        class Player(
            override val room: Room.Results,
            val username: String,
            val playerIndex: Int,
            val scoreExplained: Int,
            val scoreGuessed: Int,
        ): Room.Player

        class Spectator(
            override val room: Results,
        ): Room.Player
    }
}