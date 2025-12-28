package dev.lounres.halfhat.client.components.navigation.controller

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.kone.collections.interop.asKotlin
import dev.lounres.kone.collections.interop.toKoneList
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.map.*
import dev.lounres.kone.registry.Registry
import dev.lounres.kone.registry.RegistryBuilder
import dev.lounres.kone.registry.RegistryKey
import dev.lounres.kone.registry.correspondsTo
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
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


@Serializable
public data class NavigationNodeState(
    public val configuration: String?,
    @Serializable(with = NavigationNodeStateMapSerializer::class)
    public val children: KoneReifiedMap<String, NavigationNodeState>,
)

// TODO: Replace with Kone specialized serializer
private object NavigationNodeStateMapSerializer: KSerializer<KoneReifiedMap<String, NavigationNodeState>> {
    val mapSerializer = MapSerializer(String.serializer(), NavigationNodeState.serializer())
    
    override val descriptor: SerialDescriptor = mapSerializer.descriptor
    override fun serialize(encoder: Encoder, value: KoneReifiedMap<String, NavigationNodeState>) {
        encoder.encodeSerializableValue(mapSerializer, value.nodesView.asKotlin().associate { it.key to it.value })
    }
    override fun deserialize(decoder: Decoder): KoneReifiedMap<String, NavigationNodeState> {
        return decoder.decodeSerializableValue(mapSerializer).entries.toKoneList().associateReified { it.key mapsTo it.value }
    }
}

public data class NavigationNodePath(
    public val path: KoneList<String>,
    public val arguments: KoneMap<String, String>,
)

public class NavigationNodeController {
    public var configuration: String? = null
    private var restoration: (suspend (configuration: String) -> Unit)? = null
    public fun setRestoration(restoration: suspend (configuration: String) -> Unit) {
        check(this.restoration == null)
        this.restoration = restoration
    }
    public var pathBuilder: (suspend () -> NavigationNodePath)? = null
        private set
    public fun setPathBuilder(pathBuilder: suspend () -> NavigationNodePath) {
        check(this.pathBuilder == null)
        this.pathBuilder = pathBuilder
    }
    public var restorationByPath: (suspend (path: NavigationNodePath) -> Unit)? = null
        private set
    public fun setRestorationByPath(restoration: suspend (path: NavigationNodePath) -> Unit) {
        check(this.restorationByPath == null)
        this.restorationByPath = restoration
    }
    
    internal var children = KoneMap.of<String, NavigationNodeController>()
    
    internal val state: NavigationNodeState
        get() = NavigationNodeState(configuration, children.mapValuesReified { it.value.state })
    
    internal suspend fun restore(state: NavigationNodeState) {
        val children = this.children
        
        val newConfiguration = state.configuration
        if (newConfiguration != null) this.restoration?.invoke(newConfiguration)
        
        coroutineScope {
            for ((configuration, navigationItem) in state.children) launch {
                children.getOrNull(configuration)?.restore(navigationItem)
            }
        }
    }
    
    internal object Key : RegistryKey<NavigationNodeController>
}

public val UIComponentContext.navigationController: NavigationNodeController? get() = this.getOrNull(NavigationNodeController.Key)

public abstract class NavigationContext internal constructor() {
    @PublishedApi
    internal val mutex: Mutex = Mutex()
    
    @PublishedApi
    internal abstract suspend fun store()
    
    public object Key : RegistryKey<NavigationContext>
}

public val Registry.navigationContext: NavigationContext? get() = this.getOrNull(NavigationContext.Key)

public suspend inline fun NavigationContext?.doStoringNavigation(block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
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

public class NavigationRoot(public val onStore: (suspend (NavigationNodeState, NavigationNodePath?) -> Unit)? = null) {
    internal val controller = NavigationNodeController()
    internal val context = object : NavigationContext() {
        override suspend fun store() {
            onStore?.invoke(controller.state, controller.pathBuilder?.invoke())
        }
    }
    public suspend fun getState(): NavigationNodeState = context.mutex.withLock { controller.state }
    public suspend fun getPath(): NavigationNodePath? = context.mutex.withLock { controller.pathBuilder?.invoke() }
    public suspend fun restore(state: NavigationNodeState) {
        context.mutex.withLock {
            controller.restore(state)
        }
    }
    public suspend fun restoreByPath(path: NavigationNodePath) {
        context.mutex.withLock {
            controller.restorationByPath?.invoke(path)
        }
    }
}

public data object NavigationControllerStringFormatKey : RegistryKey<StringFormat>

public fun RegistryBuilder<UIComponentContext>.setUpNavigationControl(
    navigationRoot: NavigationRoot,
    stringFormat: StringFormat,
) {
    NavigationNodeController.Key correspondsTo navigationRoot.controller
    NavigationContext.Key correspondsTo navigationRoot.context
    NavigationControllerStringFormatKey correspondsTo stringFormat
}