package dev.lounres.halfhat.client.desktop.ui.components.game

import dev.lounres.halfhat.client.desktop.ui.components.game.modeSelection.FakeModeSelectionPageComponent
import dev.lounres.komponentual.navigation.ChildrenSlot
import dev.lounres.kone.state.KoneMutableState
import dev.lounres.kone.state.KoneState


class FakeGamePageComponent(
    child: GamePageComponent.Child = GamePageComponent.Child.ModeSelection(FakeModeSelectionPageComponent())
): GamePageComponent {
    override val currentChild: KoneState<ChildrenSlot<*, GamePageComponent.Child>> =
        KoneMutableState(ChildrenSlot("", child))
}