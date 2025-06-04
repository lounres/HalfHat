package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.komponentual.lifecycle.UIComponentLifecycleState
import dev.lounres.komponentual.navigation.ChildrenPossibility
import dev.lounres.komponentual.navigation.PossibilityNavigation
import dev.lounres.kone.maybe.Maybe
import dev.lounres.kone.relations.Equality
import dev.lounres.kone.relations.Hashing
import dev.lounres.kone.relations.Order
import dev.lounres.kone.state.KoneState


public actual fun <Configuration, Component> UIComponentContext.uiChildrenDefaultPossibility(
    configurationEquality: Equality<Configuration>,
    configurationHashing: Hashing<Configuration>?,
    configurationOrder: Order<Configuration>?,
    source: PossibilityNavigation<Configuration>,
    initialConfiguration: () -> Maybe<Configuration>,
    childrenFactory: (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneState<ChildrenPossibility<Configuration, Component>> {
    TODO("Not yet implemented")
}