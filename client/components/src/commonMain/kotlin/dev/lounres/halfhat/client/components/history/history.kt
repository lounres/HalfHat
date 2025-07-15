@file:OptIn(DelicateCollectionsInheritanceAPI::class)

package dev.lounres.halfhat.client.components.history

import dev.lounres.kone.collections.DelicateCollectionsInheritanceAPI
import dev.lounres.kone.collections.map.KoneReifiedMap


public sealed interface NavigationNode {
    public val configuration: String?
    
    public data class Unidirectional(
        public override val configuration: String?,
        public val item: NavigationItem,
    ) : NavigationNode
    public data class Multidirectional(
        public override val configuration: String?,
        public val items: KoneReifiedMap<String, NavigationItem>,
    ) : NavigationNode
}

public data class NavigationItem(
    public val configuration: String,
    public val nodeArguments: KoneReifiedMap<String, NavigationNode>,
)

public interface NavigationController {
    public fun registerConfiguration()
}