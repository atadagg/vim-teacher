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
            ),
            QuestionModel(
                    questionId = 5,
            questionBody = "Which Vim command would you use to join the current line with the line below it, removing the line break but keeping one space between them?",
            difficulty = DifficultyLevel.EASY,
            options = listOf(
                OptionModel(1, "J", "Join lines with space"),
                OptionModel(2, "gJ", "Join lines without space"),
                OptionModel(3, "K", "Look up keyword"),
                OptionModel(4, "H", "Move to top of screen")
            ),
            correctOptionId = 1
            ),
            QuestionModel(
                questionId = 6,
                questionBody = "In Vim, which command allows you to repeat the last change you made?",
                difficulty = DifficultyLevel.EASY,
                options = listOf(
                    OptionModel(1, ".", "Repeat last change"),
                    OptionModel(2, "@:", "Repeat last command-line command"),
                    OptionModel(3, "@@", "Repeat last macro"),
                    OptionModel(4, "&", "Repeat last substitution")
                ),
                correctOptionId = 1
            ),
            QuestionModel(
                questionId = 7,
                questionBody = "Which Vim command would you use to delete all text from the cursor position to the end of the current word?",
                difficulty = DifficultyLevel.MEDIUM,
                options = listOf(
                    OptionModel(1, "de", "Delete to end of word"),
                    OptionModel(2, "dw", "Delete word and space after"),
                    OptionModel(3, "db", "Delete to beginning of word"),
                    OptionModel(4, "daw", "Delete entire word")
                ),
                correctOptionId = 1
            ),
            QuestionModel(
                questionId = 8,
                questionBody = "What command would you use in Vim to copy (yank) everything from the cursor position to the end of the line?",
                difficulty = DifficultyLevel.MEDIUM,
                options = listOf(
                    OptionModel(1, "y$", "Yank to end of line"),
                    OptionModel(2, "Y", "Yank entire line"),
                    OptionModel(3, "yl", "Yank character"),
                    OptionModel(4, "yy", "Yank line")
                ),
                correctOptionId = 1
            ),
            QuestionModel(
                questionId = 9,
                questionBody = "Which Vim command marks the current cursor position with mark 'a'?",
                difficulty = DifficultyLevel.MEDIUM,
                options = listOf(
                    OptionModel(1, "ma", "Set mark a"),
                    OptionModel(2, "'a", "Jump to mark a"),
                    OptionModel(3, "`a", "Jump to exact position of mark a"),
                    OptionModel(4, "mA", "Set global mark A")
                ),
                correctOptionId = 1
            ),
            QuestionModel(
                questionId = 10,
                questionBody = "In Vim, which command would you use to replace every occurrence of 'foo' with 'bar' in the entire file, but asking for confirmation for each replacement?",
                difficulty = DifficultyLevel.HARD,
                options = listOf(
                    OptionModel(1, ":%s/foo/bar/gc", "Global substitute with confirmation"),
                    OptionModel(2, ":%s/foo/bar/g", "Global substitute"),
                    OptionModel(3, ":s/foo/bar/gc", "Current line substitute with confirmation"),
                    OptionModel(4, ":g/foo/s//bar/g", "Global substitute alternative")
                ),
                correctOptionId = 1
            ),
            QuestionModel(
                questionId = 11,
                questionBody = "Which Vim command would you use to delete everything inside parentheses, including nested parentheses?",
                difficulty = DifficultyLevel.HARD,
                options = listOf(
                    OptionModel(1, "di)", "Delete inside parentheses"),
                    OptionModel(2, "da)", "Delete around parentheses"),
                    OptionModel(3, "dib", "Delete inside block"),
                    OptionModel(4, "d%", "Delete to matching bracket")
                ),
                correctOptionId = 1
            ),
            QuestionModel(
                questionId = 12,
                questionBody = "What command would you use in Vim to center the current line on the screen?",
                difficulty = DifficultyLevel.MEDIUM,
                options = listOf(
                    OptionModel(1, "zz", "Center current line"),
                    OptionModel(2, "zt", "Move current line to top"),
                    OptionModel(3, "zb", "Move current line to bottom"),
                    OptionModel(4, "z.", "Redraw with cursor at center")
                ),
                correctOptionId = 1
            ),
            QuestionModel(
                questionId = 13,
                questionBody = "Which Vim command records a macro into register 'q'?",
                difficulty = DifficultyLevel.HARD,
                options = listOf(
                    OptionModel(1, "qq", "Start recording to register q"),
                    OptionModel(2, "@q", "Play macro from register q"),
                    OptionModel(3, "qQ", "Start recording to register Q"),
                    OptionModel(4, "q:", "Open command-line window")
                ),
                correctOptionId = 1
            ),
            QuestionModel(
                questionId = 14,
                questionBody = "In Vim, which command moves the cursor to the last line of the file?",
                difficulty = DifficultyLevel.EASY,
                options = listOf(
                    OptionModel(1, "G", "Go to last line"),
                    OptionModel(2, "gg", "Go to first line"),
                    OptionModel(3, "$", "Go to end of line"),
                    OptionModel(4, "L", "Go to bottom of screen")
                ),
                correctOptionId = 1
            ),
            QuestionModel(
                questionId = 15,
                questionBody = "Which Vim command allows you to increment the number under the cursor?",
                difficulty = DifficultyLevel.MEDIUM,
                options = listOf(
                    OptionModel(1, "CTRL-A", "Increment number"),
                    OptionModel(2, "CTRL-X", "Decrement number"),
                    OptionModel(3, "CTRL-D", "Scroll down"),
                    OptionModel(4, "CTRL-U", "Scroll up")
                ),
                correctOptionId = 1
            ),
            QuestionModel(
                questionId = 16,
                questionBody = "What command in Vim opens a new horizontal split window?",
                difficulty = DifficultyLevel.EASY,
                options = listOf(
                    OptionModel(1, ":split", "Open horizontal split"),
                    OptionModel(2, ":vsplit", "Open vertical split"),
                    OptionModel(3, ":new", "Create new buffer"),
                    OptionModel(4, ":sp", "Short form of split")
                ),
                correctOptionId = 1
            ),
            QuestionModel(
                questionId = 17,
                questionBody = "Which Vim command formats (indents) the entire file according to the current formatting rules?",
                difficulty = DifficultyLevel.HARD,
                options = listOf(
                    OptionModel(1, "gg=G", "Format entire file"),
                    OptionModel(2, "==", "Format current line"),
                    OptionModel(3, "=%", "Format to matching bracket"),
                    OptionModel(4, "=i{", "Format inside curly braces")
                ),
                correctOptionId = 1
            ),
            QuestionModel(
                questionId = 18,
                questionBody = "In Vim, which command deletes from the cursor position to the next occurrence of the character 'x'?",
                difficulty = DifficultyLevel.MEDIUM,
                options = listOf(
                    OptionModel(1, "dtx", "Delete till x"),
                    OptionModel(2, "dfx", "Delete including x"),
                    OptionModel(3, "dTx", "Delete backward till x"),
                    OptionModel(4, "dFx", "Delete backward including x")
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
