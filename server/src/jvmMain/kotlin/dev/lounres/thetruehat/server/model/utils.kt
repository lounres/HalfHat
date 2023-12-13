package dev.lounres.thetruehat.server.model

import dev.lounres.thetruehat.api.models.RoomDescription
import dev.lounres.thetruehat.api.models.UserGameState
import dev.lounres.thetruehat.server.Connection


fun Room.Waiting.getOrCreatePlayerByNickname(nickname: String): Room.Waiting.Player =
    players.find { it.username == nickname } ?: addPlayer(nickname, null)

fun Room.descriptionFor(playerIndex: Int): RoomDescription =
    RoomDescription(
        id = id,
        settings = settings,
        phase = when(this) {
            is Room.Waiting ->
                RoomDescription.Phase.WaitingForPlayers(
                    currentPlayersList = players.map { RoomDescription.Player(it.username, it.online) }
                )
            is Room.Playing ->
                RoomDescription.Phase.GameInProgress(
                    palyersList = players.map { RoomDescription.Player(it.username, it.online) },
                    timetable = emptyList(), // TODO: Implement it actually
                    speaker = 0, // TODO: Implement it actually
                    listener = 0, // TODO: Implement it actually
                    unitsUntilEnd = RoomDescription.UnitsUntilEnd.Words(100), // TODO: Implement it actually
                    roundPhase = RoomDescription.RoundPhase.WaitingForPlayersToBeReady, // TODO: Implement it actually
                )
            is Room.Results -> TODO()
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