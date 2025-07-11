package dev.lounres.halfhat.client.common.logic.wordsProviders

import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.interop.toKoneList
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.of
import dev.lounres.kone.collections.map.KoneMap
import dev.lounres.kone.collections.map.getOrElse
import dev.lounres.kone.collections.map.mapsTo
import dev.lounres.kone.collections.map.of
import dev.lounres.kone.collections.set.KoneMutableSet
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.set.of
import dev.lounres.kone.collections.set.toKoneSet
import dev.lounres.kone.collections.utils.filterTo
import dev.lounres.kone.collections.utils.shuffled
import dev.lounres.kone.collections.utils.take
import dev.lounres.kone.relations.defaultEquality
import dev.lounres.kone.relations.defaultHashing
import dev.lounres.kone.scope
import js.buffer.ArrayBufferLike
import js.core.JsPrimitives.toJsByte
import js.typedarrays.Int8Array
import kotlinx.serialization.Serializable
import web.encoding.TextDecoder
import kotlin.random.Random


public actual sealed interface NoDeviceGameWordsProviderReason {
    public data object NoSuchWordProvider : NoDeviceGameWordsProviderReason
}

@Serializable
public actual sealed interface DeviceGameWordsProviderID {
    public actual val name: String
    @Serializable
    public data class Local(val id: String) : DeviceGameWordsProviderID {
        override val name: String get() = id
    }
}

public class LocalDeviceGameWordsProvider(
    private val words: KoneSet<String>,
) : GameStateMachine.WordsProvider {
    override val size: UInt = words.size
    override fun allWords(): KoneSet<String> = words
    override fun randomWords(number: UInt): KoneSet<String> =
        if (number <= words.size) words.shuffled(Random).take(number).toKoneSet()
        else error("Cannot get $number random words from words provider of size ${words.size}")
}

public actual object DeviceGameWordsProviderRegistry : GameStateMachine.WordsProviderRegistry<DeviceGameWordsProviderID, NoDeviceGameWordsProviderReason> {
    private val decoder = TextDecoder("utf-8")
    
    private val localWordsProviders =
        KoneMap.of(
            "easy" mapsTo "files/wordsProviders/easy",
            "medium" mapsTo "files/wordsProviders/medium",
            "hard" mapsTo "files/wordsProviders/hard",
            keyEquality = defaultEquality(),
            keyHashing = defaultHashing(),
        )
    
    private val localWordsProvidersIDs =
        KoneList.of(
            DeviceGameWordsProviderID.Local("easy"),
            DeviceGameWordsProviderID.Local("medium"),
            DeviceGameWordsProviderID.Local("hard"),
        )
    
    public actual suspend fun list(): KoneList<DeviceGameWordsProviderID> = localWordsProvidersIDs
    
    actual override suspend operator fun get(providerId: DeviceGameWordsProviderID): GameStateMachine.WordsProviderRegistry.ResultOrReason<NoDeviceGameWordsProviderReason> =
        when (providerId) {
            is DeviceGameWordsProviderID.Local -> scope {
                val wordsSetPath = localWordsProviders.getOrElse(providerId.id) {
                    return@scope GameStateMachine.WordsProviderRegistry.ResultOrReason.Failure(
                        NoDeviceGameWordsProviderReason.NoSuchWordProvider
                    )
                }
                
                GameStateMachine.WordsProviderRegistry.ResultOrReason.Success(
                    LocalDeviceGameWordsProvider(
                        Res
                            .readBytes(wordsSetPath)
                            .let { decoder.decode(Int8Array<ArrayBufferLike>(it.size).apply { it.forEachIndexed { index, b -> this[index] = b.toJsByte() } }) }
                            .lines()
                            .toKoneList()
                            .filterTo(KoneMutableSet.of()) { line -> line.isNotBlank() }
                    )
                )
            }
        }
}