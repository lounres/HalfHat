package dev.lounres.kone.state

import dev.lounres.kone.automata.LockingAutomaton
import dev.lounres.kone.automata.apply
import dev.lounres.kone.automata.applyMaybe
import dev.lounres.kone.collections.list.KoneMutableNoddedList
import dev.lounres.kone.collections.list.implementations.KoneTwoThreeTreeList
import dev.lounres.kone.collections.list.toKoneList
import dev.lounres.kone.collections.utils.forEach
import dev.lounres.kone.contexts.invoke
import dev.lounres.kone.maybe.None
import dev.lounres.kone.maybe.Some
import dev.lounres.kone.relations.Equality
import dev.lounres.kone.relations.defaultEquality
import dev.lounres.kone.relations.eq
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


public interface KoneState<out Value> {
    public val value: Value
    
    public fun subscribe(observer: (Value) -> Unit): Cancellation
    
    public fun interface Cancellation {
        public fun cancel()
    }
}

public interface KoneMutableState<Value> : KoneState<Value> {
    override var value: Value
    
    public fun compareAndSet(expected: Value, new: Value): Boolean
}

public fun <Value> KoneMutableState(
    initialElement: Value,
    elementEquality: Equality<Value> = defaultEquality()
): KoneMutableState<Value> = KoneMutableStateImpl(initialElement, elementEquality)

internal class KoneMutableStateImpl<Value>(
    initialElement: Value,
    private val elementEquality: Equality<Value>
) : KoneMutableState<Value> {
    private val observersLock = ReentrantLock()
    private val observers: KoneMutableNoddedList<(Value) -> Unit> = KoneTwoThreeTreeList()
    private val automaton =
        LockingAutomaton<Value, Value>(
            initialState = initialElement,
            checkTransition = { previousState, transition ->
                if (elementEquality { previousState eq transition }) None
                else Some(transition)
            },
            onTransition = { _, _, nextState ->
                observersLock.withLock { observers.toKoneList() }.forEach { it(nextState) }
            }
        )
    
    override var value: Value
        get() = automaton.state
        set(value) {
            automaton.apply(value)
        }
    
    override fun compareAndSet(expected: Value, new: Value): Boolean =
        automaton.applyMaybe { current ->
            if (elementEquality { current eq expected }) Some(new)
            else None
        }
    
    override fun subscribe(observer: (Value) -> Unit): KoneState.Cancellation =
        observersLock.withLock {
            val node = observers.addNode(observer)
            KoneState.Cancellation { node.remove() }
        }
}

public fun <Value> KoneMutableState<Value>.update(function: (Value) -> Value) {
    while (true) {
        val previousElement = value
        val nextElement = function(previousElement)
        
        if (compareAndSet(previousElement, nextElement)) return
    }
}

public fun <Value> KoneMutableState<Value>.updateAndGet(function: (Value) -> Value): Value {
    while (true) {
        val previousElement = value
        val nextElement = function(previousElement)
        
        if (compareAndSet(previousElement, nextElement)) return nextElement
    }
}

public fun <Value> KoneMutableState<Value>.getAndUpdate(function: (Value) -> Value): Value {
    while (true) {
        val previousElement = value
        val nextElement = function(previousElement)
        
        if (compareAndSet(previousElement, nextElement)) return previousElement
    }
}

public fun <Value, Result> KoneState<Value>.map(elementEquality: Equality<Result> = defaultEquality(), transform: (Value) -> Result): KoneState<Result> {
    val temporaryLock = ReentrantLock()
    temporaryLock.withLock {
        val temporarySubscription = subscribe { temporaryLock.withLock {  } }
        val result = KoneMutableState(transform(value), elementEquality)
        subscribe { result.value = transform(it) }
        temporarySubscription.cancel()
        return result
    }
}

public fun <Value> KoneState<Value>.toStateFlow(): StateFlow<Value> {
    val temporaryLock = ReentrantLock()
    temporaryLock.withLock {
        val temporarySubscription = subscribe { temporaryLock.withLock {  } }
        val result = MutableStateFlow(value)
        subscribe { result.value = it }
        temporarySubscription.cancel()
        return result
    }
}