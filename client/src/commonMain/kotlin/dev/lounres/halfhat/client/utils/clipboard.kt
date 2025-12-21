package dev.lounres.halfhat.client.utils


public expect suspend fun copyToClipboard(content: String)

public expect suspend fun copyFromClipboard(): String