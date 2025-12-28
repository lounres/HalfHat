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
import dev.lounres.komponentual.navigation.SlotNavigationEvent
import dev.lounres.komponentual.navigation.SlotNavigationHub
import dev.lounres.komponentual.navigation.SlotNavigationTarget
import dev.lounres.komponentual.navigation.childrenSlot
import dev.lounres.komponentual.navigation.set
import dev.lounres.kone.collections.map.KoneMap
import dev.lounres.kone.collections.map.associateReified
import dev.lounres.kone.collections.map.build
import dev.lounres.kone.collections.map.contains
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.map.mapValues
import dev.lounres.kone.collections.map.mapsTo
import dev.lounres.kone.collections.map.setAllFrom
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.buildSubscription
import dev.lounres.kone.hub.map
import dev.lounres.kone.registry.getOrNull
import dev.lounres.kone.relations.*
import dev.lounres.logKube.core.debug
import kotlinx.serialization.SerializationException


public typealias ChildrenSlot<Configuration, Component> = ChildWithConfiguration<Configuration, Component>

public interface SlotNode<Configuration, Component> : SlotNavigationTarget<Configuration>, WithComponentContext<UIComponentContext> {
    public val hub: KoneAsynchronousHub<ChildrenSlot<Configuration, Component>>
}

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenSlotNode(
    configurationEquality: Equality<Configuration> = Equality.defaultFor(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<Configuration, Configuration, Component, UIComponentContext, SlotNavigationTarget<Configuration>>? = null,
    initialConfiguration: Configuration,
    updateLifecycle: suspend (configuration: Configuration, lifecycle: MutableUIComponentLifecycle, nextState: Configuration) -> Unit,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: SlotNavigationTarget<Configuration>) -> Component,
): SlotNode<Configuration, Component> {
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
        val navigationHub = SlotNavigationHub<Configuration>()
        val storingNavigationTarget = SlotNavigationTarget {
            childrenComponentContext.navigationContext.doStoringNavigation {
                navigationHub.navigate(it)
            }
        }
        val slotHub = childrenSlot(
            configurationEquality = configurationEquality,
            configurationHashing = configurationHashing,
            configurationOrder = configurationOrder,
            source = navigationHub,
            initialConfiguration = initialConfiguration,
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
                    context = context
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
            slotHub.buildSubscription {
                subscribe {
                    childrenNavigationNodeController.configuration =
                        stringFormat.encodeToString(serializer, it.navigationState)
                    childrenNavigationNodeController.children = it.children.nodesView.associateReified { node ->
                        stringFormat.encodeToString(serializer, node.key) mapsTo node.value.navigationNodeController!!
                    }
                }
                childrenNavigationNodeController.configuration =
                    stringFormat.encodeToString(serializer, it.navigationState)
                childrenNavigationNodeController.children = it.children.nodesView.associateReified { node ->
                    stringFormat.encodeToString(serializer, node.key) mapsTo node.value.navigationNodeController!!
                }
            }
            childrenNavigationNodeController.setRestoration {
                try {
                    val restoredConfiguration = stringFormat.decodeFromString(serializer, it)
                    navigationHub.set(restoredConfiguration)
                } catch (_: SerializationException) {} catch (_: IllegalArgumentException /* TODO: Remove eventually when Kone will start using correct exception types */) {}
            }
            val pathBuilder = navigationControllerSpec.pathBuilder
            if (pathBuilder != null) childrenNavigationNodeController.setPathBuilder {
                pathBuilder(
                    slotHub.value.navigationState,
                    slotHub.value.children.mapValues {
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
        object : SlotNode<Configuration, Component> {
            override val context: UIComponentContext = childrenComponentContext
            
            override val hub: KoneAsynchronousHub<ChildrenSlot<Configuration, Component>> =
                slotHub.map { it.navigationState.let { configuration -> ChildWithConfiguration(configuration, it.children[configuration].component) } }
            
            override suspend fun navigate(slotTransformation: SlotNavigationEvent<Configuration>) {
                storingNavigationTarget.navigate(slotTransformation)
            }
        }
    }
}

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenToSlotNode(
    configurationEquality: Equality<Configuration> = Equality.defaultFor(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<Configuration, Configuration, Component, UIComponentContext, SlotNavigationTarget<Configuration>>? = null,
    initialConfiguration: Configuration,
    activeState: UIComponentLifecycleState,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: SlotNavigationTarget<Configuration>) -> Component,
): SlotNode<Configuration, Component> =
    uiChildrenSlotNode(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        loggerSource = loggerSource,
        navigationControllerSpec = navigationControllerSpec,
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
> UIComponentContext.uiChildrenDefaultSlotNode(
    configurationEquality: Equality<Configuration> = Equality.defaultFor(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<Configuration, Configuration, Component, UIComponentContext, SlotNavigationTarget<Configuration>>? = null,
    initialConfiguration: Configuration,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: SlotNavigationTarget<Configuration>) -> Component,
): SlotNode<Configuration, Component>