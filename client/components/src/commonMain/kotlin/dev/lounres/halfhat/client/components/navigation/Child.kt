package dev.lounres.halfhat.client.components.navigation


internal data class Child<Component, ControllingLifecycle>(
    val component: Component,
    val controllingLifecycle: ControllingLifecycle,
)