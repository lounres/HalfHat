package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen

import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundPreparation.FakeRoundPreparationComponent
import dev.lounres.komponentual.navigation.ChildrenStack
import dev.lounres.kone.state.KoneMutableState
import dev.lounres.kone.state.KoneState


class FakeGameScreenComponent(
    child: GameScreenComponent.Child = GameScreenComponent.Child.RoundPreparation(FakeRoundPreparationComponent()),
) : GameScreenComponent {
    override val onExitGame: () -> Unit = {}
    
    override val childStack: KoneState<ChildrenStack<*, GameScreenComponent.Child>> = KoneMutableState(ChildrenStack("", child))
}