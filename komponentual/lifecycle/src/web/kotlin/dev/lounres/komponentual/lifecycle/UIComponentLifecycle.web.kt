package dev.lounres.komponentual.lifecycle

import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.of


public actual enum class UIComponentLifecycleState {
    Destroyed, Initialized, Running, Background, Foreground
}

public actual enum class UIComponentLifecycleTransition(public actual val target: UIComponentLifecycleState) {
    Destroy(UIComponentLifecycleState.Destroyed),
    Run(UIComponentLifecycleState.Running),
    Appear(UIComponentLifecycleState.Background),
    Disappear(UIComponentLifecycleState.Running),
    Focus(UIComponentLifecycleState.Foreground),
    Defocus(UIComponentLifecycleState.Background),
}

public inline fun UIComponentLifecycle.subscribe(
    crossinline onRun: suspend () -> Unit = {},
    crossinline onAppear: suspend () -> Unit = {},
    crossinline onDisappear: suspend () -> Unit = {},
    crossinline onFocus: suspend () -> Unit = {},
    crossinline onDefocus: suspend () -> Unit = {},
    crossinline onDestroy: suspend () -> Unit = {},
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

internal actual fun checkNextState(
    previousState: UIComponentLifecycleState,
    nextState: UIComponentLifecycleState
): Boolean = when (previousState) {
    UIComponentLifecycleState.Destroyed -> false
    UIComponentLifecycleState.Initialized -> when (nextState) {
        UIComponentLifecycleState.Destroyed -> true
        UIComponentLifecycleState.Initialized -> false
        UIComponentLifecycleState.Running -> true
        UIComponentLifecycleState.Background -> true
        UIComponentLifecycleState.Foreground -> true
    }
    UIComponentLifecycleState.Running -> when (nextState) {
        UIComponentLifecycleState.Destroyed -> true
        UIComponentLifecycleState.Initialized -> false
        UIComponentLifecycleState.Running -> false
        UIComponentLifecycleState.Background -> true
        UIComponentLifecycleState.Foreground -> true
    }
    UIComponentLifecycleState.Background -> when (nextState) {
        UIComponentLifecycleState.Destroyed -> true
        UIComponentLifecycleState.Initialized -> false
        UIComponentLifecycleState.Running -> true
        UIComponentLifecycleState.Background -> false
        UIComponentLifecycleState.Foreground -> true
    }
    UIComponentLifecycleState.Foreground -> when (nextState) {
        UIComponentLifecycleState.Destroyed -> true
        UIComponentLifecycleState.Initialized -> false
        UIComponentLifecycleState.Running -> true
        UIComponentLifecycleState.Background -> true
        UIComponentLifecycleState.Foreground -> false
    }
}

internal actual fun decomposeTransition(
    previousState: UIComponentLifecycleState,
    nextState: UIComponentLifecycleState
): KoneList<UIComponentLifecycleTransition> = when (previousState) {
    UIComponentLifecycleState.Destroyed -> error("Unexpected UI component lifecycle transition")
    UIComponentLifecycleState.Initialized -> when (nextState) {
        UIComponentLifecycleState.Destroyed -> KoneList.of(UIComponentLifecycleTransition.Destroy)
        UIComponentLifecycleState.Initialized -> error("Unexpected UI component lifecycle transition")
        UIComponentLifecycleState.Running -> KoneList.of(UIComponentLifecycleTransition.Run)
        UIComponentLifecycleState.Background -> KoneList.of(UIComponentLifecycleTransition.Run, UIComponentLifecycleTransition.Appear)
        UIComponentLifecycleState.Foreground -> KoneList.of(UIComponentLifecycleTransition.Run, UIComponentLifecycleTransition.Appear, UIComponentLifecycleTransition.Focus)
    }
    UIComponentLifecycleState.Running -> when (nextState) {
        UIComponentLifecycleState.Destroyed -> KoneList.of(UIComponentLifecycleTransition.Destroy)
        UIComponentLifecycleState.Initialized -> error("Unexpected UI component lifecycle transition")
        UIComponentLifecycleState.Running -> error("Unexpected UI component lifecycle transition")
        UIComponentLifecycleState.Background -> KoneList.of(UIComponentLifecycleTransition.Appear)
        UIComponentLifecycleState.Foreground -> KoneList.of(UIComponentLifecycleTransition.Appear, UIComponentLifecycleTransition.Focus)
    }
    UIComponentLifecycleState.Background -> when (nextState) {
        UIComponentLifecycleState.Destroyed -> KoneList.of(UIComponentLifecycleTransition.Destroy)
        UIComponentLifecycleState.Initialized -> error("Unexpected UI component lifecycle transition")
        UIComponentLifecycleState.Running -> KoneList.of(UIComponentLifecycleTransition.Disappear)
        UIComponentLifecycleState.Background -> error("Unexpected UI component lifecycle transition")
        UIComponentLifecycleState.Foreground -> KoneList.of(UIComponentLifecycleTransition.Focus)
    }
    UIComponentLifecycleState.Foreground -> when (nextState) {
        UIComponentLifecycleState.Destroyed -> KoneList.of(UIComponentLifecycleTransition.Destroy)
        UIComponentLifecycleState.Initialized -> error("Unexpected UI component lifecycle transition")
        UIComponentLifecycleState.Running -> KoneList.of(UIComponentLifecycleTransition.Defocus, UIComponentLifecycleTransition.Disappear)
        UIComponentLifecycleState.Background -> KoneList.of(UIComponentLifecycleTransition.Defocus)
        UIComponentLifecycleState.Foreground -> error("Unexpected UI component lifecycle transition")
    }
}