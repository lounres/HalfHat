package dev.lounres.komponentual.lifecycle


public fun UIComponentLifecycle.runningLogicChild(controllingLifecycle: LogicComponentLifecycle? = null): LogicComponentLifecycle =
    if (controllingLifecycle == null) MutableLogicComponentLifecycle().also { this.attach(it) }
    else MergingUIRunningAndLogicLifecycle(this, controllingLifecycle)

internal expect fun UIComponentLifecycle.attach(logicLifecycle: MutableLogicComponentLifecycle)

internal expect class MergingUIRunningAndLogicLifecycle(
    uiLifecycle: UIComponentLifecycle,
    logicLifecycle: LogicComponentLifecycle,
) : LogicComponentLifecycle {
    override val state: LogicComponentLifecycleState
    override fun subscribe(callback: (LogicComponentLifecycleTransition) -> Unit): Lifecycle.Subscription
}