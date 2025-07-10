package dev.lounres.halfhat.client.components.logger

import dev.lounres.halfhat.client.components.LogicComponentContext
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.kone.registry.RegistryKey
import dev.lounres.kone.registry.getOrElse
import dev.lounres.logKube.core.CurrentPlatformLogger
import dev.lounres.logKube.core.LogLevel


public data object LoggerKey : RegistryKey<CurrentPlatformLogger<LogLevel>>

public val UIComponentContext.logger: CurrentPlatformLogger<LogLevel>
    get() = getOrElse(LoggerKey) { error("No UI component context logger registered") }

public val LogicComponentContext.logger: CurrentPlatformLogger<LogLevel>
    get() = getOrElse(LoggerKey) { error("No logic component context logger registered") }