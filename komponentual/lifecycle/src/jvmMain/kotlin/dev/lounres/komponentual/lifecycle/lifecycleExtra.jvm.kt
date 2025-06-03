package dev.lounres.komponentual.lifecycle

import dev.lounres.kone.automata.SynchronousAutomaton
import dev.lounres.kone.automata.apply
import dev.lounres.kone.collections.list.KoneMutableNoddedList
import dev.lounres.kone.collections.list.implementations.KoneTwoThreeTreeList
import dev.lounres.kone.collections.list.toKoneList
import dev.lounres.kone.collections.utils.forEach
import dev.lounres.kone.maybe.None
import dev.lounres.kone.maybe.Some
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock


internal actual fun UIComponentLifecycle.attach(logicLifecycle: MutableLogicComponentLifecycle) {
    val temporaryLock = ReentrantLock()
    temporaryLock.withLock {
        val temporarySubscription = subscribe { temporaryLock.withLock {} }
        when (this.state) {
            UIComponentLifecycleState.Initialized -> {}
            UIComponentLifecycleState.Destroyed -> logicLifecycle.apply(LogicComponentLifecycleTransition.Destroy)
            else -> logicLifecycle.apply(LogicComponentLifecycleTransition.Run)
        }
        subscribe {
            when (it) {
                UIComponentLifecycleTransition.Destroy -> logicLifecycle.apply(LogicComponentLifecycleTransition.Destroy)
                UIComponentLifecycleTransition.Run -> logicLifecycle.apply(LogicComponentLifecycleTransition.Run)
                else -> {}
            }
        }
        temporarySubscription.cancel()
    }
}

