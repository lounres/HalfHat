package dev.lounres.halfhat.server

import dev.lounres.halfhat.api.onlineGame.ClientApi
import dev.lounres.halfhat.api.onlineGame.DictionaryId
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.halfhat.logic.server.Room
import dev.lounres.kone.collections.array.KoneMutableUIntArray
import dev.lounres.kone.collections.array.generate
import dev.lounres.kone.collections.interop.toKoneList
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.empty
import dev.lounres.kone.collections.list.getOrNull
import dev.lounres.kone.collections.list.of
import dev.lounres.kone.collections.set.*
import dev.lounres.kone.collections.utils.map
import dev.lounres.kone.collections.utils.mapIndexed
import dev.lounres.kone.collections.utils.sort
import dev.lounres.kone.repeat
import dev.lounres.logKube.core.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random
import kotlin.random.nextUInt
import kotlin.uuid.Uuid


val logger = JvmLogger(
    name = "HalfHat main server logger",
    logAcceptors = listOf(
        LogAcceptor(DefaultJvmLogWriter) /*{ it.logLevel >= LogLevel.INFO }*/,
    )
)

data class RoomMetadata(
    val name: String,
)

data class PlayerMetadata(
    val name: String
) : Room.Player.Metadata<String> {
    override val id: String get() = name
}

sealed interface WordsProviderId {
    data class HostDictionary(
        val words: KoneList<String>,
    ) : WordsProviderId
    sealed interface ServerDictionary : WordsProviderId {
        data class Builtin(
            val id: UInt,
        ) : ServerDictionary
    }
}

sealed interface WordsProviderDescription : Room.WordsProviderDescription<WordsProviderId> {
    data class HostDictionary(
        override val providerId: WordsProviderId.HostDictionary,
    ) : WordsProviderDescription
    sealed interface ServerDictionary : WordsProviderDescription {
        override val providerId: WordsProviderId.ServerDictionary
        data class Builtin(
            override val providerId: WordsProviderId.ServerDictionary.Builtin,
            val name: String,
            val wordsNumber: UInt,
        ) : ServerDictionary
    }
}

sealed interface NoWordsProviderReason {
    data object CannotFindDictionaryByID : NoWordsProviderReason
}

object OnlineGameWordsProviderRegistry : Room.WordsProviderRegistry<WordsProviderId, WordsProviderDescription, NoWordsProviderReason> {
    private class InMemoryDictionary(
        private val words: KoneSet<String>
    ) : GameStateMachine.WordsProvider {
        fun wordsNumber(): UInt = words.size
        override fun allWords(): KoneSet<String> = words
        override fun randomWords(number: UInt): KoneSet<String> {
            val number = minOf(number, words.size)
            if (number == 0u) return KoneSet.empty()

            val indices = KoneMutableUIntArray.generate(number) { Random.nextUInt(words.size - it) }
            if (number >= 2u) for (i in number - 2u downTo 0u) for (j in number - 1u downTo i + 1u) {
                if (indices[j] >= indices[i]) indices[j]++
            }

            indices.sort()

            return KoneSet.build {
                var indexIndex = 0u
                var currentIndex = 0u
                val iterator = words.iterator()
                while (indexIndex < number) {
                    repeat(indices[indexIndex] - currentIndex) { iterator.moveNext() }
                    currentIndex = indices[indexIndex]
                    add(iterator.getNext())
                    indexIndex++
                }
            }
        }
    }

    private class ServerDictionary(
        val name: String,
        private val words: KoneSet<String>
    ) : GameStateMachine.WordsProvider {
        fun wordsNumber(): UInt = words.size
        override fun allWords(): KoneSet<String> = words
        override fun randomWords(number: UInt): KoneSet<String> {
            val number = minOf(number, words.size)
            if (number == 0u) return KoneSet.empty()

            val indices = KoneMutableUIntArray.generate(number) { Random.nextUInt(words.size - it) }
            if (number >= 2u) for (i in number - 2u downTo 0u) for (j in number - 1u downTo i + 1u) {
                if (indices[j] >= indices[i]) indices[j]++
            }

            indices.sort()

            return KoneSet.build {
                var indexIndex = 0u
                var currentIndex = 0u
                val iterator = words.iterator()
                while (indexIndex < number) {
                    repeat(indices[indexIndex] - currentIndex) { iterator.moveNext() }
                    currentIndex = indices[indexIndex]
                    add(iterator.getNext())
                    indexIndex++
                }
            }
        }
    }

    private val dictionaries = KoneList
        .of("easy", "medium", "hard")
        .map {
            lazy {
                ServerDictionary(
                    name = it,
                    words = javaClass.getResourceAsStream("/dictionaries/$it")!!.bufferedReader().readLines().toKoneList().toKoneSet()
                )
            }
        }

    suspend fun getAllWordsProviderDescriptions(): KoneList<WordsProviderDescription.ServerDictionary> =
        dictionaries.mapIndexed { index, dictionary ->
            val dictionary = dictionary.value
            WordsProviderDescription.ServerDictionary.Builtin(
                providerId = WordsProviderId.ServerDictionary.Builtin(id = index),
                name = dictionary.name,
                wordsNumber = dictionary.wordsNumber()
            )
        }

    override suspend fun getWordsProviderDescription(providerId: WordsProviderId): Room.WordsProviderRegistry.WordsProviderDescriptionOrReason<WordsProviderDescription, NoWordsProviderReason> =
        when (providerId) {
            is WordsProviderId.HostDictionary -> Room.WordsProviderRegistry.WordsProviderDescriptionOrReason.Success(WordsProviderDescription.HostDictionary(providerId))
            is WordsProviderId.ServerDictionary.Builtin -> {
                val dictionary = dictionaries.getOrNull(providerId.id)?.value ?: return Room.WordsProviderRegistry.WordsProviderDescriptionOrReason.Failure(NoWordsProviderReason.CannotFindDictionaryByID)
                Room.WordsProviderRegistry.WordsProviderDescriptionOrReason.Success(
                    WordsProviderDescription.ServerDictionary.Builtin(
                        providerId = providerId,
                        name = dictionary.name,
                        wordsNumber = dictionary.wordsNumber(),
                    )
                )
            }
        }
    
