package dev.lounres.halfhat.client.components.navigation.controller

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.kone.castOrNull
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.KoneMutableList
import dev.lounres.kone.collections.list.of
import dev.lounres.kone.collections.map.KoneReifiedMap
import dev.lounres.kone.collections.map.build
import dev.lounres.kone.collections.map.component1
import dev.lounres.kone.collections.map.component2
import dev.lounres.kone.collections.map.getOrNull
import dev.lounres.kone.collections.map.iterator
import dev.lounres.kone.collections.map.mapValuesReified
import dev.lounres.kone.collections.map.mapsTo
import dev.lounres.kone.collections.map.of
import dev.lounres.kone.registry.Registry
import dev.lounres.kone.registry.RegistryBuilder
import dev.lounres.kone.registry.RegistryKey
import dev.lounres.kone.registry.getOrNull
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.StringFormat


@Serializable
public sealed interface NavigationNode {
    public val configuration: String?
    
    @Serializable
    public data class Undirectional(
        public override val configuration: String?,
    ) : NavigationNode
    @Serializable
    public data class Unidirectional(
        public override val configuration: String?,
        public val item: NavigationItem,
    ) : NavigationNode
    @Serializable
    public data class Multidirectional(
        public override val configuration: String?,
        public val items: KoneReifiedMap<String, NavigationItem>,
    ) : NavigationNode
}

@Serializable
public data class NavigationItem(
    public val configuration: String?,
    public val nodes: KoneReifiedMap<String, NavigationNode>,
)

public class NavigationNodeController {
    public var configuration: String? = null
    public var restoration: (suspend (configuration: String) -> Unit)? = null
    
    internal sealed interface Items {
        data object None : Items
        data class Sole(val item: NavigationItemController) : Items
        data class Multiple(val items: KoneReifiedMap<String, NavigationItemController>) : Items
    }
    
    private var items: Items = Items.None
    
    internal fun attachItem(key: String, item: NavigationItemController) {
        items = when (val items = items) {
            Items.None -> Items.Multiple(KoneReifiedMap.of(key mapsTo item))
            is Items.Sole -> error("Can not register usual item when there is sole item")
            is Items.Multiple -> Items.Multiple(
                KoneReifiedMap.build {
                    +items.items
                    +(key mapsTo item)
                }
            )
        }
    }
    internal fun attachSoleItem(item: NavigationItemController) {
        items = when (items) {
            Items.None -> Items.Sole(item)
            is Items.Sole -> error("Can not register sole item when there is other sole item")
            is Items.Multiple -> error("Can not register sole item when there are usual items")
        }
    }
    
    internal val state: NavigationNode
        get() = when(val items = items) {
            Items.None -> NavigationNode.Undirectional(configuration)
            is Items.Sole -> NavigationNode.Unidirectional(configuration, items.item.state)
            is Items.Multiple -> NavigationNode.Multidirectional(configuration, items.items.mapValuesReified { it.value.state })
        }
    
    internal companion object : RegistryKey<NavigationNodeController>
    
    internal suspend fun restore(state: NavigationNode) {
        val items = items
        
        val newConfiguration = state.configuration
        if (newConfiguration != null) this.restoration?.invoke(newConfiguration)
        
        coroutineScope {
            when (state) {
                is NavigationNode.Undirectional -> {}
                is NavigationNode.Unidirectional -> items.castOrNull<Items.Sole>()?.item?.let { launch { it.restore(state.item) } }
                is NavigationNode.Multidirectional -> items.castOrNull<Items.Multiple>()?.items?.let {
                    for ((configuration, navigationItem) in state.items) launch {
                        it.getOrNull(configuration)?.restore(navigationItem)
                    }
                }
            }
        }
    }
}

public val UIComponentContext.navigationController: NavigationNodeController? get() = this.getOrNull(NavigationNodeController)

internal class NavigationItemController {
    var configuration: String? = null
    var restoration: (suspend (configuration: String) -> Unit)? = null
    
    var nodes: KoneReifiedMap<String, NavigationNodeController> = KoneReifiedMap.of()
    
    val state: NavigationItem get() = NavigationItem(configuration, nodes.mapValuesReified { it.value.state })
    
    internal suspend fun restore(state: NavigationItem) {
        val controller = this
        
        val newConfiguration = state.configuration
        if (newConfiguration != null) this.restoration?.invoke(newConfiguration)
        
        coroutineScope {
            for ((configuration, navigationNode) in state.nodes) launch {
                controller.nodes.getOrNull(configuration)?.restore(navigationNode)
            }
        }
    }
}

public abstract class NavigationContext internal constructor(
    @PublishedApi
    internal val mutex: Mutex
) {
    @PublishedApi
    internal abstract suspend fun store()
    
    public companion object : RegistryKey<NavigationContext>
}

public val Registry.navigationContext: NavigationContext get() = this[NavigationContext]

public suspend inline fun NavigationContext.doStoringNavigation(block: () -> Unit) {
    mutex.withLock {
        try {
            block()
        } finally {
            store()
        }
    }
}

public class NavigationRoot(
    onStore: (newHistory: KoneList<NavigationNode>) -> Unit,
) {
    private val mutex: Mutex = Mutex()
    internal val history = KoneMutableList.of<NavigationNode>()
    internal var currentIndex = 0u
    internal val controller = NavigationNodeController()
    internal val context = object : NavigationContext(mutex) {
        override suspend fun store() {
            history.add(controller.state)
            onStore(history)
        }
    }
    public suspend fun init() {
        context.store()
    }
    public suspend fun navigate(node: NavigationNode) {
        mutex.withLock {
            controller.restore(node)
        }
    }
}

public data object NavigationControllerStringFormatKey : RegistryKey<StringFormat>

public fun RegistryBuilder.setUpNavigationControl(
    navigationRoot: NavigationRoot,
    stringFormat: StringFormat,
) {
    NavigationNodeController correspondsTo navigationRoot.controller
    NavigationContext correspondsTo navigationRoot.context
    NavigationControllerStringFormatKey correspondsTo stringFormat
}