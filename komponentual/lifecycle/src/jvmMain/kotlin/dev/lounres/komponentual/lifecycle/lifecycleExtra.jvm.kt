package dev.lounres.komponentual.lifecycle

import dev.lounres.kone.automata.CheckResult
import dev.lounres.kone.automata.SynchronousAutomaton
import dev.lounres.kone.automata.move
import dev.lounres.kone.collections.list.KoneMutableNoddedList
import dev.lounres.kone.collections.list.implementations.KoneGCLinkedList
import dev.lounres.kone.collections.list.toKoneList
import dev.lounres.kone.collections.utils.forEach
import dev.lounres.utils.atomicFUAtomics.withLock
import kotlinx.atomicfu.locks.ReentrantLock


internal actual fun UIComponentLifecycle.attach(logicLifecycle: MutableLogicComponentLifecycle) {
    val temporaryLock = ReentrantLock()
    temporaryLock.withLock {
        val temporarySubscription = subscribe { temporaryLock.withLock {} }
        when (this.state) {
            UIComponentLifecycleState.Initialized -> {}
            UIComponentLifecycleState.Destroyed -> logicLifecycle.move(LogicComponentLifecycleTransition.Destroy)
            else -> logicLifecycle.move(LogicComponentLifecycleTransition.Run)
        }
        subscribe {
            when (it) {
                UIComponentLifecycleTransition.Destroy -> logicLifecycle.move(LogicComponentLifecycleTransition.Destroy)
                UIComponentLifecycleTransition.Run -> logicLifecycle.move(LogicComponentLifecycleTransition.Run)
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
    private val callbacks: KoneMutableNoddedList<(LogicComponentLifecycleTransition) -> Unit> = KoneGCLinkedList() // TODO: Replace with concurrent queue
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
    
    private var automaton: SynchronousAutomaton<BiState, BiTransition, Nothing?>
    
    init {
        automatonLock.withLock {
            val subscription1 = uiLifecycle.subscribe { transition ->
                automatonLock.withLock {
                    automaton.move(BiTransition.UITransition(transition))
                }
            }
            val subscription2 = logicLifecycle.subscribe { transition ->
                automatonLock.withLock {
                    automaton.move(BiTransition.LogicTransition(transition))
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
                                            UIComponentLifecycleTransition.Run -> CheckResult.Success(BiState(UIComponentLifecycleState.Running, state.logicState))
                                            UIComponentLifecycleTransition.Destroy -> CheckResult.Success(BiState(UIComponentLifecycleState.Destroyed, state.logicState))
                                            else -> CheckResult.Failure(null)
                                        }
                                    UIComponentLifecycleState.Running ->
                                        when (transition.transition) {
                                            UIComponentLifecycleTransition.Appear -> CheckResult.Success(BiState(UIComponentLifecycleState.Background, state.logicState))
                                            UIComponentLifecycleTransition.Destroy -> CheckResult.Success(BiState(UIComponentLifecycleState.Destroyed, state.logicState))
                                            else -> CheckResult.Failure(null)
                                        }
                                    UIComponentLifecycleState.Background ->
                                        when (transition.transition) {
                                            UIComponentLifecycleTransition.Disappear -> CheckResult.Success(BiState(UIComponentLifecycleState.Running, state.logicState))
                                            UIComponentLifecycleTransition.Focus -> CheckResult.Success(BiState(UIComponentLifecycleState.Foreground, state.logicState))
                                            UIComponentLifecycleTransition.Destroy -> CheckResult.Success(BiState(UIComponentLifecycleState.Destroyed, state.logicState))
                                            else -> CheckResult.Failure(null)
                                        }
                                    UIComponentLifecycleState.Foreground ->
                                        when (transition.transition) {
                                            UIComponentLifecycleTransition.Defocus -> CheckResult.Success(BiState(UIComponentLifecycleState.Background, state.logicState))
                                            UIComponentLifecycleTransition.Destroy -> CheckResult.Success(BiState(UIComponentLifecycleState.Destroyed, state.logicState))
                                            else -> CheckResult.Failure(null)
                                        }
                                    UIComponentLifecycleState.Destroyed -> CheckResult.Failure(null)
                                }
                            is BiTransition.LogicTransition ->
                                when (state.logicState) {
                                    LogicComponentLifecycleState.Initialized ->
                                        when (transition.transition) {
                                            LogicComponentLifecycleTransition.Run -> CheckResult.Success(BiState(state.uiState, LogicComponentLifecycleState.Running))
                                            LogicComponentLifecycleTransition.Stop -> CheckResult.Failure(null)
                                            LogicComponentLifecycleTransition.Destroy -> CheckResult.Success(BiState(state.uiState, LogicComponentLifecycleState.Destroyed))
                                        }
                                    
                                    LogicComponentLifecycleState.Running ->
                                        when (transition.transition) {
                                            LogicComponentLifecycleTransition.Run -> CheckResult.Failure(null)
                                            LogicComponentLifecycleTransition.Stop -> CheckResult.Success(BiState(state.uiState, LogicComponentLifecycleState.Initialized))
                                            LogicComponentLifecycleTransition.Destroy -> CheckResult.Success(BiState(state.uiState, LogicComponentLifecycleState.Destroyed))
                                        }
                                    
                                    LogicComponentLifecycleState.Destroyed -> CheckResult.Failure(null)
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