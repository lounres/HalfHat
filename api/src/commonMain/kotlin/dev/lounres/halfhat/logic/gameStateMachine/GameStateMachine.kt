package dev.lounres.halfhat.logic.gameStateMachine

import dev.lounres.kone.collections.array.KoneUIntArray
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.map.KoneMap
import dev.lounres.kone.collections.set.KoneSet
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Instant


public object GameStateMachine {
    @Serializable
    public sealed interface GameEndCondition {
        @Serializable
        public data class Words(val number: UInt) : GameEndCondition
        @Serializable
        public data class Cycles(val number: UInt) : GameEndCondition
        
        @Serializable
        public enum class Type {
            Words, Cycles,
        }
    }
    
    public interface WordsProvider {
        // Maybe not exactly this `number` of words but no more than this number.
        public fun randomWords(number: UInt): KoneSet<String>
        public fun allWords(): KoneSet<String>
    }
    
    public interface WordsProviderRegistry<in WPD, out Reason> {
        public suspend fun getWordsProvider(providerId: WPD): WordsProviderOrReason<Reason>
        
        public sealed interface WordsProviderOrReason<out Reason> {
            public data class Success(val result: WordsProvider) : WordsProviderOrReason<Nothing>
            public data class Failure<Reason>(val reason: Reason) : WordsProviderOrReason<Reason>
        }
    }
    
    @Serializable
    public sealed interface WordsSource<out WPD> {
        @Serializable
        public data object Players : WordsSource<Nothing>
        @Serializable
        public data class Custom<out WPD>(val providerId: WPD) : WordsSource<WPD>
    }
    
    @Serializable
    public data class WordExplanation(
        val word: String,
        val state: State
    ) {
        @Serializable
        public enum class State {
            Explained, Mistake, NotExplained;
        }
    }
    
    @Serializable
    public data class WordStatistic(
        val spentTime: Duration,
        val state: State,
    ) {
        @Serializable
        public enum class State {
            Explained, Mistake, InProgress;
        }
        
        @Serializable
        public data class AndWord(
            val word: String,
            val spentTime: Duration,
            val state: State,
        )
        
//        public typealias WordsStatistic = @Serializable(with = WordsStatisticSerializer::class) KoneMap<String, WordStatistic>
//
//        public object WordsStatisticSerializer : KSerializer<KoneMap<String, WordStatistic>> {
//            @OptIn(InternalSerializationApi::class)
//            override val descriptor: SerialDescriptor = buildSerialDescriptor("dev.lounres.kone.registry.serialization.RegistrySerializer", StructureKind.MAP) {
//                element(
//                    elementName = "key",
//                    descriptor = String.serializer().descriptor,
//                )
//                element(
//                    elementName = "value",
//                    descriptor = WordStatistic.serializer().descriptor,
//                )
//            }
//
//            override fun serialize(encoder: Encoder, value: KoneMap<String, WordStatistic>) {
//                encoder.encodeStructure(descriptor) {
//                    var registrationIndex = 0
//                    for ((key, value) in value.nodesView) {
//                        encodeSerializableElement(descriptor, registrationIndex++, String.serializer(), key)
//                        encodeSerializableElement(descriptor, registrationIndex++, WordStatistic.serializer(), value)
//                    }
//                }
//            }
//
//            @OptIn(ExperimentalSerializationApi::class)
//            override fun deserialize(decoder: Decoder): KoneMap<String, WordStatistic> =
//                decoder.decodeStructure(descriptor) {
//                    val result = KoneMutableMap.of<String, WordStatistic>()
//                    if (decodeSequentially()) {
//                        val size = decodeCollectionSize(descriptor)
//                        repeat(size) {
//                            val key = decodeSerializableElement(descriptor, it * 2, String.serializer())
//                            val value = decodeSerializableElement(descriptor, it * 2 + 1, WordStatistic.serializer())
//                            result[key] = value
//                        }
//                    } else {
//                        while (true) {
//                            val keyIndex = decodeElementIndex(descriptor)
//                            if (keyIndex == CompositeDecoder.DECODE_DONE) break
//                            val key = decodeSerializableElement(descriptor, keyIndex, String.serializer())
//                            decodeElementIndex(descriptor).also {
//                                require(it == keyIndex + 1) { "Value must follow key in a map, index for key: $keyIndex, returned index for value: $it" }
//                            }
//                            val value = decodeSerializableElement(descriptor, keyIndex + 1, WordStatistic.serializer())
//                            result[key] = value
//                        }
//                    }
//                    result
//                }
//        }
    }
    
