package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.uiChild
import dev.lounres.komponentual.lifecycle.MutableUIComponentLifecycle
import dev.lounres.komponentual.lifecycle.UIComponentLifecycleState
import dev.lounres.komponentual.lifecycle.UIComponentLifecycleTransition
import dev.lounres.komponentual.lifecycle.moveTo
import dev.lounres.komponentual.navigation.ChildrenVariants
import dev.lounres.komponentual.navigation.InnerVariantsNavigationState
import dev.lounres.komponentual.navigation.VariantsNavigation
import dev.lounres.komponentual.navigation.childrenVariants
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.relations.*
import dev.lounres.kone.state.KoneState


public fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenVariants(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: VariantsNavigation<Configuration>,
    allVariants: () -> KoneSet<Configuration>,
    initialVariant: () -> Configuration,
    updateLifecycle: (configuration: Configuration, lifecycle: MutableUIComponentLifecycle, nextState: InnerVariantsNavigationState<Configuration>) -> Unit,
    childrenFactory: (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneState<ChildrenVariants<Configuration, Component>> =
    childrenVariants(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        allVariants = allVariants,
        initialVariant = initialVariant,
        createChild = { configuration, nextState ->
            val controllingLifecycle = MutableUIComponentLifecycle()
            updateLifecycle(configuration, controllingLifecycle, nextState)
            val child = childrenFactory(configuration, this.uiChild(controllingLifecycle))
            Child(
                component = child,
                controllingLifecycle = controllingLifecycle,
            )
        },
        destroyChild = { it.controllingLifecycle.move(UIComponentLifecycleTransition.Destroy) },
        updateChild = { configuration, data, nextState ->
            updateLifecycle(configuration, data.controllingLifecycle, nextState)
        },
        componentAccessor = { it.component },
    )

public fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenFromToVariants(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: VariantsNavigation<Configuration>,
    allVariants: () -> KoneSet<Configuration>,
    initialVariant: () -> Configuration,
    inactiveState: UIComponentLifecycleState,
    activeState: UIComponentLifecycleState,
    childrenFactory: (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneState<ChildrenVariants<Configuration, Component>> =
    uiChildrenVariants(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        allVariants = allVariants,
        initialVariant = initialVariant,
        updateLifecycle = { configuration, lifecycle, nextState ->
            if (configurationEquality { configuration eq nextState.currentVariant }) lifecycle.moveTo(activeState)
            else lifecycle.moveTo(inactiveState)
        },
        childrenFactory = childrenFactory,
    )

public expect fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenDefaultVariants(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: VariantsNavigation<Configuration>,
    allVariants: () -> KoneSet<Configuration>,
    initialVariant: () -> Configuration,
    childrenFactory: (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneState<ChildrenVariants<Configuration, Component>>