package dev.lounres.thetruehat.client.common.models

import dev.lounres.thetruehat.api.models.RoomDescription


public data class MutableExplanationResult(
    val word: String,
    var state: RoomDescription.WordExplanationResult.State
)