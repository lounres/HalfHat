package dev.lounres.thetruehat.server.model

import dev.lounres.thetruehat.api.models.RoomDescription
import dev.lounres.thetruehat.api.models.UserGameState


fun Room.Waiting.getOrCreatePlayerByNickname(nickname: String): Room.Waiting.Player =
    players.find { it.username == nickname } ?: addPlayer(nickname, null)

fun Room.descriptionFor(playerIndex: Int): RoomDescription =
    RoomDescription(
        id = id,
        settings = settings,
        phase = when(this) {
            is Room.Waiting -> RoomDescription.Phase.WaitingForPlayers(players.map { RoomDescription.Player(it.username, it.online) })
            is Room.Playing -> TODO()
        }
    )

val Room.Player.online: Boolean get() = connection != null

val Room.Player.state: UserGameState
    get() = UserGameState(
        roomDescription = room.descriptionFor(playerIndex = playerIndex),
        username = username,
        userIndex = playerIndex,
    )