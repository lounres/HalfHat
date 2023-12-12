package dev.lounres.thetruehat.api.models

import kotlinx.serialization.Serializable


@Serializable
public data class UserGameState(
    val roomDescription: RoomDescription,
    val username: String,
    val userIndex: Int,
)
