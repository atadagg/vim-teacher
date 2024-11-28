package com.example.vimteacher

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vimteacher.model.OptionModel
import com.example.vimteacher.model.QuestionModel

class QuestionActivity : AppCompatActivity() {

    private var currentQuestion: QuestionModel? = null
    private var currentQuestionPosition: Int = 0
    private var questions: List<QuestionModel> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)

        // Initialize questions (you might want to get these from a repository or intent)
        setupQuestions()

        // Display first question
        displayQuestion(currentQuestionPosition)

        // Setup click listeners
        setupClickListeners()
    }

    private fun setupQuestions() {
        questions = listOf(
            QuestionModel(
                "First Question",
                "What is your favorite color?",
                listOf(
                    OptionModel("Red", 1),
                    OptionModel("Blue", 2),
                    OptionModel("Green", 3),
                    OptionModel("Yellow", 4)
                ),
                1
            )
            // Add more questions here
        )
        currentQuestion = questions.firstOrNull()
    }

    private fun displayQuestion(position: Int) {
        currentQuestion = questions.getOrNull(position)
        currentQuestion?.let { question ->
            // Assuming you have these views in your layout
            findViewById<TextView>(R.id.questionTitle).text = question.questionTitle
            findViewById<TextView>(R.id.questionBody).text = question.questionBody

            // Display options (assuming you have a RadioGroup)
            val optionsGroup = findViewById<RadioGroup>(R.id.optionsGroup)
            optionsGroup.removeAllViews()

            question.options.forEach { option ->
                val radioButton = RadioButton(this)
                radioButton.text = option.optionText
                radioButton.id = option.optionId
                optionsGroup.addView(radioButton)
            }
        }
    }

    private fun setupClickListeners() {
        // Next button click
        findViewById<Button>(R.id.nextButton).setOnClickListener {
            if (currentQuestionPosition < questions.size - 1) {
                currentQuestionPosition++
                displayQuestion(currentQuestionPosition)
            } else {
                // Handle end of questions
                finishQuiz()
            }
        }

        // Option selection
        findViewById<RadioGroup>(R.id.optionsGroup).setOnCheckedChangeListener { group, checkedId ->
            // Handle option selection
            currentQuestion?.let { question ->
                if (checkedId == question.correctOptionId) {
                    // Correct answer
                    Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
                } else {
                    // Wrong answer
                    Toast.makeText(this, "Incorrect!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun finishQuiz() {
        // Handle quiz completion (e.g., show score, navigate to results screen)
        Toast.makeText(this, "Quiz completed!", Toast.LENGTH_LONG).show()
        finish()
    }
}