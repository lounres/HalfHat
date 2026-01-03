package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.buildUiChild
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.halfhat.client.components.lifecycle.newMutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.logger.LoggerKey
import dev.lounres.halfhat.client.components.navigation.controller.*
import dev.lounres.komponentual.navigation.NavigationHub
import dev.lounres.komponentual.navigation.NavigationTarget
import dev.lounres.komponentual.navigation.children
import dev.lounres.kone.collections.map.*
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.buildSubscription
import dev.lounres.kone.hub.map
import dev.lounres.kone.registry.getOrNull
import dev.lounres.kone.relations.Equality
import dev.lounres.kone.relations.Hashing
import dev.lounres.kone.relations.Order
import dev.lounres.kone.relations.defaultFor
import dev.lounres.logKube.core.debug
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException


public data class ChildWithConfigurationAndContext<out Configuration, out Component, out ComponentContext>(
    public val configuration: Configuration,
    public val component: Component,
    public val componentContext: ComponentContext,
)

public interface WithComponentContext<out ComponentContext> {
    public val context: ComponentContext
}

internal data class Child<Component, ControllingLifecycle, ComponentContext>(
    val component: Component,
    val controllingLifecycle: ControllingLifecycle,
    val navigationNodeController: NavigationNodeController?,
    val context: ComponentContext
)

public data class BuiltChild<Component, ComponentContext>(
    val component: Component,
    val context: ComponentContext,
)

public data class NavigationControllerSpec<NavigationState, Configuration, Component, ComponentContext, NavigationPublicState, NavigationEvent>(
    val key: String,
    val configurationSerializer: KSerializer<Configuration>,
    val pathBuilder: (suspend (navigationState: NavigationState, children: KoneMap<Configuration, BuiltChild<Component, ComponentContext>>) -> NavigationNodePath)? = null,
    val restorationByPath: (suspend (path: NavigationNodePath, navigationTarget: ChildrenNode<NavigationPublicState, NavigationEvent>) -> Unit)? = null
)

public interface ChildrenNode<NavigationPublicState, NavigationEvent> : NavigationTarget<NavigationEvent>, WithComponentContext<UIComponentContext> {
    public val hub: KoneAsynchronousHub<NavigationPublicState>
}

public suspend fun <
    Configuration,
    NavigationState,
    NavigationPublicState,
    NavigationEvent,
    Component,
> UIComponentContext.uiChildrenNode(
    configurationEquality: Equality<Configuration> = Equality.defaultFor(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    navigationStateEquality: Equality<NavigationState> = Equality.defaultFor(),
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<NavigationState, Configuration, Component, UIComponentContext, NavigationPublicState, NavigationEvent>? = null,
    navigationStateSerializer: (KSerializer<Configuration>) -> KSerializer<NavigationState>,
    initialState: NavigationState,
    stateConfigurationsMapping: (NavigationState) -> KoneSet<Configuration>,
    navigationTransition: suspend (previousState: NavigationState, event: NavigationEvent) -> NavigationState,
    restorationEvent: (nextState: NavigationState) -> NavigationEvent,
    updateLifecycle: suspend (configuration: Configuration, lifecycle: MutableUIComponentLifecycle, nextState: NavigationState) -> Unit,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: NavigationTarget<NavigationEvent>) -> Component,
    navigationStateMapper: (navigationState: NavigationState, children: KoneMap<Configuration, BuiltChild<Component, UIComponentContext>>) -> NavigationPublicState,
): ChildrenNode<NavigationPublicState, NavigationEvent> {
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
        val navigationHub = NavigationHub<NavigationEvent>()
        val storingNavigationTarget = NavigationTarget<NavigationEvent> {
            childrenComponentContext.navigationContext.doStoringNavigation {
                navigationHub.navigate(it)
            }
        }
        val childrenHub = children(
            configurationEquality = configurationEquality,
            configurationHashing = configurationHashing,
            configurationOrder = configurationOrder,
            navigationStateEquality = navigationStateEquality,
            childEquality = Equality { left, right ->
                left.component === right.component
                        && left.controllingLifecycle === right.controllingLifecycle
                        && left.navigationNodeController === right.navigationNodeController
            },
            source = navigationHub,
            initialState = initialState,
            stateConfigurationsMapping = stateConfigurationsMapping,
            navigationTransition = navigationTransition,
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
                val componentContext: UIComponentContext
                val component = childrenComponentContext.buildUiChild(controllingLifecycle, childNavigationNodeController) {
                    componentContext = it
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
                    context = componentContext,
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
            val configurationSerializer = navigationControllerSpec!!.configurationSerializer
            val navigationStateSerializer = navigationStateSerializer(configurationSerializer)
            childrenHub.buildSubscription {
                subscribe {
                    childrenNavigationNodeController.configuration =
                        stringFormat.encodeToString(navigationStateSerializer, it.navigationState)
                    childrenNavigationNodeController.children = it.children.nodesView.associateReified { node ->
                        stringFormat.encodeToString(configurationSerializer, node.key) mapsTo node.value.navigationNodeController!!
                    }
                }
                childrenNavigationNodeController.configuration =
                    stringFormat.encodeToString(navigationStateSerializer, it.navigationState)
                childrenNavigationNodeController.children = it.children.nodesView.associateReified { node ->
                    stringFormat.encodeToString(configurationSerializer, node.key) mapsTo node.value.navigationNodeController!!
                }
            }
            childrenNavigationNodeController.setRestoration {
                try {
                    val restoredState = stringFormat.decodeFromString(navigationStateSerializer, it)
                    navigationHub.navigate(restorationEvent(restoredState))
                } catch (_: SerializationException) {} catch (_: IllegalArgumentException /* TODO: Remove eventually when Kone will start using correct exception types */) {}
            }
            val pathBuilder = navigationControllerSpec.pathBuilder
            if (pathBuilder != null) childrenNavigationNodeController.setPathBuilder {
                pathBuilder(
                    childrenHub.value.navigationState,
                    childrenHub.value.children.mapValues {
                        BuiltChild(
                            component = it.value.component,
                            context = it.value.context,
                        )
                    }
                )
            }
            val restorationByPath = navigationControllerSpec.restorationByPath
            if (restorationByPath != null) childrenNavigationNodeController.setRestorationByPath {
                restorationByPath(
                    it,
                    object : ChildrenNode<NavigationPublicState, NavigationEvent> {
                        override val context: UIComponentContext = childrenComponentContext
                        
                        override val hub: KoneAsynchronousHub<NavigationPublicState> =
                            childrenHub.map { navigationStateMapper(it.navigationState, it.children.mapValues { (_, value) -> BuiltChild(value.component, value.context) }) }
                        
                        override suspend fun navigate(event: NavigationEvent) {
                            navigationHub.navigate(event)
                        }
                    }
                )
            }
        }
        object : ChildrenNode<NavigationPublicState, NavigationEvent> {
            override val context: UIComponentContext = childrenComponentContext
            
            override val hub: KoneAsynchronousHub<NavigationPublicState> =
                childrenHub.map { navigationStateMapper(it.navigationState, it.children.mapValues { (_, value) -> BuiltChild(value.component, value.context) }) }
            
            override suspend fun navigate(event: NavigationEvent) {
                storingNavigationTarget.navigate(event)
            }
        }
    }
}