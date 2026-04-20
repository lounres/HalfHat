package dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen

import dev.lounres.halfhat.api.onlineGame.ServerApi
import dev.lounres.halfhat.client.components.UIComponentContext
import dev.lounres.halfhat.client.components.navigation.ChildrenPossibility
import dev.lounres.halfhat.client.components.navigation.ChildrenSlot
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundEditing.RoundEditingComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundExplanation.RoundExplanationComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundLastGuess.RoundLastGuessComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundPreparation.RoundPreparationComponent
import dev.lounres.halfhat.client.ui.components.game.onlineGame.gameScreen.roundScreen.roundWaiting.RoundWaitingComponent
import dev.lounres.halfhat.client.ui.theming.DarkTheme
import dev.lounres.halfhat.logic.gameStateMachine.GameStateMachine
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.of
import dev.lounres.kone.collections.utils.filter
import dev.lounres.kone.collections.utils.firstThatOrNull
import dev.lounres.kone.hub.KoneAsynchronousHub
import dev.lounres.kone.hub.KoneMutableAsynchronousHub
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow


interface RoundScreenComponent {
    public val onExitOnlineGame: () -> Unit
    public val onCopyOnlineGameKey: () -> Unit
    public val onCopyOnlineGameLink: () -> Unit
    public val onFinishGame: () -> Unit
    
    public val gameState: StateFlow<ServerApi.OnlineGame.State.Round>
    
    public val roundChildSlot: KoneAsynchronousHub<ChildrenSlot<*, RoundChild, UIComponentContext>>
    public val openAdditionalCard: KoneMutableAsynchronousHub<Boolean>
    public val additionalCardButton: KoneAsynchronousHub<AdditionalCardButtonsChild>
    public val onSelectButton: suspend (AdditionalCardButton) -> Unit
    public val additionalCardChildPossibility: KoneAsynchronousHub<ChildrenPossibility<*, AdditionalCardChild, UIComponentContext>>
    
    public val coroutineScope: CoroutineScope
    public val darkTheme: KoneMutableAsynchronousHub<DarkTheme>

    public sealed interface RoundChild {
        public data class RoundWaiting(val component: RoundWaitingComponent) : RoundChild
        public data class RoundPreparation(val component: RoundPreparationComponent) : RoundChild
        public data class RoundExplanation(val component: RoundExplanationComponent) : RoundChild
        public data class RoundLastGuess(val component: RoundLastGuessComponent) : RoundChild
        public data class RoundEditing(val component: RoundEditingComponent) : RoundChild
    }

    public sealed interface AdditionalCardButton {
        public val type: Type

        public data object Schedule : AdditionalCardButton {
            override val type: Type get() = Type.Schedule
        }
        public data class PlayersStatistic(val leaderboardPermutation: ServerApi.Leaderboard?) : AdditionalCardButton {
            override val type: Type get() = Type.PlayersStatistic
        }
        public data class WordsStatistic(val wordsStatistic:  KoneList<GameStateMachine.WordStatistic.AndWord>) : AdditionalCardButton {
            override val type: Type get() = Type.WordsStatistic
        }
        public data object Settings : AdditionalCardButton {
            override val type: Type get() = Type.Settings
        }

        public enum class Type {
            Schedule, PlayersStatistic, WordsStatistic, Settings,
        }
    }

    public data class AdditionalCardButtonsChild(
        val leaderboard: ServerApi.Leaderboard?,
        val wordsStatistic:  KoneList<GameStateMachine.WordStatistic.AndWord>?,
        val selectedButtonType: AdditionalCardButton.Type?,
    ) {
        @Suppress("UNCHECKED_CAST")
        val buttonsList = KoneList.of(
            AdditionalCardButton.Schedule,
            AdditionalCardButton.PlayersStatistic(leaderboard),
            wordsStatistic?.let { AdditionalCardButton.WordsStatistic(it) },
            AdditionalCardButton.Settings,
        ).filter { it != null } as KoneList<AdditionalCardButton>

        val selectedButton = buttonsList.firstThatOrNull { it.type == selectedButtonType }
    }

    public sealed interface AdditionalCardChild {
        public data object Schedule : AdditionalCardChild
        public data class PlayersStatistic(val leaderboard: ServerApi.Leaderboard?) : AdditionalCardChild
        public data class WordsStatistic(val wordsStatistic:  KoneList<GameStateMachine.WordStatistic.AndWord>) : AdditionalCardChild
        public data object Settings : AdditionalCardChild
    }
}