package dev.lounres.halfhat.client.ui.components.game.controller.roomSettings

import kotlinx.coroutines.flow.MutableStateFlow


public interface RoomSettingsComponent {
    public val onApplySettings: () -> Unit
    public val onDiscardSettings: () -> Unit
    
    public val preparationTimeSeconds: MutableStateFlow<String>
    public val showErrorForPreparationTimeSeconds: MutableStateFlow<Boolean>
    public val explanationTimeSeconds: MutableStateFlow<String>
    public val showErrorForExplanationTimeSeconds: MutableStateFlow<Boolean>
    public val finalGuessTimeSeconds: MutableStateFlow<String>
    public val showErrorForFinalGuessTimeSeconds: MutableStateFlow<Boolean>
//    public val strictMode: MutableStateFlow<Boolean>
}