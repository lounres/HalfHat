package dev.lounres.komponentual.navigation

import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.build
import dev.lounres.kone.collections.list.empty
import dev.lounres.kone.collections.map.KoneMap
import dev.lounres.kone.collections.map.associateWith
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.set.KoneSet
import dev.lounres.kone.relations.Equality
import dev.lounres.kone.relations.Hashing
import dev.lounres.kone.relations.Order
import dev.lounres.kone.relations.defaultEquality
import dev.lounres.kone.state.KoneState
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update


public typealias VariantsNavigationEvent<Configuration> = (allVariants: KoneSet<Configuration>, Configuration) -> Configuration

public typealias VariantsNavigation<Configuration> = NavigationSource<VariantsNavigationEvent<Configuration>>

public interface MutableVariantsNavigation<Configuration> : VariantsNavigation<Configuration> {
    public fun navigate(variantsTransformation: VariantsNavigationEvent<Configuration>)
}

public fun <Configuration> MutableVariantsNavigation(): MutableVariantsNavigation<Configuration> = MutableVariantsNavigationImpl()

public fun <Configuration> MutableVariantsNavigation<Configuration>.set(configuration: Configuration) {
    navigate { _, _ -> configuration }
}

internal class MutableVariantsNavigationImpl<Configuration>() : MutableVariantsNavigation<Configuration> {
    private val callbacksAtomicRef: AtomicRef<KoneList<(VariantsNavigationEvent<Configuration>) -> Unit>> = atomic(KoneList.empty())
    
    override fun subscribe(observer: (VariantsNavigationEvent<Configuration>) -> Unit) {
        callbacksAtomicRef.update {
            KoneList.build(it.size + 1u) {
                +it
                +observer
            }
        }
    }
    
    override fun navigate(variantsTransformation: VariantsNavigationEvent<Configuration>) {
        for (observer in callbacksAtomicRef.value) observer(variantsTransformation)
    }
}

public class InnerVariantsNavigationState<Configuration> internal constructor(
    override val configurations: KoneSet<Configuration>,
    public val currentVariant: Configuration,
) : NavigationState<Configuration>

public data class ChildrenVariants<Configuration, out Component>(
    public val active: ChildWithConfiguration<Configuration, Component>,
    public val allVariants: KoneMap<Configuration, Component>,
)

public fun <
    Configuration,
    Child,
    Component,
> childrenVariants(
    configurationEquality: Equality<Configuration> = defaultEquality(),
    configurationHashing: Hashing<Configuration>? = null,
    configurationOrder: Order<Configuration>? = null,
    source: VariantsNavigation<Configuration>,
    allVariants: () -> KoneSet<Configuration>,
    initialVariant: () -> Configuration,
    createChild: (configuration: Configuration, nextState: InnerVariantsNavigationState<Configuration>) -> Child,
    destroyChild: (Child) -> Unit,
    updateChild: (configuration: Configuration, data: Child, nextState: InnerVariantsNavigationState<Configuration>) -> Unit,
    componentAccessor: (Child) -> Component,
): KoneState<ChildrenVariants<Configuration, Component>> =
    children(
        configurationEquality = configurationEquality,
        configurationHashing = configurationHashing,
        configurationOrder = configurationOrder,
        source = source,
        initialState = {
            InnerVariantsNavigationState(
                configurations = allVariants(),
                currentVariant = initialVariant(),
            )
        },
        navigationTransition = { previousState, event ->
            InnerVariantsNavigationState(
                configurations = previousState.configurations,
                currentVariant = event(previousState.configurations, previousState.currentVariant)
            )
        },
        createChild = createChild,
        destroyChild = destroyChild,
        updateChild = updateChild,
        publicNavigationStateMapper = { innerState, componentByConfiguration ->
            ChildrenVariants(
                active = ChildWithConfiguration(
                    configuration = innerState.currentVariant,
                    component = componentAccessor(componentByConfiguration[innerState.currentVariant]),
                ),
                allVariants = innerState.configurations.associateWith { componentAccessor(componentByConfiguration[it]) },
            )
        },
    )