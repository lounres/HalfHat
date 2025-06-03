package dev.lounres.komponentual.lifecycle

import dev.lounres.kone.registry.RegistryKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext


public expect enum class UIComponentLifecycleState {
    Destroyed, Initialized,
}

public expect enum class UIComponentLifecycleTransition {
    Destroy, Run,
}

public typealias UIComponentLifecycleCallback = (UIComponentLifecycleTransition) -> Unit

public typealias UIComponentLifecycle = Lifecycle<UIComponentLifecycleState, UIComponentLifecycleTransition>

public data object UIComponentLifecycleKey : RegistryKey<UIComponentLifecycle>

public val UIComponentLifecycle.isRun: Boolean get() = state != UIComponentLifecycleState.Initialized
public val UIComponentLifecycle.isDestroyed: Boolean get() = state == UIComponentLifecycleState.Destroyed

public typealias MutableUIComponentLifecycle = MutableLifecycle<UIComponentLifecycleState, UIComponentLifecycleTransition>

public expect fun MutableUIComponentLifecycle(): MutableUIComponentLifecycle

public expect fun mergeUIComponentLifecycles(
    lifecycle1: UIComponentLifecycle,
    lifecycle2: UIComponentLifecycle,
): UIComponentLifecycle

public expect fun MutableUIComponentLifecycle.moveTo(state: UIComponentLifecycleState)

public fun UIComponentLifecycle.attach(child: MutableUIComponentLifecycle) {
    val subscription = subscribe { child.apply(it) }
    child.subscribe { if (it == UIComponentLifecycleTransition.Destroy) subscription.cancel() }
    if (child.state == UIComponentLifecycleState.Destroyed) subscription.cancel()
}

public fun UIComponentLifecycle.createChild(optionalUIComponentLifecycle: UIComponentLifecycle? = null): UIComponentLifecycle =
    if (optionalUIComponentLifecycle == null) MutableUIComponentLifecycle().also { this.attach(it) }
    else mergeUIComponentLifecycles(this, optionalUIComponentLifecycle)

public fun CoroutineScope.attachTo(lifecycle: UIComponentLifecycle) {
    lifecycle.subscribe { if (it == UIComponentLifecycleTransition.Destroy) cancel() }
}

public fun UIComponentLifecycle.coroutineScope(context: CoroutineContext): CoroutineScope =
    CoroutineScope(context).apply { attachTo(this@coroutineScope) }