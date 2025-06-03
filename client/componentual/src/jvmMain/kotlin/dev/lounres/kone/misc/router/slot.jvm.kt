package dev.lounres.kone.misc.router

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


public fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenToForegroundSlot(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: SlotNavigation<Configuration>,
    initialConfiguration: () -> Configuration,
    childrenFactory: (configuration: Configuration, lifecycle: UIComponentLifecycle) -> Component,
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