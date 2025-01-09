package com.example.vimteacher.repositories

import android.util.Log
import com.example.vimteacher.model.QuestionModel
import com.example.vimteacher.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

interface FirebaseRepositoryInterface {
    suspend fun loginUser(email: String, password: String): Result<FirebaseUser>
    suspend fun registerUser(email: String, password: String): Result<FirebaseUser>
    suspend fun checkAndUpdateSolvedQuestion(questionId: Int): Result<Boolean>
    suspend fun getLeaderboardUsers(): List<UserModel>
    suspend fun getQuestions(): List<QuestionModel>
    fun observeSolvedQuestions(userId: String, onUpdate: (Set<Int>) -> Unit)
}

class FirebaseRepository : FirebaseRepositoryInterface {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override suspend fun loginUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Authentication failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerUser(email: String, password: String): Result<FirebaseUser> {
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


    override suspend fun checkAndUpdateSolvedQuestion(questionId: Int): Result<Boolean> {
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
            Log.e("FirebaseRepository", "Error updating solved question", e)
            Result.failure(e)
        }
    }

    override suspend fun getLeaderboardUsers(): List<UserModel> {
        return try {
            val snapshot = db.collection("users")
                .orderBy("questions_solved", Query.Direction.DESCENDING)
                .get()
                .await()

            Log.d("FirebaseRepository", "Raw leaderboard snapshot data: ${snapshot.documents.map { it.data }}")

            val leaderboardUsers = snapshot.documents.map { document ->
                UserModel(
                    uid = document.id,
                    email = document.getString("email") ?: "",
                    questions_solved = document.getLong("questions_solved")?.toInt() ?: 0
                )
            }

            // Log the deserialized UserLeaderboardModel objects
            Log.d("FirebaseRepository", "Deserialized Leaderboard Users: ${leaderboardUsers.joinToString(separator = "\n")}")

            leaderboardUsers
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting leaderboard users: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getQuestions(): List<QuestionModel> {
        return try {
            val snapshot = db.collection("questions")
                .orderBy("questionId")
                .get()
                .await()

            val questions = snapshot.toObjects(QuestionModel::class.java)

            Log.d("FirebaseRepository", "Deserialized QuestionModels: ${questions.joinToString(separator = "\n")}")

            questions
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting questions: ${e.message}")
            emptyList()
        }
    }

    override fun observeSolvedQuestions(userId: String, onUpdate: (Set<Int>) -> Unit) {
        db
            .collection("userQuestions")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FirebaseRepository", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val solvedIds = snapshot?.documents?.mapNotNull {
                    it.getLong("questionId")?.toInt()
                }?.toSet() ?: emptySet()

                onUpdate(solvedIds)
            }
    }
}


