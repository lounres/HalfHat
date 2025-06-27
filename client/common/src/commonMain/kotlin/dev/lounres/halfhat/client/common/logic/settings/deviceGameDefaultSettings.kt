package dev.lounres.halfhat.client.common.logic.settings

import dev.lounres.halfhat.client.common.logic.wordsProviders.DeviceGameWordsProviderID
import dev.lounres.halfhat.client.components.LogicComponentContext
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.registry.RegistryKey
import dev.lounres.kone.state.KoneMutableAsynchronousState


public typealias DeviceGameDefaultSettingsState = KoneMutableAsynchronousState<GameStateMachine.GameSettings.Builder<DeviceGameWordsProviderID>>

public data object DeviceGameDefaultSettingsKey : RegistryKey<DeviceGameDefaultSettingsState>

public val UIComponentContext.deviceGameDefaultSettings: DeviceGameDefaultSettingsState get() = this[DeviceGameDefaultSettingsKey]
public val LogicComponentContext.deviceGameDefaultSettings: DeviceGameDefaultSettingsState get() = this[DeviceGameDefaultSettingsKey]