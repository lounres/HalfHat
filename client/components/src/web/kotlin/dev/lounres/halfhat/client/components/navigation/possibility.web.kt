package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.lifecycle.UIComponentLifecycleState
import dev.lounres.komponentual.navigation.PossibilityNavigationTarget
import dev.lounres.kone.maybe.Maybe
import dev.lounres.kone.relations.Equality
import dev.lounres.kone.relations.Hashing
import dev.lounres.kone.relations.Order


public actual suspend fun <Configuration, Component> UIComponentContext.uiChildrenDefaultPossibilityItem(
    configurationEquality: Equality<Configuration>,
    configurationHashing: Hashing<Configuration>?,
    configurationOrder: Order<Configuration>?,
    loggerSource: String?,
    navigationControllerSpec: NavigationControllerSpec<Configuration>?,
    initialConfiguration: Maybe<Configuration>,
    childrenFactory: (configuration: Configuration, componentContext: UIComponentContext, navigationTarget: PossibilityNavigationTarget<Configuration>) -> Component,
): PossibilityItem<Configuration, Component> =
    uiChildrenToPossibilityItem(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        loggerSource = loggerSource,
        navigationControllerSpec = navigationControllerSpec,
        initialConfiguration = initialConfiguration,
        activeState = UIComponentLifecycleState.Foreground,
        childrenFactory = childrenFactory,
    )