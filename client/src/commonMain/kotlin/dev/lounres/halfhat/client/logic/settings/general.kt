package dev.lounres.halfhat.client.logic.settings

import dev.lounres.halfhat.Language
import dev.lounres.halfhat.client.storage.settings.Settings
import dev.lounres.halfhat.client.storage.settings.get
import dev.lounres.kone.hub.KoneMutableAsynchronousHubView
import dev.lounres.kone.registry.serialization.RegistrySerializableKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer


data object VolumeOnKey : RegistrySerializableKey<Boolean> {
    override val serializer: KSerializer<Boolean> = Boolean.serializer()
}

val Settings.volumeOn: Boolean get() = get(VolumeOnKey)
val KoneMutableAsynchronousHubView<Settings, *>.volumeOn: KoneMutableAsynchronousHubView<Boolean, *> get() = get(VolumeOnKey)

data object LanguageKey : RegistrySerializableKey<Language> {
    override val serializer: KSerializer<Language> = Language.serializer()
}

val Settings.language: Language get() = get(LanguageKey)
val KoneMutableAsynchronousHubView<Settings, *>.language: KoneMutableAsynchronousHubView<Language, *> get() = get(LanguageKey)