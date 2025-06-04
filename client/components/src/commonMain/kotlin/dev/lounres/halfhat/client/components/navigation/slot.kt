package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.lifecycle
import dev.lounres.halfhat.client.components.uiChild
import dev.lounres.komponentual.lifecycle.*
import dev.lounres.komponentual.navigation.ChildrenSlot
import dev.lounres.komponentual.navigation.InnerSlotNavigationState
import dev.lounres.komponentual.navigation.SlotNavigation
import dev.lounres.komponentual.navigation.childrenSlot
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.relations.*
import dev.lounres.kone.state.KoneState


public fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenSlot(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: SlotNavigation<Configuration>,
    initialConfiguration: () -> Configuration,
    updateLifecycle: (configuration: Configuration, lifecycle: MutableUIComponentLifecycle, nextState: InnerSlotNavigationState<Configuration>) -> Unit,
    childrenFactory: (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneState<ChildrenSlot<Configuration, Component>> =
    childrenSlot(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        initialConfiguration = initialConfiguration,
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
> UIComponentContext.uiChildrenToSlot(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: SlotNavigation<Configuration>,
    initialConfiguration: () -> Configuration,
    activeState: UIComponentLifecycleState,
    childrenFactory: (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneState<ChildrenSlot<Configuration, Component>> =
    uiChildrenSlot(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        initialConfiguration = initialConfiguration,
        updateLifecycle = { configuration, lifecycle, nextState ->
            check(configurationEquality { configuration eq nextState.current }) { "For some reason, there is preserved configuration that is different from the only configuration in the slot" }
            lifecycle.moveTo(activeState)
        },
        childrenFactory = childrenFactory,
    )

public expect fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenDefaultSlot(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: SlotNavigation<Configuration>,
    initialConfiguration: () -> Configuration,
    childrenFactory: (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneState<ChildrenSlot<Configuration, Component>>