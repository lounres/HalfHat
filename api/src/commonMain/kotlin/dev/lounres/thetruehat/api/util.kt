package dev.lounres.thetruehat.api

//import site.m20sch57.thetruehat.api.Room.*
//import site.m20sch57.thetruehat.api.Settings.*
//
//
//fun String.toExplanationResultStrict() : ExplanationResult = when (this) {
//    "explained" -> ExplanationResult.explained
//    "mistake" -> ExplanationResult.mistake
//    "notExplained" -> ExplanationResult.notExplained
//    else -> throw IllegalArgumentException("The string doesn't represent an ExplanationResult value: $this")
//}
//
//fun String.toStageStrict() : Stage = when (this) {
//    "wait" -> Stage.wait
//    "prepare_wordCollection" -> Stage.prepare_wordCollection
//    "prepare_pairMatching" -> Stage.prepare_pairMatching
//    "play_wait" -> Stage.play_wait
//    "play_explanation" -> Stage.play_explanation
//    "play_edit" -> Stage.play_edit
//    "end" -> Stage.end
//    else -> throw IllegalArgumentException("The string doesn't represent an Room.Stage value: $this")
//}
//
//fun String.toTermConditionStrict() : TermCondition = when (this) {
//    "words" -> TermCondition.words
//    "rounds" -> TermCondition.rounds
//    else -> throw IllegalArgumentException("The string doesn't represent an Setting.TermCondition value: $this")
//}
//
//fun String.toWordsetTypeStrict() : WordsetType = when (this) {
//    "serverDictionary" -> WordsetType.serverDictionary
//    "hostDictionary" -> WordsetType.hostDictionary
//    "playerWords" -> WordsetType.playerWords
//    else -> throw IllegalArgumentException("The string doesn't represent an Settings.WordsetType value: $this")
//}
//
//fun String.toPairMatchingStrict() : PairMatching = when (this) {
//    "random" -> PairMatching.random
//    "host" -> PairMatching.host
//    else -> throw IllegalArgumentException("The string doesn't represent an Settings.PairMatching value: $this")
//}