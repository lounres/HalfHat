package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.buildUiChild
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.halfhat.client.components.logger.logger
import dev.lounres.komponentual.navigation.ChildrenVariants
import dev.lounres.komponentual.navigation.InnerVariantsNavigationState
import dev.lounres.komponentual.navigation.VariantsNavigation
import dev.lounres.komponentual.navigation.childrenVariants
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.relations.*
import dev.lounres.kone.state.KoneAsynchronousState
import dev.lounres.logKube.core.debug


public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenVariants(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
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
            val controllingLifecycle = MutableUIComponentLifecycle()
            logger.debug(
                source = loggerSource,
                items = {
                    mapOf(
                        "configuration" to configuration.toString(),
                        "controllingLifecycle" to controllingLifecycle.toString(),
                    )
                }
            ) { "Creating child" }
            val component = this.buildUiChild(controllingLifecycle) {
                childrenFactory(configuration, it)
            }
            logger.debug(
                source = loggerSource,
                items = {
                    mapOf(
                        "configuration" to configuration.toString(),
                        "controllingLifecycle" to controllingLifecycle.toString(),
                        "component" to component.toString(),
                    )
                }
            ) { "Created child" }
            logger.debug(
                source = loggerSource,
                items = {
                    mapOf(
                        "configuration" to configuration.toString(),
                        "controllingLifecycle" to controllingLifecycle.toString(),
                        "component" to component.toString(),
                        "nextState" to nextState.toString(),
                    )
                }
            ) { "Updating controlling lifecycle" }
            updateLifecycle(configuration, controllingLifecycle, nextState)
            logger.debug(
                source = loggerSource,
                items = {
                    mapOf(
                        "configuration" to configuration.toString(),
                        "controllingLifecycle" to controllingLifecycle.toString(),
                        "component" to component.toString(),
                        "nextState" to nextState.toString(),
                    )
                }
            ) { "Updated controlling lifecycle" }
            Child(
                component = component,
                controllingLifecycle = controllingLifecycle,
            )
        },
        destroyChild = { configuration, child, nextState ->
            logger.debug(
                source = loggerSource,
                items = {
                    mapOf(
                        "configuration" to configuration.toString(),
                        "controllingLifecycle" to child.controllingLifecycle.toString(),
                        "component" to child.component.toString(),
                        "nextState" to nextState.toString(),
                    )
                }
            ) { "Destroying controlling lifecycle" }
            child.controllingLifecycle.moveTo(UIComponentLifecycleState.Destroyed)
            logger.debug(
                source = loggerSource,
                items = {
                    mapOf(
                        "configuration" to configuration.toString(),
                        "controllingLifecycle" to child.controllingLifecycle.toString(),
                        "component" to child.component.toString(),
                        "nextState" to nextState.toString(),
                    )
                }
            ) { "Destroyed controlling lifecycle" }
        },
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
    loggerSource: String? = null,
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
        loggerSource = loggerSource,
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
    loggerSource: String? = null,
    source: VariantsNavigation<Configuration>,
    allVariants: KoneSet<Configuration>,
    initialVariant: Configuration,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneAsynchronousState<ChildrenVariants<Configuration, Component>>