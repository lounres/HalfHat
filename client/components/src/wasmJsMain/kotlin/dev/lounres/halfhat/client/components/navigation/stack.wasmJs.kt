package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.komponentual.navigation.ChildrenStack
import dev.lounres.komponentual.navigation.StackNavigation
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.relations.Equality
import dev.lounres.kone.relations.Hashing
import dev.lounres.kone.relations.Order
import dev.lounres.kone.state.KoneState


public actual fun <Configuration, Component> UIComponentContext.uiChildrenDefaultStack(
    configurationEquality: Equality<Configuration>,
    configurationHashing: Hashing<Configuration>?,
    configurationOrder: Order<Configuration>?,
    source: StackNavigation<Configuration>,
    initialStack: () -> KoneList<Configuration>,
    childrenFactory: (configuration: Configuration, componentContext: UIComponentContext) -> Component,
): KoneState<ChildrenStack<Configuration, Component>> {
    TODO("Not yet implemented")
}