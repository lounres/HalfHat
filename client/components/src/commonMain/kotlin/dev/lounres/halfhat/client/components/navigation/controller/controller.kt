package dev.lounres.halfhat.client.components.navigation.controller

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.kone.collections.interop.asKotlin
import dev.lounres.kone.collections.interop.toKoneList
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.map.*
import dev.lounres.kone.registry.RegistryBuilder
import dev.lounres.kone.registry.RegistryKey
import dev.lounres.kone.registry.correspondsTo
import dev.lounres.kone.registry.getOrNull
import dev.lounres.logKube.core.CurrentPlatformLogger
import dev.lounres.logKube.core.LogLevel
import dev.lounres.logKube.core.debug
import dev.lounres.logKube.core.warn
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

public data class NavigationLoggerSpec(
    val logger: CurrentPlatformLogger<LogLevel>,
    val loggerSource: String?,
)

public class NavigationNodeController(
    private val loggerSpec: NavigationLoggerSpec? = null,
) {
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
        loggerSpec?.logger?.debug(
            source = "dev.lounres.halfhat.client.components.navigation.controller.NavigationNodeController at ${loggerSpec.loggerSource}",
            items = {
                mapOf(
                    "node controller" to this.toString(),
                    "configuration" to this.configuration.toString(),
                    "children" to this.children.toString(),
                    "new state" to state.toString(),
                )
            }
        ) { "Restoring state of node" }
        
        loggerSpec?.logger?.debug(
            source = "dev.lounres.halfhat.client.components.navigation.controller.NavigationNodeController at ${loggerSpec.loggerSource}",
            items = {
                mapOf(
                    "node controller" to this.toString(),
                    "configuration" to this.configuration.toString(),
                    "children" to this.children.toString(),
                    "new configuration" to state.configuration.toString(),
                )
            }
        ) { "Restoring configuration of node" }
        
        val newConfiguration = state.configuration
        if (newConfiguration != null) {
            val restoration = this.restoration
            if (restoration != null) {
                restoration(newConfiguration)
                if (this.configuration != newConfiguration) loggerSpec?.logger?.warn(
                    source = "dev.lounres.halfhat.client.components.navigation.controller.NavigationNodeController at ${loggerSpec.loggerSource}",
                    items = {
                        mapOf(
                            "node controller" to this.toString(),
                            "configuration" to this.configuration.toString(),
                            "children" to this.children.toString(),
                            "expected configuration" to newConfiguration,
                        )
                    }
                ) { "After restoration, configuration did not become the new configuration" }
            } else {
                loggerSpec?.logger?.warn(
                    source = "dev.lounres.halfhat.client.components.navigation.controller.NavigationNodeController at ${loggerSpec.loggerSource}",
                    items = {
                        mapOf(
                            "node controller" to this.toString(),
                            "configuration" to this.configuration.toString(),
                            "children" to this.children.toString(),
                            "new configuration" to newConfiguration,
                        )
                    }
                ) { "During restoration, got new configuration for node without restoration lambda." }
            }
        }
        
        loggerSpec?.logger?.debug(
            source = "dev.lounres.halfhat.client.components.navigation.controller.NavigationNodeController at ${loggerSpec.loggerSource}",
            items = {
                mapOf(
                    "node controller" to this.toString(),
                    "configuration" to this.configuration.toString(),
                    "children" to this.children.toString(),
                    "new configuration" to state.configuration.toString(),
                )
            }
        ) { "Restored configuration of node" }
        
        loggerSpec?.logger?.debug(
            source = "dev.lounres.halfhat.client.components.navigation.controller.NavigationNodeController at ${loggerSpec.loggerSource}",
            items = {
                mapOf(
                    "node controller" to this.toString(),
                    "configuration" to this.configuration.toString(),
                    "children" to this.children.toString(),
                    "new children configurations" to state.children.toString(),
                )
            }
        ) { "Restoring states of node's children" }
        
        val children = this.children
        coroutineScope {
            for ((configuration, navigationNodeState) in state.children) {
                val child = children.getOrNull(configuration)
                if (child != null) launch {
                    child.restore(navigationNodeState)
                }
            }
        }
        
        loggerSpec?.logger?.debug(
            source = "dev.lounres.halfhat.client.components.navigation.controller.NavigationNodeController at ${loggerSpec.loggerSource}",
            items = {
                mapOf(
                    "node controller" to this.toString(),
                    "configuration" to this.configuration.toString(),
                    "children" to this.children.toString(),
                    "new children configurations" to state.children.toString(),
                )
            }
        ) { "Restored states of node's children" }
        
        loggerSpec?.logger?.debug(
            source = "dev.lounres.halfhat.client.components.navigation.controller.NavigationNodeController at ${loggerSpec.loggerSource}",
            items = {
                mapOf(
                    "node controller" to this.toString(),
                    "configuration" to this.configuration.toString(),
                    "children" to this.children.toString(),
                    "new state" to state.toString(),
                )
            }
        ) { "Restored state of node" }
    }
    
    internal object Key : RegistryKey<NavigationNodeController>
}

