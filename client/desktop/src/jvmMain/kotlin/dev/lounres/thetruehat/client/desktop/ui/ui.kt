package dev.lounres.thetruehat.client.desktop.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


//interface RoomSettingsPage: TTHPage {
//    @Composable
//    override fun pageContent() {
//        Column(
//            modifier = Modifier
//                .fillMaxHeight()
//                .width(350.dp)
//                .padding(vertical = 10.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween,
//                modifier = Modifier.fillMaxWidth(),
//            ) {
//                Text(
//                    text = "Играть",
//                    fontSize = 20.sp,
//                )
//                Row {
//                    var expanded by remember { mutableStateOf(false) }
//                    DropdownMenu(
//                        expanded = expanded,
//                        onDismissRequest = { expanded = false }
//                    ) {
//                        DropdownMenuItem(
//                            onClick = { }
//                        ) {
//                            Text(
//                                text = "пока не кончатся слова",
//                            )
//                        }
//                        DropdownMenuItem(
//                            onClick = { }
//                        ) {
//                            Text(
//                                text = "заданное число кругов",
//                            )
//                        }
//                    }
//                    OutlinedButton(
//                        shape = CircleShape,
//                        onClick = { expanded = !expanded },
//                    ) {
//                        Text("пока не кончатся слова")
//                    }
//                }
//            }
//        }
//    }
//}



//sealed interface RoundUserRole {
//    data object Speaker: RoundUserRole
//    data object Listener: RoundUserRole
//    data class SpeakerIn(val rounds: UInt): RoundUserRole
//    data class ListenerIn(val rounds: UInt): RoundUserRole
//}
//
//@Composable
//fun RoundPage(
//    backButtonEnabled: Boolean,
//    wordsNumber: Int,
//    showFinishButton: Boolean,
//    volumeOn: Boolean,
//    speakerNickname: String,
//    listenerNickname: String,
//    userRole: RoundUserRole,
//    onBackButtonClick: () -> Unit,
//    onLanguageButtonClick: (language: Language) -> Unit,
//    onFeedbackButtonClick: () -> Unit,
//    onHatButtonClick: () -> Unit,
//    onVolumeButtonClick: () -> Unit,
//    onFinishButtonClick: () -> Unit,
//    onExitButtonClick: () -> Unit,
//) {
//    GamePage(
//        backButtonEnabled = backButtonEnabled,
//        wordsNumber = wordsNumber,
//        showFinishButton = showFinishButton,
//        volumeOn = volumeOn,
//        speakerNickname = speakerNickname,
//        listenerNickname = listenerNickname,
//        onBackButtonClick = onBackButtonClick,
//        onLanguageButtonClick = onLanguageButtonClick,
//        onFeedbackButtonClick = onFeedbackButtonClick,
//        onHatButtonClick = onHatButtonClick,
//        onVolumeButtonClick = onVolumeButtonClick,
//        onFinishButtonClick = onFinishButtonClick,
//        onExitButtonClick = onExitButtonClick,
//        header = {
//            when (userRole) {
//                RoundUserRole.Speaker -> {
//                    Text(
//                        text = "Ты объясняешь",
//                    )
//                }
//            }
//        },
//        footer = {
//            when(userRole) {
//                RoundBreakUserRole.SpeakerWaiting, RoundBreakUserRole.ListenerWaiting, RoundBreakUserRole.SpeakerReady, RoundBreakUserRole.ListenerReady ->
//                    Button(
//                        enabled = userRole == RoundBreakUserRole.SpeakerWaiting || userRole == RoundBreakUserRole.ListenerWaiting,
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = CircleShape,
//                        onClick = {},
//                    ) {
//                        Text(
//                            when (userRole) {
//                                RoundBreakUserRole.SpeakerWaiting -> "Я готов объяснять"
//                                RoundBreakUserRole.ListenerWaiting -> "Я готов отгадывать"
//                                RoundBreakUserRole.SpeakerReady, RoundBreakUserRole.ListenerReady -> "Подожди напарника"
//                                else -> error("There are forgotten cases in button definition on GamePage")
//                            },
//                            fontSize = 20.sp,
//                        )
//                    }
//                else -> {}
//            }
//        }
//    )
//}