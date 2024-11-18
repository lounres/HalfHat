package dev.lounres.halfhat.api.server

import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.KoneList
import kotlinx.serialization.Serializable


@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
public annotation class BetterBeReplaced

public object ServerApi {
    @Serializable
    public sealed interface WordsSource {
        @Serializable
        public data object Players : WordsSource
//        @Serializable
//        public data object HostDictionary: WordsSource
        @Serializable
        public data class ServerDictionary(
            val name: String,
        ) : WordsSource
    }
    
    @Serializable
    public data class PlayerDescription(
        val name: String,
        val isOnline: Boolean,
    )
    
    @Serializable
    public data class RoomDescription(
        val name: String,
        val playersList: KoneList<PlayerDescription>,
        val state: RoomStateType,
    )
    
    @Serializable
    public enum class RoomStateType {
        GameInitialisation,
        RoundWaiting,
        RoundPreparation,
        RoundExplanation,
        RoundLastGuess,
        RoundEditing,
        GameResults,
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
    public data class Settings(
        val preparationTimeSeconds: UInt,
        val explanationTimeSeconds: UInt,
        val finalGuessTimeSeconds: UInt,
        val strictMode: Boolean,
        val gameEndCondition: GameStateMachine.GameEndCondition,
//    val wordsSource: WordsSource,
    )
    
    public object OnlineGame {
        @Serializable
        public sealed interface Role {
            public val name: String
            public val isHost: Boolean
            
            @Serializable
            public enum class RoundRole {
                Player, Speaker, Listener,
            }
            
            @Serializable
            public data class GameInitialisation(
                override val name: String,
                override val isHost: Boolean,
            ) : Role
            
            @Serializable
            public data class RoundWaiting(
                override val name: String,
                override val isHost: Boolean,
                val roundRole: RoundRole,
            ) : Role
            
            @Serializable
            public data class RoundPreparation(
                override val name: String,
                override val isHost: Boolean,
                val roundRole: RoundRole,
            ) : Role
            
            @Serializable
            public data class RoundExplanation(
                override val name: String,
                override val isHost: Boolean,
                val roundRole: RoundRole,
            ) : Role
            
            @Serializable
            public data class RoundLastGuess(
                override val name: String,
                override val isHost: Boolean,
                val roundRole: RoundRole,
            ) : Role
            
            @Serializable
            public data class RoundEditing(
                override val name: String,
                override val isHost: Boolean,
                val roundRole: RoundRole,
            ) : Role
            
            @Serializable
            public data class GameResults(
                override val name: String,
                override val isHost: Boolean,
            ) : Role
        }
        
        @Serializable
        public sealed interface State {
            public val role: Role
            
            @Serializable
            public data class GameInitialisation(
                override val role: Role.GameInitialisation,
                val playersList: KoneList<PlayerDescription>,
                val userIndex: UInt,
                val settingsBuilder: SettingsBuilder,
            ) : State
            
            @Serializable
            public data class RoundWaiting(
                override val role: Role.RoundWaiting,
                val playersList: KoneList<PlayerDescription>,
                val userIndex: UInt,
                val settings: Settings,
                val roundNumber: UInt,
                val cycleNumber: UInt,
                val speakerIndex: UInt,
                val listenerIndex: UInt,
                val explanationScores: KoneList<UInt>,
                val guessingScores: KoneList<UInt>,
                val speakerReady: Boolean,
                val listenerReady: Boolean,
            ) : State
            
            @Serializable
            public data class RoundPreparation(
                override val role: Role.RoundPreparation,
                val playersList: KoneList<PlayerDescription>,
                val userIndex: UInt,
                val settings: Settings,
                val roundNumber: UInt,
                val cycleNumber: UInt,
                val speakerIndex: UInt,
                val listenerIndex: UInt,
                val millisecondsLeft: UInt,
                val explanationScores: KoneList<UInt>,
                val guessingScores: KoneList<UInt>,
            ) : State
            
            @Serializable
            public data class RoundExplanation(
                override val role: Role.RoundExplanation,
                val playersList: KoneList<PlayerDescription>,
                val userIndex: UInt,
                val settings: Settings,
                val roundNumber: UInt,
                val cycleNumber: UInt,
                val speakerIndex: UInt,
                val listenerIndex: UInt,
                val millisecondsLeft: UInt,
                val explanationScores: KoneList<UInt>,
                val guessingScores: KoneList<UInt>,
            ) : State
            
            @Serializable
            public data class RoundLastGuess(
                override val role: Role.RoundLastGuess,
                val playersList: KoneList<PlayerDescription>,
                val userIndex: UInt,
                val settings: Settings,
                val roundNumber: UInt,
                val cycleNumber: UInt,
                val speakerIndex: UInt,
                val listenerIndex: UInt,
                val millisecondsLeft: UInt,
                val explanationScores: KoneList<UInt>,
                val guessingScores: KoneList<UInt>,
            ) : State
            
            @Serializable
            public data class RoundEditing(
                override val role: Role.RoundEditing,
                val playersList: KoneList<PlayerDescription>,
                val userIndex: UInt,
                val settings: Settings,
                val roundNumber: UInt,
                val cycleNumber: UInt,
                val speakerIndex: UInt,
                val listenerIndex: UInt,
                val wordsToEdit: KoneList<GameStateMachine.WordExplanation>,
                val explanationScores: KoneList<UInt>,
                val guessingScores: KoneList<UInt>,
            ) : State
            
            @Serializable
            public data class GameResults(
                override val role: Role.GameResults,
                val playersList: KoneList<String>,
                val userIndex: UInt,
                val results: KoneList<GameStateMachine.GameResult>,
            ) : State
        }
    }
    
    @Serializable
    public sealed interface Signal {
        @Serializable
        @BetterBeReplaced
        public data object UnspecifiedError : Signal
        
        @Serializable
        public data class Error(val errorId: UInt) : Signal
        
        @Serializable
        public data class RoomInfo(val info: RoomDescription) : Signal
        
        @Serializable
        public data class OnlineGameStateUpdate(val state: OnlineGame.State) : Signal
    }
}