public val UIComponentContext.navigationController: NavigationNodeController? get() = this.getOrNull(NavigationNodeController.Key)

public sealed interface NavigationAction {
    public data object PushState : NavigationAction
    public data object ReplaceState : NavigationAction
}

public abstract class NavigationContext internal constructor() {
    @PublishedApi
    internal val mutex: Mutex = Mutex()
    
    @PublishedApi
    internal abstract suspend fun store(action: NavigationAction = NavigationAction.PushState)
    
    public object Key : RegistryKey<NavigationContext>
}

public val UIComponentContext.navigationContext: NavigationContext? get() = this.getOrNull(NavigationContext.Key)

public suspend inline fun NavigationContext?.doStoringNavigation(action: NavigationAction = NavigationAction.PushState, block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    if (this != null)
        mutex.withLock {
            try {
                block()
            } finally {
                store(action)
            }
        }
    else block()
}

public class NavigationRoot(
    private val loggerSpec: NavigationLoggerSpec? = null,
    onStore: (suspend (action: NavigationAction, state: NavigationNodeState, path: NavigationNodePath?) -> Unit)? = null
) {
    internal val controller = NavigationNodeController(loggerSpec = loggerSpec)
    internal val context = object : NavigationContext() {
        override suspend fun store(action: NavigationAction) {
            val state = controller.state
            val path = controller.pathBuilder?.invoke()
            loggerSpec?.logger?.debug(
                source = "dev.lounres.halfhat.client.components.navigation.controller.NavigationRoot",
                items = {
                    mapOf(
                        "action" to action.toString(),
                        "state" to state.toString(),
                        "path" to path.toString(),
                    )
                },
            ) { "Storing navigation state and path" }
            if (!mutex.isLocked) loggerSpec?.logger?.warn(
                source = "dev.lounres.halfhat.client.components.navigation.controller.NavigationRoot"
            ) { "For some reason navigation is stored when navigation mutex is not locked!" }
            onStore?.invoke(action, state, path)
            loggerSpec?.logger?.debug(
                source = "dev.lounres.halfhat.client.components.navigation.controller.NavigationRoot",
                items = {
                    mapOf(
                        "action" to action.toString(),
                        "state" to state.toString(),
                        "path" to path.toString(),
                    )
                },
            ) { "Stored navigation state and path" }
        }
    }
    public suspend fun getState(): NavigationNodeState = context.mutex.withLock { controller.state }
    public suspend fun getPath(): NavigationNodePath? = context.mutex.withLock { controller.pathBuilder?.invoke() }
    public suspend fun restore(state: NavigationNodeState) {
        context.mutex.withLock {
            loggerSpec?.logger?.debug(
                source = "dev.lounres.halfhat.client.components.navigation.controller.NavigationRoot",
                items = {
                    mapOf(
                        "state" to state.toString(),
                    )
                },
            ) { "Restoring navigation by state" }
            controller.restore(state)
            loggerSpec?.logger?.debug(
                source = "dev.lounres.halfhat.client.components.navigation.controller.NavigationRoot",
                items = {
                    mapOf(
                        "state" to state.toString(),
                    )
                },
            ) { "Restored navigation by state" }
        }
    }
    public suspend fun restoreByPath(path: NavigationNodePath) {
        context.mutex.withLock {
            loggerSpec?.logger?.debug(
                source = "dev.lounres.halfhat.client.components.navigation.controller.NavigationRoot",
                items = {
                    mapOf(
                        "path" to path.toString(),
                    )
                },
            ) { "Restoring navigation by path" }
            controller.restorationByPath?.invoke(path)
            loggerSpec?.logger?.debug(
                source = "dev.lounres.halfhat.client.components.navigation.controller.NavigationRoot",
                items = {
                    mapOf(
                        "path" to path.toString(),
                    )
                },
            ) { "Restored navigation by path" }
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