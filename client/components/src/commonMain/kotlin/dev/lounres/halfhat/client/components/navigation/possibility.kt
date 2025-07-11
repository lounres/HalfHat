package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.buildUiChild
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.halfhat.client.components.logger.logger
import dev.lounres.komponentual.navigation.ChildrenPossibility
import dev.lounres.komponentual.navigation.InnerPossibilityNavigationState
import dev.lounres.komponentual.navigation.PossibilityNavigation
import dev.lounres.komponentual.navigation.childrenPossibility
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.maybe.Maybe
import dev.lounres.kone.maybe.Some
import dev.lounres.kone.relations.*
import dev.lounres.kone.state.KoneAsynchronousState
import dev.lounres.logKube.core.debug


public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenPossibility(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    source: PossibilityNavigation<Configuration>,
    initialConfiguration: Maybe<Configuration>,
    updateLifecycle: suspend (configuration: Configuration, lifecycle: MutableUIComponentLifecycle, nextState: InnerPossibilityNavigationState<Configuration>) -> Unit,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneAsynchronousState<ChildrenPossibility<Configuration, Component>> =
    childrenPossibility(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        initialConfiguration = initialConfiguration,
        createChild = { configuration, nextState ->
            val controllingLifecycle = MutableUIComponentLifecycle()
            logger.debug(
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
            logger.debug(
                source = loggerSource,
                items = {
                    mapOf(
                        "configuration" to configuration.toString(),
                        "controllingLifecycle" to controllingLifecycle.toString(),
                        "component" to component.toString(),
                    )
                }
            ) { "Created child" }
            logger.debug(
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
            logger.debug(
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
            logger.debug(
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
            logger.debug(
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
        componentAccessor = { it.component },
    )

public suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenToPossibility(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    source: PossibilityNavigation<Configuration>,
    initialConfiguration: Maybe<Configuration>,
    activeState: UIComponentLifecycleState,
    childrenFactory: suspend (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneAsynchronousState<ChildrenPossibility<Configuration, Component>> =
    uiChildrenPossibility(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        loggerSource = loggerSource,
        source = source,
        initialConfiguration = initialConfiguration,
        updateLifecycle = { configuration, lifecycle, nextState ->
            check(nextState.current.let { it is Some<Configuration> && configurationEquality { it.value eq configuration } }) { "For some reason, there is preserved configuration that is different from the only configuration in the possibility" }
            lifecycle.moveTo(activeState)
        },
        childrenFactory = childrenFactory,
    )

public expect suspend fun <
    Configuration,
    Component,
> UIComponentContext.uiChildrenDefaultPossibility(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    loggerSource: String? = null,
    source: PossibilityNavigation<Configuration>,
    initialConfiguration: Maybe<Configuration>,
    childrenFactory: (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneAsynchronousState<ChildrenPossibility<Configuration, Component>>