package dev.lounres.halfhat.client.logic.settings

import dev.lounres.halfhat.Language
import dev.lounres.halfhat.client.logic.wordsProviders.DeviceGameWordsProviderID
import dev.lounres.halfhat.client.storage.settings.Settings
import dev.lounres.halfhat.client.storage.settings.get
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import dev.lounres.kone.registry.serialization.RegistrySerializableKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer


data object VolumeOnKey : RegistrySerializableKey<Boolean> {
    override val serializer: KSerializer<Boolean> = Boolean.serializer()
}

val Settings.volumeOn: Boolean get() = get(VolumeOnKey)
val KoneMutableAsynchronousHub<Settings>.volumeOn: KoneMutableAsynchronousHub<Boolean> get() = get(VolumeOnKey)

data object LanguageKey : RegistrySerializableKey<Language> {
    override val serializer: KSerializer<Language> = Language.serializer()
}

val Settings.language: Language get() = get(LanguageKey)
val KoneMutableAsynchronousHub<Settings>.language: KoneMutableAsynchronousHub<Language> get() = get(LanguageKey)

data object DeviceGameDefaultSettingsKey : RegistrySerializableKey<GameStateMachine.GameSettings.Builder<DeviceGameWordsProviderID>> {
    override val serializer: KSerializer<GameStateMachine.GameSettings.Builder<DeviceGameWordsProviderID>> =
        GameStateMachine.GameSettings.Builder.serializer(DeviceGameWordsProviderID.serializer())
}

val Settings.deviceGameDefaultSettings: GameStateMachine.GameSettings.Builder<DeviceGameWordsProviderID> get() = this[DeviceGameDefaultSettingsKey]
val KoneMutableAsynchronousHub<Settings>.deviceGameDefaultSettings: KoneMutableAsynchronousHub<GameStateMachine.GameSettings.Builder<DeviceGameWordsProviderID>> get() = this[DeviceGameDefaultSettingsKey]