package dev.lounres.halfhat.client.ui.components.miscellanea

import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.kone.hub.KoneMutableAsynchronousHub


interface MiscellaneaComponent {
    val darkTheme: KoneMutableAsynchronousHub<DarkTheme>
    val volumeOn: KoneMutableAsynchronousHub<Boolean>
    
    val openSettings: () -> Unit
    val openGameHistory: () -> Unit
    val openFeedback: () -> Unit
    val openRules: () -> Unit
    val openFAQ: () -> Unit
    val openAbout: () -> Unit
    val openNews: () -> Unit
}