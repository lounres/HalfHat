package dev.lounres.thetruehat.client.desktop.components

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.client.desktop.components.feedback.FeedbackPageComponent
import dev.lounres.thetruehat.client.desktop.components.game.GameFlowComponent
import dev.lounres.thetruehat.client.desktop.components.home.HomePageComponent
import dev.lounres.thetruehat.client.desktop.components.nrfa.NewsRulesFaqAboutPageComponent


interface RootComponent {
    val childStack: Value<ChildStack<*, Child>>

    sealed interface Child {
        data class HomePage(val component: HomePageComponent): Child
        data class NewRulesFaqAboutPage(val component: NewsRulesFaqAboutPageComponent): Child
        data class Feedback(val component: FeedbackPageComponent): Child
        data class GameFLow(val component: GameFlowComponent): Child
    }
}