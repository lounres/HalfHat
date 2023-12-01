package dev.lounres.thetruehat.client.desktop.components.fake

import dev.lounres.thetruehat.client.desktop.components.HomePageComponent


class FakeHomePageComponent(
    backButtonEnabled: Boolean = true
): HomePageComponent {
    override val theTrueHatPageWithHatComponent = FakeTheTrueHatPageWithHatComponent(
        backButtonEnabled = backButtonEnabled,
    )

    override fun onCreateButtonClick() {}
    override fun onEnterButtonClick() {}
}