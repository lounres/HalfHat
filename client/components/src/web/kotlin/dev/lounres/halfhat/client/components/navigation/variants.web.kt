package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.komponentual.navigation.ChildrenVariants
import dev.lounres.komponentual.navigation.VariantsNavigation
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.relations.Equality
import dev.lounres.kone.relations.Hashing
import dev.lounres.kone.relations.Order
import dev.lounres.kone.state.KoneAsynchronousState


public actual suspend fun <Configuration, Component> UIComponentContext.uiChildrenDefaultVariants(
    configurationEquality: Equality<Configuration>,
    configurationHashing: Hashing<Configuration>?,
    configurationOrder: Order<Configuration>?,
    loggerSource: String?,
    source: VariantsNavigation<Configuration>,
    allVariants: KoneSet<Configuration>,
    initialVariant: Configuration,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneAsynchronousState<ChildrenVariants<Configuration, Component>> =
    uiChildrenFromToVariants(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        loggerSource = loggerSource,
        source = source,
        allVariants = allVariants,
        initialVariant = initialVariant,
        inactiveState = UIComponentLifecycleState.Running,
        activeState = UIComponentLifecycleState.Foreground,
        childrenFactory = childrenFactory,
    )