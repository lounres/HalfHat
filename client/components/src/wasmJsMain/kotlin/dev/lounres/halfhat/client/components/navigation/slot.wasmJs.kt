package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.komponentual.lifecycle.UIComponentLifecycleState
import dev.lounres.komponentual.navigation.ChildrenSlot
import dev.lounres.komponentual.navigation.SlotNavigation
import dev.lounres.kone.relations.Equality
import dev.lounres.kone.relations.Hashing
import dev.lounres.kone.relations.Order
import dev.lounres.kone.state.KoneState


public actual fun <Configuration, Component> UIComponentContext.uiChildrenDefaultSlot(
    configurationEquality: Equality<Configuration>,
    configurationHashing: Hashing<Configuration>?,
    configurationOrder: Order<Configuration>?,
    source: SlotNavigation<Configuration>,
    initialConfiguration: () -> Configuration,
    childrenFactory: (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneState<ChildrenSlot<Configuration, Component>> {
    TODO("Not yet implemented")
}