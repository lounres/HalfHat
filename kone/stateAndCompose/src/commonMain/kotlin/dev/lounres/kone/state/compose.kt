package dev.lounres.kone.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.structuralEqualityPolicy


@Composable
public fun <Element> KoneState<Element>.subscribeAsState(policy: SnapshotMutationPolicy<Element> = structuralEqualityPolicy()): State<Element> {
    val state = remember(this, policy) { mutableStateOf(element, policy) }
    
    DisposableEffect(this) {
        val disposable = subscribe { state.value = it }
        onDispose { disposable.cancel() }
    }
    
    return state
}