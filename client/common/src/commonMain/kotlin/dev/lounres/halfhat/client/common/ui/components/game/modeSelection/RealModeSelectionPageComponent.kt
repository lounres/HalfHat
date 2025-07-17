package dev.lounres.halfhat.client.common.ui.components.game.modeSelection

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.navigation.ChildrenPossibility
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultPossibilityItem
import dev.lounres.komponentual.navigation.PossibilityNavigationTarget
import dev.lounres.komponentual.navigation.clear
import dev.lounres.komponentual.navigation.set
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.maybe.None
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


public class RealModeSelectionPageComponent(
    private val infoPopupNavigation: PossibilityNavigationTarget<ModeSelectionPageComponent.InfoPopup>,
    
    override val infoPopup: KoneAsynchronousHub<ChildrenPossibility<*, ModeSelectionPageComponent.InfoPopup>>,
    
    override val onOnlineGameSelect: () -> Unit,
    override val onLocalGameSelect: () -> Unit,
    override val onDeviceGameSelect: () -> Unit,
    override val onGameTimerSelect: () -> Unit,
): ModeSelectionPageComponent {
    
    override val onOnlineGameInfo: () -> Unit = {
        CoroutineScope(Dispatchers.Default).launch {
            infoPopupNavigation.set(ModeSelectionPageComponent.InfoPopup.OnlineGame)
        }
    }
    override val onLocalGameInfo: () -> Unit = {
        CoroutineScope(Dispatchers.Default).launch {
            infoPopupNavigation.set(ModeSelectionPageComponent.InfoPopup.LocalGame)
        }
    }
    override val onDeviceGameInfo: () -> Unit = {
        CoroutineScope(Dispatchers.Default).launch {
            infoPopupNavigation.set(ModeSelectionPageComponent.InfoPopup.DeviceGame)
        }
    }
    override val onGameTimerInfo: () -> Unit = {
        CoroutineScope(Dispatchers.Default).launch {
            infoPopupNavigation.set(ModeSelectionPageComponent.InfoPopup.GameTimer)
        }
    }
    override val onCloseInfo: () -> Unit = {
        CoroutineScope(Dispatchers.Default).launch {
            infoPopupNavigation.clear()
        }
    }
}

public suspend fun RealModeSelectionPageComponent(
    componentContext: UIComponentContext,
    
    onOnlineGameSelect: () -> Unit,
    onLocalGameSelect: () -> Unit,
    onDeviceGameSelect: () -> Unit,
    onGameTimerSelect: () -> Unit,
): RealModeSelectionPageComponent {
    
    val infoPopup =
        componentContext.uiChildrenDefaultPossibilityItem<ModeSelectionPageComponent.InfoPopup, _>(
            initialConfiguration = None,
        ) { configuration, _, _ -> configuration }
    
    return RealModeSelectionPageComponent(
        infoPopupNavigation = infoPopup,
        infoPopup = infoPopup.hub,
        
        onOnlineGameSelect = onOnlineGameSelect,
        onLocalGameSelect = onLocalGameSelect,
        onDeviceGameSelect = onDeviceGameSelect,
        onGameTimerSelect = onGameTimerSelect,
    )
}