package dev.lounres.halfhat.client.common.ui.components.faq


public data class QuestionAndAnswer(
    val question: String,
    val answer: String,
)

// TODO: Review frequent questions and answers list
public val questionsAndAnswers: List<QuestionAndAnswer> = listOf(
    QuestionAndAnswer(
        question = "Sanctus sadipscing ea et in labore ipsum sanctus minim at blandit ea sanctus no est duis magna.",
        answer = "Dolore illum ipsum eum dolore dolore dolore at et consetetur sed."
    ),
    QuestionAndAnswer(
        question = "Dolores id kasd lorem diam gubergren gubergren at diam sanctus sed dolor accusam.",
        answer = "Ea gubergren stet magna vero eos aliquyam lorem no ipsum ea stet kasd vulputate exerci."
    ),
    QuestionAndAnswer(
        question = "Amet diam labore ipsum sea vero dolor ut diam consetetur dolore.",
        answer = "Amet dignissim velit amet et diam vero vel stet molestie luptatum sit accusam amet vel."
    ),
)