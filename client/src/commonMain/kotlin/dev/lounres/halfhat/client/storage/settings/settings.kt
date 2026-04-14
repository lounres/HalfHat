package dev.lounres.halfhat.client.storage.settings

import dev.lounres.halfhat.client.components.LogicComponentContext
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import dev.lounres.kone.hub.view
import dev.lounres.kone.registry.OwnedRegistry
import dev.lounres.kone.registry.MutableOwnedRegistry
import dev.lounres.kone.registry.Registry
import dev.lounres.kone.registry.RegistryKey
import dev.lounres.kone.registry.build
import dev.lounres.kone.registry.correspondsTo
import dev.lounres.kone.registry.serialization.RegistrySerializableKey
import dev.lounres.kone.registry.serialization.RegistrySerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline


@JvmInline
value class Settings(val elements: OwnedRegistry<Settings>) : Registry by elements {
    data object Key : RegistryKey<KoneMutableAsynchronousHub<Settings>>
}

inline fun Settings(builder: MutableOwnedRegistry<Settings>.() -> Unit): Settings {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }
    return Settings(OwnedRegistry.build(builder))
}

val UIComponentContext.settings: KoneMutableAsynchronousHub<Settings> get() = get(Settings.Key)
val LogicComponentContext.settings: KoneMutableAsynchronousHub<Settings> get() = get(Settings.Key)

class SettingsSerializer(
    serializableKeys: Map<String, RegistrySerializableKey<*>>,
) : KSerializer<Settings> {
    private val registrySerializer = RegistrySerializer(serializableKeys)
    override val descriptor: SerialDescriptor = SerialDescriptor("dev.lounres.halfhat.client.storage.settings.Settings", registrySerializer.descriptor)
    override fun serialize(encoder: Encoder, value: Settings) {
        encoder.encodeSerializableValue(registrySerializer, value.elements)
    }
    override fun deserialize(decoder: Decoder): Settings =
        Settings(OwnedRegistry(decoder.decodeSerializableValue(registrySerializer)))
}

operator fun <T> KoneMutableAsynchronousHub<Settings>.get(registryKey: RegistryKey<T>): KoneMutableAsynchronousHub<T> =
    view(
        get = { it[registryKey] },
        set = { settings, newT ->
            Settings {
                setFrom(settings)
                registryKey correspondsTo newT
            }
        }
    )