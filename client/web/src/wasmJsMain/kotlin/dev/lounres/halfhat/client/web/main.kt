package dev.lounres.halfhat.client.web

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import dev.lounres.halfhat.client.common.resources.Res
import dev.lounres.halfhat.client.common.resources.allDrawableResources
import dev.lounres.halfhat.client.web.ui.components.MainWindowComponent
import dev.lounres.halfhat.client.web.ui.components.RealMainWindowComponent
import dev.lounres.halfhat.client.web.ui.implementation.MainWindowContentUI
import dev.lounres.komponentual.lifecycle.UIComponentLifecycleState
import dev.lounres.komponentual.lifecycle.UIComponentLifecycleTransition
import js.buffer.ArrayBuffer
import js.buffer.ArrayBufferLike
import js.core.JsPrimitives.toJsByte
import js.typedarrays.Int8Array
import korlibs.memory.toNBufferUInt8
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.configureWebResources
import org.jetbrains.compose.resources.preloadImageVector
import org.khronos.webgl.toInt8Array
import web.audio.AudioContext


@OptIn(ExperimentalComposeUiApi::class, ExperimentalResourceApi::class)
fun main() {
    configureWebResources {
        // Overrides the resource location
        resourcePathMapping { path -> "./$path" }
    }
    
    ComposeViewport(document.body!!) {
        val preloadedDrawables = Res.allDrawableResources.mapValues { (_, resource) -> preloadImageVector(resource) }
        var component by remember { mutableStateOf<MainWindowComponent?>(null) }

        val allPreloaded by remember { derivedStateOf { preloadedDrawables.all { it.value.value != null } && component != null } }

        LaunchedEffect(allPreloaded) {
            val theComponent = RealMainWindowComponent()
            theComponent.globalLifecycle.moveTo(UIComponentLifecycleState.Running)
            component = theComponent
        }

        if (allPreloaded) {
            MainWindowContentUI(
                component = component!!,
                windowSizeClass = calculateWindowSizeClass()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Loading...",
                        fontSize = 36.sp,
                    )
                    LoadingIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
    
//    ComposeViewport(document.body!!) {
//        var sound: ByteArray? by remember { mutableStateOf(null) }
//
//        LaunchedEffect(sound == null) {
//            sound = Res.readBytes("files/sounds/finalGuessEnd.wav")
//        }
//
//        val theSound = sound
//        if (theSound == null) {
//            LoadingIndicator(
//                modifier = Modifier.size(48.dp)
//            )
//        } else {
//            SoundButtonScreen(theSound)
//        }
//    }
}

//@Composable
//fun SoundButtonScreen(
//    sound: ByteArray,
//) {
//    Box(
//        modifier = Modifier.fillMaxSize()
//    ) {
//        Button(
//            onClick = {
//                CoroutineScope(Dispatchers.Default).launch {
//                    val context = AudioContext()
//
////                    val audioBuffer = context.decodeAudioData(
////                        ArrayBuffer(sound.size).apply {
////                            val view = Int8Array(this)
////                            sound.forEachIndexed { index, b -> view[index] = b.toJsByte() }
////                        }
////                    )
//
////                    val source = context.createBufferSource()
////                    source.buffer = audioBuffer
////                    source.connect(context.destination)
////                    source.start()
//                }
//            }
//        ) {
//            Text("Click me!!!")
//        }
//    }
//}