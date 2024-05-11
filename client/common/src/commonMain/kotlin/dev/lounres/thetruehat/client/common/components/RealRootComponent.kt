package dev.lounres.thetruehat.client.common.components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.client.common.components.feedback.RealFeedbackPageComponent
import dev.lounres.thetruehat.client.common.components.gameTimer.RealGameTimerComponent
import dev.lounres.thetruehat.client.common.components.onlineGame.RealOnlineGameFlowComponent
import dev.lounres.thetruehat.client.common.components.home.RealHomePageComponent
import dev.lounres.thetruehat.client.common.components.nrfa.RealNewsRulesFaqAboutPageComponent
import dev.lounres.thetruehat.client.common.utils.runOnUiThread
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer


public class RealRootComponent(
    private val componentContext: ComponentContext,
): RootComponent {
    private val navigation = StackNavigation<ChildConfiguration>()

    private var stackHeight: UInt = 0u
    override val childStack: Value<ChildStack<*, RootComponent.Child>> = runOnUiThread {
        componentContext.childStack(
            source = navigation,
            initialConfiguration = ChildConfiguration.HomePage(
                0u
            ),
            serializer = serializer<ChildConfiguration>(),
            childFactory = ::createChild,
        )
    }

    private fun createChild(configuration: ChildConfiguration, componentContext: ComponentContext): RootComponent.Child {
        val backButtonEnabled = stackHeight != 0u
        val onBackButtonClick = { navigation.pop() }
        val onLanguageChange = { _: Language -> TODO() }
        val onFeedbackButtonClick = { navigation.push(
            ChildConfiguration.FeedbackPage(
                stackHeight
            )
        ) }
        val onHatButtonClick = { navigation.push(
            ChildConfiguration.NewRulesFaqAboutPage(
                stackHeight
            )
        ) }
        stackHeight++
        return when (configuration) {
            is ChildConfiguration.HomePage ->
                RootComponent.Child.HomePage(
                    RealHomePageComponent(
                        backButtonEnabled = backButtonEnabled,
                        onBackButtonClick = onBackButtonClick,
                        onLanguageChange = onLanguageChange,
                        onFeedbackButtonClick = onFeedbackButtonClick,
                        onHatButtonClick = onHatButtonClick,
                        onCreateOnlineGameButtonClick = {
                            navigation.push(
                                ChildConfiguration.OnlineGameFLow(
                                    stackHeight,
                                    true
                                )
                            )
                        },
                        onEnterOnlineGameButtonClick = {
                            navigation.push(
                                ChildConfiguration.OnlineGameFLow(
                                    stackHeight
                                )
                            )
                        },
                        onCreateGameTimerButtonClick = {
                            navigation.push(
                                ChildConfiguration.GameTimer(
                                    stackHeight
                                )
                            )
                        }
                    )
                )

            is ChildConfiguration.NewRulesFaqAboutPage ->
                RootComponent.Child.NewRulesFaqAboutPage(
                    RealNewsRulesFaqAboutPageComponent(
                        backButtonEnabled = backButtonEnabled,
                        onBackButtonClick = onBackButtonClick,
                        onLanguageChange = onLanguageChange,
                        onFeedbackButtonClick = onFeedbackButtonClick,
                    )
                )

            is ChildConfiguration.FeedbackPage ->
                RootComponent.Child.Feedback(
                    RealFeedbackPageComponent(
                        backButtonEnabled = backButtonEnabled,
                        onBackButtonClick = onBackButtonClick,
                        onLanguageChange = onLanguageChange,
                        onFeedbackButtonClick = onFeedbackButtonClick,
                        onHatButtonClick = onHatButtonClick,
                        sendFeedback = { _, _ -> TODO() },
                    )
                )

            is ChildConfiguration.OnlineGameFLow ->
                RootComponent.Child.OnlineGameFLow(
                    RealOnlineGameFlowComponent(
                        componentContext = componentContext,
                        coroutineContext = Dispatchers.Default,
                        backButtonEnabled = backButtonEnabled,
                        onBackButtonClick = onBackButtonClick,
                        onLanguageChange = onLanguageChange,
                        onFeedbackButtonClick = onFeedbackButtonClick,
                        onHatButtonClick = onHatButtonClick,
                        generateNewRoomId = configuration.generateNewRoomId,
                    )
                )

            is ChildConfiguration.GameTimer ->
                RootComponent.Child.GameTimer(
                    RealGameTimerComponent(
                        componentContext = componentContext,
                        coroutineContext = Dispatchers.Default,
                        backButtonEnabled = backButtonEnabled,
                        onBackButtonClick = onBackButtonClick,
                        onLanguageChange = onLanguageChange,
                        onFeedbackButtonClick = onFeedbackButtonClick,
                        onHatButtonClick = onHatButtonClick,
                    )
                )
        }
    }

    @Serializable
    public sealed interface ChildConfiguration {
        public val stackHeight: UInt

        @Serializable
        public data class HomePage(
            override val stackHeight: UInt,
        ): ChildConfiguration
        @Serializable
        public data class NewRulesFaqAboutPage(
            override val stackHeight: UInt,
        ): ChildConfiguration
        @Serializable
        public data class FeedbackPage(
            override val stackHeight: UInt,
        ): ChildConfiguration
        @Serializable
        public data class OnlineGameFLow(
            override val stackHeight: UInt,
            val generateNewRoomId: Boolean = false,
        ): ChildConfiguration
        @Serializable
        public data class GameTimer(
            override val stackHeight: UInt,
        ): ChildConfiguration
    }
}