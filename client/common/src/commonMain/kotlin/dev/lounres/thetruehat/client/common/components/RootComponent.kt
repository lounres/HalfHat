package dev.lounres.thetruehat.client.common.components

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import dev.lounres.thetruehat.client.common.components.feedback.FeedbackPageComponent
import dev.lounres.thetruehat.client.common.components.game.GameFlowComponent
import dev.lounres.thetruehat.client.common.components.home.HomePageComponent
import dev.lounres.thetruehat.client.common.components.nrfa.NewsRulesFaqAboutPageComponent


public interface RootComponent {
    public val childStack: Value<ChildStack<*, Child>>

    public sealed interface Child {
        public data class HomePage(val component: HomePageComponent): Child
        public data class NewRulesFaqAboutPage(val component: NewsRulesFaqAboutPageComponent): Child
        public data class Feedback(val component: FeedbackPageComponent): Child
        public data class GameFLow(val component: GameFlowComponent): Child
    }
}