package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.buildUiChild
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.halfhat.client.components.lifecycle.newMutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.logger.LoggerKey
import dev.lounres.halfhat.client.components.navigation.controller.NavigationControllerStringFormatKey
import dev.lounres.halfhat.client.components.navigation.controller.NavigationItemController
import dev.lounres.halfhat.client.components.navigation.controller.NavigationNodeController
import dev.lounres.komponentual.navigation.SlotNavigationEvent
import dev.lounres.komponentual.navigation.SlotNavigationHub
import dev.lounres.komponentual.navigation.SlotNavigationTarget
import dev.lounres.komponentual.navigation.childrenSlot
import dev.lounres.komponentual.navigation.set
import dev.lounres.kone.collections.map.associateReified
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.map.mapsTo
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.buildSubscription
import dev.lounres.kone.hub.map
import dev.lounres.kone.registry.getOrNull
import dev.lounres.kone.relations.*
import dev.lounres.logKube.core.debug
import kotlinx.serialization.SerializationException


public typealias ChildrenSlot<Configuration, Component> = ChildWithConfiguration<Configuration, Component>

public interface SlotItem<Configuration, Component> : SlotNavigationTarget<Configuration> {
    public val hub: KoneAsynchronousHub<ChildrenSlot<Configuration, Component>>
}

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenSlotItem(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<Configuration>? = null,
    initialConfiguration: Configuration,
    updateLifecycle: suspend (configuration: Configuration, lifecycle: MutableUIComponentLifecycle, nextState: Configuration) -> Unit,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: SlotNavigationTarget<Configuration>) -> Component,
): SlotItem<Configuration, Component> {
    val logger = this.getOrNull(LoggerKey)
    val navigationNodeController = this.getOrNull(NavigationNodeController)
    val navigationItemController =
        if (navigationNodeController == null || navigationControllerSpec == null) null
        else NavigationItemController().also { navigationNodeController.attachItem(navigationControllerSpec.key, it) }
    val navigationHub = SlotNavigationHub<Configuration>()
    val slotHub = childrenSlot(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = navigationHub,
        initialConfiguration = initialConfiguration,
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
        val stringFormat = this[NavigationControllerStringFormatKey]
        val serializer = navigationControllerSpec!!.configurationSerializer
        slotHub.buildSubscription {
            subscribe {
                navigationItemController.configuration =
                    stringFormat.encodeToString(serializer, it.navigationState)
                navigationItemController.nodes = it.children.nodesView.associateReified { node ->
                    stringFormat.encodeToString(serializer, node.key) mapsTo node.value.navigationNodeController!!
                }
            }
            navigationItemController.configuration =
                stringFormat.encodeToString(serializer, it.navigationState)
            navigationItemController.nodes = it.children.nodesView.associateReified { node ->
                stringFormat.encodeToString(serializer, node.key) mapsTo node.value.navigationNodeController!!
            }
        }
        navigationItemController.restoration = {
            try {
                val restoredConfiguration = stringFormat.decodeFromString(serializer, it)
                navigationHub.set(restoredConfiguration)
            } catch (_: SerializationException) {} catch (_: IllegalArgumentException /* TODO: Remove eventually when Kone will start using correct exception types */) {}
        }
    }
    return object : SlotItem<Configuration, Component> {
        override val hub: KoneAsynchronousHub<ChildrenSlot<Configuration, Component>> =
            slotHub.map { it.navigationState.let { configuration -> ChildWithConfiguration(configuration, it.children[configuration].component) } }
        
        override suspend fun navigate(slotTransformation: SlotNavigationEvent<Configuration>) {
            navigationHub.navigate(slotTransformation)
        }
    }
}

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenToSlotItem(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<Configuration>? = null,
    initialConfiguration: Configuration,
    activeState: UIComponentLifecycleState,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: SlotNavigationTarget<Configuration>) -> Component,
): SlotItem<Configuration, Component> =
    uiChildrenSlotItem(
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
> UIComponentContext.uiChildrenDefaultSlotItem(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<Configuration>? = null,
    initialConfiguration: Configuration,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: SlotNavigationTarget<Configuration>) -> Component,
): SlotItem<Configuration, Component>