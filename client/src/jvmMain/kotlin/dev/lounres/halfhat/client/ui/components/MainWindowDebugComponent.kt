package dev.lounres.halfhat.client.ui.components

import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.hub.KoneMutableAsynchronousHub


interface MainWindowDebugComponent {
    public val darkTheme: KoneMutableAsynchronousHub<DarkTheme>
    public val mainComponents: KoneList<MainWindowComponent>
}