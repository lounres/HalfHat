package dev.lounres.komponentual.lifecycle


public actual enum class UIComponentLifecycleState {
    Destroyed, Initialized, Running, Background , Foreground
}

public actual enum class UIComponentLifecycleTransition {
    Destroy, Run, Appear, Disappear, Focus, Defocus
}

public inline fun UIComponentLifecycle.subscribe(
    crossinline onRun: () -> Unit = {},
    crossinline onAppear: () -> Unit = {},
    crossinline onDisappear: () -> Unit = {},
    crossinline onFocus: () -> Unit = {},
    crossinline onDefocus: () -> Unit = {},
    crossinline onDestroy: () -> Unit = {},
) {
    subscribe {
        when (it) {
            UIComponentLifecycleTransition.Run -> onRun()
            UIComponentLifecycleTransition.Appear -> onAppear()
            UIComponentLifecycleTransition.Disappear -> onDisappear()
            UIComponentLifecycleTransition.Focus -> onFocus()
            UIComponentLifecycleTransition.Defocus -> onDefocus()
            UIComponentLifecycleTransition.Destroy -> onDestroy()
        }
    }
}

public actual fun MutableUIComponentLifecycle(): MutableUIComponentLifecycle = TODO()

public actual fun mergeUIComponentLifecycles(
    lifecycle1: UIComponentLifecycle,
    lifecycle2: UIComponentLifecycle,
): UIComponentLifecycle = TODO()

public actual fun MutableUIComponentLifecycle.moveTo(state: UIComponentLifecycleState) {
    when (state) {
        UIComponentLifecycleState.Destroyed -> {
            apply(UIComponentLifecycleTransition.Destroy)
        }
        UIComponentLifecycleState.Initialized -> {}
        UIComponentLifecycleState.Running -> {
            apply(UIComponentLifecycleTransition.Run)
            apply(UIComponentLifecycleTransition.Defocus)
            apply(UIComponentLifecycleTransition.Disappear)
        }
        UIComponentLifecycleState.Background -> {
            apply(UIComponentLifecycleTransition.Run)
            apply(UIComponentLifecycleTransition.Appear)
            apply(UIComponentLifecycleTransition.Defocus)
        }
        UIComponentLifecycleState.Foreground -> {
            apply(UIComponentLifecycleTransition.Run)
            apply(UIComponentLifecycleTransition.Appear)
            apply(UIComponentLifecycleTransition.Focus)
        }
    }
}