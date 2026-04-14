package dev.lounres.halfhat.client.ui.components

import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.navigation.ChildrenVariants
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.KoneMutableAsynchronousHub


actual interface MainWindowComponent {
    actual val globalLifecycle: MutableUIComponentLifecycle
    
    actual val darkTheme: KoneMutableAsynchronousHub<DarkTheme>
    
    actual val pageVariants: KoneAsynchronousHub<ChildrenVariants<MainWindowComponentConfiguration, MainWindowComponentChild, UIComponentContext>>
    actual val openPage: (page: MainWindowComponentConfiguration) -> Unit
}