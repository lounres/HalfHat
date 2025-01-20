package dev.lounres.halfhat.client.common.utils

import com.arkivanov.essenty.statekeeper.SerializableContainer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
public fun SerializableContainer.writeToFile(file: File) {
    file.outputStream().use { output ->
        Json.encodeToStream(SerializableContainer.serializer(), this, output)
    }
}

@OptIn(ExperimentalSerializationApi::class)
public fun File.readSerializableContainer(): SerializableContainer? =
    takeIf { it.exists() }?.inputStream()?.use { input ->
        try {
            Json.decodeFromStream(SerializableContainer.serializer(), input)
        } catch (e: Exception) {
            null
        }
    }