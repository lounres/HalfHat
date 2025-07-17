package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.komponentual.navigation.StackNavigationTarget
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.relations.Equality
import dev.lounres.kone.relations.Hashing
import dev.lounres.kone.relations.Order


public actual suspend fun <Configuration, Component> UIComponentContext.uiChildrenDefaultStackItem(
    configurationEquality: Equality<Configuration>,
    configurationHashing: Hashing<Configuration>?,
    configurationOrder: Order<Configuration>?,
    loggerSource: String?,
    navigationControllerSpec: NavigationControllerSpec<Configuration>?,
    initialStack: KoneList<Configuration>,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: StackNavigationTarget<Configuration>) -> Component,
): StackItem<Configuration, Component> =
    uiChildrenFromToStackItem(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        loggerSource = loggerSource,
        navigationControllerSpec = navigationControllerSpec,
        initialStack = initialStack,
        inactiveState = UIComponentLifecycleState.Running,
        activeState = UIComponentLifecycleState.Foreground,
        childrenFactory = childrenFactory,
    )