package dev.lounres.halfhat.client.common.logic.wordsProviders

import dev.lounres.halfhat.client.components.LogicComponentContext
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.registry.RegistryKey
import kotlinx.serialization.Serializable


public expect sealed interface NoDeviceGameWordsProviderReason

@Serializable
public expect sealed interface DeviceGameWordsProviderID

public expect class DeviceGameWordsProviderRegistry : GameStateMachine.WordsProviderRegistry<DeviceGameWordsProviderID, NoDeviceGameWordsProviderReason> {
    public override suspend operator fun get(providerId: DeviceGameWordsProviderID): GameStateMachine.WordsProviderRegistry.ResultOrReason<NoDeviceGameWordsProviderReason>
}

public data object DeviceGameWordsProviderRegistryKey: RegistryKey<DeviceGameWordsProviderRegistry>

public val UIComponentContext.deviceGameWordsProviderRegistry: DeviceGameWordsProviderRegistry get() = this[DeviceGameWordsProviderRegistryKey]
public val LogicComponentContext.deviceGameWordsProviderRegistry: DeviceGameWordsProviderRegistry get() = this[DeviceGameWordsProviderRegistryKey]