package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.komponentual.navigation.PossibilityNavigationEvent
import dev.lounres.komponentual.navigation.PossibilityNavigationState
import dev.lounres.komponentual.navigation.PossibilityNavigationTarget
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.set.empty
import dev.lounres.kone.collections.set.of
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.maybe.Maybe
import dev.lounres.kone.maybe.None
import dev.lounres.kone.maybe.Some
import dev.lounres.kone.maybe.map
import dev.lounres.kone.relations.*


public typealias ChildrenPossibility<Configuration, Component> = Maybe<ChildWithConfiguration<Configuration, Component>>

public typealias PossibilityNode<Configuration, Component> = ChildrenNode<Configuration, Component, ChildrenPossibility<Configuration, Component>, PossibilityNavigationEvent<Configuration>>

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenPossibilityNode(
    configurationEquality: Equality<Configuration> = Equality.defaultFor(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<PossibilityNavigationState<Configuration>, Configuration, Component, UIComponentContext, PossibilityNavigationEvent<Configuration>>? = null,
    initialConfiguration: Maybe<Configuration>,
    updateLifecycle: suspend (configuration: Configuration, lifecycle: MutableUIComponentLifecycle, nextState: PossibilityNavigationState<Configuration>) -> Unit,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: PossibilityNavigationTarget<Configuration>) -> Component,
): PossibilityNode<Configuration, Component> =
    uiChildrenNode(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        loggerSource = loggerSource,
        navigationControllerSpec = navigationControllerSpec,
        navigationStateSerializer = { Maybe.serializer(it) },
        initialState = initialConfiguration,
        stateConfigurationsMapping = { currentNavigationState ->
            when (currentNavigationState) {
                None -> KoneSet.empty()
                is Some<Configuration> -> KoneSet.of(
                    currentNavigationState.value,
                    elementEquality = configurationEquality,
                    elementHashing = configurationHashing,
                    elementOrder = configurationOrder,
                )
            }
        },
        navigationTransition = { previousState, event -> event(previousState) },
        restorationEvent = { nextState -> { nextState } },
        updateLifecycle = updateLifecycle,
        childrenFactory = childrenFactory,
        navigationStateMapper = { navigationState, children -> navigationState.map { ChildWithConfiguration(it, children[it]) } },
    )

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenToPossibilityNode(
    configurationEquality: Equality<Configuration> = Equality.defaultFor(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<PossibilityNavigationState<Configuration>, Configuration, Component, UIComponentContext, PossibilityNavigationEvent<Configuration>>? = null,
    initialConfiguration: Maybe<Configuration>,
    activeState: UIComponentLifecycleState,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: PossibilityNavigationTarget<Configuration>) -> Component,
): PossibilityNode<Configuration, Component> =
    uiChildrenPossibilityNode(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        loggerSource = loggerSource,
        navigationControllerSpec = navigationControllerSpec,
        initialConfiguration = initialConfiguration,
        updateLifecycle = { configuration, lifecycle, nextState ->
            check(nextState.let { it is Some<Configuration> && configurationEquality { it.value eq configuration } }) { "For some reason, there is preserved configuration that is different from the only configuration in the possibility" }
            lifecycle.moveTo(activeState)
        },
        childrenFactory = childrenFactory,
    )

public expect suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenDefaultPossibilityNode(
    configurationEquality: Equality<Configuration> = Equality.defaultFor(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<PossibilityNavigationState<Configuration>, Configuration, Component, UIComponentContext, PossibilityNavigationEvent<Configuration>>? = null,
    initialConfiguration: Maybe<Configuration>,
    childrenFactory: (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: PossibilityNavigationTarget<Configuration>) -> Component,
): PossibilityNode<Configuration, Component>