package dev.lounres.thetruehat.client.desktop.components.fake

import dev.lounres.thetruehat.client.desktop.components.RoundBreakPageComponent
import dev.lounres.thetruehat.client.desktop.components.RoundBreakUserRole


class FakeRoundBreakPageComponent(
    backButtonEnabled: Boolean = true,
    wordsNumber: Int = 100,
    showFinishButton: Boolean = true,
    volumeOn: Boolean = true,
    speakerNickname: String = "Panther",
    listenerNickname: String = "Jaguar",
    override val userRole: RoundBreakUserRole = RoundBreakUserRole.SpeakerReady,
): RoundBreakPageComponent {
    override val gamePageComponent = FakeGamePageComponent(
        backButtonEnabled = backButtonEnabled,
        wordsNumber = wordsNumber,
        showFinishButton = showFinishButton,
        volumeOn = volumeOn,
        speakerNickname = speakerNickname,
        listenerNickname = listenerNickname,
    )
}