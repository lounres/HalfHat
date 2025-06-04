package dev.lounres.halfhat.client.desktop.ui.components.game.modeSelection

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultPossibility
import dev.lounres.komponentual.navigation.ChildrenPossibility
import dev.lounres.komponentual.navigation.MutablePossibilityNavigation
import dev.lounres.komponentual.navigation.clear
import dev.lounres.komponentual.navigation.set
import dev.lounres.kone.maybe.None
import dev.lounres.kone.state.KoneState


class RealModeSelectionPageComponent(
    componentContext: UIComponentContext,
    
    override val onOnlineGameSelect: () -> Unit,
    override val onLocalGameSelect: () -> Unit,
    override val onDeviceGameSelect: () -> Unit,
    override val onGameTimerSelect: () -> Unit,
): ModeSelectionPageComponent {
    private val infoPopupNavigation = MutablePossibilityNavigation<ModeSelectionPageComponent.InfoPopup>()
    
    override val infoPopup: KoneState<ChildrenPossibility<ModeSelectionPageComponent.InfoPopup, ModeSelectionPageComponent.InfoPopup>> =
        componentContext.uiChildrenDefaultPossibility(
            source = infoPopupNavigation,
            initialConfiguration = { None }
        ) { configuration, _ -> configuration }
    
    override val onOnlineGameInfo: () -> Unit = { infoPopupNavigation.set(ModeSelectionPageComponent.InfoPopup.OnlineGame) }
    override val onLocalGameInfo: () -> Unit = { infoPopupNavigation.set(ModeSelectionPageComponent.InfoPopup.LocalGame) }
    override val onDeviceGameInfo: () -> Unit = { infoPopupNavigation.set(ModeSelectionPageComponent.InfoPopup.DeviceGame) }
    override val onGameTimerInfo: () -> Unit = { infoPopupNavigation.set(ModeSelectionPageComponent.InfoPopup.GameTimer) }
    override val onCloseInfo: () -> Unit = { infoPopupNavigation.clear() }
}