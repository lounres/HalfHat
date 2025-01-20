package dev.lounres.halfhat.client.web

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.browser.document


@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val lifecycle = LifecycleRegistry()
    
    ComposeViewport(document.body!!) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {}
            ) {
                Text("Click me!")
            }
        }
    }
}