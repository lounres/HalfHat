package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.komponentual.navigation.StackNavigationEvent
import dev.lounres.komponentual.navigation.StackNavigationState
import dev.lounres.komponentual.navigation.StackNavigationTarget
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.relations.equality
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.set.KoneMutableSet
import dev.lounres.kone.collections.set.of
import dev.lounres.kone.collections.utils.copyTo
import dev.lounres.kone.collections.utils.dropLast
import dev.lounres.kone.collections.utils.last
import dev.lounres.kone.collections.utils.map
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.relations.*


public data class ChildrenStack<Configuration, Component, ComponentContext>(
    public val active: ChildWithConfigurationAndContext<Configuration, Component, ComponentContext>,
    public val backStack: KoneList<ChildWithConfigurationAndContext<Configuration, Component, ComponentContext>>,
)

public typealias StackNode<Configuration, Component, ComponentContext> = ChildrenNode<ChildrenStack<Configuration, Component, ComponentContext>, StackNavigationEvent<Configuration>>

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenStackNode(
    configurationEquality: Equality<Configuration> = Equality.defaultFor(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<StackNavigationState<Configuration>, Configuration, Component, UIComponentContext, ChildrenStack<Configuration, Component, UIComponentContext>, StackNavigationEvent<Configuration>>? = null,
    initialStack: KoneList<Configuration>,
    updateLifecycle: suspend (configuration: Configuration, lifecycle: MutableUIComponentLifecycle, nextState: StackNavigationState<Configuration>) -> Unit,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: StackNavigationTarget<Configuration>) -> Component,
): StackNode<Configuration, Component, UIComponentContext> =
    uiChildrenNode(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        navigationStateEquality = KoneList.equality(configurationEquality),
        loggerSource = loggerSource,
        navigationControllerSpec = navigationControllerSpec,
        navigationStateSerializer = { KoneList.serializer(it) },
        initialState = initialStack.also { require(it.size != 0u) { "Cannot initialize a children stack without configurations" } },
        stateConfigurationsMapping = { currentNavigationState ->
            currentNavigationState.copyTo(
                KoneMutableSet.of(
                    elementEquality = configurationEquality,
                    elementHashing = configurationHashing,
                    elementOrder = configurationOrder,
                )
            )
        },
        navigationTransition = { previousState, event ->
            event(previousState).also { require(it.size != 0u) { "Cannot initialize a children stack without configurations" } }
        },
        restorationEvent = { nextState -> { nextState } },
        updateLifecycle = updateLifecycle,
        childrenFactory = childrenFactory,
        navigationStateMapper = { navigationState, children ->
            val stack = navigationState.map { configuration ->
                val child = children[configuration]
                ChildWithConfigurationAndContext(configuration, child.component, child.context)
            }
            ChildrenStack(
                active = stack.last(),
                backStack = stack.dropLast(1u),
            )
        },
    )

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenFromToStackNode(
    configurationEquality: Equality<Configuration> = Equality.defaultFor(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<StackNavigationState<Configuration>, Configuration, Component, UIComponentContext, ChildrenStack<Configuration, Component, UIComponentContext>, StackNavigationEvent<Configuration>>? = null,
    initialStack: KoneList<Configuration>,
    inactiveState: UIComponentLifecycleState,
    activeState: UIComponentLifecycleState,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: StackNavigationTarget<Configuration>) -> Component,
): StackNode<Configuration, Component, UIComponentContext> =
    uiChildrenStackNode(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        loggerSource = loggerSource,
        navigationControllerSpec = navigationControllerSpec,
        initialStack = initialStack,
        updateLifecycle = { configuration, lifecycle, nextState ->
            if (configurationEquality { configuration eq nextState.last() }) lifecycle.moveTo(activeState)
            else lifecycle.moveTo(inactiveState)
        },
        childrenFactory = childrenFactory,
    )

public expect suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenDefaultStackNode(
    configurationEquality: Equality<Configuration> = Equality.defaultFor(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<StackNavigationState<Configuration>, Configuration, Component, UIComponentContext, ChildrenStack<Configuration, Component, UIComponentContext>, StackNavigationEvent<Configuration>>? = null,
    initialStack: KoneList<Configuration>,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: StackNavigationTarget<Configuration>) -> Component,
): StackNode<Configuration, Component, UIComponentContext>