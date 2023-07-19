package dev.lounres.thetruehat.api.signals

//import kotlinx.serialization.Serializable
//import site.m20sch57.thetruehat.api.*
//
//@Serializable
//data class cJoinRoom(
//    val key: String,
//    val username: String,
//    val timeZoneOffset: Int
//)
//
//@Serializable
//object cLeaveRoom
//
//@Serializable
//data class sPlayerJoined (
//    val username: String,
//    val playerList: List<Player>,
//    val host: String,
//)
//
//@Serializable
//data class sPlayerLeft (
//    val username: String,
//    val playerList: List<Player>,
//    val host: String
//)
//
//@Serializable
//data class sYouJoined (
//    val key: String,
//    val stage: Room.Stage,
//    val playerList: List<Player> = listOf(),
//    val host: String,
//    val settings: Settings
//)
//
//@Serializable
//data class sNewSettings (
//    val settings: Settings
//)
//
//@Serializable
//data class cApplySettings (
//    val settings: Settings
//)
//
//@Serializable
//data class sFailure (
//    val request: String,
//    val message: String
//)
//
//@Serializable
//data class cEndStage (
//    val stage: Room.Stage
//)
//
//@Serializable
//data class sStageStarted (
//    val stage: Room.Stage
//)
//
//@Serializable
//data class cWordsReady (
//    val words: List<String>
//)
//
//@Serializable
//data class cConstructPair (
//    val username1: String,
//    val username2: String
//)
//
//@Serializable
//data class sPairConstructed (
//    val username1: String,
//    val username2: String,
//    val pairs: List<Pair<Player, Player>>
//)
//
//@Serializable
//data class cDestroyPair (
//    val username1: String,
//    val username2: String
//)
//
//@Serializable
//data class sPairDestroyed (
//    val username1: String,
//    val username2: String,
//    val pairs: List<Pair<Player, Player>>
//)
//
//@Serializable
//data class sGameStarted (
//    val timetable: List<TimeTableEntry>,
//    val speaker: String,
//    val listener: String,
//    val wordsLeft: Int,
//    val roundsLeft: Int
//)
//
//@Serializable
//data class sNextTurn (
//    val timetable: List<TimeTableEntry>,
//    val speaker: String,
//    val listener: String,
//    val wordsLeft: Int,
//    val roundsLeft: Int,
//    val words: List<String>
//)
//
//@Serializable
//object cListenerReady
//
//@Serializable
//object cSpeakerReady
//
//@Serializable
//data class sExplanationStarted (
//    val startTime: Int
//)
//
//@Serializable
//data class sNewWord (
//    val word: String
//)
//
//@Serializable
//data class sWordExplanationEnded (
//    val cause: ExplanationResult,
//    val wordsLeft: Int
//)
//
//@Serializable
//data class sExplanationEnded (
//    val wordsLeft: Int
//)
//
//@Serializable
//data class sWordsToEdit (
//    val editWords: List<ExplanationRecord>
//)
//
//@Serializable
//data class cWordsEdited (
//    val editWords: List<ExplanationRecord>
//)
//
//@Serializable
//object cEndGame
//
//@Serializable
//data class sGameEnded (
//    val nextKey: String,
//    val results: List<PlayerResult>
//)