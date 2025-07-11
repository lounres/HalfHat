package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.komponentual.navigation.ChildrenSlot
import dev.lounres.komponentual.navigation.SlotNavigation
import dev.lounres.kone.relations.Equality
import dev.lounres.kone.relations.Hashing
import dev.lounres.kone.relations.Order
import dev.lounres.kone.state.KoneAsynchronousState


public actual suspend fun <Configuration, Component> UIComponentContext.uiChildrenDefaultSlot(
    configurationEquality: Equality<Configuration>,
    configurationHashing: Hashing<Configuration>?,
    configurationOrder: Order<Configuration>?,
    loggerSource: String?,
    source: SlotNavigation<Configuration>,
    initialConfiguration: Configuration,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneAsynchronousState<ChildrenSlot<Configuration, Component>> =
    uiChildrenToSlot(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        loggerSource = loggerSource,
        source = source,
        initialConfiguration = initialConfiguration,
        activeState = UIComponentLifecycleState.Foreground,
        childrenFactory = childrenFactory,
    )