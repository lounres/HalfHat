package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.uiChild
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
    Configuration,
    Component,
> UIComponentContext.uiChildrenStack(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: StackNavigation<Configuration>,
    initialStack: () -> KoneList<Configuration>,
    updateLifecycle: (configuration: Configuration, lifecycle: MutableUIComponentLifecycle, nextState: InnerStackNavigationState<Configuration>) -> Unit,
    childrenFactory: (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneState<ChildrenStack<Configuration, Component>> =
    childrenStack(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        initialStack = initialStack,
        createChild = { configuration, nextState ->
            val controllingLifecycle = MutableUIComponentLifecycle()
            updateLifecycle(configuration, controllingLifecycle, nextState)
            val child = childrenFactory(configuration, this.uiChild(controllingLifecycle))
            Child(
                component = child,
                controllingLifecycle = controllingLifecycle,
            )
        },
        destroyChild = { it.controllingLifecycle.apply(UIComponentLifecycleTransition.Destroy) },
        updateChild = { configuration, data, nextState ->
            updateLifecycle(configuration, data.controllingLifecycle, nextState)
        },
        componentAccessor = { it.component },
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
    childrenFactory: (configuration: Configuration, componentContext: UIComponentContext) -> Component,
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

public expect fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenDefaultStack(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: StackNavigation<Configuration>,
    initialStack: () -> KoneList<Configuration>,
    childrenFactory: (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneState<ChildrenStack<Configuration, Component>>