internal actual class MergingUIRunningAndLogicLifecycle actual constructor(
    uiLifecycle: UIComponentLifecycle,
    logicLifecycle: LogicComponentLifecycle,
) : LogicComponentLifecycle {
    private val callbacksLock = ReentrantLock()
    private val callbacks: KoneMutableNoddedList<(LogicComponentLifecycleTransition) -> Unit> = KoneTwoThreeTreeList() // TODO: Replace with concurrent queue
    private val automatonLock = ReentrantLock()
    
    private data class BiState(
        val uiState: UIComponentLifecycleState,
        val logicState: LogicComponentLifecycleState,
    ) {
        fun mapped(): LogicComponentLifecycleState =
            when {
                uiState == UIComponentLifecycleState.Destroyed || logicState == LogicComponentLifecycleState.Destroyed ->
                    LogicComponentLifecycleState.Destroyed
                uiState != UIComponentLifecycleState.Initialized || logicState != LogicComponentLifecycleState.Initialized ->
                    LogicComponentLifecycleState.Running
                else -> LogicComponentLifecycleState.Initialized
            }
    }
    
    private sealed interface BiTransition {
        data class UITransition(val transition: UIComponentLifecycleTransition) : BiTransition
        data class LogicTransition(val transition: LogicComponentLifecycleTransition) : BiTransition
    }
    
    private var automaton: SynchronousAutomaton<BiState, BiTransition>? = null
    
    init {
        automatonLock.withLock {
            val subscription1 = uiLifecycle.subscribe { transition ->
                automatonLock.withLock {
                    automaton!!.apply(BiTransition.UITransition(transition))
                }
            }
            val subscription2 = logicLifecycle.subscribe { transition ->
                automatonLock.withLock {
                    automaton!!.apply(BiTransition.LogicTransition(transition))
                }
            }
            
            val uiState = uiLifecycle.state
            val logicState = logicLifecycle.state
            
            automaton =
                SynchronousAutomaton(
                    initialState = BiState(uiState, logicState),
                    checkTransition = { state, transition ->
                        when (transition) {
                            is BiTransition.UITransition ->
                                when (state.uiState) {
                                    UIComponentLifecycleState.Initialized ->
                                        when (transition.transition) {
                                            UIComponentLifecycleTransition.Run -> Some(BiState(UIComponentLifecycleState.Running, state.logicState))
                                            UIComponentLifecycleTransition.Destroy -> Some(BiState(UIComponentLifecycleState.Destroyed, state.logicState))
                                            else -> None
                                        }
                                    UIComponentLifecycleState.Running ->
                                        when (transition.transition) {
                                            UIComponentLifecycleTransition.Appear -> Some(BiState(UIComponentLifecycleState.Background, state.logicState))
                                            UIComponentLifecycleTransition.Destroy -> Some(BiState(UIComponentLifecycleState.Destroyed, state.logicState))
                                            else -> None
                                        }
                                    UIComponentLifecycleState.Background ->
                                        when (transition.transition) {
                                            UIComponentLifecycleTransition.Disappear -> Some(BiState(UIComponentLifecycleState.Running, state.logicState))
                                            UIComponentLifecycleTransition.Focus -> Some(BiState(UIComponentLifecycleState.Foreground, state.logicState))
                                            UIComponentLifecycleTransition.Destroy -> Some(BiState(UIComponentLifecycleState.Destroyed, state.logicState))
                                            else -> None
                                        }
                                    UIComponentLifecycleState.Foreground ->
                                        when (transition.transition) {
                                            UIComponentLifecycleTransition.Defocus -> Some(BiState(UIComponentLifecycleState.Background, state.logicState))
                                            UIComponentLifecycleTransition.Destroy -> Some(BiState(UIComponentLifecycleState.Destroyed, state.logicState))
                                            else -> None
                                        }
                                    UIComponentLifecycleState.Destroyed -> None
                                }
                            is BiTransition.LogicTransition ->
                                when (state.logicState) {
                                    LogicComponentLifecycleState.Initialized ->
                                        when (transition.transition) {
                                            LogicComponentLifecycleTransition.Run -> Some(BiState(state.uiState, LogicComponentLifecycleState.Running))
                                            LogicComponentLifecycleTransition.Stop -> None
                                            LogicComponentLifecycleTransition.Destroy -> Some(BiState(state.uiState, LogicComponentLifecycleState.Destroyed))
                                        }
                                    
                                    LogicComponentLifecycleState.Running ->
                                        when (transition.transition) {
                                            LogicComponentLifecycleTransition.Run -> None
                                            LogicComponentLifecycleTransition.Stop -> Some(BiState(state.uiState, LogicComponentLifecycleState.Initialized))
                                            LogicComponentLifecycleTransition.Destroy -> Some(BiState(state.uiState, LogicComponentLifecycleState.Destroyed))
                                        }
                                    
                                    LogicComponentLifecycleState.Destroyed -> None
                                }
                        }
                    },
                    onTransition = { previousState, transition, nextState ->
                        if (previousState.mapped() != nextState.mapped()) {
                            val transition = when(transition) {
                                is BiTransition.LogicTransition -> transition.transition
                                is BiTransition.UITransition ->
                                    when (transition.transition) {
                                        UIComponentLifecycleTransition.Destroy ->  LogicComponentLifecycleTransition.Destroy
                                        UIComponentLifecycleTransition.Run -> LogicComponentLifecycleTransition.Run
                                        else -> error("This transition should not have triggered merged state change, but actually did it.")
                                    }
                            }
                            callbacksLock.withLock { callbacks.toKoneList() }.forEach { it(transition) }
                            if (transition == LogicComponentLifecycleTransition.Destroy) {
                                subscription1.cancel()
                                subscription2.cancel()
                            }
                        }
                    },
                )
            
            if (uiState == UIComponentLifecycleState.Destroyed || logicState == LogicComponentLifecycleState.Destroyed) {
                subscription1.cancel()
                subscription2.cancel()
            }
        }
    }
    
    actual override val state: LogicComponentLifecycleState
        get() = automaton!!.state.mapped()
    actual override fun subscribe(callback: LogicComponentLifecycleCallback) =
        callbacksLock.withLock {
            val node = callbacks.addNode(callback)
            Lifecycle.Subscription {
                callbacksLock.withLock {
                    node.remove()
                }
            }
        }
}