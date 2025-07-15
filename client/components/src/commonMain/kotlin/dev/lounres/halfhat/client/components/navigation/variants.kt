package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.buildUiChild
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.halfhat.client.components.lifecycle.newMutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.logger.LoggerKey
import dev.lounres.komponentual.navigation.ChildWithConfiguration
import dev.lounres.komponentual.navigation.VariantsNavigationState
import dev.lounres.komponentual.navigation.VariantsNavigationSource
import dev.lounres.komponentual.navigation.childrenVariants
import dev.lounres.kone.collections.map.KoneMap
import dev.lounres.kone.collections.map.component1
import dev.lounres.kone.collections.map.component2
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.map.mapValues
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.map
import dev.lounres.kone.registry.getOrNull
import dev.lounres.kone.relations.*
import dev.lounres.logKube.core.debug


public data class ChildrenVariants<Configuration, Component>(
    public val active: ChildWithConfiguration<Configuration, Component>,
    public val allVariants: KoneMap<Configuration, Component>,
)

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenVariants(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    source: VariantsNavigationSource<Configuration>,
    allVariants: KoneSet<Configuration>,
    initialVariant: Configuration,
    updateLifecycle: suspend (configuration: Configuration, lifecycle: MutableUIComponentLifecycle, nextState: VariantsNavigationState<Configuration>) -> Unit,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneAsynchronousHub<ChildrenVariants<Configuration, Component>> {
    val logger = this.getOrNull(LoggerKey)
    return childrenVariants(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        allVariants = allVariants,
        initialVariant = initialVariant,
        createChild = { configuration, nextState ->
            val controllingLifecycle = newMutableUIComponentLifecycle()
            logger?.debug(
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
            logger?.debug(
                source = loggerSource,
                items = {
                    mapOf(
                        "configuration" to configuration.toString(),
                        "controllingLifecycle" to controllingLifecycle.toString(),
                        "component" to component.toString(),
                    )
                }
            ) { "Created child" }
            logger?.debug(
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
            logger?.debug(
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
            logger?.debug(
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
            logger?.debug(
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
    ).map {
        ChildrenVariants(
            active = it.navigationState.currentVariant.let { configuration ->
                ChildWithConfiguration(
                    configuration = configuration,
                    component = it.children[configuration].component,
                )
            },
            allVariants = it.children.mapValues(
                keyEquality = configurationEquality,
                keyHashing = configurationHashing,
                keyOrder = configurationOrder,
            ) { (_, child) -> child.component },
        )
    }
}

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenFromToVariants(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    source: VariantsNavigationSource<Configuration>,
    allVariants: KoneSet<Configuration>,
    initialVariant: Configuration,
    inactiveState: UIComponentLifecycleState,
    activeState: UIComponentLifecycleState,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneAsynchronousHub<ChildrenVariants<Configuration, Component>> =
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
    source: VariantsNavigationSource<Configuration>,
    allVariants: KoneSet<Configuration>,
    initialVariant: Configuration,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneAsynchronousHub<ChildrenVariants<Configuration, Component>>