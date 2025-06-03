package dev.lounres.komponentual.lifecycle


internal actual fun UIComponentLifecycle.attach(logicLifecycle: MutableLogicComponentLifecycle) {
    TODO("Not yet implemented")
}

internal actual class MergingUIRunningAndLogicLifecycle actual constructor(
    uiLifecycle: UIComponentLifecycle,
    logicLifecycle: LogicComponentLifecycle,
) : LogicComponentLifecycle {
    actual override val state: LogicComponentLifecycleState
        get() = TODO("Not yet implemented")
    
    actual override fun subscribe(callback: (LogicComponentLifecycleTransition) -> Unit): Lifecycle.Subscription {
        TODO("Not yet implemented")
    }
}