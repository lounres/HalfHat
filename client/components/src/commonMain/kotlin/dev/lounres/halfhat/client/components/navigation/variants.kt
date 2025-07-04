package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.coroutineScope
import dev.lounres.halfhat.client.components.launch
import dev.lounres.halfhat.client.components.uiChildDeferring
import dev.lounres.komponentual.lifecycle.DelicateLifecycleAPI
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.komponentual.navigation.ChildrenVariants
import dev.lounres.komponentual.navigation.InnerVariantsNavigationState
import dev.lounres.komponentual.navigation.VariantsNavigation
import dev.lounres.komponentual.navigation.childrenVariants
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.relations.*
import dev.lounres.kone.state.KoneAsynchronousState
import kotlinx.coroutines.Dispatchers


@OptIn(DelicateLifecycleAPI::class)
public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenVariants(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: VariantsNavigation<Configuration>,
    allVariants: KoneSet<Configuration>,
    initialVariant: Configuration,
    updateLifecycle: suspend (configuration: Configuration, lifecycle: MutableUIComponentLifecycle, nextState: InnerVariantsNavigationState<Configuration>) -> Unit,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneAsynchronousState<ChildrenVariants<Configuration, Component>> =
    childrenVariants(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        allVariants = allVariants,
        initialVariant = initialVariant,
        createChild = { configuration, nextState ->
            val controllingLifecycle = MutableUIComponentLifecycle(this.coroutineScope(Dispatchers.Default))
            updateLifecycle(configuration, controllingLifecycle, nextState)
            val childContext = this.uiChildDeferring(controllingLifecycle)
            val child = childrenFactory(configuration, childContext)
            childContext.launch()
            Child(
                component = child,
                controllingLifecycle = controllingLifecycle,
            )
        },
        destroyChild = { it.controllingLifecycle.moveTo(UIComponentLifecycleState.Destroyed) },
        updateChild = { configuration, data, nextState ->
            updateLifecycle(configuration, data.controllingLifecycle, nextState)
        },
        componentAccessor = { it.component },
    )

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenFromToVariants(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: VariantsNavigation<Configuration>,
    allVariants: KoneSet<Configuration>,
    initialVariant: Configuration,
    inactiveState: UIComponentLifecycleState,
    activeState: UIComponentLifecycleState,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneAsynchronousState<ChildrenVariants<Configuration, Component>> =
    uiChildrenVariants(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
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
> UIComponentContext.uiChildrenDefaultVariants(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: VariantsNavigation<Configuration>,
    allVariants: KoneSet<Configuration>,
    initialVariant: Configuration,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneAsynchronousState<ChildrenVariants<Configuration, Component>>