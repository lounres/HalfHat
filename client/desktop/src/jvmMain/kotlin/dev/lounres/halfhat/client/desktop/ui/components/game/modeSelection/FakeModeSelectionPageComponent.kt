package dev.lounres.halfhat.client.desktop.ui.components.game.modeSelection

import dev.lounres.komponentual.navigation.ChildWithConfiguration
import dev.lounres.komponentual.navigation.ChildrenPossibility
import dev.lounres.kone.maybe.Maybe
import dev.lounres.kone.maybe.None
import dev.lounres.kone.maybe.computeOn
import dev.lounres.kone.state.KoneMutableState
import dev.lounres.kone.state.KoneState


class FakeModeSelectionPageComponent(
    initialInfoPopup: Maybe<ModeSelectionPageComponent.InfoPopup> = None,
): ModeSelectionPageComponent {
    override val onOnlineGameSelect: () -> Unit = {}
    override val onLocalGameSelect: () -> Unit = {}
    override val onDeviceGameSelect: () -> Unit = {}
    override val onGameTimerSelect: () -> Unit = {}
    
    override val infoPopup: KoneState<ChildrenPossibility<*, ModeSelectionPageComponent.InfoPopup>> =
        KoneMutableState(initialInfoPopup.computeOn { ChildWithConfiguration(it, it) })
    
    override val onOnlineGameInfo: () -> Unit = {}
    override val onLocalGameInfo: () -> Unit = {}
    override val onDeviceGameInfo: () -> Unit = {}
    override val onGameTimerInfo: () -> Unit = {}
    override val onCloseInfo: () -> Unit = {}
}