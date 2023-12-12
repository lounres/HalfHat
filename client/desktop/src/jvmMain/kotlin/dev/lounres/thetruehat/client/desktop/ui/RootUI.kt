package dev.lounres.thetruehat.client.desktop.ui

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import dev.lounres.thetruehat.client.desktop.components.RootComponent
import dev.lounres.thetruehat.client.desktop.ui.feedback.FeedbackPageUI
import dev.lounres.thetruehat.client.desktop.ui.game.GameFlowUI
import dev.lounres.thetruehat.client.desktop.ui.home.HomePageUI
import dev.lounres.thetruehat.client.desktop.ui.nrfa.NewsRulesFaqAboutPageUI


@Composable
fun RootUI(
    component: RootComponent
) {
    Children(component.childStack) { childDescription ->
        when(val child = childDescription.instance) {
            is RootComponent.Child.HomePage -> HomePageUI(component = child.component)
            is RootComponent.Child.NewRulesFaqAboutPage -> NewsRulesFaqAboutPageUI(component = child.component)
            is RootComponent.Child.Feedback -> FeedbackPageUI(component = child.component)
            is RootComponent.Child.GameFLow -> GameFlowUI(component = child.component)
        }
    }
}