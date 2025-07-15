package dev.lounres.halfhat.client.components.hub

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.structuralEqualityPolicy
import dev.lounres.kone.hub.KoneBlockingHub
import dev.lounres.kone.hub.buildSubscription


private class KoneBlockingHubSubscriptionComposeState<Value>(
    private val subscription: KoneBlockingHub.Subscription,
    private val actualState: MutableState<Value>,
) : RememberObserver, MutableState<Value> by actualState {
    override fun onRemembered() {}
    override fun onForgotten() {
        subscription.cancel()
    }
    override fun onAbandoned() {
        subscription.cancel()
    }
}

@Composable
public fun <Value> KoneBlockingHub<Value>.subscribeAsState(policy: SnapshotMutationPolicy<Value> = structuralEqualityPolicy()): State<Value> =
    remember(this, policy) {
        buildSubscription { initialValue ->
            val state = mutableStateOf(initialValue)
            val subscription = subscribe { state.value = it }
            KoneBlockingHubSubscriptionComposeState(subscription, state)
        }
    }