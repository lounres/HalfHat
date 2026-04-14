package dev.lounres.halfhat.api.onlineGame

import kotlinx.serialization.Serializable


@Serializable
public sealed interface DictionaryId {
    @Serializable
    public data class Builtin(
        val id: UInt,
    ) : DictionaryId

    @Serializable
    public sealed interface WithDescription {
        public val id: DictionaryId

        @Serializable
        public data class Builtin(
            override val id: DictionaryId.Builtin,
            public val name: String,
            public val wordsNumber: UInt,
        ) : WithDescription
    }
}