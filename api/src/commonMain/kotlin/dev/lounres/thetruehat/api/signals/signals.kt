package dev.lounres.thetruehat.api.signals

import dev.lounres.thetruehat.api.models.RoomDescription
import dev.lounres.thetruehat.api.models.SettingsUpdate
import dev.lounres.thetruehat.api.models.UserGameState
import kotlinx.serialization.Serializable


@Serializable
public sealed interface ServerSignal {
    public val userGameState: UserGameState?


    @Serializable
    public data class StatusUpdate(
        override val userGameState: UserGameState?,
    ): ServerSignal

    @Serializable
    public data class RequestError(
        override val userGameState: UserGameState?,
        val errorMessage: String,
    ): ServerSignal

    @Serializable
    public data class ProvideFreeRoomId(
        override val userGameState: UserGameState?,
        val freeRoomId: String
    ): ServerSignal

    @Serializable
    public data class ProvideRoomResults(
        override val userGameState: UserGameState?,
        val gameResult: RoomDescription.GameResult,
    ): ServerSignal
}

@Serializable
public sealed interface ClientSignal {
    @Serializable
    public data object RequestFreeRoomId: ClientSignal

    @Serializable
    public data class JoinRoom(val roomId: String, val nickname: String): ClientSignal

    @Serializable
    public data object LeaveRoom: ClientSignal

    @Serializable
    public data class UpdateSettings(val settingsUpdate: SettingsUpdate): ClientSignal

    @Serializable
    public data object StartGame: ClientSignal

    @Serializable
    public data object EndGame: ClientSignal

    @Serializable
    public data object ReadyForTheRound: ClientSignal

    @Serializable
    public data class ExplanationResult(val result: RoomDescription.WordExplanationResult.State): ClientSignal

    @Serializable
    public data class SubmitResults(val results: List<RoomDescription.WordExplanationResult>): ClientSignal
}