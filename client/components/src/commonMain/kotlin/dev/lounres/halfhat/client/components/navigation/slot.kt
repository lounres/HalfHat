package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.komponentual.navigation.SlotNavigationEvent
import dev.lounres.komponentual.navigation.SlotNavigationTarget
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.set.of
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.relations.*


public typealias ChildrenSlot<Configuration, Component> = ChildWithConfiguration<Configuration, Component>

public typealias SlotNode<Configuration, Component> = ChildrenNode<Configuration, Component, ChildrenSlot<Configuration, Component>, SlotNavigationEvent<Configuration>>

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenSlotNode(
    configurationEquality: Equality<Configuration> = Equality.defaultFor(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<Configuration, Configuration, Component, UIComponentContext, SlotNavigationEvent<Configuration>>? = null,
    initialConfiguration: Configuration,
    updateLifecycle: suspend (configuration: Configuration, lifecycle: MutableUIComponentLifecycle, nextState: Configuration) -> Unit,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: SlotNavigationTarget<Configuration>) -> Component,
): SlotNode<Configuration, Component> =
    uiChildrenNode(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        navigationStateEquality = configurationEquality,
        loggerSource = loggerSource,
        navigationControllerSpec = navigationControllerSpec,
        navigationStateSerializer = { it },
        initialState = initialConfiguration,
        stateConfigurationsMapping = { currentNavigationState ->
            KoneSet.of(
                currentNavigationState,
                elementEquality = configurationEquality,
                elementHashing = configurationHashing,
                elementOrder = configurationOrder,
            )
        },
        navigationTransition = { previousState, event -> event(previousState) },
        restorationEvent = { nextState -> { nextState } },
        updateLifecycle = updateLifecycle,
        childrenFactory = childrenFactory,
        navigationStateMapper = { navigationState, children -> ChildWithConfiguration(navigationState, children[navigationState]) },
    )

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenToSlotNode(
    configurationEquality: Equality<Configuration> = Equality.defaultFor(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<Configuration, Configuration, Component, UIComponentContext, SlotNavigationEvent<Configuration>>? = null,
    initialConfiguration: Configuration,
    activeState: UIComponentLifecycleState,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: SlotNavigationTarget<Configuration>) -> Component,
): SlotNode<Configuration, Component> =
    uiChildrenSlotNode(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        loggerSource = loggerSource,
        navigationControllerSpec = navigationControllerSpec,
        initialConfiguration = initialConfiguration,
        updateLifecycle = { configuration, lifecycle, nextState ->
            check(configurationEquality { configuration eq nextState }) { "For some reason, there is preserved configuration that is different from the only configuration in the slot" }
            lifecycle.moveTo(activeState)
        },
        childrenFactory = childrenFactory,
    )

public expect suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenDefaultSlotNode(
    configurationEquality: Equality<Configuration> = Equality.defaultFor(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<Configuration, Configuration, Component, UIComponentContext, SlotNavigationEvent<Configuration>>? = null,
    initialConfiguration: Configuration,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: SlotNavigationTarget<Configuration>) -> Component,
): SlotNode<Configuration, Component>