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
        roundsCount = 1,
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
        var connection: Connection?
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
            override val room: Room,
            override val username: String,
            override val playerIndex: Int,
            override var connection: Connection?,
        ): Room.Player
    }

    class Playing(
        override val id: String,
        override val settings: Settings,
        playerListToProcess: List<Player>,
    ): Room {
        override val players: List<Player> =
            playerListToProcess.map {
                Player(room = this, username = it.username, playerIndex = it.playerIndex, connection = it.connection)
                    .also {
                        val connection = it.connection
                        if (connection != null) connection.player = it
                    }
            }

        class Player(
            override val room: Room,
            override val username: String,
            override val playerIndex: Int,
            override var connection: Connection?,
        ): Room.Player {
            var scoreExplained = 0
            var scoreGuessed = 0
        }
    }
}