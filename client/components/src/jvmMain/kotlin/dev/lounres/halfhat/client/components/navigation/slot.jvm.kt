package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.komponentual.lifecycle.UIComponentLifecycle
import dev.lounres.komponentual.lifecycle.UIComponentLifecycleState
import dev.lounres.komponentual.navigation.ChildrenSlot
import dev.lounres.komponentual.navigation.SlotNavigation
import dev.lounres.kone.relations.Equality
import dev.lounres.kone.relations.Hashing
import dev.lounres.kone.relations.Order
import dev.lounres.kone.relations.defaultEquality
import dev.lounres.kone.state.KoneState


public actual fun <Configuration, Component> UIComponentContext.uiChildrenDefaultSlot(
    configurationEquality: Equality<Configuration>,
    configurationHashing: Hashing<Configuration>?,
    configurationOrder: Order<Configuration>?,
    source: SlotNavigation<Configuration>,
    initialConfiguration: () -> Configuration,
    childrenFactory: (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneState<ChildrenSlot<Configuration, Component>> =
    uiChildrenToSlot(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        initialConfiguration = initialConfiguration,
        activeState = UIComponentLifecycleState.Foreground,
        childrenFactory = childrenFactory,
    )