    override suspend fun getWordsProvider(providerId: WordsProviderId): GameStateMachine.WordsProviderRegistry.WordsProviderOrReason<NoWordsProviderReason> =
        when (providerId) {
            is WordsProviderId.HostDictionary -> GameStateMachine.WordsProviderRegistry.WordsProviderOrReason.Success(InMemoryDictionary(providerId.words.toKoneSet()))
            is WordsProviderId.ServerDictionary.Builtin -> {
                val provider = dictionaries.getOrNull(providerId.id)?.value
                if (provider != null) GameStateMachine.WordsProviderRegistry.WordsProviderOrReason.Success(provider)
                else GameStateMachine.WordsProviderRegistry.WordsProviderOrReason.Failure(NoWordsProviderReason.CannotFindDictionaryByID)
            }
        }
}

typealias ServerRoom = Room<RoomMetadata, String, PlayerMetadata, WordsProviderId, WordsProviderDescription, NoWordsProviderReason, Connection>

val rooms = ConcurrentHashMap<String, ServerRoom>()

fun getRoomByIdOrCreate(id: String): ServerRoom = rooms.computeIfAbsent(id) {
    Room(
        metadata = RoomMetadata(id),
        wordsProviderRegistry = OnlineGameWordsProviderRegistry,
        initialSettingsBuilder = Room.GameSettings.Builder( // TODO: Захардкоженные константы
            preparationTimeSeconds = 3u,
            explanationTimeSeconds = 40u,
            finalGuessTimeSeconds = 3u,
            strictMode = false,
            cachedEndConditionWordsNumber = 100u,
            cachedEndConditionCyclesNumber = 4u,
            gameEndConditionType = GameStateMachine.GameEndCondition.Type.Words,
//            wordsSource = Room.WordsSource.Dictionary(2uL),
            wordsSource = Room.WordsSource.Players,
            showWordsStatistic = false,
            showLeaderboardPermutation = false,
        ),
        initialMetadataFactory = { PlayerMetadata(it) },
        checkConnectionAttachment = { _, isOnline, _ -> !isOnline }
    )
}

