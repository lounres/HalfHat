package dev.lounres.halfhat.client.ui.components

import dev.lounres.halfhat.Language
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.lifecycle.MutableUIComponentLifecycle
import dev.lounres.halfhat.client.components.navigation.ChildrenVariants
import dev.lounres.halfhat.client.logic.settings.LanguageKey
import dev.lounres.halfhat.client.logic.settings.VolumeOnKey
import dev.lounres.halfhat.client.storage.settings.SettingsSerializer
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.KoneMutableAsynchronousHubView
import dev.lounres.kone.registry.serialization.RegistrySerializableKey


expect class RealMainWindowComponent: MainWindowComponent {
    override val globalLifecycle: MutableUIComponentLifecycle
    
    override val darkTheme: KoneMutableAsynchronousHubView<DarkTheme, *>
    override val volumeOn: KoneMutableAsynchronousHubView<Boolean, *>
    override val language: KoneMutableAsynchronousHubView<Language, *>
    
    override val pageVariants: KoneAsynchronousHub<ChildrenVariants<MainWindowComponentChild.Kind, MainWindowComponentChild, UIComponentContext>>
    override val openPage: (page: MainWindowComponentChild.Kind) -> Unit
    
    override val menuList: KoneAsynchronousHub<KoneList<MainWindowComponentMenuItem>>
}

sealed interface RealMainWindowComponentMenuItemByKind {
    data object Separator: RealMainWindowComponentMenuItemByKind
    data class Child(val child: MainWindowComponentChild.Kind): RealMainWindowComponentMenuItemByKind
}

data class SettingDescription<T>(
    val key: RegistrySerializableKey<T>,
    val value: T,
)

val settingsDefaults: Map<String, SettingDescription<*>> = mapOf(
    "DarkTheme" to SettingDescription(DarkTheme.Key, DarkTheme.System),
    "VolumeOn" to SettingDescription(VolumeOnKey, true),
    "Language" to SettingDescription(LanguageKey, Language.English),
)

val settingsSerializer = SettingsSerializer(settingsDefaults.mapValues { it.value.key })
