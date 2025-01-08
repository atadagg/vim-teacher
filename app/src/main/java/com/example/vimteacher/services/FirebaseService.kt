package com.example.vimteacher.services

import android.util.Log
import com.example.vimteacher.model.QuestionModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseService {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getQuestions(): List<QuestionModel> {
        return try {
            val snapshot = db.collection("questions").get().await()

            // Log the raw snapshot data
            Log.d("FirebaseService", "Raw snapshot data: ${snapshot.documents.map { it.data }}")

            // Deserialize to QuestionModel list
            val questions = snapshot.toObjects(QuestionModel::class.java)

            // Log the deserialized QuestionModel objects
            Log.d("FirebaseService", "Deserialized QuestionModels: ${questions.joinToString(separator = "\n")}")

            questions
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error getting questions: ${e.message}")
            emptyList()
        }
    }

    //Just to make writing in firebase easier
    fun uploadQuestionsToFirebase() {
        val db = FirebaseFirestore.getInstance()

        val questionList = listOf(
            mapOf(
                "questionId" to 1,
                "questionBody" to "In Vim's normal mode, which command sequence would you use to move the current line one line down (i.e., swap the current line with the line below it)?",
                "difficulty" to "EASY",
                "options" to listOf(
                    mapOf("optionId" to 1, "optionBody" to "ddp", "optionDescription" to "Delete line and put it below the cursor position"),
                    mapOf("optionId" to 2, "optionBody" to "yyP", "optionDescription" to "Yank line and put it above the cursor position"),
                    mapOf("optionId" to 3, "optionBody" to "ddP", "optionDescription" to "Delete line and put it above the cursor position"),
                    mapOf("optionId" to 4, "optionBody" to "yyp", "optionDescription" to "Yank line and put it below the cursor position")
                ),
                "correctOptionId" to 1
            ),
            mapOf(
                "questionId" to 2,
                "questionBody" to "Which Vim command would you use to replace an entire word under the cursor, regardless of where the cursor is positioned within that word?",
                "difficulty" to "MEDIUM",
                "options" to listOf(
                    mapOf("optionId" to 1, "optionBody" to "ciw", "optionDescription" to "Change inner word"),
                    mapOf("optionId" to 2, "optionBody" to "cw", "optionDescription" to "Change word from cursor"),
                    mapOf("optionId" to 3, "optionBody" to "ci'", "optionDescription" to "Change inner quotes"),
                    mapOf("optionId" to 4, "optionBody" to "ci(", "optionDescription" to "Change inner parentheses")
                ),
                "correctOptionId" to 1
            )
        )

        questionList.forEach { question ->
            db.collection("questions")
                .add(question)
                .addOnSuccessListener { documentReference ->
                    println("Question added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { exception ->
                    println("Error adding question: ${exception.message}")
                }
        }
    }
}
