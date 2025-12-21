package dev.lounres.halfhat.client.utils

import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection


// TODO: Review the implementations

public actual suspend fun copyToClipboard(content: String) {
    val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
    clipboard.setContents(StringSelection(content), null)
}

// TODO: This function writes some errors to `System.err`. Fix it if possible.
public actual suspend fun copyFromClipboard(): String {
    val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
    return clipboard.getContents(null).getTransferData(DataFlavor.stringFlavor) as String
}