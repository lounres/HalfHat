package dev.lounres.halfhat.client.ui.implementation.game.onlineGame.gameScreen.roundScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lounres.halfhat.api.onlineGame.DictionaryId
import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.RoundScreenComponent
import dev.lounres.halfhat.client.ui.icons.HalfHatIcon
import dev.lounres.halfhat.client.ui.icons.OnlineGameSettingsIconBetweenTimes
import dev.lounres.halfhat.client.ui.utils.commonIconModifier
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine


@Composable
fun RoundScreenAdditionalCardSettingsUI(
    component: RoundScreenComponent,
    additionalCardChild: RoundScreenComponent.AdditionalCardChild.Settings,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val gameState = component.gameState.collectAsState().value
        val settingsBuilder = gameState.settings
        val extraSettings = gameState.extraSettings

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                enabled = false,
                value = settingsBuilder.preparationTimeSeconds.toString(),
                onValueChange = {},
                label = {
                    Text(
                        text = "Preparation",
                    )
                },
                singleLine = true,
                textStyle = TextStyle(textAlign = TextAlign.Center),
                colors = OutlinedTextFieldDefaults.colors(
                    errorCursorColor = MaterialTheme.colorScheme.tertiary,
                    errorBorderColor = MaterialTheme.colorScheme.tertiary,
                    errorTrailingIconColor = MaterialTheme.colorScheme.tertiary,
                    errorLabelColor = MaterialTheme.colorScheme.tertiary,
                    errorSupportingTextColor = MaterialTheme.colorScheme.tertiary,
                ),
            )

            Column {
                Icon(
                    modifier = commonIconModifier,
                    imageVector = HalfHatIcon.OnlineGameSettingsIconBetweenTimes,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                modifier = Modifier.weight(1f),
                enabled = false,
                value = settingsBuilder.explanationTimeSeconds.toString(),
                onValueChange = {},
                label = {
                    Text(
                        text = "Explanation",
                    )
                },
                singleLine = true,
                textStyle = TextStyle(textAlign = TextAlign.Center),
                colors = OutlinedTextFieldDefaults.colors(
                    errorCursorColor = MaterialTheme.colorScheme.tertiary,
                    errorBorderColor = MaterialTheme.colorScheme.tertiary,
                    errorTrailingIconColor = MaterialTheme.colorScheme.tertiary,
                    errorLabelColor = MaterialTheme.colorScheme.tertiary,
                    errorSupportingTextColor = MaterialTheme.colorScheme.tertiary,
                ),
            )

            Column {
                Icon(
                    modifier = commonIconModifier,
                    imageVector = HalfHatIcon.OnlineGameSettingsIconBetweenTimes,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                modifier = Modifier.weight(1f),
                enabled = false,
                value = settingsBuilder.finalGuessTimeSeconds.toString(),
                onValueChange = {},
                label = {
                    Text(
                        text = "Final guess",
                    )
                },
                singleLine = true,
                textStyle = TextStyle(textAlign = TextAlign.Center),
                colors = OutlinedTextFieldDefaults.colors(
                    errorCursorColor = MaterialTheme.colorScheme.tertiary,
                    errorBorderColor = MaterialTheme.colorScheme.tertiary,
                    errorTrailingIconColor = MaterialTheme.colorScheme.tertiary,
                    errorLabelColor = MaterialTheme.colorScheme.tertiary,
                    errorSupportingTextColor = MaterialTheme.colorScheme.tertiary,
                ),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            val actualGameEndConditionType = settingsBuilder.gameEndCondition
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                enabled = false,
                value = when (actualGameEndConditionType) {
                    is GameStateMachine.GameEndCondition.Words -> "Words"
                    is GameStateMachine.GameEndCondition.Cycles -> "Cycles"
                },
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = {
                    Text(
                        text = "Game end condition",
                    )
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    errorCursorColor = MaterialTheme.colorScheme.tertiary,
                    errorBorderColor = MaterialTheme.colorScheme.tertiary,
                    errorTrailingIconColor = MaterialTheme.colorScheme.tertiary,
                    errorLabelColor = MaterialTheme.colorScheme.tertiary,
//                                        errorSupportingTextColor = MaterialTheme.colorScheme.tertiary,
                ),
            )

            Spacer(modifier = Modifier.width(8.dp))

            when (val gameEndCondition = settingsBuilder.gameEndCondition) {
                is GameStateMachine.GameEndCondition.Words -> {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        enabled = false,
                        value = gameEndCondition.number.toString(),
                        onValueChange = {},
                        label = {
                            Text(
                                text = "The number of words",
                            )
                        },
                        textStyle = TextStyle(textAlign = TextAlign.Center),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            errorCursorColor = MaterialTheme.colorScheme.tertiary,
                            errorBorderColor = MaterialTheme.colorScheme.tertiary,
                            errorTrailingIconColor = MaterialTheme.colorScheme.tertiary,
                            errorLabelColor = MaterialTheme.colorScheme.tertiary,
                            errorSupportingTextColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                }

                is GameStateMachine.GameEndCondition.Cycles -> {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        enabled = false,
                        value = gameEndCondition.number.toString(),
                        onValueChange = {},
                        label = {
                            Text(
                                text = "The number of cycles",
                            )
                        },
                        textStyle = TextStyle(textAlign = TextAlign.Center),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            errorCursorColor = MaterialTheme.colorScheme.tertiary,
                            errorBorderColor = MaterialTheme.colorScheme.tertiary,
                            errorTrailingIconColor = MaterialTheme.colorScheme.tertiary,
                            errorLabelColor = MaterialTheme.colorScheme.tertiary,
                            errorSupportingTextColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            value = when (val wordsSource = settingsBuilder.wordsSource) {
                ServerApi.WordsSource.Players -> "Players"
                ServerApi.WordsSource.HostDictionary -> "Host dictionary"
                is ServerApi.WordsSource.ServerDictionary -> when (val description = wordsSource.dictionaryIdWithDescription) {
                    is DictionaryId.WithDescription.Builtin -> description.name
                }
            },
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = {
                Text(
                    text = "Words source",
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                enabled = false,
                checked = settingsBuilder.strictMode,
                onCheckedChange = {},
                colors = CheckboxDefaults.colors()
            )

            Text(
                text = "Strict mode",
                fontSize = 20.sp,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                enabled = false,
                checked = extraSettings.showLeaderboardPermutation,
                onCheckedChange = {},
                colors = CheckboxDefaults.colors()
            )

            Text(
                text = "Show leaderboard",
                fontSize = 20.sp,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                enabled = false,
                checked = extraSettings.showWordsStatistic,
                onCheckedChange = {},
                colors = CheckboxDefaults.colors()
            )

            Text(
                text = "Show words statistics",
                fontSize = 20.sp,
            )
        }
    }
}