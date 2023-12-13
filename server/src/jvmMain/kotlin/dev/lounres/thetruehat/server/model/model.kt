package dev.lounres.thetruehat.server.model

import dev.lounres.thetruehat.api.models.Settings
import dev.lounres.thetruehat.server.Connection
import java.util.concurrent.CopyOnWriteArrayList


val defaultSettings: Settings =
    Settings(
        countdownTime = 3,
        explanationTime = 40,
        finalGuessTime = 3,
        strictMode = false,
        gameEndCondition = Settings.GameEndCondition.Words,
        wordsCount = 100,
        roundsCount = 10,
        wordsSource = Settings.WordsSource.ServerDictionary(dictionaryId = 0)
    )

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
        override val players: List<Player> =
            playerListToProcess.mapNotNull {
                val connection = it.connection
                if (connection != null) {
                    val newPlayer = Player(
                        room = this,
                        username = it.username,
                        playerIndex = it.playerIndex,
                        connection = connection
                    )
                    connection.player = newPlayer
                    newPlayer
                } else null
            }

//        val words

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
        playerListToProcess: List<Playing.Player>,
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