package dev.lounres.kone.misc.router

import dev.lounres.komponentual.navigation.NavigationSource
import dev.lounres.komponentual.navigation.NavigationState
import dev.lounres.komponentual.navigation.children
import dev.lounres.kone.collections.map.KoneMap
import dev.lounres.kone.collections.map.mapValues
import dev.lounres.kone.relations.Equality
import dev.lounres.kone.relations.Hashing
import dev.lounres.kone.relations.Order
import dev.lounres.kone.relations.defaultEquality
import dev.lounres.kone.state.KoneState


internal data class ChildWithLifecycle<Component, Lifecycle>(
    val component: Component,
    val lifecycle: Lifecycle,
)

public fun <
    ParentLifecycle,
    ControllingLifecycle,
    ChildLifecycle,
    Configuration,
    InnerNavigationState : NavigationState<Configuration>,
    PublicNavigationState,
    NavigationEvent,
    Component,
> children(
    parentLifecycle: ParentLifecycle,
    mutableControllingLifecycleProducer: () -> ControllingLifecycle,
    mergeLifecycles: (ParentLifecycle, ControllingLifecycle) -> ChildLifecycle,
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: NavigationSource<NavigationEvent>,
    initialState: () -> InnerNavigationState,
    navigationTransition: (previousState: InnerNavigationState, event: NavigationEvent) -> InnerNavigationState,
    destroyLifecycle: (ControllingLifecycle) -> Unit,
    updateLifecycle: (configuration: Configuration, lifecycle: ControllingLifecycle, nextState: InnerNavigationState) -> Unit,
    publicNavigationStateMapper: (InnerNavigationState, KoneMap<Configuration, Component>) -> PublicNavigationState,
    childrenFactory: (configuration: Configuration, lifecycle: ChildLifecycle) -> Component,
): KoneState<PublicNavigationState> =
    children(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        initialState = initialState,
        navigationTransition = navigationTransition,
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
        publicNavigationStateMapper = { innerState, datas ->
            publicNavigationStateMapper(innerState, datas.mapValues { it.value.component } /* TODO: Replace with view map */)
        },
    )