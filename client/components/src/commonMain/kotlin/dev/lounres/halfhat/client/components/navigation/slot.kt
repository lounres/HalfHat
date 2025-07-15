package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.buildUiChild
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.halfhat.client.components.lifecycle.newMutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.logger.LoggerKey
import dev.lounres.komponentual.navigation.ChildWithConfiguration
import dev.lounres.komponentual.navigation.SlotNavigationSource
import dev.lounres.komponentual.navigation.childrenSlot
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.map
import dev.lounres.kone.registry.getOrNull
import dev.lounres.kone.relations.*
import dev.lounres.logKube.core.debug


public typealias ChildrenSlot<Configuration, Component> = ChildWithConfiguration<Configuration, Component>

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenSlot(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    source: SlotNavigationSource<Configuration>,
    initialConfiguration: Configuration,
    updateLifecycle: suspend (configuration: Configuration, lifecycle: MutableUIComponentLifecycle, nextState: Configuration) -> Unit,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneAsynchronousHub<ChildrenSlot<Configuration, Component>> {
    val logger = this.getOrNull(LoggerKey)
    return childrenSlot(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        initialConfiguration = initialConfiguration,
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
    ).map { it.navigationState.let { configuration -> ChildWithConfiguration(configuration, it.children[configuration].component) } }
}

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenToSlot(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    source: SlotNavigationSource<Configuration>,
    initialConfiguration: Configuration,
    activeState: UIComponentLifecycleState,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneAsynchronousHub<ChildrenSlot<Configuration, Component>> =
    uiChildrenSlot(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        loggerSource = loggerSource,
        source = source,
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
> UIComponentContext.uiChildrenDefaultSlot(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    source: SlotNavigationSource<Configuration>,
    initialConfiguration: Configuration,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneAsynchronousHub<ChildrenSlot<Configuration, Component>>