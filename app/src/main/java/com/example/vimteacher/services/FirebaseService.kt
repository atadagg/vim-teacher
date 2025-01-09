package com.example.vimteacher.services

import android.util.Log
import com.example.vimteacher.model.QuestionModel
import com.example.vimteacher.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    suspend fun getLeaderboardUsers(): List<UserModel> {
        return try {
            val snapshot = db.collection("users")
                .orderBy("questions_solved", Query.Direction.DESCENDING)
                .get()
                .await()

            Log.d("FirebaseService", "Raw leaderboard snapshot data: ${snapshot.documents.map { it.data }}")

            val leaderboardUsers = snapshot.documents.map { document ->
                UserModel(
                    uid = document.id,
                    email = document.getString("email") ?: "",
                    questions_solved = document.getLong("questions_solved")?.toInt() ?: 0
                )
            }

            // Log the deserialized UserLeaderboardModel objects
            Log.d("FirebaseService", "Deserialized Leaderboard Users: ${leaderboardUsers.joinToString(separator = "\n")}")

            leaderboardUsers
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error getting leaderboard users: ${e.message}")
            emptyList()
        }
    }
    suspend fun getQuestions(): List<QuestionModel> {
        return try {
            val snapshot = db.collection("questions")
                .orderBy("questionId")
                .get()
                .await()

            val questions = snapshot.toObjects(QuestionModel::class.java)

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
            ),
            mapOf(
                "questionId" to 3,
                "questionBody" to "In Vim, what command would you use to join the current line with the line below it, adding a single space between them?",
                "difficulty" to "EASY",
                "options" to listOf(
                    mapOf("optionId" to 1, "optionBody" to "J", "optionDescription" to "Join lines with space"),
                    mapOf("optionId" to 2, "optionBody" to "gJ", "optionDescription" to "Join lines without space"),
                    mapOf("optionId" to 3, "optionBody" to "K", "optionDescription" to "Move up and join lines"),
                    mapOf("optionId" to 4, "optionBody" to "C", "optionDescription" to "Change to end of line")
                ),
                "correctOptionId" to 1
            ),
            mapOf(
                "questionId" to 4,
                "questionBody" to "Which command sequence would delete all text from the cursor position to the first occurrence of the character 'x' on the current line?",
                "difficulty" to "MEDIUM",
                "options" to listOf(
                    mapOf("optionId" to 1, "optionBody" to "d/x", "optionDescription" to "Delete until search pattern 'x'"),
                    mapOf("optionId" to 2, "optionBody" to "dtx", "optionDescription" to "Delete until (not including) character 'x'"),
                    mapOf("optionId" to 3, "optionBody" to "dfx", "optionDescription" to "Delete including character 'x'"),
                    mapOf("optionId" to 4, "optionBody" to "dFx", "optionDescription" to "Delete backwards to character 'x'")
                ),
                "correctOptionId" to 2
            ),
            mapOf(
                "questionId" to 5,
                "questionBody" to "What command would you use to copy (yank) all text between paired curly braces { }, including the braces themselves?",
                "difficulty" to "HARD",
                "options" to listOf(
                    mapOf("optionId" to 1, "optionBody" to "yi{", "optionDescription" to "Yank inner curly braces"),
                    mapOf("optionId" to 2, "optionBody" to "ya{", "optionDescription" to "Yank around curly braces"),
                    mapOf("optionId" to 3, "optionBody" to "yib", "optionDescription" to "Yank inner block"),
                    mapOf("optionId" to 4, "optionBody" to "y%", "optionDescription" to "Yank to matching bracket")
                ),
                "correctOptionId" to 2
            ),
            mapOf(
                "questionId" to 6,
                "questionBody" to "Which command would you use to replace every occurrence of the word 'foo' with 'bar' in the entire file, but ask for confirmation before each replacement?",
                "difficulty" to "MEDIUM",
                "options" to listOf(
                    mapOf("optionId" to 1, "optionBody" to ":%s/foo/bar/g", "optionDescription" to "Global substitute without confirmation"),
                    mapOf("optionId" to 2, "optionBody" to ":%s/foo/bar/gc", "optionDescription" to "Global substitute with confirmation"),
                    mapOf("optionId" to 3, "optionBody" to ":g/foo/s//bar/g", "optionDescription" to "Global search and substitute"),
                    mapOf("optionId" to 4, "optionBody" to ":g/foo/s//bar/gc", "optionDescription" to "Global search and substitute with confirmation")
                ),
                "correctOptionId" to 2
            ),
            mapOf(
                "questionId" to 7,
                "questionBody" to "What command would you use to delete all text from the current line to the end of the file?",
                "difficulty" to "HARD",
                "options" to listOf(
                    mapOf("optionId" to 1, "optionBody" to "dG", "optionDescription" to "Delete to end of file"),
                    mapOf("optionId" to 2, "optionBody" to "D$", "optionDescription" to "Delete to end of line"),
                    mapOf("optionId" to 3, "optionBody" to "d1G", "optionDescription" to "Delete to beginning of file"),
                    mapOf("optionId" to 4, "optionBody" to "d%", "optionDescription" to "Delete to matching bracket")
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


