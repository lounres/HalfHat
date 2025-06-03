package dev.lounres.kone.misc.router

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.lifecycle
import dev.lounres.komponentual.lifecycle.*
import dev.lounres.komponentual.navigation.ChildrenStack
import dev.lounres.komponentual.navigation.InnerStackNavigationState
import dev.lounres.komponentual.navigation.StackNavigation
import dev.lounres.komponentual.navigation.childrenStack
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.utils.last
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.relations.*
import dev.lounres.kone.state.KoneState


public fun <
    ParentLifecycle,
    ControllingLifecycle,
    ChildLifecycle,
    Configuration,
    Component,
> childrenStack(
    parentLifecycle: ParentLifecycle,
    mutableControllingLifecycleProducer: () -> ControllingLifecycle,
    mergeLifecycles: (ParentLifecycle, ControllingLifecycle) -> ChildLifecycle,
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: StackNavigation<Configuration>,
    initialStack: () -> KoneList<Configuration>,
    destroyLifecycle: (ControllingLifecycle) -> Unit,
    updateLifecycle: (configuration: Configuration, lifecycle: ControllingLifecycle, nextState: InnerStackNavigationState<Configuration>) -> Unit,
    childrenFactory: (configuration: Configuration, lifecycle: ChildLifecycle) -> Component,
): KoneState<ChildrenStack<Configuration, Component>> =
    childrenStack(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        initialStack = initialStack,
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
> UIComponentContext.childrenStack(
    mutableControllingLifecycleProducer: () -> ControllingLifecycle,
    mergeLifecycles: (UIComponentLifecycle, ControllingLifecycle) -> ChildLifecycle,
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: StackNavigation<Configuration>,
    initialStack: () -> KoneList<Configuration>,
    destroyLifecycle: (ControllingLifecycle) -> Unit,
    updateLifecycle: (configuration: Configuration, lifecycle: ControllingLifecycle, nextState: InnerStackNavigationState<Configuration>) -> Unit,
    childrenFactory: (configuration: Configuration, lifecycle: ChildLifecycle) -> Component,
): KoneState<ChildrenStack<Configuration, Component>> =
    childrenStack(
        parentLifecycle = lifecycle,
        mutableControllingLifecycleProducer = mutableControllingLifecycleProducer,
        mergeLifecycles = mergeLifecycles,
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        initialStack = initialStack,
        destroyLifecycle = destroyLifecycle,
        updateLifecycle = updateLifecycle,
        childrenFactory = childrenFactory,
    )

public fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenStack(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: StackNavigation<Configuration>,
    initialStack: () -> KoneList<Configuration>,
    updateLifecycle: (configuration: Configuration, lifecycle: MutableUIComponentLifecycle, nextState: InnerStackNavigationState<Configuration>) -> Unit,
    childrenFactory: (configuration: Configuration, lifecycle: UIComponentLifecycle) -> Component,
): KoneState<ChildrenStack<Configuration, Component>> =
    childrenStack(
        mutableControllingLifecycleProducer = ::MutableUIComponentLifecycle,
        mergeLifecycles = { parentLifecycle, controllingLifecycle ->  mergeUIComponentLifecycles(parentLifecycle, controllingLifecycle) },
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        initialStack = initialStack,
        destroyLifecycle = { it.apply(UIComponentLifecycleTransition.Destroy) },
        updateLifecycle = updateLifecycle,
        childrenFactory = childrenFactory,
    )

public fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenFromToStack(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: StackNavigation<Configuration>,
    initialStack: () -> KoneList<Configuration>,
    inactiveState: UIComponentLifecycleState,
    activeState: UIComponentLifecycleState,
    childrenFactory: (configuration: Configuration, lifecycle: UIComponentLifecycle) -> Component,
): KoneState<ChildrenStack<Configuration, Component>> =
    uiChildrenStack(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        initialStack = initialStack,
        updateLifecycle = { configuration, lifecycle, nextState ->
            if (configurationEquality { configuration eq nextState.stack.last() }) lifecycle.moveTo(activeState)
            else lifecycle.moveTo(inactiveState)
        },
        childrenFactory = childrenFactory,
    )