package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.komponentual.navigation.VariantsNavigationTarget
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.relations.Equality
import dev.lounres.kone.relations.Hashing
import dev.lounres.kone.relations.Order


public actual suspend fun <Configuration, Component> UIComponentContext.uiChildrenDefaultVariantsItem(
    configurationEquality: Equality<Configuration>,
    configurationHashing: Hashing<Configuration>?,
    configurationOrder: Order<Configuration>?,
    loggerSource: String?,
    navigationControllerSpec: NavigationControllerSpec<Configuration>?,
    allVariants: KoneSet<Configuration>,
    initialVariant: Configuration,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: VariantsNavigationTarget<Configuration>) -> Component,
): VariantsItem<Configuration, Component> =
    uiChildrenFromToVariantsItem(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        loggerSource = loggerSource,
        navigationControllerSpec = navigationControllerSpec,
        allVariants = allVariants,
        initialVariant = initialVariant,
        inactiveState = UIComponentLifecycleState.Running,
        activeState = UIComponentLifecycleState.Foreground,
        childrenFactory = childrenFactory,
    )