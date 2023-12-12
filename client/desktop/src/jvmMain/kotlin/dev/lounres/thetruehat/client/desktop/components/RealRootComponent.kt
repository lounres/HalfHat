package dev.lounres.thetruehat.client.desktop.components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.api.localization.Language
import dev.lounres.thetruehat.client.desktop.components.feedback.RealFeedbackPageComponent
import dev.lounres.thetruehat.client.desktop.components.game.RealGameFlowComponent
import dev.lounres.thetruehat.client.desktop.components.home.RealHomePageComponent
import dev.lounres.thetruehat.client.desktop.components.nrfa.RealNewsRulesFaqAboutPageComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer


class RealRootComponent(
    private val componentContext: ComponentContext,
): RootComponent {
    private val navigation = StackNavigation<ChildConfiguration>()

    private var stackHeight: UInt = 1u
    override val childStack: Value<ChildStack<*, RootComponent.Child>> =
        componentContext.childStack(
            source = navigation,
            initialConfiguration = ChildConfiguration.HomePage(0u),
            serializer = serializer<ChildConfiguration>(),
            childFactory = ::createChild,
        )

    private fun createChild(configuration: ChildConfiguration, componentContext: ComponentContext): RootComponent.Child {
        val backButtonEnabled = stackHeight != 0u
        val onBackButtonClick = { navigation.pop() }
        val onLanguageChange = { _: Language -> TODO() }
        val onFeedbackButtonClick = { navigation.push(ChildConfiguration.FeedbackPage(stackHeight++)) }
        val onHatButtonClick = { navigation.push(ChildConfiguration.NewRulesFaqAboutPage(stackHeight++)) }
        return when (configuration) {
            is ChildConfiguration.HomePage ->
                RootComponent.Child.HomePage(
                    RealHomePageComponent(
                        backButtonEnabled = backButtonEnabled,
                        onBackButtonClick = onBackButtonClick,
                        onLanguageChange = onLanguageChange,
                        onFeedbackButtonClick = onFeedbackButtonClick,
                        onHatButtonClick = onHatButtonClick,
                        onCreateButtonClick = { navigation.push(ChildConfiguration.GameFLow(stackHeight++, true)) },
                        onEnterButtonClick = { navigation.push(ChildConfiguration.GameFLow(stackHeight++)) },
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

            is ChildConfiguration.GameFLow ->
                RootComponent.Child.GameFLow(
                    RealGameFlowComponent(
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
        }
    }

    @Serializable
    sealed interface ChildConfiguration {
        val stackHeight: UInt

        @Serializable
        data class HomePage(
            override val stackHeight: UInt,
        ): ChildConfiguration
        @Serializable
        data class NewRulesFaqAboutPage(
            override val stackHeight: UInt,
        ): ChildConfiguration
        @Serializable
        data class FeedbackPage(
            override val stackHeight: UInt,
        ): ChildConfiguration
        @Serializable
        data class GameFLow(
            override val stackHeight: UInt,
            val generateNewRoomId: Boolean = false,
        ): ChildConfiguration
    }
}