class Connection(
    val socketSession: WebSocketServerSession,
) : Room.Connection<RoomMetadata, PlayerMetadata, WordsProviderId, WordsProviderDescription, NoWordsProviderReason> {
    val id: Uuid = Uuid.random()
    override fun toString(): String = "Connection#${id.toHexString()}"
    
    var playerAttachment : Room.Player.Attachment<RoomMetadata, String, PlayerMetadata, WordsProviderId, WordsProviderDescription, NoWordsProviderReason, Connection>? = null
    val playerAttachmentMutex = Mutex()
    
    override suspend fun sendNewState(state: Room.Outgoing.State<RoomMetadata, PlayerMetadata, WordsProviderDescription>) {
        fun Room.GameSettings.Builder<WordsProviderDescription>.toServerApi(): ServerApi.Settings.Builder =
            ServerApi.Settings.Builder(
                preparationTimeSeconds = this.preparationTimeSeconds,
                explanationTimeSeconds = this.explanationTimeSeconds,
                finalGuessTimeSeconds = this.finalGuessTimeSeconds,
                strictMode = this.strictMode,
                cachedEndConditionWordsNumber = this.cachedEndConditionWordsNumber,
                cachedEndConditionCyclesNumber = this.cachedEndConditionCyclesNumber,
                gameEndConditionType = this.gameEndConditionType,
                wordsSource = when (val wordsSource = this.wordsSource) {
                    Room.WordsSource.Players -> ServerApi.WordsSource.Players
                    is Room.WordsSource.Custom -> when (val wordsProviderID = wordsSource.description) {
                        is WordsProviderDescription.HostDictionary -> ServerApi.WordsSource.HostDictionary
                        is WordsProviderDescription.ServerDictionary.Builtin -> ServerApi.WordsSource.ServerDictionary(
                            DictionaryId.WithDescription.Builtin(
                                id = DictionaryId.Builtin(wordsProviderID.providerId.id),
                                name = wordsProviderID.name,
                                wordsNumber = wordsProviderID.wordsNumber,
                            )
                        )
                    }
                },
                showWordsStatistic = this.showWordsStatistic,
                showLeaderboardPermutation = this.showLeaderboardPermutation,
            )
        fun Room.GameSettings<WordsProviderDescription>.toServerApi(): ServerApi.Settings =
            ServerApi.Settings(
                preparationTimeSeconds = this.preparationTimeSeconds,
                explanationTimeSeconds = this.explanationTimeSeconds,
                finalGuessTimeSeconds = this.finalGuessTimeSeconds,
                strictMode = this.strictMode,
                gameEndCondition = this.gameEndCondition,
                wordsSource = when (val wordsSource = this.wordsSource) {
                    Room.WordsSource.Players -> ServerApi.WordsSource.Players
                    is Room.WordsSource.Custom -> when (val wordsProviderID = wordsSource.description) {
                        is WordsProviderDescription.HostDictionary -> ServerApi.WordsSource.HostDictionary
                        is WordsProviderDescription.ServerDictionary.Builtin -> ServerApi.WordsSource.ServerDictionary(
                            DictionaryId.WithDescription.Builtin(
                                id = DictionaryId.Builtin(wordsProviderID.providerId.id),
                                name = wordsProviderID.name,
                                wordsNumber = wordsProviderID.wordsNumber,
                            )
                        )
                    }
                },
                showWordsStatistic = this.showWordsStatistic,
                showLeaderboardPermutation = this.showLeaderboardPermutation,
            )
        
        socketSession.sendSerialized<ServerApi.Signal>(
            ServerApi.Signal.OnlineGameStateUpdate(
                when (state) {
                    is Room.Outgoing.State.GameInitialisation<RoomMetadata, PlayerMetadata, WordsProviderDescription> ->
                        ServerApi.OnlineGame.State.GameInitialisation(
                            roomName = state.roomMetadata.name,
                            role = ServerApi.OnlineGame.Role.GameInitialisation(
                                name = state.role.metadata.name,
                                userIndex = state.role.userIndex,
                                isHost = state.role.isHost,
                                isStartAvailable = state.role.isStartAvailable,
                                areSettingsChangeable = state.role.areSettingsChangeable,
                            ),
                            playersList = state.playersList.mapIndexed { index, player ->
                                ServerApi.OnlineGame.PlayerDescription.GameInitialisation(
                                    name = player.metadata.name,
                                    userIndex = index,
                                    isOnline = player.isOnline,
                                    isHost = player.isHost,
                                )
                            },
                            settingsBuilder = state.settingsBuilder.toServerApi(),
                        )
                    is Room.Outgoing.State.PlayersWordsCollection<RoomMetadata, PlayerMetadata, WordsProviderDescription> ->
                        ServerApi.OnlineGame.State.PlayersWordsCollection(
                            roomName = state.roomMetadata.name,
                            role = ServerApi.OnlineGame.Role.PlayersWordsCollection(
                                name = state.role.metadata.name,
                                userIndex = state.role.userIndex,
                                isHost = state.role.isHost,
                                finishedWordsCollection = state.role.finishedWordsCollection,
                            ),
                            playersList = state.playersList.mapIndexed { index, player ->
                                ServerApi.OnlineGame.PlayerDescription.PlayersWordsCollection(
                                    name = player.metadata.name,
                                    userIndex = index,
                                    isOnline = player.isOnline,
                                    isHost = player.isHost,
                                    finishedWordsCollection = player.finishedWordsCollection,
                                )
                            },
                            settings = state.settings.toServerApi(),
                        )
                    is Room.Outgoing.State.Round.Waiting<RoomMetadata, PlayerMetadata, WordsProviderDescription> ->
                        ServerApi.OnlineGame.State.Round.Waiting(
                            roomName = state.roomMetadata.name,
                            role = ServerApi.OnlineGame.Role.Round.Waiting(
                                name = state.role.metadata.name,
                                userIndex = state.role.userIndex,
                                isHost = state.role.isHost,
                                roundRole = when (state.role.roundRole) {
                                    Room.Outgoing.Role.Round.Waiting.RoundRole.Player -> ServerApi.OnlineGame.Role.Round.Waiting.RoundRole.Player
                                    Room.Outgoing.Role.Round.Waiting.RoundRole.Listener -> ServerApi.OnlineGame.Role.Round.Waiting.RoundRole.Listener
                                    Room.Outgoing.Role.Round.Waiting.RoundRole.Speaker -> ServerApi.OnlineGame.Role.Round.Waiting.RoundRole.Speaker
                                },
                                isGameFinishable = state.role.isGameFinishable,
                                roundsBeforeSpeaking = state.role.roundsBeforeSpeaking,
                                roundsBeforeListening = state.role.roundsBeforeListening,
                            ),
                            playersList = state.playersList.mapIndexed { index, player ->
                                ServerApi.OnlineGame.PlayerDescription.Round.Waiting(
                                    name = player.metadata.name,
                                    userIndex = index,
                                    isOnline = player.isOnline,
                                    isHost = player.isHost,
                                    roundRole = when (player.roundRole) {
                                        Room.Outgoing.PlayerDescription.Round.RoundRole.Player -> ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Player
                                        Room.Outgoing.PlayerDescription.Round.RoundRole.Speaker -> ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Speaker
                                        Room.Outgoing.PlayerDescription.Round.RoundRole.Listener -> ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Listener
                                    },
                                    scoreExplained = player.scoreExplained,
                                    scoreGuessed = player.scoreGuessed,
                                    scoreSum = player.scoreSum,
                                )
                            },
                            settings = state.settings.toServerApi(),
                            initialWordsNumber = state.initialWordsNumber,
                            roundNumber = state.roundNumber,
                            cycleNumber = state.cycleNumber,
                            speakerIndex = state.speakerIndex,
                            listenerIndex = state.listenerIndex,
                            nextSpeakerIndex = state.nextSpeakerIndex,
                            nextListenerIndex = state.nextListenerIndex,
                            restWordsNumber = state.restWordsNumber,
                            wordsInProgressNumber = state.wordsInProgressNumber,
                            wordsStatistic = state.wordsStatistic,
                            speakerReady = state.speakerReady,
                            listenerReady = state.listenerReady,
                            leaderboardPermutation = state.leaderboardPermutation,
                        )
                    is Room.Outgoing.State.Round.Preparation<RoomMetadata, PlayerMetadata, WordsProviderDescription> ->
                        ServerApi.OnlineGame.State.Round.Preparation(
                            roomName = state.roomMetadata.name,
                            role = ServerApi.OnlineGame.Role.Round.Preparation(
                                name = state.role.metadata.name,
                                userIndex = state.role.userIndex,
                                isHost = state.role.isHost,
                                roundRole = when (state.role.roundRole) {
                                    Room.Outgoing.Role.Round.Preparation.RoundRole.Player -> ServerApi.OnlineGame.Role.Round.Preparation.RoundRole.Player
                                    Room.Outgoing.Role.Round.Preparation.RoundRole.Listener -> ServerApi.OnlineGame.Role.Round.Preparation.RoundRole.Listener
                                    Room.Outgoing.Role.Round.Preparation.RoundRole.Speaker -> ServerApi.OnlineGame.Role.Round.Preparation.RoundRole.Speaker
                                },
                                roundsBeforeSpeaking = state.role.roundsBeforeSpeaking,
                                roundsBeforeListening = state.role.roundsBeforeListening,
                            ),
                            playersList = state.playersList.mapIndexed { index, player ->
                                ServerApi.OnlineGame.PlayerDescription.Round.Preparation(
                                    name = player.metadata.name,
                                    userIndex = index,
                                    isOnline = player.isOnline,
                                    isHost = player.isHost,
                                    roundRole = when (player.roundRole) {
                                        Room.Outgoing.PlayerDescription.Round.RoundRole.Player -> ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Player
                                        Room.Outgoing.PlayerDescription.Round.RoundRole.Speaker -> ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Speaker
                                        Room.Outgoing.PlayerDescription.Round.RoundRole.Listener -> ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Listener
                                    },
                                    scoreExplained = player.scoreExplained,
                                    scoreGuessed = player.scoreGuessed,
                                    scoreSum = player.scoreSum,
                                )
                            },
                            settings = state.settings.toServerApi(),
                            initialWordsNumber = state.initialWordsNumber,
                            roundNumber = state.roundNumber,
                            cycleNumber = state.cycleNumber,
                            speakerIndex = state.speakerIndex,
                            listenerIndex = state.listenerIndex,
                            nextSpeakerIndex = state.nextSpeakerIndex,
                            nextListenerIndex = state.nextListenerIndex,
                            restWordsNumber = state.restWordsNumber,
                            wordsInProgressNumber = state.wordsInProgressNumber,
                            wordsStatistic = state.wordsStatistic,
                            millisecondsLeft = state.millisecondsLeft,
                            leaderboardPermutation = state.leaderboardPermutation,
                        )
                    is Room.Outgoing.State.Round.Explanation<RoomMetadata, PlayerMetadata, WordsProviderDescription> ->
                        ServerApi.OnlineGame.State.Round.Explanation(
                            roomName = state.roomMetadata.name,
                            role = ServerApi.OnlineGame.Role.Round.Explanation(
                                name = state.role.metadata.name,
                                userIndex = state.role.userIndex,
                                isHost = state.role.isHost,
                                roundRole = when (val role = state.role.roundRole) {
                                    Room.Outgoing.Role.Round.Explanation.RoundRole.Player -> ServerApi.OnlineGame.Role.Round.Explanation.RoundRole.Player
                                    Room.Outgoing.Role.Round.Explanation.RoundRole.Listener -> ServerApi.OnlineGame.Role.Round.Explanation.RoundRole.Listener
                                    is Room.Outgoing.Role.Round.Explanation.RoundRole.Speaker -> ServerApi.OnlineGame.Role.Round.Explanation.RoundRole.Speaker(role.currentWord)
                                },
                                roundsBeforeSpeaking = state.role.roundsBeforeSpeaking,
                                roundsBeforeListening = state.role.roundsBeforeListening,
                            ),
                            playersList = state.playersList.mapIndexed { index, player ->
                                ServerApi.OnlineGame.PlayerDescription.Round.Explanation(
                                    name = player.metadata.name,
                                    userIndex = index,
                                    isOnline = player.isOnline,
                                    isHost = player.isHost,
                                    roundRole = when (player.roundRole) {
                                        Room.Outgoing.PlayerDescription.Round.RoundRole.Player -> ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Player
                                        Room.Outgoing.PlayerDescription.Round.RoundRole.Speaker -> ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Speaker
                                        Room.Outgoing.PlayerDescription.Round.RoundRole.Listener -> ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Listener
                                    },
                                    scoreExplained = player.scoreExplained,
                                    scoreGuessed = player.scoreGuessed,
                                    scoreSum = player.scoreSum,
                                )
                            },
                            settings = state.settings.toServerApi(),
                            initialWordsNumber = state.initialWordsNumber,
                            roundNumber = state.roundNumber,
                            cycleNumber = state.cycleNumber,
                            speakerIndex = state.speakerIndex,
                            listenerIndex = state.listenerIndex,
                            nextSpeakerIndex = state.nextSpeakerIndex,
                            nextListenerIndex = state.nextListenerIndex,
                            restWordsNumber = state.restWordsNumber,
                            wordsInProgressNumber = state.wordsInProgressNumber,
                            wordsStatistic = state.wordsStatistic,
                            millisecondsLeft = state.millisecondsLeft,
                            leaderboardPermutation = state.leaderboardPermutation,
                        )
                    is Room.Outgoing.State.Round.LastGuess<RoomMetadata, PlayerMetadata, WordsProviderDescription> ->
                        ServerApi.OnlineGame.State.Round.LastGuess(
                            roomName = state.roomMetadata.name,
                            role = ServerApi.OnlineGame.Role.Round.LastGuess(
                                name = state.role.metadata.name,
                                userIndex = state.role.userIndex,
                                isHost = state.role.isHost,
                                roundRole = when (val role = state.role.roundRole) {
                                    Room.Outgoing.Role.Round.LastGuess.RoundRole.Player -> ServerApi.OnlineGame.Role.Round.LastGuess.RoundRole.Player
                                    Room.Outgoing.Role.Round.LastGuess.RoundRole.Listener -> ServerApi.OnlineGame.Role.Round.LastGuess.RoundRole.Listener
                                    is Room.Outgoing.Role.Round.LastGuess.RoundRole.Speaker -> ServerApi.OnlineGame.Role.Round.LastGuess.RoundRole.Speaker(role.currentWord)
                                },
                                roundsBeforeSpeaking = state.role.roundsBeforeSpeaking,
                                roundsBeforeListening = state.role.roundsBeforeListening,
                            ),
                            playersList = state.playersList.mapIndexed { index, player ->
                                ServerApi.OnlineGame.PlayerDescription.Round.LastGuess(
                                    name = player.metadata.name,
                                    userIndex = index,
                                    isOnline = player.isOnline,
                                    isHost = player.isHost,
                                    roundRole = when (player.roundRole) {
                                        Room.Outgoing.PlayerDescription.Round.RoundRole.Player -> ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Player
                                        Room.Outgoing.PlayerDescription.Round.RoundRole.Speaker -> ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Speaker
                                        Room.Outgoing.PlayerDescription.Round.RoundRole.Listener -> ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Listener
                                    },
                                    scoreExplained = player.scoreExplained,
                                    scoreGuessed = player.scoreGuessed,
                                    scoreSum = player.scoreSum,
                                )
                            },
                            settings = state.settings.toServerApi(),
                            initialWordsNumber = state.initialWordsNumber,
                            roundNumber = state.roundNumber,
                            cycleNumber = state.cycleNumber,
                            speakerIndex = state.speakerIndex,
                            listenerIndex = state.listenerIndex,
                            nextSpeakerIndex = state.nextSpeakerIndex,
                            nextListenerIndex = state.nextListenerIndex,
                            restWordsNumber = state.restWordsNumber,
                            wordsInProgressNumber = state.wordsInProgressNumber,
                            wordsStatistic = state.wordsStatistic,
                            millisecondsLeft = state.millisecondsLeft,
                            leaderboardPermutation = state.leaderboardPermutation,
                        )
                    is Room.Outgoing.State.Round.Editing<RoomMetadata, PlayerMetadata, WordsProviderDescription> ->
                        ServerApi.OnlineGame.State.Round.Editing(
                            roomName = state.roomMetadata.name,
                            role = ServerApi.OnlineGame.Role.Round.Editing(
                                name = state.role.metadata.name,
                                userIndex = state.role.userIndex,
                                isHost = state.role.isHost,
                                roundRole = when (val role = state.role.roundRole) {
                                    Room.Outgoing.Role.Round.Editing.RoundRole.Player -> ServerApi.OnlineGame.Role.Round.Editing.RoundRole.Player
                                    Room.Outgoing.Role.Round.Editing.RoundRole.Listener -> ServerApi.OnlineGame.Role.Round.Editing.RoundRole.Listener
                                    is Room.Outgoing.Role.Round.Editing.RoundRole.Speaker -> ServerApi.OnlineGame.Role.Round.Editing.RoundRole.Speaker(
                                        role.wordsToEdit
                                    )
                                },
                                roundsBeforeSpeaking = state.role.roundsBeforeSpeaking,
                                roundsBeforeListening = state.role.roundsBeforeListening,
                            ),
                            playersList = state.playersList.mapIndexed { index, player ->
                                ServerApi.OnlineGame.PlayerDescription.Round.Editing(
                                    name = player.metadata.name,
                                    userIndex = index,
                                    isOnline = player.isOnline,
                                    isHost = player.isHost,
                                    roundRole = when (player.roundRole) {
                                        Room.Outgoing.PlayerDescription.Round.RoundRole.Player -> ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Player
                                        Room.Outgoing.PlayerDescription.Round.RoundRole.Speaker -> ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Speaker
                                        Room.Outgoing.PlayerDescription.Round.RoundRole.Listener -> ServerApi.OnlineGame.PlayerDescription.Round.RoundRole.Listener
                                    },
                                    scoreExplained = player.scoreExplained,
                                    scoreGuessed = player.scoreGuessed,
                                    scoreSum = player.scoreSum,
                                )
                            },
                            settings = state.settings.toServerApi(),
                            initialWordsNumber = state.initialWordsNumber,
                            roundNumber = state.roundNumber,
                            cycleNumber = state.cycleNumber,
                            speakerIndex = state.speakerIndex,
                            listenerIndex = state.listenerIndex,
                            nextSpeakerIndex = state.nextSpeakerIndex,
                            nextListenerIndex = state.nextListenerIndex,
                            restWordsNumber = state.restWordsNumber,
                            wordsInProgressNumber = state.wordsInProgressNumber,
                            wordsStatistic = state.wordsStatistic,
                            leaderboardPermutation = state.leaderboardPermutation,
                        )
                    is Room.Outgoing.State.GameResults<RoomMetadata, PlayerMetadata, WordsProviderDescription> ->
                        ServerApi.OnlineGame.State.GameResults(
                            roomName = state.roomMetadata.name,
                            role = ServerApi.OnlineGame.Role.GameResults(
                                name = state.role.metadata.name,
                                userIndex = state.role.userIndex,
                                isHost = state.role.isHost,
                            ),
                            playersList = state.playersList.mapIndexed { index, player ->
                                ServerApi.OnlineGame.PlayerDescription.GameResults(
                                    name = player.metadata.name,
                                    userIndex = index,
                                    isOnline = player.isOnline,
                                    isHost = player.isHost,
                                    scoreExplained = player.scoreExplained,
                                    scoreGuessed = player.scoreGuessed,
                                    scoreSum = player.scoreSum,
                                )
                            },
                            settings = state.settings.toServerApi(),
                            leaderboardPermutation = state.leaderboardPermutation,
                            wordsStatistic = state.wordsStatistic,
                        )
                }
            )
        )
    }
    
    override suspend fun sendError(error: Room.Outgoing.Error<NoWordsProviderReason>) {
        val errorToSend = when (error) {
            is Room.Outgoing.Error.NoWordsProvider<NoWordsProviderReason> ->
                when (val reason = error.reason) {
                    NoWordsProviderReason.CannotFindDictionaryByID -> ServerApi.OnlineGame.Error.CannotFindDictionaryByID
                }
            Room.Outgoing.Error.AttachmentIsDenied -> ServerApi.OnlineGame.Error.AttachmentIsDenied
            Room.Outgoing.Error.AttachmentIsAlreadySevered -> ServerApi.OnlineGame.Error.AttachmentIsAlreadySevered
            Room.Outgoing.Error.NotHostChangingGameSettings -> ServerApi.OnlineGame.Error.NotHostChangingGameSettings
            Room.Outgoing.Error.CannotUpdateGameSettingsAfterInitialization -> ServerApi.OnlineGame.Error.CannotUpdateGameSettingsAfterInitialization
            Room.Outgoing.Error.NotEnoughPlayersForInitialization -> ServerApi.OnlineGame.Error.NotEnoughPlayersForInitialization
            Room.Outgoing.Error.CannotInitializeGameAfterInitialization -> ServerApi.OnlineGame.Error.CannotInitializeGameAfterInitialization
            Room.Outgoing.Error.PlayerAlreadySubmittedWords -> ServerApi.OnlineGame.Error.PlayerAlreadySubmittedWords
            Room.Outgoing.Error.CannotSubmitPlayerWordsNotDuringPlayersWordsCollection -> ServerApi.OnlineGame.Error.CannotSubmitPlayerWordsNotDuringPlayersWordsCollection
            Room.Outgoing.Error.CannotSetSpeakerReadinessNotDuringRoundWaiting -> ServerApi.OnlineGame.Error.CannotSetSpeakerReadinessNotDuringRoundWaiting
            Room.Outgoing.Error.NotSpeakerSettingSpeakerReadiness -> ServerApi.OnlineGame.Error.NotSpeakerSettingSpeakerReadiness
            Room.Outgoing.Error.CannotSetListenerReadinessNotDuringRoundWaiting -> ServerApi.OnlineGame.Error.CannotSetListenerReadinessNotDuringRoundWaiting
            Room.Outgoing.Error.NotListenerSettingListenerReadiness -> ServerApi.OnlineGame.Error.NotListenerSettingListenerReadiness
            Room.Outgoing.Error.CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting -> ServerApi.OnlineGame.Error.CannotSetSpeakerAndListenerReadinessNotDuringRoundWaiting
            Room.Outgoing.Error.CannotUpdateRoundInfoNotDuringTheRound -> ServerApi.OnlineGame.Error.CannotUpdateRoundInfoNotDuringTheRound
            Room.Outgoing.Error.CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess -> ServerApi.OnlineGame.Error.CannotSubmitWordExplanationResultNotDuringExplanationOrLastGuess
            Room.Outgoing.Error.NotSpeakerSubmittingWordExplanationResult -> ServerApi.OnlineGame.Error.NotSpeakerSubmittingWordExplanationResult
            Room.Outgoing.Error.CannotUpdateWordExplanationResultsNotDuringRoundEditing -> ServerApi.OnlineGame.Error.CannotUpdateWordExplanationResultsNotDuringRoundEditing
            Room.Outgoing.Error.NotSpeakerUpdatingWordExplanationResults -> ServerApi.OnlineGame.Error.NotSpeakerUpdatingWordExplanationResults
            Room.Outgoing.Error.CannotUpdateWordExplanationResultsWithOtherWordsSet -> ServerApi.OnlineGame.Error.CannotUpdateWordExplanationResultsWithOtherWordsSet
            Room.Outgoing.Error.CannotConfirmWordExplanationResultsNotDuringRoundEditing -> ServerApi.OnlineGame.Error.CannotConfirmWordExplanationResultsNotDuringRoundEditing
            Room.Outgoing.Error.NotSpeakerConfirmingWordExplanationResults -> ServerApi.OnlineGame.Error.NotSpeakerConfirmingWordExplanationResults
            Room.Outgoing.Error.CannotFinishGameNotDuringRoundWaiting -> ServerApi.OnlineGame.Error.CannotFinishGameNotDuringRoundWaiting
            Room.Outgoing.Error.NotHostFinishingGame -> ServerApi.OnlineGame.Error.NotHostFinishingGame
        }
        
        logger.debug(
            items = {
                mapOf(
                    "connection" to this.toString(),
                    "error" to errorToSend.toString(),
                )
            }
        ) { "Sending error" }
        
        socketSession.sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(errorToSend))
    }
}

