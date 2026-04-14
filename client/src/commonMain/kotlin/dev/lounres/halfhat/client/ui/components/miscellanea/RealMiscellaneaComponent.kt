package dev.lounres.halfhat.client.ui.components.miscellanea

import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.kone.hub.KoneMutableAsynchronousHub


class RealMiscellaneaComponent(
    override val darkTheme: KoneMutableAsynchronousHub<DarkTheme>,
    override val volumeOn: KoneMutableAsynchronousHub<Boolean>,
    
    override val openSettings: () -> Unit,
    override val openGameHistory: () -> Unit,
    override val openFeedback: () -> Unit,
    override val openRules: () -> Unit,
    override val openFAQ: () -> Unit,
    override val openAbout: () -> Unit,
    override val openNews: () -> Unit,
) : MiscellaneaComponent