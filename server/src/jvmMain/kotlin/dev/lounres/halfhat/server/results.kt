package dev.lounres.halfhat.server


sealed interface ConnectionToPlayerAttachmentResult {
    data object GameStartedWithoutThePlayer: ConnectionToPlayerAttachmentResult
    data object SomeOtherConnectionIsAlreadyAttached: ConnectionToPlayerAttachmentResult
    data class Success(val attachment: Room.Player.AttachmentHandle): ConnectionToPlayerAttachmentResult
}

sealed interface SettingsUpdateResult {
    data object GameAlreadyStarted : SettingsUpdateResult
    data object Success : SettingsUpdateResult
}

sealed interface GameInitialisationResult {
    data object Success : GameInitialisationResult
    data object InvalidState : GameInitialisationResult
}

sealed interface SpeakerReadinessResult {
    data object Success : SpeakerReadinessResult
    data object InvalidState : SpeakerReadinessResult
}

sealed interface ListenerReadinessResult {
    data object Success : ListenerReadinessResult
    data object InvalidState : ListenerReadinessResult
}

sealed interface WordExplanationStatementResult {
    data object Success : WordExplanationStatementResult
    data object InvalidState : WordExplanationStatementResult
}

sealed interface WordsExplanationResultsUpdateResult {
    data object Success : WordsExplanationResultsUpdateResult
    data object InvalidState : WordsExplanationResultsUpdateResult
}

sealed interface WordsExplanationResultsConfirmationResult {
    data object Success : WordsExplanationResultsConfirmationResult
    data object InvalidState : WordsExplanationResultsConfirmationResult
}

sealed interface GameFinishingResult {
    data object Success : GameFinishingResult
    data object InvalidState : GameFinishingResult
}