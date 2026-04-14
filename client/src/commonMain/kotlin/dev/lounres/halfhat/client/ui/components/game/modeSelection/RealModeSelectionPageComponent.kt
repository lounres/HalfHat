package dev.lounres.halfhat.client.ui.components.game.modeSelection

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.navigation.ChildrenPossibility
import dev.lounres.halfhat.client.components.navigation.uiChildrenDefaultPossibilityNode
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
    
    override val infoPopup: KoneAsynchronousHub<ChildrenPossibility<*, ModeSelectionPageComponent.InfoPopup, UIComponentContext>>,
    
    override val onOnlineGameSelect: () -> Unit,
    override val onLocalGameSelect: () -> Unit,
    override val onDeviceGameSelect: () -> Unit,
    override val onGameControllerSelect: () -> Unit,
    override val onGameTimerSelect: () -> Unit,
    
    coroutineScope: CoroutineScope,
): ModeSelectionPageComponent {
    
    override val onOnlineGameInfo: () -> Unit = {
        coroutineScope.launch {
            infoPopupNavigation.set(ModeSelectionPageComponent.InfoPopup.OnlineGame)
        }
    }
    override val onLocalGameInfo: () -> Unit = {
        coroutineScope.launch {
            infoPopupNavigation.set(ModeSelectionPageComponent.InfoPopup.LocalGame)
        }
    }
    override val onDeviceGameInfo: () -> Unit = {
        coroutineScope.launch {
            infoPopupNavigation.set(ModeSelectionPageComponent.InfoPopup.DeviceGame)
        }
    }
    override val onGameControllerInfo: () -> Unit = {
        coroutineScope.launch {
            infoPopupNavigation.set(ModeSelectionPageComponent.InfoPopup.GameController)
        }
    }
    override val onGameTimerInfo: () -> Unit = {
        coroutineScope.launch {
            infoPopupNavigation.set(ModeSelectionPageComponent.InfoPopup.GameTimer)
        }
    }
    override val onCloseInfo: () -> Unit = {
        coroutineScope.launch {
            infoPopupNavigation.clear()
        }
    }
}

public suspend fun RealModeSelectionPageComponent(
    componentContext: UIComponentContext,
    
    onOnlineGameSelect: () -> Unit,
    onLocalGameSelect: () -> Unit,
    onDeviceGameSelect: () -> Unit,
    onGameControllerSelect: () -> Unit,
    onGameTimerSelect: () -> Unit,
): RealModeSelectionPageComponent {
    
    val infoPopup =
        componentContext.uiChildrenDefaultPossibilityNode<ModeSelectionPageComponent.InfoPopup, _>(
            initialConfiguration = None,
        ) { configuration, _, _ -> configuration }
    
    return RealModeSelectionPageComponent(
        infoPopupNavigation = infoPopup,
        infoPopup = infoPopup.hub,
        
        onOnlineGameSelect = onOnlineGameSelect,
        onLocalGameSelect = onLocalGameSelect,
        onDeviceGameSelect = onDeviceGameSelect,
        onGameControllerSelect = onGameControllerSelect,
        onGameTimerSelect = onGameTimerSelect,
        
        coroutineScope = componentContext.coroutineScope(Dispatchers.Default)
    )
}