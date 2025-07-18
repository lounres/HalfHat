package dev.lounres.halfhat.client.components.navigation.controller

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.kone.castOrNull
import dev.lounres.kone.collections.interop.asKotlin
import dev.lounres.kone.collections.interop.toKoneList
import dev.lounres.kone.collections.iterables.KoneIterator
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.map.*
import dev.lounres.kone.registry.Registry
import dev.lounres.kone.registry.RegistryBuilder
import dev.lounres.kone.registry.RegistryKey
import dev.lounres.kone.registry.getOrNull
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.StringFormat
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


private fun <Element> KoneIterator<Element>.forEach(block: (Element) -> Unit) {
    while (hasNext()) {
        block(getNext())
        moveNext()
    }
}

@Serializable
public sealed interface NavigationNodeState {
    public val configuration: String?
    
    @Serializable
    public data class Undirectional(
        public override val configuration: String?,
    ) : NavigationNodeState
    @Serializable
    public data class Unidirectional(
        public override val configuration: String?,
        public val item: NavigationItemState,
    ) : NavigationNodeState
    @Serializable
    public data class Multidirectional(
        public override val configuration: String?,
        @Serializable(with = NavigationItemStateMapSerializer::class)
        public val items: KoneReifiedMap<String, NavigationItemState>,
    ) : NavigationNodeState
}

private object NavigationNodeStateMapSerializer: KSerializer<KoneReifiedMap<String, NavigationNodeState>> {
    val stringSerializer = String.serializer()
    val navigationItemStateSerializer = NavigationNodeState.serializer()
    val mapSerializer = MapSerializer(stringSerializer, navigationItemStateSerializer)
    
    override val descriptor: SerialDescriptor = mapSerializer.descriptor
    override fun serialize(encoder: Encoder, value: KoneReifiedMap<String, NavigationNodeState>) {
        encoder.encodeSerializableValue(mapSerializer, value.nodesView.asKotlin().associate { it.key to it.value })
    }
    override fun deserialize(decoder: Decoder): KoneReifiedMap<String, NavigationNodeState> {
        return decoder.decodeSerializableValue(mapSerializer).entries.toKoneList().associateReified { it.key mapsTo it.value }
    }
}

@Serializable
public data class NavigationItemState(
    public val configuration: String?,
    @Serializable(with = NavigationNodeStateMapSerializer::class)
    public val nodes: KoneReifiedMap<String, NavigationNodeState>,
)

private object NavigationItemStateMapSerializer: KSerializer<KoneReifiedMap<String, NavigationItemState>> {
    val stringSerializer = String.serializer()
    val navigationItemStateSerializer = NavigationItemState.serializer()
    val mapSerializer = MapSerializer(stringSerializer, navigationItemStateSerializer)
    
    override val descriptor: SerialDescriptor = mapSerializer.descriptor
    override fun serialize(encoder: Encoder, value: KoneReifiedMap<String, NavigationItemState>) {
        encoder.encodeSerializableValue(mapSerializer, value.nodesView.asKotlin().associate { it.key to it.value })
    }
    override fun deserialize(decoder: Decoder): KoneReifiedMap<String, NavigationItemState> {
        return decoder.decodeSerializableValue(mapSerializer).entries.toKoneList().associateReified { it.key mapsTo it.value }
    }
}

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
    
    internal val state: NavigationNodeState
        get() = when(val items = items) {
            Items.None -> NavigationNodeState.Undirectional(configuration)
            is Items.Sole -> NavigationNodeState.Unidirectional(configuration, items.item.state)
            is Items.Multiple -> NavigationNodeState.Multidirectional(configuration, items.items.mapValuesReified { it.value.state })
        }
    
    internal companion object : RegistryKey<NavigationNodeController>
    
    internal suspend fun restore(state: NavigationNodeState) {
        val items = items
        
        val newConfiguration = state.configuration
        if (newConfiguration != null) this.restoration?.invoke(newConfiguration)
        
        coroutineScope {
            when (state) {
                is NavigationNodeState.Undirectional -> {}
                is NavigationNodeState.Unidirectional -> items.castOrNull<Items.Sole>()?.item?.let { launch { it.restore(state.item) } }
                is NavigationNodeState.Multidirectional -> items.castOrNull<Items.Multiple>()?.items?.let {
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
    
    val state: NavigationItemState get() = NavigationItemState(configuration, nodes.mapValuesReified { it.value.state })
    
    internal suspend fun restore(state: NavigationItemState) {
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

public val Registry.navigationContext: NavigationContext? get() = this.getOrNull(NavigationContext)

public suspend inline fun NavigationContext?.doStoringNavigation(block: () -> Unit) {
    if (this != null)
        mutex.withLock {
            try {
                block()
            } finally {
                store()
            }
        }
    else block()
}

public class NavigationRoot {
    public var onStore: (suspend (NavigationNodeState) -> Unit)? = null
    private val mutex: Mutex = Mutex()
    internal val controller = NavigationNodeController()
    internal val context = object : NavigationContext(mutex) {
        override suspend fun store() {
            onStore?.invoke(controller.state)
        }
    }
    public val state: NavigationNodeState get() = controller.state
    public suspend fun restore(state: NavigationNodeState) {
        mutex.withLock {
            controller.restore(state)
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