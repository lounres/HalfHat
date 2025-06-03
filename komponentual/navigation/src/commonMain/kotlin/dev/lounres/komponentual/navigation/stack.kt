package dev.lounres.komponentual.navigation

import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.build
import dev.lounres.kone.collections.list.empty
import dev.lounres.kone.collections.list.lastIndex
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.set.toKoneSet
import dev.lounres.kone.collections.utils.dropLast
import dev.lounres.kone.collections.utils.last
import dev.lounres.kone.collections.utils.map
import dev.lounres.kone.relations.Equality
import dev.lounres.kone.relations.Hashing
import dev.lounres.kone.relations.Order
import dev.lounres.kone.relations.defaultEquality
import dev.lounres.kone.state.KoneState
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update


public typealias StackNavigationEvent<Configuration> = (stack: KoneList<Configuration>) -> KoneList<Configuration>

public typealias StackNavigation<Configuration> = NavigationSource<StackNavigationEvent<Configuration>>

public interface MutableStackNavigation<Configuration> : StackNavigation<Configuration> {
    public fun navigate(stackTransformation: StackNavigationEvent<Configuration>)
}

public fun <Configuration> MutableStackNavigation(): MutableStackNavigation<Configuration> = MutableStackNavigationImpl()

public fun <Configuration> MutableStackNavigation<Configuration>.push(configuration: Configuration) {
    navigate { stack ->
        KoneList.build(stack.size + 1u) {
            +stack
            +configuration
        }
    }
}

public fun <Configuration> MutableStackNavigation<Configuration>.pop() {
    navigate { stack -> stack.dropLast(1u) }
}

public fun <Configuration> MutableStackNavigation<Configuration>.replaceCurrent(configuration: Configuration) {
    navigate { stack ->
        KoneList.build(stack.size + 1u) {
            +stack
            removeAt(stack.lastIndex)
            +configuration
        }
    }
}

public fun <C> MutableStackNavigation<C>.updateCurrent(update: (C) -> C) {
    navigate { stack ->
        KoneList.build(stack.size + 1u) {
            +stack
            removeAt(stack.lastIndex)
            +update(stack.last())
        }
    }
}

internal class MutableStackNavigationImpl<Configuration>() : MutableStackNavigation<Configuration> {
    private val callbacksAtomicRef: AtomicRef<KoneList<(StackNavigationEvent<Configuration>) -> Unit>> = atomic(KoneList.empty())
    
    override fun subscribe(observer: (StackNavigationEvent<Configuration>) -> Unit) {
        callbacksAtomicRef.update {
            KoneList.build(it.size + 1u) {
                +it
                +observer
            }
        }
    }
    
    override fun navigate(stackTransformation: StackNavigationEvent<Configuration>) {
        for (observer in callbacksAtomicRef.value) observer(stackTransformation)
    }
}

public class InnerStackNavigationState<Configuration> internal constructor(
    public val stack: KoneList<Configuration>,
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
) : NavigationState<Configuration> {
    override val configurations: KoneSet<Configuration> =
        stack.toKoneSet(
            elementEquality = configurationEquality,
            elementHashing = configurationHashing,
            elementOrder = configurationOrder,
        )
}

public data class ChildrenStack<out Configuration, out Component>(
    public val active: ChildWithConfiguration<Configuration, Component>,
    public val backStack: KoneList<ChildWithConfiguration<Configuration, Component>> = KoneList.empty(),
)

public fun <Configuration, Component> ChildrenStack(configuration: Configuration, component: Component): ChildrenStack<Configuration, Component> =
    ChildrenStack(
        active = ChildWithConfiguration(configuration, component),
    )

public fun <
    Configuration,
    Child,
    Component,
> childrenStack(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: StackNavigation<Configuration>,
    initialStack: () -> KoneList<Configuration>,
    createChild: (configuration: Configuration, nextState: InnerStackNavigationState<Configuration>) -> Child,
    destroyChild: (Child) -> Unit,
    updateChild: (configuration: Configuration, data: Child, nextState: InnerStackNavigationState<Configuration>) -> Unit,
    componentAccessor: (Child) -> Component,
): KoneState<ChildrenStack<Configuration, Component>> =
    children(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        initialState = {
            InnerStackNavigationState(
                stack = initialStack().also { require(it.size != 0u) { "Cannot initialize a children stack without configurations" } },
                configurationEquality = configurationEquality,
                configurationHashing = configurationHashing,
                configurationOrder = configurationOrder,
            )
        },
        navigationTransition = { previousState, event ->
            InnerStackNavigationState(
                stack = event(previousState.stack).also { require(it.size != 0u) { "Cannot initialize a children stack without configurations" } },
                configurationEquality = configurationEquality,
                configurationHashing = configurationHashing,
                configurationOrder = configurationOrder,
            )
        },
        createChild = createChild,
        destroyChild = destroyChild,
        updateChild = updateChild,
        publicNavigationStateMapper = { innerState, componentByConfiguration ->
            val stack = innerState.stack
                .also {
                    check(it.size != 0u) { "Navigation stack is empty for some reason" }
                }
                .map {
                    ChildWithConfiguration(
                        configuration = it,
                        component = componentAccessor(componentByConfiguration[it]),
                    )
                }
            ChildrenStack(
                active = stack.last(),
                backStack = stack.dropLast(1u),
            )
        },
    )