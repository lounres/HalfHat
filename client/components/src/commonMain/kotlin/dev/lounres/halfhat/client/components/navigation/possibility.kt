package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.buildUiChild
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.halfhat.client.components.lifecycle.newMutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.logger.LoggerKey
import dev.lounres.halfhat.client.components.navigation.controller.NavigationItemController
import dev.lounres.halfhat.client.components.navigation.controller.NavigationNodeController
import dev.lounres.komponentual.navigation.*
import dev.lounres.kone.collections.map.associateReified
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.map.mapsTo
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.buildSubscription
import dev.lounres.kone.hub.map
import dev.lounres.kone.maybe.Maybe
import dev.lounres.kone.maybe.Some
import dev.lounres.kone.maybe.map
import dev.lounres.kone.registry.getOrNull
import dev.lounres.kone.relations.*
import dev.lounres.logKube.core.debug
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json


public typealias ChildrenPossibility<Configuration, Component> = Maybe<ChildWithConfiguration<Configuration, Component>>

public interface PossibilityItem<Configuration, Component> : PossibilityNavigationTarget<Configuration> {
    public val hub: KoneAsynchronousHub<ChildrenPossibility<Configuration, Component>>
}

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenPossibilityItem(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<Configuration>? = null,
    initialConfiguration: Maybe<Configuration>,
    updateLifecycle: suspend (configuration: Configuration, lifecycle: MutableUIComponentLifecycle, nextState: PossibilityNavigationState<Configuration>) -> Unit,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: PossibilityNavigationTarget<Configuration>) -> Component,
): PossibilityItem<Configuration, Component> {
    val logger = this.getOrNull(LoggerKey)
    val navigationNodeController = this.getOrNull(NavigationNodeController)
    val navigationItemController = when {
        navigationNodeController == null || navigationControllerSpec == null -> null
        navigationControllerSpec.key == null -> NavigationItemController().also { navigationNodeController.attachSoleItem(it) }
        else -> NavigationItemController().also { navigationNodeController.attachItem(navigationControllerSpec.key, it) }
    }
    val navigationHub = PossibilityNavigationHub<Configuration>()
    val possibilityHub = childrenPossibility(
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
        val serializer = navigationControllerSpec!!.configurationSerializer
        possibilityHub.buildSubscription {
            subscribe {
                navigationItemController.configuration =
                    Json.encodeToString(Maybe.serializer(serializer), it.navigationState)
                navigationItemController.nodes = it.children.nodesView.associateReified { node ->
                    Json.encodeToString(serializer, node.key) mapsTo node.value.navigationNodeController!!
                }
            }
            navigationItemController.configuration =
                Json.encodeToString(Maybe.serializer(serializer), it.navigationState)
            navigationItemController.nodes = it.children.nodesView.associateReified { node ->
                Json.encodeToString(serializer, node.key) mapsTo node.value.navigationNodeController!!
            }
        }
        navigationItemController.restoration = {
            try {
                val restoredConfiguration = Json.decodeFromString(Maybe.serializer(serializer), it)
                navigationHub.set(restoredConfiguration)
            } catch (e: SerializationException) {} catch (e: IllegalArgumentException /* TODO: Remove eventually when Kone will start using correct exception types */) {}
        }
    }
    return object : PossibilityItem<Configuration, Component> {
        override val hub: KoneAsynchronousHub<ChildrenPossibility<Configuration, Component>> =
            possibilityHub.map { it.navigationState.map { configuration -> ChildWithConfiguration(configuration, it.children[configuration].component) } }
        
        override suspend fun navigate(possibilityTransformation: PossibilityNavigationEvent<Configuration>) {
            navigationHub.navigate(possibilityTransformation)
        }
    }
}

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenToPossibilityItem(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<Configuration>? = null,
    initialConfiguration: Maybe<Configuration>,
    activeState: UIComponentLifecycleState,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: PossibilityNavigationTarget<Configuration>) -> Component,
): PossibilityItem<Configuration, Component> =
    uiChildrenPossibilityItem(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        loggerSource = loggerSource,
        navigationControllerSpec = navigationControllerSpec,
        initialConfiguration = initialConfiguration,
        updateLifecycle = { configuration, lifecycle, nextState ->
            check(nextState.let { it is Some<Configuration> && configurationEquality { it.value eq configuration } }) { "For some reason, there is preserved configuration that is different from the only configuration in the possibility" }
            lifecycle.moveTo(activeState)
        },
        childrenFactory = childrenFactory,
    )

public expect suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenDefaultPossibilityItem(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    navigationControllerSpec: NavigationControllerSpec<Configuration>? = null,
    initialConfiguration: Maybe<Configuration>,
    childrenFactory: (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: PossibilityNavigationTarget<Configuration>) -> Component,
): PossibilityItem<Configuration, Component>