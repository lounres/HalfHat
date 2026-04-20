package dev.lounres.halfhat.api.onlineGame

import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
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
        @Serializable
        public data object HostDictionary: WordsSource
        @Serializable
        public data class ServerDictionary(
            val dictionaryIdWithDescription: DictionaryId.WithDescription,
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
        RoomPlayersGathering,
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
    public data class Settings(
        public val preparationTimeSeconds: UInt,
        public val explanationTimeSeconds: UInt,
        public val finalGuessTimeSeconds: UInt,
        public val strictMode: Boolean,
        public val gameEndCondition: GameStateMachine.GameEndCondition,
        public val wordsSource: WordsSource,
    ) {
        @Serializable
        public data class Builder(
            public val preparationTimeSeconds: UInt,
            public val explanationTimeSeconds: UInt,
            public val finalGuessTimeSeconds: UInt,
            public val strictMode: Boolean,
            public val cachedEndConditionWordsNumber: UInt,
            public val cachedEndConditionCyclesNumber: UInt,
            public val gameEndConditionType: GameStateMachine.GameEndCondition.Type,
            public val wordsSource: WordsSource,
        )
    }

    @Serializable
    public data class ExtraSettings(
        public val showWordsStatistic: Boolean,
        public val showLeaderboardPermutation: Boolean,
    )

    @Serializable
    public data class Leaderboard(
        val permutation: KoneUIntArray,
        val scoreExplained: KoneUIntArray,
        val scoreGuessed: KoneUIntArray,
        val scoreSum: KoneUIntArray,
    )
    
    public object OnlineGame {
        @Serializable
        public sealed interface PlayerDescription {
            public val name: String
            public val userIndex: UInt
            public val isOnline: Boolean
            public val isHost: Boolean

            @Serializable
            public data class RoomPlayersGathering(
                override val name: String,
                override val userIndex: UInt,
                override val isOnline: Boolean,
                override val isHost: Boolean,
            ) : PlayerDescription
            
            @Serializable
            public data class GameInitialisation(
                override val name: String,
                override val userIndex: UInt,
                override val isOnline: Boolean,
                override val isHost: Boolean,
                public val globalRole: GlobalRole,
            ) : PlayerDescription {
                @Serializable
                public sealed interface GlobalRole {
                    @Serializable
                    public data object Player : GlobalRole
                    @Serializable
                    public data object Spectator : GlobalRole
                }
            }
            
            @Serializable
            public data class PlayersWordsCollection(
                override val name: String,
                override val userIndex: UInt,
                override val isOnline: Boolean,
                override val isHost: Boolean,
                public val globalRole: GlobalRole,
            ) : PlayerDescription {
                @Serializable
                public sealed interface GlobalRole {
                    @Serializable
                    public data class Player(
                        public val finishedWordsCollection: Boolean,
                    ) : GlobalRole
                    @Serializable
                    public data object Spectator : GlobalRole
                }
            }
            
            @Serializable
            public sealed interface Round : PlayerDescription {
                public val globalRole: GlobalRole
                
                @Serializable
                public sealed interface GlobalRole {
                    @Serializable
                    public data class Player(
                        public val roundRole: RoundRole?,
                    ) : GlobalRole {
                        @Serializable
                        public enum class RoundRole {
                            Speaker, Listener,
                        }
                    }
                    @Serializable
                    public data object Spectator : GlobalRole
                }
                
                @Serializable
                public data class Waiting(
                    override val name: String,
                    override val userIndex: UInt,
                    override val isOnline: Boolean,
                    override val isHost: Boolean,
                    override val globalRole: GlobalRole,
                ) : Round
                
                @Serializable
                public data class Preparation(
                    override val name: String,
                    override val userIndex: UInt,
                    override val isOnline: Boolean,
                    override val isHost: Boolean,
                    override val globalRole: GlobalRole,
                ) : Round
                
                @Serializable
                public data class Explanation(
                    override val name: String,
                    override val userIndex: UInt,
                    override val isOnline: Boolean,
                    override val isHost: Boolean,
                    override val globalRole: GlobalRole,
                ) : Round
                
                @Serializable
                public data class LastGuess(
                    override val name: String,
                    override val userIndex: UInt,
                    override val isOnline: Boolean,
                    override val isHost: Boolean,
                    override val globalRole: GlobalRole,
                ) : Round
                
                @Serializable
                public data class Editing(
                    override val name: String,
                    override val userIndex: UInt,
                    override val isOnline: Boolean,
                    override val isHost: Boolean,
                    override val globalRole: GlobalRole,
                ) : Round
            }
            
            @Serializable
            public data class GameResults(
                override val name: String,
                override val userIndex: UInt,
                override val isOnline: Boolean,
                override val isHost: Boolean,
                public val globalRole: GlobalRole,
            ) : PlayerDescription {
                @Serializable
                public sealed interface GlobalRole {
                    @Serializable
                    public data object Player : GlobalRole
                    @Serializable
                    public data object Spectator : GlobalRole
                }
            }
        }
        
        @Serializable
        public sealed interface SelfRole {
            public val name: String
            public val userIndex: UInt
            public val isHost: Boolean

            @Serializable
            public data class RoomPlayersGathering(
                override val name: String,
                override val userIndex: UInt,
                override val isHost: Boolean,
                public val isRoomFixable: Boolean,
            ) : SelfRole

            @Serializable
            public data class GameInitialisation(
                override val name: String,
                override val userIndex: UInt,
                override val isHost: Boolean,
                public val globalRole: GlobalRole,
                public val isStartAvailable: Boolean,
                public val areSettingsChangeable: Boolean,
            ) : SelfRole {
                @Serializable
                public sealed interface GlobalRole {
                    @Serializable
                    public data object Player : GlobalRole
                    @Serializable
                    public data object Spectator : GlobalRole
                }
            }

            @Serializable
            public data class PlayersWordsCollection(
                override val name: String,
                override val userIndex: UInt,
                override val isHost: Boolean,
                public val globalRole: GlobalRole,
            ) : SelfRole {
                @Serializable
                public sealed interface GlobalRole {
                    @Serializable
                    public data class Player(
                        public val finishedWordsCollection: Boolean,
                    ) : GlobalRole
                    @Serializable
                    public data object Spectator : GlobalRole
                }
            }

            @Serializable
            public sealed interface Round : SelfRole {
                @Serializable
                public data class Waiting(
                    override val name: String,
                    override val userIndex: UInt,
                    override val isHost: Boolean,
                    public val globalRole: GlobalRole,
                    public val isGameFinishable: Boolean,
                ) : Round {
                    @Serializable
                    public sealed interface GlobalRole {
                        @Serializable
                        public data class Player(
                            public val roundRole: RoundRole?,
                            public val roundsBeforeSpeaking: UInt,
                            public val roundsBeforeListening: UInt,
                        ) : GlobalRole {
                            @Serializable
                            public enum class RoundRole {
                                Speaker, Listener,
                            }
                        }
                        @Serializable
                        public data object Spectator : GlobalRole
                    }
                }

                @Serializable
                public data class Preparation(
                    override val name: String,
                    override val userIndex: UInt,
                    override val isHost: Boolean,
                    public val globalRole: GlobalRole,
                ) : Round {
                    @Serializable
                    public sealed interface GlobalRole {
                        @Serializable
                        public data class Player(
                            public val roundRole: RoundRole?,
                            public val roundsBeforeSpeaking: UInt,
                            public val roundsBeforeListening: UInt,
                        ) : GlobalRole {
                            @Serializable
                            public enum class RoundRole {
                                Speaker, Listener,
                            }
                        }
                        @Serializable
                        public data object Spectator : GlobalRole
                    }
                }

                @Serializable
                public data class Explanation(
                    override val name: String,
                    override val userIndex: UInt,
                    override val isHost: Boolean,
                    public val globalRole: GlobalRole,
                ) : Round {
                    @Serializable
                    public sealed interface GlobalRole {
                        @Serializable
                        public data class Player(
                            public val roundRole: RoundRole?,
                            public val roundsBeforeSpeaking: UInt,
                            public val roundsBeforeListening: UInt,
                        ) : GlobalRole {
                            @Serializable
                            public sealed interface RoundRole {
                                @Serializable
                                public data class Speaker(val currentWord: String) : RoundRole
                                @Serializable
                                public data object Listener : RoundRole
                            }
                        }
                        @Serializable
                        public data object Spectator : GlobalRole
                    }
                }

                @Serializable
                public data class LastGuess(
                    override val name: String,
                    override val userIndex: UInt,
                    override val isHost: Boolean,
                    public val globalRole: GlobalRole,
                ) : Round {
                    @Serializable
                    public sealed interface GlobalRole {
                        @Serializable
                        public data class Player(
                            public val roundRole: RoundRole?,
                            public val roundsBeforeSpeaking: UInt,
                            public val roundsBeforeListening: UInt,
                        ) : GlobalRole {
                            @Serializable
                            public sealed interface RoundRole {
                                @Serializable
                                public data class Speaker(val currentWord: String) : RoundRole
                                @Serializable
                                public data object Listener : RoundRole
                            }
                        }
                        @Serializable
                        public data object Spectator : GlobalRole
                    }
                }

                @Serializable
                public data class Editing(
                    override val name: String,
                    override val userIndex: UInt,
                    override val isHost: Boolean,
                    public val globalRole: GlobalRole,
                ) : Round {
                    @Serializable
                    public sealed interface GlobalRole {
                        @Serializable
                        public data class Player(
                            public val roundRole: RoundRole?,
                            public val roundsBeforeSpeaking: UInt,
                            public val roundsBeforeListening: UInt,
                        ) : GlobalRole {
                            @Serializable
                            public sealed interface RoundRole {
                                @Serializable
                                public data class Speaker(
                                    public val wordsToEdit: KoneList<GameStateMachine.WordExplanation>,
                                ) : RoundRole
                                @Serializable
                                public data object Listener : RoundRole
                            }
                        }
                        @Serializable
                        public data object Spectator : GlobalRole
                    }
                }
            }

            @Serializable
            public data class GameResults(
                override val name: String,
                override val userIndex: UInt,
                override val isHost: Boolean,
                public val globalRole: GlobalRole,
            ) : SelfRole {
                @Serializable
                public sealed interface GlobalRole {
                    @Serializable
                    public data object Player: GlobalRole
                    @Serializable
                    public data object Spectator : GlobalRole
                }
            }
        }
        
        @Serializable
        public sealed interface State {
            public val roomName: String
            public val selfRole: SelfRole
            public val playersList: KoneList<PlayerDescription>

            @Serializable
            public data class RoomPlayersGathering(
                override val roomName: String,
                override val selfRole: SelfRole.RoomPlayersGathering,
                override val playersList: KoneList<PlayerDescription.RoomPlayersGathering>,
            ) : State

            @Serializable
            public data class GameInitialisation(
                override val roomName: String,
                override val selfRole: SelfRole.GameInitialisation,
                override val playersList: KoneList<PlayerDescription.GameInitialisation>,
                public val settingsBuilder: Settings.Builder,
                public val extraSettings: ExtraSettings,
            ) : State

            @Serializable
            public data class PlayersWordsCollection(
                override val roomName: String,
                override val selfRole: SelfRole.PlayersWordsCollection,
                override val playersList: KoneList<PlayerDescription.PlayersWordsCollection>,
                public val settings: Settings,
                public val extraSettings: ExtraSettings,
            ) : State

            @Serializable
            public sealed interface Round : State {
                override val selfRole: SelfRole.Round
                override val playersList: KoneList<PlayerDescription.Round>
                public val settings: Settings
                public val extraSettings: ExtraSettings
                public val initialWordsNumber: UInt
                public val roundNumber: UInt
                public val cycleNumber: UInt
                public val speakerIndex: UInt
                public val listenerIndex: UInt
                public val nextSpeakerIndex: UInt
                public val nextListenerIndex: UInt
                public val restWordsNumber: UInt
                public val wordsInProgressNumber: UInt
                public val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>?
                public val leaderboard: Leaderboard?

                @Serializable
                public data class Waiting(
                    override val roomName: String,
                    override val selfRole: SelfRole.Round.Waiting,
                    override val playersList: KoneList<PlayerDescription.Round.Waiting>,
                    override val settings: Settings,
                    override val extraSettings: ExtraSettings,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val restWordsNumber: UInt,
                    override val wordsInProgressNumber: UInt,
                    override val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>?,
                    public val speakerReady: Boolean,
                    public val listenerReady: Boolean,
                    override val leaderboard: Leaderboard?,
                ) : Round

                @Serializable
                public data class Preparation(
                    override val roomName: String,
                    override val selfRole: SelfRole.Round.Preparation,
                    override val playersList: KoneList<PlayerDescription.Round.Preparation>,
                    override val settings: Settings,
                    override val extraSettings: ExtraSettings,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val restWordsNumber: UInt,
                    override val wordsInProgressNumber: UInt,
                    override val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>?,
                    public val millisecondsLeft: UInt,
                    override val leaderboard: Leaderboard?,
                ) : Round

                @Serializable
                public data class Explanation(
                    override val roomName: String,
                    override val selfRole: SelfRole.Round.Explanation,
                    override val playersList: KoneList<PlayerDescription.Round.Explanation>,
                    override val settings: Settings,
                    override val extraSettings: ExtraSettings,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val restWordsNumber: UInt,
                    override val wordsInProgressNumber: UInt,
                    override val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>?,
                    public val millisecondsLeft: UInt,
                    override val leaderboard: Leaderboard?,
                ) : Round

                @Serializable
                public data class LastGuess(
                    override val roomName: String,
                    override val selfRole: SelfRole.Round.LastGuess,
                    override val playersList: KoneList<PlayerDescription.Round.LastGuess>,
                    override val settings: Settings,
                    override val extraSettings: ExtraSettings,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val restWordsNumber: UInt,
                    override val wordsInProgressNumber: UInt,
                    override val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>?,
                    public val millisecondsLeft: UInt,
                    override val leaderboard: Leaderboard?,
                ) : Round

                @Serializable
                public data class Editing(
                    override val roomName: String,
                    override val selfRole: SelfRole.Round.Editing,
                    override val playersList: KoneList<PlayerDescription.Round.Editing>,
                    override val settings: Settings,
                    override val extraSettings: ExtraSettings,
                    override val initialWordsNumber: UInt,
                    override val roundNumber: UInt,
                    override val cycleNumber: UInt,
                    override val speakerIndex: UInt,
                    override val listenerIndex: UInt,
                    override val nextSpeakerIndex: UInt,
                    override val nextListenerIndex: UInt,
                    override val restWordsNumber: UInt,
                    override val wordsInProgressNumber: UInt,
                    override val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>?,
                    override val leaderboard: Leaderboard?,
                ) : Round
            }

            @Serializable
            public data class GameResults(
                override val roomName: String,
                override val selfRole: SelfRole.GameResults,
                override val playersList: KoneList<PlayerDescription.GameResults>,
                public val settings: Settings,
                public val extraSettings: ExtraSettings,
                public val wordsStatistic: KoneList<GameStateMachine.WordStatistic.AndWord>,
                public val leaderboard: Leaderboard,
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
            public data object NoAttachmentWhenItIsNeeded : Error
            @Serializable
            public data object AttachmentIsAlreadySevered : Error
            @Serializable
            public data object RoomIsAlreadyFixed : Error
            @Serializable
            public data object NoGameSettingsToChange : Error
            @Serializable
            public data object UnableToApplyGameStateMachineTransition : Error
            @Serializable
            public data object NotHostChangingGameSettings : Error
            @Serializable
            public data object IncorrectNumberOfGlobalRoles : Error
            @Serializable
            public data object CannotInitializeGameNotDuringGameInitialisation : Error
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
            public data object ForbiddenSpeakerAndListenerReadyTransition : Error
            @Serializable
            public data object ForbiddenUpdateRoundInfoTransition : Error
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
        public data class AvailableDictionariesUpdate(val descriptions: KoneList<DictionaryId.WithDescription>) : Signal
        
        @Serializable
        public data class OnlineGameStateUpdate(val state: OnlineGame.State) : Signal
        
        @Serializable
        public data class OnlineGameError(val error: OnlineGame.Error) : Signal
    }
}