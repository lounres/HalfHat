package dev.lounres.halfhat.client.ui.components.game.controller.roomSettings

import kotlinx.coroutines.flow.MutableStateFlow


public class RealRoomSettingsComponent(
    initialPreparationTimeSeconds: UInt,
    initialExplanationTimeSeconds: UInt,
    initialFinalGuessTimeSeconds: UInt,
    onUpdateSettingsBuilder: (preparationTimeSeconds: UInt, explanationTimeSeconds: UInt, finalGuessTimeSeconds: UInt) -> Unit,
    onExitSettings: () -> Unit,
) : RoomSettingsComponent {
    override val onApplySettings: () -> Unit = onApplySettings@{
        showErrorForPreparationTimeSeconds.value = true
        showErrorForExplanationTimeSeconds.value = true
        showErrorForFinalGuessTimeSeconds.value = true
        onUpdateSettingsBuilder(
            preparationTimeSeconds.value.let { if (it.isBlank()) return@onApplySettings else it.toUInt() },
            explanationTimeSeconds.value.let { if (it.isBlank()) return@onApplySettings else it.toUInt() },
            finalGuessTimeSeconds.value.let { if (it.isBlank()) return@onApplySettings else it.toUInt() },
        )
        onExitSettings()
    }
    override val onDiscardSettings: () -> Unit = onExitSettings
    
    override val preparationTimeSeconds: MutableStateFlow<String> = MutableStateFlow(initialPreparationTimeSeconds.toString())
    override val showErrorForPreparationTimeSeconds: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val explanationTimeSeconds: MutableStateFlow<String> = MutableStateFlow(initialExplanationTimeSeconds.toString())
    override val showErrorForExplanationTimeSeconds: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val finalGuessTimeSeconds: MutableStateFlow<String> = MutableStateFlow(initialFinalGuessTimeSeconds.toString())
    override val showErrorForFinalGuessTimeSeconds: MutableStateFlow<Boolean> = MutableStateFlow(false)
//    override val strictMode: MutableStateFlow<Boolean> = MutableStateFlow(initialSettingsBuilder.strictMode)
}