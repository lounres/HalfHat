package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.buildUiChild
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.halfhat.client.components.lifecycle.newMutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.logger.LoggerKey
import dev.lounres.halfhat.client.components.navigation.controller.NavigationControllerStringFormatKey
import dev.lounres.halfhat.client.components.navigation.controller.NavigationNodeController
import dev.lounres.halfhat.client.components.navigation.controller.doStoringNavigation
import dev.lounres.halfhat.client.components.navigation.controller.navigationContext
import dev.lounres.komponentual.navigation.StackNavigationEvent
import dev.lounres.komponentual.navigation.StackNavigationHub
import dev.lounres.komponentual.navigation.StackNavigationState
import dev.lounres.komponentual.navigation.StackNavigationTarget
import dev.lounres.komponentual.navigation.childrenStack
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.map.KoneMap
import dev.lounres.kone.collections.map.associateReified
import dev.lounres.kone.collections.map.build
import dev.lounres.kone.collections.map.contains
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.map.mapValues
import dev.lounres.kone.collections.map.mapsTo
import dev.lounres.kone.collections.map.setAllFrom
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


public data class ChildrenStack<Configuration, Component>(
    public val active: ChildWithConfiguration<Configuration, Component>,
    public val backStack: KoneList<ChildWithConfiguration<Configuration, Component>>,
)

public interface StackNode<Configuration, Component> : StackNavigationTarget<Configuration>, WithComponentContext<UIComponentContext> {
    public val hub: KoneAsynchronousHub<ChildrenStack<Configuration, Component>>
}

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenStackNode(
    configurationEquality: Equality<Configuration> = Equality.defaultFor(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<StackNavigationState<Configuration>, Configuration, Component, UIComponentContext, StackNavigationTarget<Configuration>>? = null,
    initialStack: KoneList<Configuration>,
    updateLifecycle: suspend (configuration: Configuration, lifecycle: MutableUIComponentLifecycle, nextState: StackNavigationState<Configuration>) -> Unit,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: StackNavigationTarget<Configuration>) -> Component,
): StackNode<Configuration, Component> {
    val logger = this.getOrNull(LoggerKey)
    val componentNavigationNodeController = this.getOrNull(NavigationNodeController.Key)
    val childrenNavigationNodeController =
        if (componentNavigationNodeController == null || navigationControllerSpec == null) null
        else NavigationNodeController().also {
            componentNavigationNodeController.children = KoneMap.build {
                setAllFrom(componentNavigationNodeController.children)
                if (navigationControllerSpec.key in this) error("Navigation node controller already registered an item with the key: '${navigationControllerSpec.key}'")
                set(navigationControllerSpec.key, it)
            }
        }
    return buildUiChild(
        navigationNodeController = childrenNavigationNodeController,
    ) { childrenComponentContext ->
        val navigationHub = StackNavigationHub<Configuration>()
        val storingNavigationTarget = StackNavigationTarget {
            childrenComponentContext.navigationContext.doStoringNavigation {
                navigationHub.navigate(it)
            }
        }
        val stackHub = childrenStack(
            configurationEquality = configurationEquality,
            configurationHashing = configurationHashing,
            configurationOrder = configurationOrder,
            source = navigationHub,
            initialStack = initialStack,
            createChild = { configuration, nextState ->
                val controllingLifecycle = newMutableUIComponentLifecycle()
                val childNavigationNodeController = if (childrenNavigationNodeController != null) NavigationNodeController() else null
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
                val context: UIComponentContext
                val component = childrenComponentContext.buildUiChild(controllingLifecycle, childNavigationNodeController) {
                    context = it
                    childrenFactory(configuration, it, storingNavigationTarget)
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
                    context = context,
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
        if (childrenNavigationNodeController != null) {
            val stringFormat = childrenComponentContext[NavigationControllerStringFormatKey]
            val serializer = navigationControllerSpec!!.configurationSerializer
            stackHub.buildSubscription {
                subscribe {
                    childrenNavigationNodeController.configuration =
                        stringFormat.encodeToString(KoneList.serializer(serializer), it.navigationState)
                    childrenNavigationNodeController.children = it.children.nodesView.associateReified { node ->
                        stringFormat.encodeToString(serializer, node.key) mapsTo node.value.navigationNodeController!!
                    }
                }
                childrenNavigationNodeController.configuration =
                    stringFormat.encodeToString(KoneList.serializer(serializer), it.navigationState)
                childrenNavigationNodeController.children = it.children.nodesView.associateReified { node ->
                    stringFormat.encodeToString(serializer, node.key) mapsTo node.value.navigationNodeController!!
                }
            }
            childrenNavigationNodeController.setRestoration {
                try {
                    val restoredConfiguration = stringFormat.decodeFromString(KoneList.serializer(serializer), it)
                    navigationHub.navigate { restoredConfiguration }
                } catch (_: SerializationException) {} catch (_: IllegalArgumentException /* TODO: Remove eventually when Kone will start using correct exception types */) {}
            }
            val pathBuilder = navigationControllerSpec.pathBuilder
            if (pathBuilder != null) childrenNavigationNodeController.setPathBuilder {
                pathBuilder(
                    stackHub.value.navigationState,
                    stackHub.value.children.mapValues {
                        BuiltChild(
                            component = it.value.component,
                            context = it.value.context,
                        )
                    }
                )
            }
            val restorationByPath = navigationControllerSpec.restorationByPath
            if (restorationByPath != null) childrenNavigationNodeController.setRestorationByPath {
                restorationByPath(it, navigationHub)
            }
        }
        object : StackNode<Configuration, Component> {
            override val context: UIComponentContext = childrenComponentContext
            
            override val hub: KoneAsynchronousHub<ChildrenStack<Configuration, Component>> =
                stackHub.map {
                    val stack = it.navigationState.map { configuration -> ChildWithConfiguration(configuration, it.children[configuration].component) }
                    ChildrenStack(
                        active = stack.last(),
                        backStack = stack.dropLast(1u),
                    )
                }
            
            override suspend fun navigate(stackTransformation: StackNavigationEvent<Configuration>) {
                storingNavigationTarget.navigate(stackTransformation)
            }
        }
    }
}

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenFromToStackNode(
    configurationEquality: Equality<Configuration> = Equality.defaultFor(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<StackNavigationState<Configuration>, Configuration, Component, UIComponentContext, StackNavigationTarget<Configuration>>? = null,
    initialStack: KoneList<Configuration>,
    inactiveState: UIComponentLifecycleState,
    activeState: UIComponentLifecycleState,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: StackNavigationTarget<Configuration>) -> Component,
): StackNode<Configuration, Component> =
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
    navigationControllerSpec: NavigationControllerSpec<StackNavigationState<Configuration>, Configuration, Component, UIComponentContext, StackNavigationTarget<Configuration>>? = null,
    initialStack: KoneList<Configuration>,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: StackNavigationTarget<Configuration>) -> Component,
): StackNode<Configuration, Component>