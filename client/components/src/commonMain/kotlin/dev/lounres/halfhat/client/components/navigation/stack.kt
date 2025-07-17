package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.buildUiChild
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.halfhat.client.components.lifecycle.newMutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.logger.LoggerKey
import dev.lounres.halfhat.client.components.navigation.controller.NavigationItemController
import dev.lounres.halfhat.client.components.navigation.controller.NavigationNodeController
import dev.lounres.komponentual.navigation.StackNavigationEvent
import dev.lounres.komponentual.navigation.StackNavigationHub
import dev.lounres.komponentual.navigation.StackNavigationState
import dev.lounres.komponentual.navigation.StackNavigationTarget
import dev.lounres.komponentual.navigation.childrenStack
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.map.associateReified
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.map.mapsTo
import dev.lounres.kone.collections.utils.dropLast
import dev.lounres.kone.collections.utils.last
import dev.lounres.kone.collections.utils.map
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.buildSubscription
import dev.lounres.kone.hub.map
import dev.lounres.kone.registry.getOrNull
import dev.lounres.kone.relations.*
import dev.lounres.logKube.core.debug
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json


public data class ChildrenStack<Configuration, Component>(
    public val active: ChildWithConfiguration<Configuration, Component>,
    public val backStack: KoneList<ChildWithConfiguration<Configuration, Component>>,
)

public interface StackItem<Configuration, Component> : StackNavigationTarget<Configuration> {
    public val hub: KoneAsynchronousHub<ChildrenStack<Configuration, Component>>
}

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenStackItem(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<Configuration>? = null,
    initialStack: KoneList<Configuration>,
    updateLifecycle: suspend (configuration: Configuration, lifecycle: MutableUIComponentLifecycle, nextState: StackNavigationState<Configuration>) -> Unit,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: StackNavigationTarget<Configuration>) -> Component,
): StackItem<Configuration, Component> {
    val logger = this.getOrNull(LoggerKey)
    val navigationNodeController = this.getOrNull(NavigationNodeController)
    val navigationItemController = when {
        navigationNodeController == null || navigationControllerSpec == null -> null
        navigationControllerSpec.key == null -> NavigationItemController().also { navigationNodeController.attachSoleItem(it) }
        else -> NavigationItemController().also { navigationNodeController.attachItem(navigationControllerSpec.key, it) }
    }
    val navigationHub = StackNavigationHub<Configuration>()
    val stackHub = childrenStack(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = navigationHub,
        initialStack = initialStack,
        createChild = { configuration, nextState ->
            val controllingLifecycle = newMutableUIComponentLifecycle()
            val childNavigationNodeController = if (navigationItemController != null) NavigationNodeController() else null
            logger?.debug(
                source = loggerSource,
                items = {
                    mapOf(
                        "configuration" to configuration.toString(),
                        "controllingLifecycle" to controllingLifecycle.toString(),
                        "navigationNodeController" to childNavigationNodeController.toString(),
                    )
                }
            ) { "Creating child" }
            val component = this.buildUiChild(controllingLifecycle, childNavigationNodeController) {
                childrenFactory(configuration, it, navigationHub)
            }
            logger?.debug(
                source = loggerSource,
                items = {
                    mapOf(
                        "configuration" to configuration.toString(),
                        "controllingLifecycle" to controllingLifecycle.toString(),
                        "navigationNodeController" to childNavigationNodeController.toString(),
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
                        "navigationNodeController" to childNavigationNodeController.toString(),
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
                        "navigationNodeController" to childNavigationNodeController.toString(),
                        "component" to component.toString(),
                        "nextState" to nextState.toString(),
                    )
                }
            ) { "Updated controlling lifecycle" }
            Child(
                component = component,
                controllingLifecycle = controllingLifecycle,
                navigationNodeController = childNavigationNodeController,
            )
        },
        destroyChild = { configuration, child, nextState ->
            logger?.debug(
                source = loggerSource,
                items = {
                    mapOf(
                        "configuration" to configuration.toString(),
                        "controllingLifecycle" to child.controllingLifecycle.toString(),
                        "navigationNodeController" to child.navigationNodeController.toString(),
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
                        "navigationNodeController" to child.navigationNodeController.toString(),
                        "component" to child.component.toString(),
                        "nextState" to nextState.toString(),
                    )
                }
            ) { "Destroyed controlling lifecycle" }
        },
        updateChild = { configuration, data, nextState ->
            updateLifecycle(configuration, data.controllingLifecycle, nextState)
        },
    )
    if (navigationItemController != null) {
        val serializer = navigationControllerSpec!!.configurationSerializer
        stackHub.buildSubscription {
            subscribe {
                navigationItemController.configuration =
                    Json.encodeToString(KoneList.serializer(serializer), it.navigationState)
                navigationItemController.nodes = it.children.nodesView.associateReified { node ->
                    Json.encodeToString(serializer, node.key) mapsTo node.value.navigationNodeController!!
                }
            }
            navigationItemController.configuration =
                Json.encodeToString(KoneList.serializer(serializer), it.navigationState)
            navigationItemController.nodes = it.children.nodesView.associateReified { node ->
                Json.encodeToString(serializer, node.key) mapsTo node.value.navigationNodeController!!
            }
        }
        navigationItemController.restoration = {
            try {
                val restoredConfiguration = Json.decodeFromString(KoneList.serializer(serializer), it)
                navigationHub.navigate { restoredConfiguration }
            } catch (e: SerializationException) {} catch (e: IllegalArgumentException /* TODO: Remove eventually when Kone will start using correct exception types */) {}
        }
    }
    return object : StackItem<Configuration, Component> {
        override val hub: KoneAsynchronousHub<ChildrenStack<Configuration, Component>> =
            stackHub.map {
                val stack = it.navigationState.map { configuration -> ChildWithConfiguration(configuration, it.children[configuration].component) }
                ChildrenStack(
                    active = stack.last(),
                    backStack = stack.dropLast(1u),
                )
            }
        
        override suspend fun navigate(stackTransformation: StackNavigationEvent<Configuration>) {
            navigationHub.navigate(stackTransformation)
        }
    }
}

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenFromToStackItem(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<Configuration>? = null,
    initialStack: KoneList<Configuration>,
    inactiveState: UIComponentLifecycleState,
    activeState: UIComponentLifecycleState,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: StackNavigationTarget<Configuration>) -> Component,
): StackItem<Configuration, Component> =
    uiChildrenStackItem(
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
> UIComponentContext.uiChildrenDefaultStackItem(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<Configuration>? = null,
    initialStack: KoneList<Configuration>,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: StackNavigationTarget<Configuration>) -> Component,
): StackItem<Configuration, Component>