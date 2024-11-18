package dev.lounres.halfhat.client.desktop.ui.components

import com.arkivanov.decompose.ComponentContext
import dev.lounres.halfhat.api.localization.Language
import dev.lounres.halfhat.client.desktop.ui.components.game.GamePageComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.RealGamePageComponent
import dev.lounres.halfhat.client.desktop.ui.components.home.HomePageComponent
import dev.lounres.halfhat.client.desktop.ui.components.home.RealHomePageComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.Page
import kotlinx.coroutines.flow.MutableStateFlow


class RealMainWindowComponent(
    componentContext: ComponentContext,
    
    override val onWindowCloseRequest: () -> Unit = {},
    
    initialVolumeOn: Boolean = true,
    initialLanguage: Language = Language.English,
    
    initialSelectedPage: Page = Page.Primary.Game /* TODO: Page.Primary.Home */,
): MainWindowComponent {
    override val volumeOn: MutableStateFlow<Boolean> = MutableStateFlow(initialVolumeOn)
    override val language: MutableStateFlow<Language> = MutableStateFlow(initialLanguage)

    override val selectedPage: MutableStateFlow<Page> = MutableStateFlow(initialSelectedPage)

    override val homePageComponent: HomePageComponent = RealHomePageComponent()
    override val gamePageComponent: GamePageComponent = RealGamePageComponent(componentContext, volumeOn)
}