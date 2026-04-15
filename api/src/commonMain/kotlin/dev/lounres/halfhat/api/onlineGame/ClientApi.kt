package dev.lounres.halfhat.api.onlineGame

import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import kotlinx.serialization.Serializable


public object ClientApi {
    @Serializable
    public sealed interface WordsSource {
        @Serializable
        public data object Players : WordsSource
        @Serializable
        public data class HostDictionary(
            val words: KoneList<String>,
        ): WordsSource
        @Serializable
        public data class ServerDictionary(
            val dictionaryId: DictionaryId,
        ) : WordsSource
    }

    @Serializable
    public data class SettingsBuilderPatch(
        val preparationTimeSeconds: UInt?,
        val explanationTimeSeconds: UInt?,
        val finalGuessTimeSeconds: UInt?,
        val strictMode: Boolean?,
        val cachedEndConditionWordsNumber: UInt?,
        val cachedEndConditionCyclesNumber: UInt?,
        val gameEndConditionType: GameStateMachine.GameEndCondition.Type?,
        val wordsSource: WordsSource?,
        val showWordsStatistic: Boolean?,
        val showLeaderboardPermutation: Boolean?,
    )
    
    @Serializable
    public sealed interface Signal {
        
        @Serializable
        public data object FetchFreeRoomId : Signal
        
        @Serializable
        public data class FetchRoomInfo(val roomId: String) : Signal
        
        @Serializable
        public sealed interface OnlineGame : Signal {
            @Serializable
            public data class JoinRoom(val roomId: String, val playerName: String) : OnlineGame
            
            @Serializable
            public data object LeaveRoom : OnlineGame

            @Serializable
            public data object RequestAvailableDictionaries : OnlineGame
            
            @Serializable
            public data class UpdateSettings(val settingsBuilderPatch: SettingsBuilderPatch) : OnlineGame
            
            @Serializable
            public data object InitializeGame : OnlineGame
            
            @Serializable
            public data class SubmitWords(val words: /* TODO: Replace with `KoneSet` with serializer */ KoneList<String>): OnlineGame
            
            @Serializable
            public data object SpeakerReady : OnlineGame
            
            @Serializable
            public data object ListenerReady : OnlineGame
            
            @Serializable
            public data class WordExplanationState(val state: GameStateMachine.WordExplanation.State) : OnlineGame
            
            @Serializable
            public data class UpdateWordsExplanationResults(val newExplanationResults: KoneList<GameStateMachine.WordExplanation>) : OnlineGame
            
            @Serializable
            public data object ConfirmWordsExplanationResults : OnlineGame
            
            @Serializable
            public data object FinishGame : OnlineGame
        }
    }
}