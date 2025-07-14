package dev.lounres.halfhat.api.onlineGame

import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
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
            public val name: String,
        ) : WordsSource
    }
    
    @Serializable
    public data class PlayerDescription(
        public val name: String,
        public val isOnline: Boolean,
    )
    
    @Serializable
    public data class RoomDescription(
        public val name: String,
        public val playersList: KoneList<PlayerDescription>,
        public val state: RoomStateType,
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
        public val preparationTimeSeconds: UInt,
        public val explanationTimeSeconds: UInt,
        public val finalGuessTimeSeconds: UInt,
        public val strictMode: Boolean,
        public val cachedEndConditionWordsNumber: UInt,
        public val cachedEndConditionCyclesNumber: UInt,
        public val gameEndConditionType: GameStateMachine.GameEndCondition.Type,
        public val wordsSource: WordsSource,
    )
    
    @Serializable
    public data class Settings(
        public val preparationTimeSeconds: UInt,
        public val explanationTimeSeconds: UInt,
        public val finalGuessTimeSeconds: UInt,
        public val strictMode: Boolean,
        public val gameEndCondition: GameStateMachine.GameEndCondition,
        public val wordsSource: WordsSource,
    )
    
    public object OnlineGame {
        @Serializable
        public sealed interface Role {
            public val name: String
            public val userIndex: UInt
            public val isHost: Boolean
            
            @Serializable
            public data class GameInitialisation(
                override val name: String,
                override val userIndex: UInt,
                override val isHost: Boolean,
            ) : Role
            
            @Serializable
            public data class RoundWaiting(
                override val name: String,
                override val userIndex: UInt,
                override val isHost: Boolean,
                public val roundRole: RoundRole,
            ) : Role {
                @Serializable
                public enum class RoundRole {
                    Player, Speaker, Listener,
                }
            }
            
            @Serializable
            public data class RoundPreparation(
                override val name: String,
                override val userIndex: UInt,
                override val isHost: Boolean,
                public val roundRole: RoundRole,
            ) : Role {
                @Serializable
                public enum class RoundRole {
                    Player, Speaker, Listener,
                }
            }
            
            @Serializable
            public data class RoundExplanation(
                override val name: String,
                override val userIndex: UInt,
                override val isHost: Boolean,
                public val roundRole: RoundRole,
            ) : Role {
                @Serializable
                public sealed interface RoundRole {
                    public data object Player : RoundRole
                    public data class Speaker(val currentWord: String) : RoundRole
                    public data object Listener : RoundRole
                }
            }
            
            @Serializable
            public data class RoundLastGuess(
                override val name: String,
                override val userIndex: UInt,
                override val isHost: Boolean,
                public val roundRole: RoundRole,
            ) : Role {
                @Serializable
                public sealed interface RoundRole {
                    public data object Player : RoundRole
                    public data class Speaker(val currentWord: String) : RoundRole
                    public data object Listener : RoundRole
                }
            }
            
            @Serializable
            public data class RoundEditing(
                override val name: String,
                override val userIndex: UInt,
                override val isHost: Boolean,
                public val roundRole: RoundRole,
            ) : Role {
                @Serializable
                public sealed interface RoundRole {
                    public data object Player : RoundRole
                    public data class Speaker(
                        public val wordsToEdit: KoneList<GameStateMachine.WordExplanation>,
                    ) : RoundRole
                    public data object Listener : RoundRole
                }
            }
            
            @Serializable
            public data class GameResults(
                override val name: String,
                override val userIndex: UInt,
                override val isHost: Boolean,
            ) : Role
        }
        
        @Serializable
        public sealed interface State {
            public val role: Role
            
            @Serializable
            public data class GameInitialisation(
                override val role: Role.GameInitialisation,
                public val playersList: KoneList<PlayerDescription>,
                public val settingsBuilder: SettingsBuilder,
            ) : State
            
            @Serializable
            public data class RoundWaiting(
                override val role: Role.RoundWaiting,
                public val playersList: KoneList<PlayerDescription>,
                public val settings: Settings,
                public val roundNumber: UInt,
                public val cycleNumber: UInt,
                public val speakerIndex: UInt,
                public val listenerIndex: UInt,
                public val explanationScores: KoneList<UInt>,
                public val guessingScores: KoneList<UInt>,
                public val speakerReady: Boolean,
                public val listenerReady: Boolean,
            ) : State
            
            @Serializable
            public data class RoundPreparation(
                override val role: Role.RoundPreparation,
                public val playersList: KoneList<PlayerDescription>,
                public val settings: Settings,
                public val roundNumber: UInt,
                public val cycleNumber: UInt,
                public val speakerIndex: UInt,
                public val listenerIndex: UInt,
                public val millisecondsLeft: UInt,
                public val explanationScores: KoneList<UInt>,
                public val guessingScores: KoneList<UInt>,
            ) : State
            
            @Serializable
            public data class RoundExplanation(
                override val role: Role.RoundExplanation,
                public val playersList: KoneList<PlayerDescription>,
                public val settings: Settings,
                public val roundNumber: UInt,
                public val cycleNumber: UInt,
                public val speakerIndex: UInt,
                public val listenerIndex: UInt,
                public val millisecondsLeft: UInt,
                public val explanationScores: KoneList<UInt>,
                public val guessingScores: KoneList<UInt>,
            ) : State
            
            @Serializable
            public data class RoundLastGuess(
                override val role: Role.RoundLastGuess,
                public val playersList: KoneList<PlayerDescription>,
                public val settings: Settings,
                public val roundNumber: UInt,
                public val cycleNumber: UInt,
                public val speakerIndex: UInt,
                public val listenerIndex: UInt,
                public val millisecondsLeft: UInt,
                public val explanationScores: KoneList<UInt>,
                public val guessingScores: KoneList<UInt>,
            ) : State
            
            @Serializable
            public data class RoundEditing(
                override val role: Role.RoundEditing,
                public val playersList: KoneList<PlayerDescription>,
                public val settings: Settings,
                public val roundNumber: UInt,
                public val cycleNumber: UInt,
                public val speakerIndex: UInt,
                public val listenerIndex: UInt,
                public val explanationScores: KoneList<UInt>,
                public val guessingScores: KoneList<UInt>,
            ) : State
            
            @Serializable
            public data class GameResults(
                override val role: Role.GameResults,
                public val playersList: KoneList<String>,
                public val results: KoneList<GameStateMachine.GameResult>,
            ) : State
        }
        
        @Serializable
        public sealed interface Error {
            @Serializable
            @BetterBeReplaced
            public data object UnspecifiedError : Error
            
            @Serializable
            public data object AttachmentIsDenied : Error
            @Serializable
            public data object AttachmentIsAlreadySevered : Error
            @Serializable
            public data object NoAttachmentWhenItIsNeeded : Error
            @Serializable
            public data object NotHostChangingGameSettings : Error
            @Serializable
            public data object CannotUpdateGameSettingsAfterInitialization : Error
            @Serializable
            public data object NotEnoughPlayersForInitialization : Error
            @Serializable
            public data object CannotFindDictionaryByID : Error
            @Serializable
            public data object CannotInitializationGameSettingsAfterInitialization : Error
            @Serializable
            public data object CannotSetSpeakerReadinessNotDuringRoundWaiting : Error
            @Serializable
            public data object CannotSetListenerReadinessNotDuringRoundWaiting : Error
            @Serializable
            public data object CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting : Error
            @Serializable
            public data object CannotUpdateRoundInfoNotDuringTheRound : Error
            @Serializable
            public data object CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess : Error
            @Serializable
            public data object CannotUpdateWordExplanationResultsNotDuringRoundEditing : Error
            @Serializable
            public data object CannotConfirmWordExplanationResultsNotDuringRoundEditing : Error
            @Serializable
            public data object CannotFinishGameNotDuringRoundWaiting : Error
        }
    }
    
    @Serializable
    public sealed interface Signal {
        @Serializable
        public data class RoomInfo(val info: RoomDescription) : Signal
        
        @Serializable
        public data class OnlineGameStateUpdate(val state: OnlineGame.State) : Signal
        
        @Serializable
        public data class OnlineGameError(val error: OnlineGame.Error) : Signal
    }
}