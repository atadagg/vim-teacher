package com.example.vimteacher.services

import android.util.Log
import com.example.vimteacher.model.QuestionModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseService {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun loginUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Authentication failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()

            result.user?.let { user ->
                try {
                    createUserProfileTransaction(user.uid, email)
                    Result.success(user)
                } catch (e: Exception) {
                    user.delete().await()
                    Result.failure(e)
                }
            } ?: Result.failure(Exception("Registration failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun createUserProfileTransaction(userId: String, email: String) {
        db.runTransaction { transaction ->
            val userRef = db.collection("users").document(userId)

            // Check if user document already exists
            val snapshot = transaction.get(userRef)
            if (snapshot.exists()) {
                throw Exception("User profile already exists")
            }

            // Create new profile
            val userProfile = hashMapOf(
                "email" to email,
                "questions_solved" to 0,
            )

            transaction.set(userRef, userProfile)
        }.await()
    }


    suspend fun checkAndUpdateSolvedQuestion(questionId: Int): Result<Boolean> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

        return try {
            var isNewlySolved = false

            db.runTransaction { transaction ->
                // Do ALL reads first
                val userQuestionDoc = db.collection("userQuestions")
                    .document("${userId}_$questionId")
                val userDoc = db.collection("users").document(userId)

                // Read operations
                val questionSnapshot = transaction.get(userQuestionDoc)
                val userSnapshot = transaction.get(userDoc)

                // After reads, perform writes if needed
                if (!questionSnapshot.exists()) {
                    isNewlySolved = true

                    // Now do the writes
                    transaction.set(userQuestionDoc, hashMapOf(
                        "userId" to userId,
                        "questionId" to questionId,
                    ))

                    val currentCount = userSnapshot.getLong("questions_solved") ?: 0
                    transaction.update(userDoc, "questions_solved", currentCount + 1)
                }
            }.await()

            Result.success(isNewlySolved)
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error updating solved question", e)
            Result.failure(e)
        }
    }

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


