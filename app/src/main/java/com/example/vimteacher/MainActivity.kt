package com.example.vimteacher

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vimteacher.adapter.QuestionAdapter
import com.example.vimteacher.model.OptionModel
import com.example.vimteacher.model.QuestionModel


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Create sample questions (replace with your data source)
        val questions = listOf(
            QuestionModel(
                "Question 1",
                "What is Android?",
                listOf(
                    OptionModel("Operating System", 1),
                    OptionModel("Web Browser", 2),
                    OptionModel("Text Editor", 3),
                    OptionModel("Game Console", 4)
                ),
                1
            ),
            QuestionModel(
                "Question 2",
                "What language is primarily used for Android development?",
                listOf(
                    OptionModel("Python", 1),
                    OptionModel("Java", 2),
                    OptionModel("Kotlin", 3),
                    OptionModel("C++", 4)
                ),
                3
            )
            // Add more questions
        )

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = QuestionAdapter(questions) { question ->
            val intent = Intent(this, QuestionActivity::class.java).apply {
                putExtra("QUESTION_TITLE", question.questionTitle)
                putExtra("QUESTION_BODY", question.questionBody)
                putExtra("CORRECT_OPTION_ID", question.correctOptionId)
                // Note: You'll need to implement Parcelable or Serializable for OptionModel
                // to pass the options array/list
            }
            startActivity(intent)
        }
    }
}