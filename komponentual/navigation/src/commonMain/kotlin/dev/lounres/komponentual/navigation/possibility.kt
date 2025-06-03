package dev.lounres.komponentual.navigation

import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.build
import dev.lounres.kone.collections.list.empty
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.collections.set.of
import dev.lounres.kone.maybe.Maybe
import dev.lounres.kone.maybe.None
import dev.lounres.kone.maybe.Some
import dev.lounres.kone.maybe.computeOn
import dev.lounres.kone.relations.Equality
import dev.lounres.kone.relations.Hashing
import dev.lounres.kone.relations.Order
import dev.lounres.kone.relations.defaultEquality
import dev.lounres.kone.state.KoneState
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update


public typealias PossibilityNavigationEvent<Configuration> = (Maybe<Configuration>) -> Maybe<Configuration>

public typealias PossibilityNavigation<Configuration> = NavigationSource<PossibilityNavigationEvent<Configuration>>

public interface MutablePossibilityNavigation<Configuration> : PossibilityNavigation<Configuration> {
    public fun navigate(possibilityTransformation: PossibilityNavigationEvent<Configuration>)
}

public fun <Configuration> MutablePossibilityNavigation(): MutablePossibilityNavigation<Configuration> = MutablePossibilityNavigationImpl()

public fun <Configuration> MutablePossibilityNavigation<Configuration>.set(configuration: Configuration) {
    navigate { Some(configuration) }
}

public fun <Configuration> MutablePossibilityNavigation<Configuration>.clear() {
    navigate { None }
}

internal class MutablePossibilityNavigationImpl<Configuration>() : MutablePossibilityNavigation<Configuration> {
    private val callbacksAtomicRef: AtomicRef<KoneList<(PossibilityNavigationEvent<Configuration>) -> Unit>> = atomic(KoneList.empty())
    
    override fun subscribe(observer: (PossibilityNavigationEvent<Configuration>) -> Unit) {
        callbacksAtomicRef.update {
            KoneList.build(it.size + 1u) {
                +it
                +observer
            }
        }
    }
    
    override fun navigate(possibilityTransformation: PossibilityNavigationEvent<Configuration>) {
        for (observer in callbacksAtomicRef.value) observer(possibilityTransformation)
    }
}

public class InnerPossibilityNavigationState<Configuration> internal constructor(
    public val current: Maybe<Configuration>,
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
) : NavigationState<Configuration> {
    override val configurations: KoneSet<Configuration> =
        when (current) {
            None -> KoneSet.of(
                elementEquality = configurationEquality,
                elementHashing = configurationHashing,
                elementOrder = configurationOrder,
            )
            is Some<Configuration> -> KoneSet.of(
                current.value,
                elementEquality = configurationEquality,
                elementHashing = configurationHashing,
                elementOrder = configurationOrder,
            )
        }
}

public typealias ChildrenPossibility<Configuration, Component> = Maybe<ChildWithConfiguration<Configuration, Component>>

public fun <
    Configuration,
    Child,
    Component,
> childrenPossibility(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: PossibilityNavigation<Configuration>,
    initialConfiguration: () -> Maybe<Configuration>,
    createChild: (configuration: Configuration, nextState: InnerPossibilityNavigationState<Configuration>) -> Child,
    destroyChild: (Child) -> Unit,
    updateChild: (configuration: Configuration, data: Child, nextState: InnerPossibilityNavigationState<Configuration>) -> Unit,
    componentAccessor: (Child) -> Component,
): KoneState<ChildrenPossibility<Configuration, Component>> =
    children(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        initialState = {
            InnerPossibilityNavigationState(
                current = initialConfiguration()
            )
        },
        navigationTransition = { previousState, event ->
            InnerPossibilityNavigationState(
                current = event(previousState.current)
            )
        },
        createChild = createChild,
        destroyChild = destroyChild,
        updateChild = updateChild,
        publicNavigationStateMapper = { innerState, componentByConfiguration ->
            innerState.current.computeOn { ChildWithConfiguration(it, componentAccessor(componentByConfiguration[it])) }
        },
    )