    public data class GameSettings<out WPD>(
        val preparationTimeSeconds: UInt,
        val explanationTimeSeconds: UInt,
        val finalGuessTimeSeconds: UInt,
        val strictMode: Boolean,
        val gameEndCondition: GameEndCondition,
        val wordsSource: WordsSource<WPD>,
    ) {
        @Serializable
        public data class Builder<out WPD>(
            val preparationTimeSeconds: UInt,
            val explanationTimeSeconds: UInt,
            val finalGuessTimeSeconds: UInt,
            val strictMode: Boolean,
            val cachedEndConditionWordsNumber: UInt,
            val cachedEndConditionCyclesNumber: UInt,
            val gameEndConditionType: GameEndCondition.Type,
            val wordsSource: WordsSource<WPD>,
        ) {
            public fun build(): GameSettings<WPD> =
                GameSettings(
                    preparationTimeSeconds = preparationTimeSeconds,
                    explanationTimeSeconds = explanationTimeSeconds,
                    finalGuessTimeSeconds = finalGuessTimeSeconds,
                    strictMode = strictMode,
                    gameEndCondition = when (gameEndConditionType) {
                        GameEndCondition.Type.Words -> GameEndCondition.Words(cachedEndConditionWordsNumber)
                        GameEndCondition.Type.Cycles -> GameEndCondition.Cycles(cachedEndConditionCyclesNumber)
                    },
                    wordsSource = wordsSource,
                )
        }
    }
    
    @Serializable
    public data class GameResult(
        val player: UInt,
        val scoreExplained: UInt,
        val scoreGuessed: UInt,
        val scoreSum: UInt,
    )
    
    public data class PersonalResult<P>(
        val player: P,
        val scoreExplained: UInt,
        val scoreGuessed: UInt,
        val scoreSum: UInt,
    )
    
    public sealed interface State<out P, out WPD, out Metadata> {
        public val metadata: Metadata
        public val playersList: KoneList<P>
        
        public data class GameInitialisation<out P, out WPD, out Metadata>(
            override val metadata: Metadata,
            override val playersList: KoneList<P>,
            val settingsBuilder: GameSettings.Builder<WPD>,
        ) : State<P, WPD, Metadata>
        
        public data class PlayersWordsCollection<out P, out WPD, out Metadata>(
            override val metadata: Metadata,
            override val playersList: KoneList<P>,
            val settings: GameSettings<WPD>,
            val playersWords: KoneList<KoneSet<String>?>,
        ) : State<P, WPD, Metadata>
        
        public data class RoundWaiting<out P, out WPD, out Metadata>(
            override val metadata: Metadata,
            override val playersList: KoneList<P>,
            val settings: GameSettings<WPD>,
            val initialWordsNumber: UInt,
            val roundNumber: UInt,
            val cycleNumber: UInt,
            val speakerIndex: UInt,
            val listenerIndex: UInt,
            val nextSpeakerIndex: UInt,
            val nextListenerIndex: UInt,
            val playersRoundsBeforeSpeaking: KoneUIntArray,
            val playersRoundsBeforeListening: KoneUIntArray,
            val restWords: KoneSet<String>,
            val explanationScores: KoneList<UInt>,
            val guessingScores: KoneList<UInt>,
            val wordsStatistic: KoneMap<String, WordStatistic>,
            val speakerReady: Boolean,
            val listenerReady: Boolean,
        ) : State<P, WPD, Metadata>
        
        public data class RoundPreparation<out P, out WPD, out Metadata>(
            override val metadata: Metadata,
            override val playersList: KoneList<P>,
            val settings: GameSettings<WPD>,
            val initialWordsNumber: UInt,
            val roundNumber: UInt,
            val cycleNumber: UInt,
            val speakerIndex: UInt,
            val listenerIndex: UInt,
            val nextSpeakerIndex: UInt,
            val nextListenerIndex: UInt,
            val playersRoundsBeforeSpeaking: KoneUIntArray,
            val playersRoundsBeforeListening: KoneUIntArray,
            val startInstant: Instant,
            val millisecondsLeft: UInt,
            val restWords: KoneSet<String>,
            val explanationScores: KoneList<UInt>,
            val guessingScores: KoneList<UInt>,
            val wordsStatistic: KoneMap<String, WordStatistic>,
            val currentExplanationResults: KoneList<WordExplanation>,
        ) : State<P, WPD, Metadata>
        
