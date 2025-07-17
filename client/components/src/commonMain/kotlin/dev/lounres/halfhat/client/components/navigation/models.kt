package dev.lounres.halfhat.client.components.navigation

import dev.lounres.halfhat.client.components.navigation.controller.NavigationNodeController
import kotlinx.serialization.KSerializer


public data class ChildWithConfiguration<out Configuration, out Component>(
    public val configuration: Configuration,
    public val component: Component,
)

internal data class Child<Component, ControllingLifecycle>(
    val component: Component,
    val controllingLifecycle: ControllingLifecycle,
    val navigationNodeController: NavigationNodeController?,
)

public data class NavigationControllerSpec<Configuration>(
    val key: String?,
    val configurationSerializer: KSerializer<Configuration>,
)