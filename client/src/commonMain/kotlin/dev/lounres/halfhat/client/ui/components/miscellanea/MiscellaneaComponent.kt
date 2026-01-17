package dev.lounres.halfhat.client.ui.components.miscellanea

import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.kone.hub.KoneMutableAsynchronousHubView


interface MiscellaneaComponent {
    val darkTheme: KoneMutableAsynchronousHubView<DarkTheme, *>
    val volumeOn: KoneMutableAsynchronousHubView<Boolean, *>
    
    val openSettings: () -> Unit
    val openGameHistory: () -> Unit
    val openFeedback: () -> Unit
    val openRules: () -> Unit
    val openFAQ: () -> Unit
    val openAbout: () -> Unit
    val openNews: () -> Unit
}