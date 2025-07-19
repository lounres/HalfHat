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
import dev.lounres.komponentual.navigation.VariantsNavigationEvent
import dev.lounres.komponentual.navigation.VariantsNavigationHub
import dev.lounres.komponentual.navigation.VariantsNavigationState
import dev.lounres.komponentual.navigation.VariantsNavigationTarget
import dev.lounres.komponentual.navigation.childrenVariants
import dev.lounres.komponentual.navigation.set
import dev.lounres.kone.collections.map.KoneMap
import dev.lounres.kone.collections.map.associateReified
import dev.lounres.kone.collections.map.component1
import dev.lounres.kone.collections.map.component2
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.map.mapValues
import dev.lounres.kone.collections.map.mapsTo
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.buildSubscription
import dev.lounres.kone.hub.map
import dev.lounres.kone.registry.getOrNull
import dev.lounres.kone.relations.*
import dev.lounres.logKube.core.debug
import kotlinx.serialization.SerializationException


public data class ChildrenVariants<Configuration, Component>(
    public val active: ChildWithConfiguration<Configuration, Component>,
    public val allVariants: KoneMap<Configuration, Component>,
)

public interface VariantsItem<Configuration, Component> : VariantsNavigationTarget<Configuration> {
    public val hub: KoneAsynchronousHub<ChildrenVariants<Configuration, Component>>
}

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenVariantsItem(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<Configuration>? = null,
    allVariants: KoneSet<Configuration>,
    initialVariant: Configuration,
    updateLifecycle: suspend (configuration: Configuration, lifecycle: MutableUIComponentLifecycle, nextState: VariantsNavigationState<Configuration>) -> Unit,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: VariantsNavigationTarget<Configuration>) -> Component,
): VariantsItem<Configuration, Component> {
    val logger = this.getOrNull(LoggerKey)
    val navigationNodeController = this.getOrNull(NavigationNodeController)
    val navigationItemController =
        if (navigationNodeController == null || navigationControllerSpec == null) null
        else NavigationItemController().also { navigationNodeController.attachItem(navigationControllerSpec.key, it) }
    val navigationHub = VariantsNavigationHub<Configuration>()
    val variantsHub = childrenVariants(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = navigationHub,
        allVariants = allVariants,
        initialVariant = initialVariant,
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
        variantsHub.buildSubscription {
            subscribe {
                navigationItemController.configuration =
                    stringFormat.encodeToString(serializer, it.navigationState.currentVariant)
                navigationItemController.nodes = it.children.nodesView.associateReified { node ->
                    stringFormat.encodeToString(serializer, node.key) mapsTo node.value.navigationNodeController!!
                }
            }
            navigationItemController.configuration =
                stringFormat.encodeToString(serializer, it.navigationState.currentVariant)
            navigationItemController.nodes = it.children.nodesView.associateReified { node ->
                stringFormat.encodeToString(serializer, node.key) mapsTo node.value.navigationNodeController!!
            }
        }
        navigationItemController.restoration = {
            try {
                val restoredConfiguration = stringFormat.decodeFromString(serializer, it)
                navigationHub.set(restoredConfiguration)
            } catch (_: SerializationException) {
            } catch (_: IllegalArgumentException /* TODO: Remove eventually when Kone will start using correct exception types */) {
            }
        }
    }
    return object : VariantsItem<Configuration, Component> {
        override val hub: KoneAsynchronousHub<ChildrenVariants<Configuration, Component>> =
            variantsHub.map {
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
        
        override suspend fun navigate(variantsTransformation: VariantsNavigationEvent<Configuration>) {
            navigationHub.navigate(variantsTransformation)
        }
    }
}

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenFromToVariantsItem(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<Configuration>? = null,
    allVariants: KoneSet<Configuration>,
    initialVariant: Configuration,
    inactiveState: UIComponentLifecycleState,
    activeState: UIComponentLifecycleState,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: VariantsNavigationTarget<Configuration>) -> Component,
): VariantsItem<Configuration, Component> =
    uiChildrenVariantsItem(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        loggerSource = loggerSource,
        navigationControllerSpec = navigationControllerSpec,
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
> UIComponentContext.uiChildrenDefaultVariantsItem(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<Configuration>? = null,
    allVariants: KoneSet<Configuration>,
    initialVariant: Configuration,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: VariantsNavigationTarget<Configuration>) -> Component,
): VariantsItem<Configuration, Component>