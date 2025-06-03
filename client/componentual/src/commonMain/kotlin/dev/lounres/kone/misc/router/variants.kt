package dev.lounres.kone.misc.router

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.lifecycle
import dev.lounres.komponentual.lifecycle.*
import dev.lounres.komponentual.navigation.ChildrenVariants
import dev.lounres.komponentual.navigation.InnerVariantsNavigationState
import dev.lounres.komponentual.navigation.VariantsNavigation
import dev.lounres.komponentual.navigation.childrenVariants
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.relations.*
import dev.lounres.kone.state.KoneState


public fun <
    ParentLifecycle,
    ControllingLifecycle,
    ChildLifecycle,
    Configuration,
    Component,
> childrenVariants(
    parentLifecycle: ParentLifecycle,
    mutableControllingLifecycleProducer: () -> ControllingLifecycle,
    mergeLifecycles: (ParentLifecycle, ControllingLifecycle) -> ChildLifecycle,
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: VariantsNavigation<Configuration>,
    allVariants: () -> KoneSet<Configuration>,
    initialVariant: () -> Configuration,
    destroyLifecycle: (ControllingLifecycle) -> Unit,
    updateLifecycle: (configuration: Configuration, lifecycle: ControllingLifecycle, nextState: InnerVariantsNavigationState<Configuration>) -> Unit,
    childrenFactory: (configuration: Configuration, lifecycle: ChildLifecycle) -> Component,
): KoneState<ChildrenVariants<Configuration, Component>> =
    childrenVariants(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        allVariants = allVariants,
        initialVariant = initialVariant,
        createChild = { configuration, nextState ->
            val controllingLifecycle = mutableControllingLifecycleProducer()
            val childLifecycle = mergeLifecycles(parentLifecycle, controllingLifecycle)
            updateLifecycle(configuration, controllingLifecycle, nextState)
            val child = childrenFactory(configuration, childLifecycle)
            ChildWithLifecycle(
                component = child,
                lifecycle = controllingLifecycle,
            )
        },
        destroyChild = {
            destroyLifecycle(it.lifecycle)
        },
        updateChild = { configuration, data, nextState ->
            updateLifecycle(configuration, data.lifecycle, nextState)
        },
        componentAccessor = { it.component },
    )

public fun <
    ControllingLifecycle,
    ChildLifecycle,
    Configuration,
    Component,
> UIComponentContext.childrenVariants(
    mutableControllingLifecycleProducer: () -> ControllingLifecycle,
    mergeLifecycles: (UIComponentLifecycle, ControllingLifecycle) -> ChildLifecycle,
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: VariantsNavigation<Configuration>,
    allVariants: () -> KoneSet<Configuration>,
    initialVariant: () -> Configuration,
    destroyLifecycle: (ControllingLifecycle) -> Unit,
    updateLifecycle: (configuration: Configuration, lifecycle: ControllingLifecycle, nextState: InnerVariantsNavigationState<Configuration>) -> Unit,
    childrenFactory: (configuration: Configuration, lifecycle: ChildLifecycle) -> Component,
): KoneState<ChildrenVariants<Configuration, Component>> =
    childrenVariants(
        parentLifecycle = lifecycle,
        mutableControllingLifecycleProducer = mutableControllingLifecycleProducer,
        mergeLifecycles = mergeLifecycles,
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        allVariants = allVariants,
        initialVariant = initialVariant,
        destroyLifecycle = destroyLifecycle,
        updateLifecycle = updateLifecycle,
        childrenFactory = childrenFactory,
    )

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
    updateLifecycle: (configuration: Configuration, lifecycle: UIComponentLifecycle, nextState: InnerVariantsNavigationState<Configuration>) -> Unit,
    childrenFactory: (configuration: Configuration, lifecycle: UIComponentLifecycle) -> Component,
): KoneState<ChildrenVariants<Configuration, Component>> =
    childrenVariants(
        mutableControllingLifecycleProducer = ::MutableUIComponentLifecycle,
        mergeLifecycles = { parentLifecycle, controllingLifecycle ->  mergeUIComponentLifecycles(parentLifecycle, controllingLifecycle) },
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        allVariants = allVariants,
        initialVariant = initialVariant,
        destroyLifecycle = { it.apply(UIComponentLifecycleTransition.Destroy) },
        updateLifecycle = updateLifecycle,
        childrenFactory = childrenFactory,
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
    childrenFactory: (configuration: Configuration, lifecycle: UIComponentLifecycle) -> Component,
): KoneState<ChildrenVariants<Configuration, Component>> =
    childrenVariants(
        mutableControllingLifecycleProducer = ::MutableUIComponentLifecycle,
        mergeLifecycles = { parentLifecycle, controllingLifecycle ->  mergeUIComponentLifecycles(parentLifecycle, controllingLifecycle) },
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        allVariants = allVariants,
        initialVariant = initialVariant,
        destroyLifecycle = { it.apply(UIComponentLifecycleTransition.Destroy) },
        updateLifecycle = { configuration, lifecycle, nextState ->
            if (configurationEquality { configuration eq nextState.currentVariant }) lifecycle.moveTo(activeState)
            else lifecycle.moveTo(inactiveState)
        },
        childrenFactory = childrenFactory,
    )