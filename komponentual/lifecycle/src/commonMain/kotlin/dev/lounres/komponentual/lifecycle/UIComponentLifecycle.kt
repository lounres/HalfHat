package dev.lounres.komponentual.lifecycle

import dev.lounres.kone.registry.RegistryKey
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
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

internal fun UIComponentLifecycle.attach(child: MutableUIComponentLifecycle) {
    val temporaryLock = ReentrantLock()
    temporaryLock.withLock {
        val temporarySubscription = subscribe { temporaryLock.withLock {} }
        child.moveTo(state)
        subscribe { child.move(it) }
        temporarySubscription.cancel()
    }
}

public fun UIComponentLifecycle.child(controllingLifecycle: UIComponentLifecycle? = null): UIComponentLifecycle =
    if (controllingLifecycle == null) MutableUIComponentLifecycle().also { this.attach(it) }
    else mergeUIComponentLifecycles(this, controllingLifecycle)

public fun CoroutineScope.attachTo(lifecycle: UIComponentLifecycle) {
    lifecycle.subscribe { if (it == UIComponentLifecycleTransition.Destroy) cancel() }
}

public fun UIComponentLifecycle.coroutineScope(context: CoroutineContext): CoroutineScope =
    CoroutineScope(context).apply { attachTo(this@coroutineScope) }