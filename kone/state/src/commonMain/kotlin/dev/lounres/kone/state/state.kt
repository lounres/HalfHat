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


public interface KoneState<out Element> {
    public val element: Element
    
    public fun subscribe(observer: (Element) -> Unit): Cancellation
    
    public fun interface Cancellation {
        public fun cancel()
    }
}

public interface KoneMutableState<Element> : KoneState<Element> {
    override var element: Element
    
    public fun compareAndSet(expected: Element, new: Element): Boolean
}

public fun <Element> KoneMutableState(
    initialElement: Element,
    elementEquality: Equality<Element> = defaultEquality()
): KoneMutableState<Element> = KoneMutableStateImpl(initialElement, elementEquality)

internal class KoneMutableStateImpl<Element>(
    initialElement: Element,
    private val elementEquality: Equality<Element>
) : KoneMutableState<Element> {
    private val observersLock = ReentrantLock()
    private val observers: KoneMutableNoddedList<(Element) -> Unit> = KoneTwoThreeTreeList()
    private val automaton =
        LockingAutomaton<Element, Element>(
            initialState = initialElement,
            checkTransition = { previousState, transition ->
                if (elementEquality { previousState eq transition }) None
                else Some(transition)
            },
            onTransition = { _, _, nextState ->
                observersLock.withLock { observers.toKoneList() }.forEach { it(nextState) }
            }
        )
    
    override var element: Element
        get() = automaton.state
        set(value) {
            automaton.apply(value)
        }
    
    override fun compareAndSet(expected: Element, new: Element): Boolean =
        automaton.applyMaybe { current ->
            if (elementEquality { current eq expected }) Some(new)
            else None
        }
    
    override fun subscribe(observer: (Element) -> Unit): KoneState.Cancellation =
        observersLock.withLock {
            val node = observers.addNode(observer)
            KoneState.Cancellation { node.remove() }
        }
}

public fun <Element> KoneMutableState<Element>.update(function: (Element) -> Element) {
    while (true) {
        val previousElement = element
        val nextElement = function(previousElement)
        
        if (compareAndSet(previousElement, nextElement)) return
    }
}

public fun <Element> KoneMutableState<Element>.updateAndGet(function: (Element) -> Element): Element {
    while (true) {
        val previousElement = element
        val nextElement = function(previousElement)
        
        if (compareAndSet(previousElement, nextElement)) return nextElement
    }
}

public fun <Element> KoneMutableState<Element>.getAndUpdate(function: (Element) -> Element): Element {
    while (true) {
        val previousElement = element
        val nextElement = function(previousElement)
        
        if (compareAndSet(previousElement, nextElement)) return previousElement
    }
}

public fun <Element, Result> KoneState<Element>.map(elementEquality: Equality<Result> = defaultEquality(), transform: (Element) -> Result): KoneState<Result> {
    val temporaryLock = ReentrantLock()
    temporaryLock.withLock {
        val temporarySubscription = subscribe { temporaryLock.withLock {  } }
        val result = KoneMutableState(transform(element), elementEquality)
        subscribe { result.element = transform(it) }
        temporarySubscription.cancel()
        return result
    }
}

public fun <Element> KoneState<Element>.toStateFlow(): StateFlow<Element> {
    val temporaryLock = ReentrantLock()
    temporaryLock.withLock {
        val temporarySubscription = subscribe { temporaryLock.withLock {  } }
        val result = MutableStateFlow(element)
        subscribe { result.value = it }
        temporarySubscription.cancel()
        return result
    }
}