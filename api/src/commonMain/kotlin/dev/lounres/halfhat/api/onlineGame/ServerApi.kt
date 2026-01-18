package dev.lounres.halfhat.api.onlineGame

import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.array.KoneBooleanArray
import dev.lounres.kone.collections.array.KoneUIntArray
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
            public val id: String,
//            public val name: String,
        ) : WordsSource
    }
    
    @Serializable
    public data class PlayerDescription(
        public val name: String,
        public val isOnline: Boolean,
        public val isHost: Boolean,
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
        PlayersWordsCollection,
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
        public sealed interface PlayerDescription {
            public val name: String
            public val userIndex: UInt
            public val isOnline: Boolean
            public val isHost: Boolean
            
            @Serializable
            public data class GameInitialisation(
                override val name: String,
                override val userIndex: UInt,
                override val isOnline: Boolean,
                override val isHost: Boolean,
            ) : PlayerDescription
            
            @Serializable
            public data class PlayersWordsCollection(
                override val name: String,
                override val userIndex: UInt,
                override val isOnline: Boolean,
                override val isHost: Boolean,
                public val finishedWordsCollection: Boolean,
            ) : PlayerDescription
            
            @Serializable
            public sealed interface Round : PlayerDescription {
                public val roundRole: RoundRole
                public val scoreExplained: UInt
                public val scoreGuessed: UInt
                public val scoreSum: UInt
                
                @Serializable
                public enum class RoundRole {
                    Player, Speaker, Listener,
                }
                
                @Serializable
                public data class Waiting(
                    override val name: String,
                    override val userIndex: UInt,
                    override val isOnline: Boolean,
                    override val isHost: Boolean,
                    override val roundRole: RoundRole,
                    override val scoreExplained: UInt,
                    override val scoreGuessed: UInt,
                    override val scoreSum: UInt,
                ) : Round
                
                @Serializable
                public data class Preparation(
                    override val name: String,
                    override val userIndex: UInt,
                    override val isOnline: Boolean,
                    override val isHost: Boolean,
                    override val roundRole: RoundRole,
                    override val scoreExplained: UInt,
                    override val scoreGuessed: UInt,
                    override val scoreSum: UInt,
                ) : Round
                
                @Serializable
                public data class Explanation(
                    override val name: String,
                    override val userIndex: UInt,
                    override val isOnline: Boolean,
                    override val isHost: Boolean,
                    override val roundRole: RoundRole,
                    override val scoreExplained: UInt,
                    override val scoreGuessed: UInt,
                    override val scoreSum: UInt,
                ) : Round
                
                @Serializable
                public data class LastGuess(
                    override val name: String,
                    override val userIndex: UInt,
                    override val isOnline: Boolean,
                    override val isHost: Boolean,
                    override val roundRole: RoundRole,
                    override val scoreExplained: UInt,
                    override val scoreGuessed: UInt,
                    override val scoreSum: UInt,
                ) : Round
                
                @Serializable
                public data class Editing(
                    override val name: String,
                    override val userIndex: UInt,
                    override val isOnline: Boolean,
                    override val isHost: Boolean,
                    override val roundRole: RoundRole,
                    override val scoreExplained: UInt,
                    override val scoreGuessed: UInt,
                    override val scoreSum: UInt,
                ) : Round
            }
            
            @Serializable
            public data class GameResults(
                override val name: String,
                override val userIndex: UInt,
                override val isOnline: Boolean,
                override val isHost: Boolean,
                public val scoreExplained: UInt,
                public val scoreGuessed: UInt,
                public val scoreSum: UInt,
            ) : PlayerDescription
        }
        
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
                public val isStartAvailable: Boolean,
                public val areSettingsChangeable: Boolean,
            ) : Role
            
            @Serializable
            public data class PlayersWordsCollection(
                override val name: String,
                override val userIndex: UInt,
                override val isHost: Boolean,
                public val finishedWordsCollection: Boolean,
            ) : Role
            
            @Serializable
            public sealed interface Round : Role {
                public val roundsBeforeSpeaking: UInt
                public val roundsBeforeListening: UInt
                
                @Serializable
                public data class Waiting(
                    override val name: String,
                    override val userIndex: UInt,
                    override val isHost: Boolean,
                    public val roundRole: RoundRole,
                    public val isGameFinishable: Boolean,
                    override val roundsBeforeSpeaking: UInt,
                    override val roundsBeforeListening: UInt,
                ) : Round {
                    @Serializable
                    public enum class RoundRole {
                        Player, Speaker, Listener,
                    }
                }
                
                @Serializable
                public data class Preparation(
                    override val name: String,
                    override val userIndex: UInt,
                    override val isHost: Boolean,
                    public val roundRole: RoundRole,
                    override val roundsBeforeSpeaking: UInt,
                    override val roundsBeforeListening: UInt,
                ) : Round {
                    @Serializable
                    public enum class RoundRole {
                        Player, Speaker, Listener,
                    }
                }
                
                @Serializable
                public data class Explanation(
                    override val name: String,
                    override val userIndex: UInt,
                    override val isHost: Boolean,
                    public val roundRole: RoundRole,
                    override val roundsBeforeSpeaking: UInt,
                    override val roundsBeforeListening: UInt,
                ) : Round {
                    @Serializable
                    public sealed interface RoundRole {
                        @Serializable
                        public data object Player : RoundRole
                        @Serializable
                        public data class Speaker(val currentWord: String) : RoundRole
                        @Serializable
                        public data object Listener : RoundRole
                    }
                }
                
                @Serializable
                public data class LastGuess(
                    override val name: String,
                    override val userIndex: UInt,
                    override val isHost: Boolean,
                    public val roundRole: RoundRole,
                    override val roundsBeforeSpeaking: UInt,
                    override val roundsBeforeListening: UInt,
                ) : Round {
                    @Serializable
                    public sealed interface RoundRole {
                        @Serializable
                        public data object Player : RoundRole
                        @Serializable
                        public data class Speaker(val currentWord: String) : RoundRole
                        @Serializable
                        public data object Listener : RoundRole
                    }
                }
                
                @Serializable
                public data class Editing(
                    override val name: String,
                    override val userIndex: UInt,
                    override val isHost: Boolean,
                    public val roundRole: RoundRole,
                    override val roundsBeforeSpeaking: UInt,
                    override val roundsBeforeListening: UInt,
                ) : Round {
                    @Serializable
                    public sealed interface RoundRole {
                        @Serializable
                        public data object Player : RoundRole
                        @Serializable
                        public data class Speaker(
                            public val wordsToEdit: KoneList<GameStateMachine.WordExplanation>,
                        ) : RoundRole
                        @Serializable
                        public data object Listener : RoundRole
                    }
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
            public val roomName: String
            public val role: Role
            
            @Serializable
            public data class GameInitialisation(
                override val roomName: String,
                override val role: Role.GameInitialisation,
                public val playersList: KoneList<PlayerDescription.GameInitialisation>,
                public val settingsBuilder: SettingsBuilder,
            ) : State
            
            @Serializable
            public data class PlayersWordsCollection(
                override val roomName: String,
                override val role: Role.PlayersWordsCollection,
                public val playersList: KoneList<PlayerDescription.PlayersWordsCollection>,
                public val settings: Settings,
                public val playersWordsAreReady: KoneBooleanArray,
            ) : State
            
            @Serializable
            public sealed interface Round : State {
                override val role: Role.Round
                public val playersList: KoneList<PlayerDescription.Round>
                public val settings: Settings
                public val initialWordsNumber: UInt
                public val roundNumber: UInt
                public val cycleNumber: UInt
                public val speakerIndex: UInt
                public val listenerIndex: UInt
                public val nextSpeakerIndex: UInt
                public val nextListenerIndex: UInt
                public val restWordsNumber: UInt
                public val wordsInProgressNumber: UInt
                public val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>
                public val leaderboardPermutation: KoneUIntArray
                
                @Serializable
                public data class Waiting(
                    override val roomName: String,
                    override val role: Role.Round.Waiting,
                    override val playersList: KoneList<PlayerDescription.Round.Waiting>,
                    override val settings: Settings,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val restWordsNumber: UInt,
                    override val wordsInProgressNumber: UInt,
                    override val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>,
                    public val speakerReady: Boolean,
                    public val listenerReady: Boolean,
                    override val leaderboardPermutation: KoneUIntArray,
                ) : Round
                
                @Serializable
                public data class Preparation(
                    override val roomName: String,
                    override val role: Role.Round.Preparation,
                    override val playersList: KoneList<PlayerDescription.Round.Preparation>,
                    override val settings: Settings,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val restWordsNumber: UInt,
                    override val wordsInProgressNumber: UInt,
                    override val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>,
                    public val millisecondsLeft: UInt,
                    override val leaderboardPermutation: KoneUIntArray,
                ) : Round
                
                @Serializable
                public data class Explanation(
                    override val roomName: String,
                    override val role: Role.Round.Explanation,
                    override val playersList: KoneList<PlayerDescription.Round.Explanation>,
                    override val settings: Settings,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val restWordsNumber: UInt,
                    override val wordsInProgressNumber: UInt,
                    override val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>,
                    public val millisecondsLeft: UInt,
                    override val leaderboardPermutation: KoneUIntArray,
                ) : Round
                
                @Serializable
                public data class LastGuess(
                    override val roomName: String,
                    override val role: Role.Round.LastGuess,
                    override val playersList: KoneList<PlayerDescription.Round.LastGuess>,
                    override val settings: Settings,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val restWordsNumber: UInt,
                    override val wordsInProgressNumber: UInt,
                    override val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>,
                    public val millisecondsLeft: UInt,
                    override val leaderboardPermutation: KoneUIntArray,
                ) : Round
                
                @Serializable
                public data class Editing(
                    override val roomName: String,
                    override val role: Role.Round.Editing,
                    override val playersList: KoneList<PlayerDescription.Round.Editing>,
                    override val settings: Settings,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val restWordsNumber: UInt,
                    override val wordsInProgressNumber: UInt,
                    override val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>,
                    override val leaderboardPermutation: KoneUIntArray,
                ) : Round
            }
            
            @Serializable
            public data class GameResults(
                override val roomName: String,
                override val role: Role.GameResults,
                public val playersList: KoneList<PlayerDescription.GameResults>,
                public val settings: Settings,
                public val leaderboardPermutation: KoneUIntArray,
                public val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>,
            ) : State
        }
        
        @Serializable
        public sealed interface Error {
            @Serializable
            @BetterBeReplaced
            public data object UnspecifiedError : Error
            
            @Serializable
            public data object AttachmentIsAlreadyProvided : Error
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
            public data object CannotInitializeGameAfterInitialization : Error
            @Serializable
            public data object PlayerAlreadySubmittedWords : Error
            @Serializable
            public data object CannotSubmitPlayerWordsNotDuringPlayersWordsCollection : Error
            @Serializable
            public data object CannotSetSpeakerReadinessNotDuringRoundWaiting : Error
            @Serializable
            public data object NotSpeakerSettingSpeakerReadiness : Error
            @Serializable
            public data object CannotSetListenerReadinessNotDuringRoundWaiting : Error
            @Serializable
            public data object NotListenerSettingListenerReadiness : Error
            @Serializable
            public data object CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting : Error
            @Serializable
            public data object CannotUpdateRoundInfoNotDuringTheRound : Error
            @Serializable
            public data object CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess : Error
            @Serializable
            public data object NotSpeakerSubmittingWordExplanationResult : Error
            @Serializable
            public data object CannotUpdateWordExplanationResultsNotDuringRoundEditing : Error
            @Serializable
            public data object NotSpeakerUpdatingWordExplanationResults : Error
            @Serializable
            public data object CannotUpdateWordExplanationResultsWithOtherWordsSet : Error
            @Serializable
            public data object CannotConfirmWordExplanationResultsNotDuringRoundEditing : Error
            @Serializable
            public data object NotSpeakerConfirmingWordExplanationResults : Error
            @Serializable
            public data object CannotFinishGameNotDuringRoundWaiting : Error
            @Serializable
            public data object NotHostFinishingGame : Error
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