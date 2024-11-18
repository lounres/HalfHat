package dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.halfhat.client.desktop.ui.components.game.deviceGame.gameScreen.roundPreparation.FakeRoundPreparationComponent


class FakeGameScreenComponent(
    child: GameScreenComponent.Child = GameScreenComponent.Child.RoundPreparation(FakeRoundPreparationComponent()),
) : GameScreenComponent {
    override val onExitGame: () -> Unit = {}
    
    override val childStack: Value<ChildStack<*, GameScreenComponent.Child>> = MutableValue(ChildStack("", child))
}