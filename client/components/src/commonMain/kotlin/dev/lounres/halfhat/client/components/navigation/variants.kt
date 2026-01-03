package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.komponentual.navigation.VariantsNavigationEvent
import dev.lounres.komponentual.navigation.VariantsNavigationState
import dev.lounres.komponentual.navigation.VariantsNavigationTarget
import dev.lounres.kone.collections.map.*
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.relations.*


public data class ChildrenVariants<Configuration, Component, ComponentContext>(
    public val active: ChildWithConfigurationAndContext<Configuration, Component, ComponentContext>,
    public val allVariants: KoneMap<Configuration, Component>,
)

public typealias VariantsNode<Configuration, Component, ComponentContext> = ChildrenNode<ChildrenVariants<Configuration, Component, ComponentContext>, VariantsNavigationEvent<Configuration>>

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenVariantsNode(
    configurationEquality: Equality<Configuration> = Equality.defaultFor(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<VariantsNavigationState<Configuration>, Configuration, Component, UIComponentContext, ChildrenVariants<Configuration, Component, UIComponentContext>, VariantsNavigationEvent<Configuration>>? = null,
    allVariants: KoneSet<Configuration>,
    initialVariant: Configuration,
    updateLifecycle: suspend (configuration: Configuration, lifecycle: MutableUIComponentLifecycle, nextState: VariantsNavigationState<Configuration>) -> Unit,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: VariantsNavigationTarget<Configuration>) -> Component,
): VariantsNode<Configuration, Component, UIComponentContext> =
    uiChildrenNode(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        navigationStateEquality = Equality { left, right ->
            configurationEquality { left.currentVariant eq right.currentVariant }
        },
        loggerSource = loggerSource,
        navigationControllerSpec = navigationControllerSpec,
        navigationStateSerializer = { VariantsNavigationState.Serializer(it, allVariants) },
        initialState = VariantsNavigationState(
            configurations = allVariants,
            currentVariant = initialVariant,
        ),
        stateConfigurationsMapping = { currentNavigationState -> currentNavigationState.configurations },
        navigationTransition = { previousState, event ->
            VariantsNavigationState(
                configurations = previousState.configurations,
                currentVariant = event(previousState.configurations, previousState.currentVariant)
            )
        },
        restorationEvent = { nextState -> { _, _ -> nextState.currentVariant } },
        updateLifecycle = updateLifecycle,
        childrenFactory = childrenFactory,
        navigationStateMapper = { navigationState, children ->
            ChildrenVariants(
                active = navigationState.currentVariant.let { configuration ->
                    val child = children[configuration]
                    ChildWithConfigurationAndContext(
                        configuration = configuration,
                        component = child.component,
                        componentContext = child.context,
                    )
                },
                allVariants = children.mapValues(
                    keyEquality = configurationEquality,
                    keyHashing = configurationHashing,
                    keyOrder = configurationOrder,
                ) { (_, child) -> child.component },
            )
        },
    )

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenFromToVariantsNode(
    configurationEquality: Equality<Configuration> = Equality.defaultFor(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<VariantsNavigationState<Configuration>, Configuration, Component, UIComponentContext, ChildrenVariants<Configuration, Component, UIComponentContext>, VariantsNavigationEvent<Configuration>>? = null,
    allVariants: KoneSet<Configuration>,
    initialVariant: Configuration,
    inactiveState: UIComponentLifecycleState,
    activeState: UIComponentLifecycleState,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: VariantsNavigationTarget<Configuration>) -> Component,
): VariantsNode<Configuration, Component, UIComponentContext> =
    uiChildrenVariantsNode(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        loggerSource = loggerSource,
        navigationControllerSpec = navigationControllerSpec,
        allVariants = allVariants,
        initialVariant = initialVariant,
        updateLifecycle = { configuration, lifecycle, nextState ->
            if (configurationEquality { configuration eq nextState.currentVariant }) lifecycle.moveTo(activeState)
            else lifecycle.moveTo(inactiveState)
        },
        childrenFactory = childrenFactory,
    )

public expect suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenDefaultVariantsNode(
    configurationEquality: Equality<Configuration> = Equality.defaultFor(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<VariantsNavigationState<Configuration>, Configuration, Component, UIComponentContext, ChildrenVariants<Configuration, Component, UIComponentContext>, VariantsNavigationEvent<Configuration>>? = null,
    allVariants: KoneSet<Configuration>,
    initialVariant: Configuration,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: VariantsNavigationTarget<Configuration>) -> Component,
): VariantsNode<Configuration, Component, UIComponentContext>