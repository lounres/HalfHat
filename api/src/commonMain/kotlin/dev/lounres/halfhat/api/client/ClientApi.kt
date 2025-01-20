package dev.lounres.halfhat.api.client

import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import kotlinx.serialization.Serializable


public object ClientApi {
    @Serializable
    public sealed interface WordsSource {
        @Serializable
        public data object Players : WordsSource
//        @Serializable
//        public data object HostDictionary: WordsSource
        @Serializable
        public data class ServerDictionary(
            val name: String, // TODO: Replace with id
        ) : WordsSource
    }
    
    @Serializable
    public data class SettingsBuilder(
        val preparationTimeSeconds: UInt,
        val explanationTimeSeconds: UInt,
        val finalGuessTimeSeconds: UInt,
        val strictMode: Boolean,
        val cachedEndConditionWordsNumber: UInt,
        val cachedEndConditionCyclesNumber: UInt,
        val gameEndConditionType: GameStateMachine.GameEndCondition.Type,
        val wordsSource: WordsSource,
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
            public data class UpdateSettings(val settingsBuilder: SettingsBuilder) : OnlineGame
            
            @Serializable
            public data object InitializeGame : OnlineGame
            
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