package dev.lounres.halfhat.client.logic.components.game.onlineGame

import dev.lounres.kone.registry.RegistryKey


public data class OnlineGameSettings(
    public val host: String?,
    public val port: Int?,
    public val path: String?,
    public val isSecure: Boolean,
)

public data object DefaultOnlineGameSettingsKey : RegistryKey<dev.lounres.halfhat.client.logic.components.game.onlineGame.OnlineGameSettings>