fun main() {
    embeddedServer(Netty, port = 3000) {
        install(WebSockets) {
            pingPeriodMillis = 1000
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        routing {
            webSocket(path = "/ws") {
                val connection = Connection(this)
                logger.info(
                    items = {
                        mapOf("connection" to connection.toString())
                    }
                ) { "Opened connection" }
                try {
                    val converter = converter!!
                    for (frame in incoming) {
                        if (!converter.isApplicable(frame)) {
                            logger.info(
                                items = {
                                    mapOf(
                                        "connection" to connection.toString(),
                                        "frame" to frame.toString(),
                                    )
                                }
                            ) { "Received inconvertible frame. Skipping." }
                            continue
                        }
                        val signal = converter.deserialize<ClientApi.Signal>(frame)
                        
                        logger.info(
                            items = {
                                mapOf(
                                    "connection" to connection.toString(),
                                    "signal" to signal.toString(),
                                )
                            }
                        ) { "Received signal" }
                        
                        when (signal) {
                            is ClientApi.Signal.FetchFreeRoomId -> {
                                // TODO
                                logger.debug(
                                    items = {
                                        mapOf(
                                            "connection" to this.toString(),
                                        )
                                    }
                                ) { "Cannot answer to 'FetchFreeRoomId' as it is not implemented!" }
                            }
                            is ClientApi.Signal.FetchRoomInfo -> {
                                val room = rooms[signal.roomId]
                                
                                val signal =
                                    ServerApi.Signal.RoomInfo(
                                        room?.description
                                            ?.let {
                                                ServerApi.RoomDescription(
                                                    name = it.metadata.name,
                                                    playersList = it.playersList.map { description ->
                                                        ServerApi.PlayerDescription(
                                                            name = description.metadata.name,
                                                            isOnline = description.isOnline,
                                                            isHost = description.isHost,
                                                        )
                                                    },
                                                    state = when (it.stateType) {
                                                        Room.StateType.GameInitialisation -> ServerApi.RoomStateType.GameInitialisation
                                                        Room.StateType.PlayersWordsCollection -> ServerApi.RoomStateType.PlayersWordsCollection
                                                        Room.StateType.RoundWaiting -> ServerApi.RoomStateType.RoundWaiting
                                                        Room.StateType.RoundPreparation -> ServerApi.RoomStateType.RoundPreparation
                                                        Room.StateType.RoundExplanation -> ServerApi.RoomStateType.RoundExplanation
                                                        Room.StateType.RoundLastGuess -> ServerApi.RoomStateType.RoundLastGuess
                                                        Room.StateType.RoundEditing -> ServerApi.RoomStateType.RoundEditing
                                                        Room.StateType.GameResults -> ServerApi.RoomStateType.GameResults
                                                    }
                                                )
                                            }
                                            ?: ServerApi.RoomDescription(
                                                name = signal.roomId,
                                                playersList = KoneList.empty(),
                                                state = ServerApi.RoomStateType.GameInitialisation
                                            )
                                    )
                                
                                logger.debug(
                                    items = {
                                        mapOf(
                                            "connection" to connection.toString(),
                                            "signal" to signal.toString(),
                                        )
                                    }
                                ) { "Sending room info" }
                                
                                sendSerialized<ServerApi.Signal>(signal)
                            }
                            ClientApi.Signal.OnlineGame.RequestAvailableDictionaries -> connection.playerAttachmentMutex.withLock {
                                if (connection.playerAttachment == null) {
                                    logger.debug(
                                        items = {
                                            mapOf(
                                                "connection" to this.toString(),
                                            )
                                        }
                                    ) { "Available dictionaries request is allowed only from inside a room." }

                                    sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(ServerApi.OnlineGame.Error.NoAttachmentWhenItIsNeeded))
                                    return@withLock
                                }

                                val descriptions = OnlineGameWordsProviderRegistry.getAllWordsProviderDescriptions()
                                val serverApiDescriptions = descriptions.map {
                                    when (it) {
                                        is WordsProviderDescription.ServerDictionary.Builtin ->
                                            DictionaryId.WithDescription.Builtin(
                                                id = DictionaryId.Builtin(it.providerId.id),
                                                name = it.name,
                                                wordsNumber = it.wordsNumber,
                                            )
                                    }
                                }
                                sendSerialized<ServerApi.Signal>(ServerApi.Signal.AvailableDictionariesUpdate(serverApiDescriptions))
                            }
                            is ClientApi.Signal.OnlineGame.JoinRoom -> connection.playerAttachmentMutex.withLock {
                                if (connection.playerAttachment != null) {
                                    logger.debug(
                                        items = {
                                            mapOf(
                                                "connection" to this.toString(),
                                            )
                                        }
                                    ) { "Attachment is already provided" }
                                    
                                    sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(ServerApi.OnlineGame.Error.AttachmentIsAlreadyProvided))
                                    return@withLock
                                }
                                
                                logger.debug(
                                    items = {
                                        mapOf(
                                            "connection" to connection.toString(),
                                            "room" to signal.roomId,
                                            "player" to signal.playerName,
                                        )
                                    }
                                ) { "Trying to attach connection to player" }
                                
                                val attachmentResult = getRoomByIdOrCreate(signal.roomId).attachConnectionToPlayer(connection, signal.playerName)
                                
                                if (attachmentResult == null) {
                                    logger.debug(
                                        items = {
                                            mapOf(
                                                "connection" to connection.toString(),
                                                "room" to signal.roomId,
                                                "player" to signal.playerName,
                                                "result" to attachmentResult.toString(),
                                            )
                                        }
                                    ) { "Connection was not attached" }
                                } else {
                                    logger.debug(
                                        items = {
                                            mapOf(
                                                "connection" to connection.toString(),
                                                "room" to signal.roomId,
                                                "player" to signal.playerName,
                                                "result" to attachmentResult.toString(),
                                            )
                                        }
                                    ) { "Successfully attached connection to player" }
                                    
                                    connection.playerAttachment = attachmentResult
                                }
                            }
                            
                            ClientApi.Signal.OnlineGame.LeaveRoom -> connection.playerAttachmentMutex.withLock {
                                val attachment = connection.playerAttachment
                                
                                if (attachment == null) {
                                    logger.debug(
                                        items = {
                                            mapOf(
                                                "connection" to this.toString(),
                                            )
                                        }
                                    ) { "No attachment when it is needed" }
                                    
                                    sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(ServerApi.OnlineGame.Error.NoAttachmentWhenItIsNeeded))
                                    return@withLock
                                }
                                
                                attachment.sever()
                                
                                connection.playerAttachment = null
                                
                                logger.debug(
                                    items = {
                                        mapOf(
                                            "connection" to this.toString(),
                                        )
                                    }
                                ) { "Attachment is successfully severed" }
                            }
                            
                            is ClientApi.Signal.OnlineGame.UpdateSettings -> connection.playerAttachmentMutex.withLock {
                                val attachment = connection.playerAttachment
                                
                                if (attachment == null) {
                                    logger.debug(
                                        items = {
                                            mapOf(
                                                "connection" to this.toString(),
                                            )
                                        }
                                    ) { "No attachment when it is needed" }
                                    
                                    sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(ServerApi.OnlineGame.Error.NoAttachmentWhenItIsNeeded))
                                    return@withLock
                                }
                                
                                val settingsBuilderPatch = signal.settingsBuilderPatch
                                
                                attachment.updateGameSettings(
                                    Room.GameSettings.Builder.Patch(
                                        preparationTimeSeconds = settingsBuilderPatch.preparationTimeSeconds,
                                        explanationTimeSeconds = settingsBuilderPatch.explanationTimeSeconds,
                                        finalGuessTimeSeconds = settingsBuilderPatch.finalGuessTimeSeconds,
                                        strictMode = settingsBuilderPatch.strictMode,
                                        cachedEndConditionWordsNumber = settingsBuilderPatch.cachedEndConditionWordsNumber,
                                        cachedEndConditionCyclesNumber = settingsBuilderPatch.cachedEndConditionCyclesNumber,
                                        gameEndConditionType = settingsBuilderPatch.gameEndConditionType,
                                        wordsSource = when (val wordsSource = settingsBuilderPatch.wordsSource) {
                                            null -> null
                                            ClientApi.WordsSource.Players -> Room.WordsSource.Players
                                            is ClientApi.WordsSource.HostDictionary -> Room.WordsSource.Custom(WordsProviderId.HostDictionary(wordsSource.words))
                                            is ClientApi.WordsSource.ServerDictionary -> when (val dictionaryId = wordsSource.dictionaryId) {
                                                is DictionaryId.Builtin -> Room.WordsSource.Custom(WordsProviderId.ServerDictionary.Builtin(id = dictionaryId.id))
                                            }
                                        },
                                        showWordsStatistic = settingsBuilderPatch.showWordsStatistic,
                                        showLeaderboardPermutation = settingsBuilderPatch.showLeaderboardPermutation,
                                    )
                                )
                            }
                            ClientApi.Signal.OnlineGame.InitializeGame -> connection.playerAttachmentMutex.withLock {
                                val attachment = connection.playerAttachment
                                
                                if (attachment == null) {
                                    logger.debug(
                                        items = {
                                            mapOf(
                                                "connection" to this.toString(),
                                            )
                                        }
                                    ) { "No attachment when it is needed" }
                                    
                                    sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(ServerApi.OnlineGame.Error.NoAttachmentWhenItIsNeeded))
                                    return@withLock
                                }
                                
                                attachment.initializeGame()
                            }
                            is ClientApi.Signal.OnlineGame.SubmitWords -> connection.playerAttachmentMutex.withLock {
                                val attachment = connection.playerAttachment
                                
                                if (attachment == null) {
                                    logger.debug(
                                        items = {
                                            mapOf(
                                                "connection" to this.toString(),
                                            )
                                        }
                                    ) { "No attachment when it is needed" }
                                    
                                    sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(ServerApi.OnlineGame.Error.NoAttachmentWhenItIsNeeded))
                                    return@withLock
                                }
                                
                                attachment.submitWords(signal.words.toKoneSet())
                            }
                            ClientApi.Signal.OnlineGame.SpeakerReady -> connection.playerAttachmentMutex.withLock {
                                val attachment = connection.playerAttachment
                                
                                if (attachment == null) {
                                    logger.debug(
                                        items = {
                                            mapOf(
                                                "connection" to this.toString(),
                                            )
                                        }
                                    ) { "No attachment when it is needed" }
                                    
                                    sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(ServerApi.OnlineGame.Error.NoAttachmentWhenItIsNeeded))
                                    return@withLock
                                }
                                
                                attachment.speakerReady()
                            }
                            ClientApi.Signal.OnlineGame.ListenerReady -> connection.playerAttachmentMutex.withLock {
                                val attachment = connection.playerAttachment
                                
                                if (attachment == null) {
                                    logger.debug(
                                        items = {
                                            mapOf(
                                                "connection" to this.toString(),
                                            )
                                        }
                                    ) { "No attachment when it is needed" }
                                    
                                    sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(ServerApi.OnlineGame.Error.NoAttachmentWhenItIsNeeded))
                                    return@withLock
                                }
                                
                                attachment.listenerReady()
                            }
                            is ClientApi.Signal.OnlineGame.WordExplanationState -> connection.playerAttachmentMutex.withLock {
                                val attachment = connection.playerAttachment
                                
                                if (attachment == null) {
                                    logger.debug(
                                        items = {
                                            mapOf(
                                                "connection" to this.toString(),
                                            )
                                        }
                                    ) { "No attachment when it is needed" }
                                    
                                    sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(ServerApi.OnlineGame.Error.NoAttachmentWhenItIsNeeded))
                                    return@withLock
                                }
                                
                                attachment.wordExplanationState(signal.state)
                            }
                            is ClientApi.Signal.OnlineGame.UpdateWordsExplanationResults -> connection.playerAttachmentMutex.withLock {
                                val attachment = connection.playerAttachment
                                
                                if (attachment == null) {
                                    logger.debug(
                                        items = {
                                            mapOf(
                                                "connection" to this.toString(),
                                            )
                                        }
                                    ) { "No attachment when it is needed" }
                                    
                                    sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(ServerApi.OnlineGame.Error.NoAttachmentWhenItIsNeeded))
                                    return@withLock
                                }
                                
                                attachment.updateWordsExplanationResults(signal.newExplanationResults)
                            }
                            ClientApi.Signal.OnlineGame.ConfirmWordsExplanationResults -> connection.playerAttachmentMutex.withLock {
                                val attachment = connection.playerAttachment
                                
                                if (attachment == null) {
                                    logger.debug(
                                        items = {
                                            mapOf(
                                                "connection" to this.toString(),
                                            )
                                        }
                                    ) { "No attachment when it is needed" }
                                    
                                    sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(ServerApi.OnlineGame.Error.NoAttachmentWhenItIsNeeded))
                                    return@withLock
                                }
                                
                                attachment.confirmWordsExplanationResults()
                            }
                            ClientApi.Signal.OnlineGame.FinishGame -> connection.playerAttachmentMutex.withLock {
                                val attachment = connection.playerAttachment
                                
                                if (attachment == null) {
                                    logger.debug(
                                        items = {
                                            mapOf(
                                                "connection" to this.toString(),
                                            )
                                        }
                                    ) { "No attachment when it is needed" }
                                    
                                    sendSerialized<ServerApi.Signal>(ServerApi.Signal.OnlineGameError(ServerApi.OnlineGame.Error.NoAttachmentWhenItIsNeeded))
                                    return@withLock
                                }
                                
                                attachment.finishGame()
                            }
                        }
                    }
                } catch (exception: Exception) {
                    logger.warn(
                        throwable = exception,
                        items = {
                            mapOf("connection" to connection.toString())
                        }
                    ) { "Caught exception during WebSocket connection" }
                    throw exception
                } catch (exception: Error) {
                    logger.warn(
                        throwable = exception,
                        items = {
                            mapOf("connection" to connection.toString())
                        }
                    ) { "Caught error during WebSocket connection" }
                    throw exception
                } finally {
                    logger.info(
                        items = {
                            mapOf("connection" to connection.toString())
                        }
                    ) { "Closed connection" }
                    withContext(NonCancellable) {
                        connection.playerAttachmentMutex.withLock {
                            connection.playerAttachment?.sever()
                        }
                    }
                }
            }
        }
    }.start(wait = true)
}