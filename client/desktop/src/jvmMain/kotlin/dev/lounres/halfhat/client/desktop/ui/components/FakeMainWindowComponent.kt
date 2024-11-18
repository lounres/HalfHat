package dev.lounres.halfhat.client.desktop.ui.components

import dev.lounres.halfhat.api.localization.Language
import dev.lounres.halfhat.client.desktop.ui.components.game.FakeGamePageComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.GamePageComponent
import dev.lounres.halfhat.client.desktop.ui.components.home.FakeHomePageComponent
import dev.lounres.halfhat.client.desktop.ui.components.home.HomePageComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.Page
import kotlinx.coroutines.flow.MutableStateFlow


class FakeMainWindowComponent(
    override val onWindowCloseRequest: () -> Unit = {},

    initialVolumeOn: Boolean = true,
    initialLanguage: Language = Language.English,

    initialSelectedPage: Page = Page.Primary.Game /* TODO: Page.Primary.Home */,

    override val homePageComponent: HomePageComponent = FakeHomePageComponent(),
    override val gamePageComponent: GamePageComponent = FakeGamePageComponent()
): MainWindowComponent {
    override val volumeOn: MutableStateFlow<Boolean> = MutableStateFlow(initialVolumeOn)
    override val language: MutableStateFlow<Language> = MutableStateFlow(initialLanguage)

    override val selectedPage: MutableStateFlow<Page> = MutableStateFlow(initialSelectedPage)
}