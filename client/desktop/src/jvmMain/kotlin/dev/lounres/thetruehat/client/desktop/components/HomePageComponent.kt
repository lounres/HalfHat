package dev.lounres.thetruehat.client.desktop.components


interface HomePageComponent {
    val theTrueHatPageWithHatComponent: TheTrueHatPageWithHatComponent

    fun onCreateButtonClick()
    fun onEnterButtonClick()
}