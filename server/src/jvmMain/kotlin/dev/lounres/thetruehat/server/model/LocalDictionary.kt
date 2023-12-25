package dev.lounres.thetruehat.server.model


data class LocalDictionary(
    val id: Int,
    val name: String,
    val words: List<String>,
)

data class LocalDictionaryDescription(
    val id: Int,
    val name: String,
    val fileName: String,
)