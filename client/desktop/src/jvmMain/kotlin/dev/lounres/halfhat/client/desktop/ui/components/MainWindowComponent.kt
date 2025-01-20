package dev.lounres.halfhat.client.desktop.ui.components

import dev.lounres.halfhat.api.localization.Language
import dev.lounres.halfhat.client.desktop.ui.components.faq.FAQPageComponent
import dev.lounres.halfhat.client.desktop.ui.components.game.GamePageComponent
import dev.lounres.halfhat.client.desktop.ui.components.home.HomePageComponent
import dev.lounres.halfhat.client.desktop.ui.implementation.Page
import kotlinx.coroutines.flow.MutableStateFlow


interface MainWindowComponent {
    val onWindowCloseRequest: () -> Unit

    val volumeOn: MutableStateFlow<Boolean>
    val language: MutableStateFlow<Language>

    val selectedPage: MutableStateFlow<Page>

    val homePageComponent: HomePageComponent
    val gamePageComponent: GamePageComponent
    val faqPageComponent: FAQPageComponent
}