package dev.lounres.halfhat.client.common.ui.components.game.modeSelection

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultPossibility
import dev.lounres.komponentual.navigation.ChildrenPossibility
import dev.lounres.komponentual.navigation.MutablePossibilityNavigation
import dev.lounres.komponentual.navigation.clear
import dev.lounres.komponentual.navigation.set
import dev.lounres.kone.maybe.None
import dev.lounres.kone.state.KoneAsynchronousState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


public class RealModeSelectionPageComponent(
    private val infoPopupNavigation: MutablePossibilityNavigation<ModeSelectionPageComponent.InfoPopup>,
    
    override val infoPopup: KoneAsynchronousState<ChildrenPossibility<*, ModeSelectionPageComponent.InfoPopup>>,
    
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
    
    val infoPopupNavigation = MutablePossibilityNavigation<ModeSelectionPageComponent.InfoPopup>(CoroutineScope(Dispatchers.Default))
    
    val infoPopup: KoneAsynchronousState<ChildrenPossibility<ModeSelectionPageComponent.InfoPopup, ModeSelectionPageComponent.InfoPopup>> =
        componentContext.uiChildrenDefaultPossibility(
            source = infoPopupNavigation,
            initialConfiguration = None,
        ) { configuration, _ -> configuration }
    
    return RealModeSelectionPageComponent(
        infoPopupNavigation = infoPopupNavigation,
        infoPopup = infoPopup,
        
        onOnlineGameSelect = onOnlineGameSelect,
        onLocalGameSelect = onLocalGameSelect,
        onDeviceGameSelect = onDeviceGameSelect,
        onGameTimerSelect = onGameTimerSelect,
    )
}