        public data class RoundExplanation<out P, out WPD, out Metadata>(
            override val metadata: Metadata,
            override val playersList: KoneList<P>,
            val settings: GameSettings<WPD>,
            val initialWordsNumber: UInt,
            val roundNumber: UInt,
            val cycleNumber: UInt,
            val speakerIndex: UInt,
            val listenerIndex: UInt,
            val nextSpeakerIndex: UInt,
            val nextListenerIndex: UInt,
            val playersRoundsBeforeSpeaking: KoneUIntArray,
            val playersRoundsBeforeListening: KoneUIntArray,
            val startInstant: Instant,
            val millisecondsLeft: UInt,
            val restWords: KoneSet<String>,
            val currentWord: String,
            val explanationScores: KoneList<UInt>,
            val guessingScores: KoneList<UInt>,
            val wordsStatistic: KoneMap<String, WordStatistic>,
            val wordExplanationStart: Instant,
            val currentExplanationResults: KoneList<WordExplanation>,
        ) : State<P, WPD, Metadata>
        
        public data class RoundLastGuess<out P, out WPD, out Metadata>(
            override val metadata: Metadata,
            override val playersList: KoneList<P>,
            val settings: GameSettings<WPD>,
            val initialWordsNumber: UInt,
            val roundNumber: UInt,
            val cycleNumber: UInt,
            val speakerIndex: UInt,
            val listenerIndex: UInt,
            val nextSpeakerIndex: UInt,
            val nextListenerIndex: UInt,
            val playersRoundsBeforeSpeaking: KoneUIntArray,
            val playersRoundsBeforeListening: KoneUIntArray,
            val startInstant: Instant,
            val millisecondsLeft: UInt,
            val restWords: KoneSet<String>,
            val currentWord: String,
            val explanationScores: KoneList<UInt>,
            val guessingScores: KoneList<UInt>,
            val wordsStatistic: KoneMap<String, WordStatistic>,
            val wordExplanationStart: Instant,
            val currentExplanationResults: KoneList<WordExplanation>,
        ) : State<P, WPD, Metadata>
        
        public data class RoundEditing<out P, out WPD, out Metadata>(
            override val metadata: Metadata,
            override val playersList: KoneList<P>,
            val settings: GameSettings<WPD>,
            val initialWordsNumber: UInt,
            val roundNumber: UInt,
            val cycleNumber: UInt,
            val speakerIndex: UInt,
            val listenerIndex: UInt,
            val nextSpeakerIndex: UInt,
            val nextListenerIndex: UInt,
            val playersRoundsBeforeSpeaking: KoneUIntArray,
            val playersRoundsBeforeListening: KoneUIntArray,
            val restWords: KoneSet<String>,
            val explanationScores: KoneList<UInt>,
            val guessingScores: KoneList<UInt>,
            val wordsStatistic: KoneMap<String, WordStatistic>,
            val currentExplanationResults: KoneList<WordExplanation>,
        ) : State<P, WPD, Metadata>
        
        public data class GameResults<out P, out WPD, out Metadata>(
            override val metadata: Metadata,
            override val playersList: KoneList<P>,
            val settings: GameSettings<WPD>,
            val results: KoneList<GameResult>,
            val wordsStatistic: KoneMap<String, WordStatistic>,
        ) : State<P, WPD, Metadata>
    }
    
    public sealed interface Transition<out P, out WPD, out NoWordsProviderReason, out MetadataTransition: Any> {
        public val metadataTransition: MetadataTransition?
        
