package dev.lounres.komponentual.navigation

import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.build
import dev.lounres.kone.collections.list.empty
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.set.of
import dev.lounres.kone.relations.Equality
import dev.lounres.kone.relations.Hashing
import dev.lounres.kone.relations.Order
import dev.lounres.kone.relations.defaultEquality
import dev.lounres.kone.state.KoneState
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update


public typealias SlotNavigationEvent<Configuration> = (Configuration) -> Configuration

public typealias SlotNavigation<Configuration> = NavigationSource<SlotNavigationEvent<Configuration>>

public interface MutableSlotNavigation<Configuration> : SlotNavigation<Configuration> {
    public fun navigate(slotTransformation: SlotNavigationEvent<Configuration>)
}

public fun <Configuration> MutableSlotNavigation(): MutableSlotNavigation<Configuration> = MutableSlotNavigationImpl()

public fun <Configuration> MutableSlotNavigation<Configuration>.set(configuration: Configuration) {
    navigate { configuration }
}

internal class MutableSlotNavigationImpl<Configuration>() : MutableSlotNavigation<Configuration> {
    private val callbacksAtomicRef: AtomicRef<KoneList<(SlotNavigationEvent<Configuration>) -> Unit>> = atomic(KoneList.empty())
    
    override fun subscribe(observer: (SlotNavigationEvent<Configuration>) -> Unit) {
        callbacksAtomicRef.update {
            KoneList.build(it.size + 1u) {
                +it
                +observer
            }
        }
    }
    
    override fun navigate(slotTransformation: SlotNavigationEvent<Configuration>) {
        for (observer in callbacksAtomicRef.value) observer(slotTransformation)
    }
}

public class InnerSlotNavigationState<Configuration> internal constructor(
    public val current: Configuration,
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
) : NavigationState<Configuration> {
    override val configurations: KoneSet<Configuration> =
        KoneSet.of(
            current,
            elementEquality = configurationEquality,
            elementHashing = configurationHashing,
            elementOrder = configurationOrder,
        )
}

public typealias ChildrenSlot<Configuration, Component> = ChildWithConfiguration<Configuration, Component>

public fun <
    Configuration,
    Child,
    Component,
> childrenSlot(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: SlotNavigation<Configuration>,
    initialConfiguration: () -> Configuration,
    createChild: (configuration: Configuration, nextState: InnerSlotNavigationState<Configuration>) -> Child,
    destroyChild: (Child) -> Unit,
    updateChild: (configuration: Configuration, data: Child, nextState: InnerSlotNavigationState<Configuration>) -> Unit,
    componentAccessor: (Child) -> Component,
): KoneState<ChildrenSlot<Configuration, Component>> =
    children(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        initialState = {
            InnerSlotNavigationState(
                current = initialConfiguration(),
                configurationEquality = configurationEquality,
                configurationHashing = configurationHashing,
                configurationOrder = configurationOrder,
            )
        },
        navigationTransition = { previousState, event ->
            InnerSlotNavigationState(
                current = event(previousState.current),
                configurationEquality = configurationEquality,
                configurationHashing = configurationHashing,
                configurationOrder = configurationOrder,
            )
        },
        createChild = createChild,
        destroyChild = destroyChild,
        updateChild = updateChild,
        publicNavigationStateMapper = { innerState, componentByConfiguration ->
            ChildrenSlot(
                configuration = innerState.current,
                component = componentAccessor(componentByConfiguration[innerState.current]),
            )
        },
    )