package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.buildUiChild
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.halfhat.client.components.lifecycle.newMutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.logger.LoggerKey
import dev.lounres.komponentual.navigation.ChildWithConfiguration
import dev.lounres.komponentual.navigation.StackNavigationSource
import dev.lounres.komponentual.navigation.StackNavigationState
import dev.lounres.komponentual.navigation.childrenStack
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.utils.dropLast
import dev.lounres.kone.collections.utils.last
import dev.lounres.kone.collections.utils.map
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.map
import dev.lounres.kone.registry.getOrNull
import dev.lounres.kone.relations.*
import dev.lounres.logKube.core.debug


public data class ChildrenStack<Configuration, Component>(
    public val active: ChildWithConfiguration<Configuration, Component>,
    public val backStack: KoneList<ChildWithConfiguration<Configuration, Component>>,
)

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenStack(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    source: StackNavigationSource<Configuration>,
    initialStack: KoneList<Configuration>,
    updateLifecycle: suspend (configuration: Configuration, lifecycle: MutableUIComponentLifecycle, nextState: StackNavigationState<Configuration>) -> Unit,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneAsynchronousHub<ChildrenStack<Configuration, Component>> {
    val logger = this.getOrNull(LoggerKey)
    return childrenStack(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        initialStack = initialStack,
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
        val stack = it.navigationState.map { configuration -> ChildWithConfiguration(configuration, it.children[configuration].component) }
        ChildrenStack(
            active = stack.last(),
            backStack = stack.dropLast(1u),
        )
    }
}

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenFromToStack(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    source: StackNavigationSource<Configuration>,
    initialStack: KoneList<Configuration>,
    inactiveState: UIComponentLifecycleState,
    activeState: UIComponentLifecycleState,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneAsynchronousHub<ChildrenStack<Configuration, Component>> =
    uiChildrenStack(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        loggerSource = loggerSource,
        source = source,
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
> UIComponentContext.uiChildrenDefaultStack(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    source: StackNavigationSource<Configuration>,
    initialStack: KoneList<Configuration>,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneAsynchronousHub<ChildrenStack<Configuration, Component>>