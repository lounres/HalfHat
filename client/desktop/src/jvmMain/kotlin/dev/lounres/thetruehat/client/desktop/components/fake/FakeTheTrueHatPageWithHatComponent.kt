package dev.lounres.thetruehat.client.desktop.components.fake

import dev.lounres.thetruehat.client.desktop.components.TheTrueHatPageWithHatComponent


class FakeTheTrueHatPageWithHatComponent(
    backButtonEnabled: Boolean = true
): TheTrueHatPageWithHatComponent {
    override val theTrueHatPageComponent = FakeTheTrueHatPageComponent(
        backButtonEnabled = backButtonEnabled
    )

    override fun onHatButtonClick() {}
}