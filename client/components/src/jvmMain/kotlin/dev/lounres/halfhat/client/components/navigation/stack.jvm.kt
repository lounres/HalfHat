package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.komponentual.navigation.StackNavigationSource
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.relations.Equality
import dev.lounres.kone.relations.Hashing
import dev.lounres.kone.relations.Order


public actual suspend fun <Configuration, Component> UIComponentContext.uiChildrenDefaultStack(
    configurationEquality: Equality<Configuration>,
    configurationHashing: Hashing<Configuration>?,
    configurationOrder: Order<Configuration>?,
    loggerSource: String?,
    source: StackNavigationSource<Configuration>,
    initialStack: KoneList<Configuration>,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneAsynchronousHub<ChildrenStack<Configuration, Component>> =
    uiChildrenFromToStack(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        loggerSource = loggerSource,
        source = source,
        initialStack = initialStack,
        inactiveState = UIComponentLifecycleState.Running,
        activeState = UIComponentLifecycleState.Foreground,
        childrenFactory = childrenFactory,
    )