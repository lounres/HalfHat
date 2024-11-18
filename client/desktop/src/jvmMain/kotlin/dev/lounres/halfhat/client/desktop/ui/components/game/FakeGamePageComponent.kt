package dev.lounres.halfhat.client.desktop.ui.components.game

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.lounres.halfhat.client.desktop.ui.components.game.modeSelection.FakeModeSelectionPageComponent


class FakeGamePageComponent(
    child: GamePageComponent.Child = GamePageComponent.Child.ModeSelection(FakeModeSelectionPageComponent())
): GamePageComponent {
    override val childStack: Value<ChildStack<*, GamePageComponent.Child>> = MutableValue(ChildStack("", child))
}