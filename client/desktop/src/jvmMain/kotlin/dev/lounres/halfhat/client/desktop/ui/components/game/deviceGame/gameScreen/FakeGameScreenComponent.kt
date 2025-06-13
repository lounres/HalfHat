package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen

import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundPreparation.FakeRoundPreparationComponent
import dev.lounres.komponentual.navigation.ChildrenSlot
import dev.lounres.kone.state.KoneMutableState
import dev.lounres.kone.state.KoneState


class FakeGameScreenComponent(
    child: GameScreenComponent.Child = GameScreenComponent.Child.RoundPreparation(FakeRoundPreparationComponent()),
) : GameScreenComponent {
    override val onExitGame: () -> Unit = {}
    
    override val childStack: KoneState<ChildrenSlot<*, GameScreenComponent.Child>> = KoneMutableState(ChildrenSlot("", child))
}