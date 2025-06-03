package dev.lounres.kone.misc.router

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.lifecycle
import dev.lounres.komponentual.lifecycle.*
import dev.lounres.komponentual.navigation.ChildrenSlot
import dev.lounres.komponentual.navigation.InnerSlotNavigationState
import dev.lounres.komponentual.navigation.SlotNavigation
import dev.lounres.komponentual.navigation.childrenSlot
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.relations.*
import dev.lounres.kone.state.KoneState


public fun <
    ParentLifecycle,
    ControllingLifecycle,
    ChildLifecycle,
    Configuration,
    Component,
> childrenSlot(
    parentLifecycle: ParentLifecycle,
    mutableControllingLifecycleProducer: () -> ControllingLifecycle,
    mergeLifecycles: (ParentLifecycle, ControllingLifecycle) -> ChildLifecycle,
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: SlotNavigation<Configuration>,
    initialConfiguration: () -> Configuration,
    destroyLifecycle: (ControllingLifecycle) -> Unit,
    updateLifecycle: (configuration: Configuration, lifecycle: ControllingLifecycle, nextState: InnerSlotNavigationState<Configuration>) -> Unit,
    childrenFactory: (configuration: Configuration, lifecycle: ChildLifecycle) -> Component,
): KoneState<ChildrenSlot<Configuration, Component>> =
    childrenSlot(
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
> UIComponentContext.childrenSlot(
    mutableControllingLifecycleProducer: () -> ControllingLifecycle,
    mergeLifecycles: (UIComponentLifecycle, ControllingLifecycle) -> ChildLifecycle,
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: SlotNavigation<Configuration>,
    initialConfiguration: () -> Configuration,
    destroyLifecycle: (ControllingLifecycle) -> Unit,
    updateLifecycle: (configuration: Configuration, lifecycle: ControllingLifecycle, nextState: InnerSlotNavigationState<Configuration>) -> Unit,
    childrenFactory: (configuration: Configuration, lifecycle: ChildLifecycle) -> Component,
): KoneState<ChildrenSlot<Configuration, Component>> =
    childrenSlot(
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
> UIComponentContext.uiChildrenSlot(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: SlotNavigation<Configuration>,
    initialConfiguration: () -> Configuration,
    updateLifecycle: (configuration: Configuration, lifecycle: UIComponentLifecycle, nextState: InnerSlotNavigationState<Configuration>) -> Unit,
    childrenFactory: (configuration: Configuration, lifecycle: UIComponentLifecycle) -> Component,
): KoneState<ChildrenSlot<Configuration, Component>> =
    childrenSlot(
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
> UIComponentContext.uiChildrenToSlot(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: SlotNavigation<Configuration>,
    initialConfiguration: () -> Configuration,
    activeState: UIComponentLifecycleState,
    childrenFactory: (configuration: Configuration, lifecycle: UIComponentLifecycle) -> Component,
): KoneState<ChildrenSlot<Configuration, Component>> =
    childrenSlot(
        mutableControllingLifecycleProducer = ::MutableUIComponentLifecycle,
        mergeLifecycles = { parentLifecycle, controllingLifecycle ->  mergeUIComponentLifecycles(parentLifecycle, controllingLifecycle) },
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        initialConfiguration = initialConfiguration,
        destroyLifecycle = { it.apply(UIComponentLifecycleTransition.Destroy) },
        updateLifecycle = { configuration, lifecycle, nextState ->
            check(configurationEquality { configuration eq nextState.current }) {
                println(
                    """
                        configuration: $configuration
                        nextState: ${nextState.current}
                    """.trimIndent()
                )
                "For some reason, there is preserved configuration that is different from the only configuration in the slot"
            }
            lifecycle.moveTo(activeState)
        },
        childrenFactory = childrenFactory,
    )