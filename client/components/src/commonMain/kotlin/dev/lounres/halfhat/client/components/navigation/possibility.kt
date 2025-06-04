package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.uiChild
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
    Configuration,
    Component,
> UIComponentContext.uiChildrenPossibility(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: PossibilityNavigation<Configuration>,
    initialConfiguration: () -> Maybe<Configuration>,
    updateLifecycle: (configuration: Configuration, lifecycle: MutableUIComponentLifecycle, nextState: InnerPossibilityNavigationState<Configuration>) -> Unit,
    childrenFactory: (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneState<ChildrenPossibility<Configuration, Component>> =
    childrenPossibility(
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
> UIComponentContext.uiChildrenToPossibility(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: PossibilityNavigation<Configuration>,
    initialConfiguration: () -> Maybe<Configuration>,
    activeState: UIComponentLifecycleState,
    childrenFactory: (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneState<ChildrenPossibility<Configuration, Component>> =
    uiChildrenPossibility(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        initialConfiguration = initialConfiguration,
        updateLifecycle = { configuration, lifecycle, nextState ->
            check(nextState.current.let { it is Some<Configuration> && configurationEquality { it.value eq configuration } }) { "For some reason, there is preserved configuration that is different from the only configuration in the possibility" }
            lifecycle.moveTo(activeState)
        },
        childrenFactory = childrenFactory,
    )

public expect fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenDefaultPossibility(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: PossibilityNavigation<Configuration>,
    initialConfiguration: () -> Maybe<Configuration>,
    childrenFactory: (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneState<ChildrenPossibility<Configuration, Component>>