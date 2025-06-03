package dev.lounres.kone.misc.router

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.lifecycle
import dev.lounres.komponentual.lifecycle.*
import dev.lounres.komponentual.navigation.ChildrenPossibility
import dev.lounres.komponentual.navigation.InnerPossibilityNavigationState
import dev.lounres.komponentual.navigation.PossibilityNavigation
import dev.lounres.komponentual.navigation.childrenPossibility
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.maybe.Maybe
import dev.lounres.kone.maybe.Some
import dev.lounres.kone.relations.*
import dev.lounres.kone.state.KoneState


public fun <
    ParentLifecycle,
    ControllingLifecycle,
    ChildLifecycle,
    Configuration,
    Component,
> childrenPossibility(
    parentLifecycle: ParentLifecycle,
    mutableControllingLifecycleProducer: () -> ControllingLifecycle,
    mergeLifecycles: (ParentLifecycle, ControllingLifecycle) -> ChildLifecycle,
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: PossibilityNavigation<Configuration>,
    initialConfiguration: () -> Maybe<Configuration>,
    destroyLifecycle: (ControllingLifecycle) -> Unit,
    updateLifecycle: (configuration: Configuration, lifecycle: ControllingLifecycle, nextState: InnerPossibilityNavigationState<Configuration>) -> Unit,
    childrenFactory: (configuration: Configuration, lifecycle: ChildLifecycle) -> Component,
): KoneState<ChildrenPossibility<Configuration, Component>> =
    childrenPossibility(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        initialConfiguration = initialConfiguration,
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
        componentAccessor = { it.component }
    )

public fun <
    ControllingLifecycle,
    ChildLifecycle,
    Configuration,
    Component,
> UIComponentContext.childrenPossibility(
    mutableControllingLifecycleProducer: () -> ControllingLifecycle,
    mergeLifecycles: (UIComponentLifecycle, ControllingLifecycle) -> ChildLifecycle,
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: PossibilityNavigation<Configuration>,
    initialConfiguration: () -> Maybe<Configuration>,
    destroyLifecycle: (ControllingLifecycle) -> Unit,
    updateLifecycle: (configuration: Configuration, lifecycle: ControllingLifecycle, nextState: InnerPossibilityNavigationState<Configuration>) -> Unit,
    childrenFactory: (configuration: Configuration, lifecycle: ChildLifecycle) -> Component,
): KoneState<ChildrenPossibility<Configuration, Component>> =
    childrenPossibility(
        parentLifecycle = lifecycle,
        mutableControllingLifecycleProducer = mutableControllingLifecycleProducer,
        mergeLifecycles = mergeLifecycles,
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        initialConfiguration = initialConfiguration,
        destroyLifecycle = destroyLifecycle,
        updateLifecycle = updateLifecycle,
        childrenFactory = childrenFactory,
    )

public fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenPossibility(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: PossibilityNavigation<Configuration>,
    initialConfiguration: () -> Maybe<Configuration>,
    updateLifecycle: (configuration: Configuration, lifecycle: UIComponentLifecycle, nextState: InnerPossibilityNavigationState<Configuration>) -> Unit,
    childrenFactory: (configuration: Configuration, lifecycle: UIComponentLifecycle) -> Component,
): KoneState<ChildrenPossibility<Configuration, Component>> =
    childrenPossibility(
        mutableControllingLifecycleProducer = ::MutableUIComponentLifecycle,
        mergeLifecycles = { parentLifecycle, controllingLifecycle ->  mergeUIComponentLifecycles(parentLifecycle, controllingLifecycle) },
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        initialConfiguration = initialConfiguration,
        destroyLifecycle = { it.apply(UIComponentLifecycleTransition.Destroy) },
        updateLifecycle = updateLifecycle,
        childrenFactory = childrenFactory,
    )

public fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenToPossibility(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: PossibilityNavigation<Configuration>,
    initialConfiguration: () -> Maybe<Configuration>,
    activeState: UIComponentLifecycleState,
    childrenFactory: (configuration: Configuration, lifecycle: UIComponentLifecycle) -> Component,
): KoneState<ChildrenPossibility<Configuration, Component>> =
    childrenPossibility(
        mutableControllingLifecycleProducer = ::MutableUIComponentLifecycle,
        mergeLifecycles = { parentLifecycle, controllingLifecycle ->  mergeUIComponentLifecycles(parentLifecycle, controllingLifecycle) },
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        initialConfiguration = initialConfiguration,
        destroyLifecycle = { it.apply(UIComponentLifecycleTransition.Destroy) },
        updateLifecycle = { configuration, lifecycle, nextState ->
            check(nextState.current.let { it is Some<Configuration> && configurationEquality { it.value eq configuration } }) { "For some reason, there is preserved configuration that is different from the only configuration in the possibility" }
            lifecycle.moveTo(activeState)
        },
        childrenFactory = childrenFactory,
    )