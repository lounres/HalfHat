package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.navigation.controller.NavigationNodeController
import dev.lounres.halfhat.client.components.navigation.controller.NavigationNodePath
import dev.lounres.kone.collections.map.KoneMap
import kotlinx.serialization.KSerializer


public data class ChildWithConfiguration<out Configuration, out Component>(
    public val configuration: Configuration,
    public val component: Component,
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

public data class NavigationControllerSpec<NavigationStateType, Configuration, Component, ComponentContext, NavigationTarget>(
    val key: String,
    val configurationSerializer: KSerializer<Configuration>,
    val pathBuilder: (suspend (navigationState: NavigationStateType, children: KoneMap<Configuration, BuiltChild<Component, ComponentContext>>) -> NavigationNodePath)? = null,
    val restorationByPath: (suspend (path: NavigationNodePath, navigationTarget: NavigationTarget) -> Unit)? = null
)