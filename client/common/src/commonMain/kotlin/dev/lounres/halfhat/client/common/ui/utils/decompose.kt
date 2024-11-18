package dev.lounres.halfhat.client.common.ui.utils

import com.arkivanov.decompose.router.stack.StackNavigator


public fun <C: Any> StackNavigator<C>.updateCurrent(update: (C) -> C) {
    navigate(
        transformer = { it.dropLast(1) + update(it.last()) },
        onComplete = { _, _ -> }
    )
}