package dev.lounres.halfhat.client.logic.settings

import dev.lounres.halfhat.Language
import dev.lounres.halfhat.client.storage.settings.Settings
import dev.lounres.halfhat.client.storage.settings.get
import dev.lounres.halfhat.client.utils.DefaultSounds
import dev.lounres.halfhat.client.utils.play
import dev.lounres.halfhat.client.utils.toAudio
import dev.lounres.kone.hub.KoneAsynchronousHubView
import dev.lounres.kone.hub.KoneMutableAsynchronousHubView
import dev.lounres.kone.hub.view
import dev.lounres.kone.registry.serialization.RegistrySerializableKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable


@Serializable
data class CustomSounds(
    public val preparationCountdown: ByteArray?,
    public val explanationStart: ByteArray?,
    public val finalGuessStart: ByteArray?,
    public val finalGuessEnd: ByteArray?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        
        other as CustomSounds
        
        if (!preparationCountdown.contentEquals(other.preparationCountdown)) return false
        if (!explanationStart.contentEquals(other.explanationStart)) return false
        if (!finalGuessStart.contentEquals(other.finalGuessStart)) return false
        if (!finalGuessEnd.contentEquals(other.finalGuessEnd)) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = preparationCountdown?.contentHashCode() ?: 0
        result = 31 * result + (explanationStart?.contentHashCode() ?: 0)
        result = 31 * result + (finalGuessStart?.contentHashCode() ?: 0)
        result = 31 * result + (finalGuessEnd?.contentHashCode() ?: 0)
        return result
    }
    
    data object Key : RegistrySerializableKey<CustomSounds> {
        override val serializer: KSerializer<CustomSounds> get() = serializer()
    }
}

val Settings.customSounds: CustomSounds get() = get(CustomSounds.Key)
val KoneMutableAsynchronousHubView<Settings, *>.customSounds: KoneMutableAsynchronousHubView<CustomSounds, *> get() = get(CustomSounds.Key)

data class Sounds(
    public val preparationCountdown: ByteArray,
    public val explanationStart: ByteArray,
    public val finalGuessStart: ByteArray,
    public val finalGuessEnd: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        
        other as Sounds
        
        if (!preparationCountdown.contentEquals(other.preparationCountdown)) return false
        if (!explanationStart.contentEquals(other.explanationStart)) return false
        if (!finalGuessStart.contentEquals(other.finalGuessStart)) return false
        if (!finalGuessEnd.contentEquals(other.finalGuessEnd)) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = preparationCountdown.contentHashCode()
        result = 31 * result + explanationStart.contentHashCode()
        result = 31 * result + finalGuessStart.contentHashCode()
        result = 31 * result + finalGuessEnd.contentHashCode()
        return result
    }
}

suspend fun Settings.playPreparationCountdown() {
    (customSounds.preparationCountdown ?: DefaultSounds.preparationCountdown.await()).toAudio().play()
}
suspend fun Settings.playExplanationStart() {
    (customSounds.explanationStart ?: DefaultSounds.explanationStart.await()).toAudio().play()
}
suspend fun Settings.playFinalGuessStart() {
    (customSounds.finalGuessStart ?: DefaultSounds.finalGuessStart.await()).toAudio().play()
}
suspend fun Settings.playFinalGuessEnd() {
    (customSounds.finalGuessEnd ?: DefaultSounds.finalGuessEnd.await()).toAudio().play()
}

suspend fun KoneAsynchronousHubView<Settings, *>.playPreparationCountdown() {
    value.playPreparationCountdown()
}
suspend fun KoneAsynchronousHubView<Settings, *>.playExplanationStart() {
    value.playExplanationStart()
}
suspend fun KoneAsynchronousHubView<Settings, *>.playFinalGuessStart() {
    value.playFinalGuessStart()
}
suspend fun KoneAsynchronousHubView<Settings, *>.playFinalGuessEnd() {
    value.playFinalGuessEnd()
}