        public data class NoOperation<out MetadataTransition: Any>(
            override val metadataTransition: MetadataTransition? = null,
        ) : Transition<Nothing, Nothing, Nothing, MetadataTransition>
        public data class UpdateGameSettings<out P, out WPD, out MetadataTransition: Any>(
            public val playersList: KoneList<P>,
            public val settingsBuilder: GameSettings.Builder<WPD>,
            override val metadataTransition: MetadataTransition? = null,
        ) : Transition<P, WPD, Nothing, MetadataTransition>
        public data class InitialiseGame<WPD, out NoWordsProviderReason, out MetadataTransition: Any>(
            val wordsProviderRegistry: WordsProviderRegistry<WPD, NoWordsProviderReason>,
            override val metadataTransition: MetadataTransition? = null,
        ) : Transition<Nothing, WPD, NoWordsProviderReason, MetadataTransition>
        public data class SubmitPlayerWords<out MetadataTransition: Any>(
            public val playerIndex: UInt,
            public val playerWords: KoneSet<String>,
            override val metadataTransition: MetadataTransition? = null,
        ) : Transition<Nothing, Nothing, Nothing, MetadataTransition>
        public data class SpeakerReady<out MetadataTransition: Any>(
            override val metadataTransition: MetadataTransition? = null,
        ) : Transition<Nothing, Nothing, Nothing, MetadataTransition>
        public data class ListenerReady<out MetadataTransition: Any>(
            override val metadataTransition: MetadataTransition? = null,
        ) : Transition<Nothing, Nothing, Nothing, MetadataTransition>
        public data class SpeakerAndListenerReady<out MetadataTransition: Any>(
            override val metadataTransition: MetadataTransition? = null,
        ) : Transition<Nothing, Nothing, Nothing, MetadataTransition>
        public data class UpdateRoundInfo<out MetadataTransition: Any>(
            public val stopTimer: () -> Unit,
            public val roundNumber: UInt,
            override val metadataTransition: MetadataTransition? = null,
        ) : Transition<Nothing, Nothing, Nothing, MetadataTransition>
        public data class WordExplanationState<out MetadataTransition: Any>(
            public val wordState: WordExplanation.State,
            override val metadataTransition: MetadataTransition? = null,
        ) : Transition<Nothing, Nothing, Nothing, MetadataTransition>
        public data class UpdateWordsExplanationResults<out MetadataTransition: Any>(
            public val newExplanationResults: KoneList<WordExplanation>,
            override val metadataTransition: MetadataTransition? = null,
        ) : Transition<Nothing, Nothing, Nothing, MetadataTransition>
        public data class ConfirmWordsExplanationResults<out MetadataTransition: Any>(
            override val metadataTransition: MetadataTransition? = null,
        ) : Transition<Nothing, Nothing, Nothing, MetadataTransition>
        public data class FinishGame<out MetadataTransition: Any>(
            override val metadataTransition: MetadataTransition? = null,
        ) : Transition<Nothing, Nothing, Nothing, MetadataTransition>
    }
    
    public sealed interface NoNextStateReason<out NoMetadataTransitionReason, out NoWordsProviderReason> {
        public data class NoMetadataUpdate<out NoMetadataTransitionReason>(public val reason: NoMetadataTransitionReason) : NoNextStateReason<NoMetadataTransitionReason, Nothing>
        public data class NoWordsProvider<out NoWordsProviderReason>(public val reason: NoWordsProviderReason) : NoNextStateReason<Nothing, NoWordsProviderReason>
        public data object CannotUpdateGameSettingsAfterInitialization : NoNextStateReason<Nothing, Nothing>
        public data object NotEnoughPlayersForInitialization : NoNextStateReason<Nothing, Nothing>
        public data object CannotInitializeGameAfterInitialization : NoNextStateReason<Nothing, Nothing>
        public data object PlayerAlreadySubmittedWords : NoNextStateReason<Nothing, Nothing>
        public data object CannotSubmitPlayerWordsNotDuringPlayersWordsCollection : NoNextStateReason<Nothing, Nothing>
        public data object CannotSetSpeakerReadinessNotDuringRoundWaiting : NoNextStateReason<Nothing, Nothing>
        public data object CannotSetListenerReadinessNotDuringRoundWaiting : NoNextStateReason<Nothing, Nothing>
        public data object CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting : NoNextStateReason<Nothing, Nothing>
        public data object CannotUpdateRoundInfoNotDuringTheRound : NoNextStateReason<Nothing, Nothing>
        public data object CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess : NoNextStateReason<Nothing, Nothing>
        public data object CannotUpdateWordExplanationResultsNotDuringRoundEditing : NoNextStateReason<Nothing, Nothing>
        public data object CannotUpdateWordExplanationResultsWithOtherWordsSet : NoNextStateReason<Nothing, Nothing>
        public data object CannotConfirmWordExplanationResultsNotDuringRoundEditing : NoNextStateReason<Nothing, Nothing>
        public data object CannotFinishGameNotDuringRoundWaiting : NoNextStateReason<Nothing, Nothing>
    }
}