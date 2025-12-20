package dev.lounres.halfhat.client.utils

import dev.lounres.logKube.core.CurrentPlatformLogger
import dev.lounres.logKube.core.DefaultCurrentPlatformLogWriter
import dev.lounres.logKube.core.LogAcceptor
import dev.lounres.logKube.core.LogLevel
import dev.lounres.logKube.core.Logger


actual val logger: CurrentPlatformLogger<LogLevel> = Logger(
    name = "Desktop HalfHat application logger",
    LogAcceptor(DefaultCurrentPlatformLogWriter),
)