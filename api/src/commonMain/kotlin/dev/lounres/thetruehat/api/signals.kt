package dev.lounres.thetruehat.api

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
    public data class GameStarts(
        override val userGameState: UserGameState?,
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
    public data object StartGame: ClientSignal

    @Serializable
    public data class UpdateSettings(val settingsUpdate: SettingsUpdate): ClientSignal
}

//@Serializable
//@SerialName("cJoinRoom")
//public data class CJoinRoomSignal(
//    val key: String,
//    val username: String,
//    val timeZoneOffset: Long,
//): ClientSignal
//
//@Serializable
//@SerialName("cLeaveRoom")
//public data object CLeaveRoomSignal: ClientSignal
//
//@Serializable
//@SerialName("sPlayerJoined")
//public data class SPlayerJoinedSignal(
//    val username: String,
//    val playersList: List<RoomDescription.Player>,
//    val host: Int,
//): ServerSignal
//
//@Serializable
//@SerialName("sPlayerLeft")
//public data class SPlayerLeftSignal(
//    val username: String,
//    val playersList: List<RoomDescription.Player>,
//    val host: Int,
//): ServerSignal
//
//@Serializable
//@SerialName("sYouJoined")
//public data class SYouJoinedSignal(
//    val room: RoomDescription,
//): ServerSignal
//
//@Serializable
//@SerialName("sNewSettings")
//public data class SNewSettingsSignal(
//    val settings: Settings,
//): ServerSignal
//
//@Serializable
//@SerialName("cApplySettings")
//public data class CApplySettingsSignal(
//    val settings: SettingsUpdate
//): ClientSignal
//
//@Serializable
//@SerialName("sFailure")
//public data class SFailureSignal(
//    val message: String,
//    val log: String,
//): ServerSignal
//
//@Serializable
//@SerialName("cStartWordCollection")
//public data object CStartWordCollectionSignal: ClientSignal
//
//@Serializable
//@SerialName("sWordCollectionStarted")
//public data object SWordCollectionStartedSignal: ServerSignal
//
//@Serializable
//@SerialName("cWordsReady")
//public data class CWordsReadySignal(
//    val words: List<String>
//): ClientSignal
//
//@Serializable
//@SerialName("cStartGame")
//public data object CStartGameSignal: ClientSignal
//
//@Serializable
//@SerialName("sGameStarted")
//public data class SGameStartedSignal(
//    val timetable: List<RoomDescription.TimetableEntry>,
//    val speaker: Int,
//    val listener: Int,
//    val termination: Termination,
//): ServerSignal
//
//@Serializable
//@SerialName("sNextTurn")
//public data class SNextTurnSignal(
//    val timetable: List<TimetableEntry>,
//    val speaker: Int,
//    val listener: Int,
//    val termination: Termination,
//    val words: List<WordExplanation>,
//): ServerSignal
//
//@Serializable
//@SerialName("cListenerReady")
//public data object CListenerReadySignal: ClientSignal
//
//@Serializable
//@SerialName("cSpeakerReady")
//public data object CSpeakerReadySignal: ClientSignal
//
//@Serializable
//@SerialName("sExplanationStarted")
//public data class SExplanationStartedSignal(
//    val startTime: Long,
//): ServerSignal
//
//@Serializable
//@SerialName("sNewWord")
//public data class SNewWordSignal(
//    val word: String,
//): ServerSignal
//
//@Serializable
//@SerialName("cEndWordExplanation")
//public data class CEndWordExplanationSignal(
//    val cause: WordExplanation.State
//): ClientSignal
//
//@Serializable
//@SerialName("sWordExplanationEnded")
//public data class SWordExplanationEndedSignal(
//    val cause: WordExplanation.State,
//    val wordsCount: Int,
//): ServerSignal
//
//@Serializable
//@SerialName("sExplanationEnded")
//public data class SExplanationEndedSignal(
//    val wordsCount: Int?,
//): ServerSignal
//
//@Serializable
//@SerialName("sWordsToEdit")
//public data class SWordsToEditSignal(
//    val editWords: List<WordExplanation>
//): ServerSignal
//
//@Serializable
//@SerialName("cWordsEdited")
//public data class CWordsEditedSignal(
//    val editWords: List<WordExplanation>
//): ClientSignal
//
//@Serializable
//@SerialName("cEndGame")
//public data object CEndGameSignal: ClientSignal
//
//@Serializable
//@SerialName("sGameEnded")
//public data class SGameEnded(
//    val nextKey: String,
//    val results: List<GameResult>
//): ServerSignal