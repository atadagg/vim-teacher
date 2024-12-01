package com.example.vimteacher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.example.vimteacher.model.DifficultyLevel
import com.example.vimteacher.model.OptionModel
import com.example.vimteacher.model.QuestionModel

class QuestionsViewModel : ViewModel() {

    // LiveData to hold all questions
    private val allQuestionsLiveData = MutableLiveData<List<QuestionModel>>()

    // LiveData for the current question the user is interacting with
    val currentQuestionLiveData = MutableLiveData<QuestionModel>()

    // Initialize with some questions (this could come from a repository or database)
    init {
        val questionList = listOf(
            QuestionModel(
                questionId = 1,
                questionBody = "In Vim's normal mode, which command sequence would you use to move the current line one line down (i.e., swap the current line with the line below it)?",
                difficulty = DifficultyLevel.EASY,
                options = listOf(
                    OptionModel(1, "ddp", "Delete line and put it below the cursor position"),
                    OptionModel(2, "yyP", "Yank line and put it above the cursor position"),
                    OptionModel(3, "ddP", "Delete line and put it above the cursor position"),
                    OptionModel(4, "yyp", "Yank line and put it below the cursor position")
                ),
                correctOptionId = 1
            ),
            QuestionModel(
                questionId = 2,
                questionBody = "Which Vim command would you use to replace an entire word under the cursor, regardless of where the cursor is positioned within that word?",
                difficulty = DifficultyLevel.MEDIUM,
                options = listOf(
                    OptionModel(1, "ciw", "Change inner word"),
                    OptionModel(2, "cw", "Change word from cursor"),
                    OptionModel(3, "ci'", "Change inner quotes"),
                    OptionModel(4, "ci(", "Change inner parentheses")
                ),
                correctOptionId = 1
            ),
            QuestionModel(
                questionId = 3,
                questionBody = "In Vim, which command can be used to ROT13 encode the text under the cursor, a technique sometimes used to obscure text like spoilers or solutions?",
                difficulty = DifficultyLevel.HARD,
                options = listOf(
                    OptionModel(1, "g?", "Search backward"),
                    OptionModel(2, "g/", "Search forward"),
                    OptionModel(3, "gg", "Go to the beginning of the file"),
                    OptionModel(4, "gd", "Go to definition")
                ),
                correctOptionId = 1
            ),
            QuestionModel(
                questionId = 4,
                questionBody = "In Vim, which command can be used to ROT13 encode the text under the cursor, a technique sometimes used to obscure text like spoilers or solutions?",
                difficulty = DifficultyLevel.HARD,
                options = listOf(
                    OptionModel(1, "g?", "Search backward"),
                    OptionModel(2, "g/", "Search forward"),
                    OptionModel(3, "gg", "Go to the beginning of the file"),
                    OptionModel(4, "gd", "Go to definition")
                ),
                correctOptionId = 1
            )
        )

        // Set all questions
        allQuestionsLiveData.value = questionList

        // Set the first question as the current question (or some default)
        currentQuestionLiveData.value = questionList.first()
    }

    // Function to get all questions
    fun getAllQuestions(): MutableLiveData<List<QuestionModel>> {
        return allQuestionsLiveData
    }

    // Function to get questions filtered by difficulty
    fun getQuestionsByDifficulty(difficulty: DifficultyLevel): List<QuestionModel>? {
        return allQuestionsLiveData.value?.filter { it.difficulty == difficulty }
    }


    // Function to set the current question (when the user selects a question)
    fun getQuestionById(id: Int) : QuestionModel? {
        return allQuestionsLiveData.value?.find { it.questionId == id }
    }
    fun setQuestionById(id: Int){
        currentQuestionLiveData.value = allQuestionsLiveData.value?.find { it.questionId == id }
    }
}
