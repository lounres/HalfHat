package dev.lounres.thetruehat.client.common.ui

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import dev.lounres.thetruehat.client.common.components.RootComponent
import dev.lounres.thetruehat.client.common.ui.feedback.FeedbackPageUI
import dev.lounres.thetruehat.client.common.ui.onlineGame.OnlineGameFlowUI
import dev.lounres.thetruehat.client.common.ui.home.HomePageUI
import dev.lounres.thetruehat.client.common.ui.nrfa.NewsRulesFaqAboutPageUI


@Composable
public fun RootUI(
    component: RootComponent
) {
    Children(component.childStack) { childDescription ->
        when(val child = childDescription.instance) {
            is RootComponent.Child.HomePage -> HomePageUI(component = child.component)
            is RootComponent.Child.NewRulesFaqAboutPage -> NewsRulesFaqAboutPageUI(component = child.component)
            is RootComponent.Child.Feedback -> FeedbackPageUI(component = child.component)
            is RootComponent.Child.GameFLow -> OnlineGameFlowUI(component = child.component)
        }
    }
}