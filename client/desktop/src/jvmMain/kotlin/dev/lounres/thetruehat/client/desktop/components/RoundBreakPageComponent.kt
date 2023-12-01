package dev.lounres.thetruehat.client.desktop.components


sealed interface RoundBreakUserRole {
    data object SpeakerWaiting: RoundBreakUserRole
    data object ListenerWaiting: RoundBreakUserRole
    data object SpeakerReady: RoundBreakUserRole
    data object ListenerReady: RoundBreakUserRole
    data class SpeakerIn(val rounds: UInt): RoundBreakUserRole
    data class ListenerIn(val rounds: UInt): RoundBreakUserRole
}

interface RoundBreakPageComponent {
    val gamePageComponent: GamePageComponent

    val userRole: RoundBreakUserRole
}