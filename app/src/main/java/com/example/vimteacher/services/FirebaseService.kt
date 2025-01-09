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

    fun observeSolvedQuestions(userId: String, onUpdate: (Set<Int>) -> Unit) {
        db
            .collection("userQuestions")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FirebaseService", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val solvedIds = snapshot?.documents?.mapNotNull {
                    it.getLong("questionId")?.toInt()
                }?.toSet() ?: emptySet()

                onUpdate(solvedIds)
            }
    }

    //Just to make writing in firebase easier
    fun uploadQuestionsToFirebase() {
        val db = FirebaseFirestore.getInstance()

        val questionList = listOf(
//            mapOf(
//                "questionId" to 1,
//                "questionBody" to "In Vim's normal mode, which command sequence would you use to move the current line one line down (i.e., swap the current line with the line below it)?",
//                "difficulty" to "EASY",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to "ddp", "optionDescription" to "Delete line and put it below the cursor position"),
//                    mapOf("optionId" to 2, "optionBody" to "yyP", "optionDescription" to "Yank line and put it above the cursor position"),
//                    mapOf("optionId" to 3, "optionBody" to "ddP", "optionDescription" to "Delete line and put it above the cursor position"),
//                    mapOf("optionId" to 4, "optionBody" to "yyp", "optionDescription" to "Yank line and put it below the cursor position")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 2,
//                "questionBody" to "Which Vim command would you use to replace an entire word under the cursor, regardless of where the cursor is positioned within that word?",
//                "difficulty" to "MEDIUM",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to "ciw", "optionDescription" to "Change inner word"),
//                    mapOf("optionId" to 2, "optionBody" to "cw", "optionDescription" to "Change word from cursor"),
//                    mapOf("optionId" to 3, "optionBody" to "ci'", "optionDescription" to "Change inner quotes"),
//                    mapOf("optionId" to 4, "optionBody" to "ci(", "optionDescription" to "Change inner parentheses")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 3,
//                "questionBody" to "In Vim, what command would you use to join the current line with the line below it, adding a single space between them?",
//                "difficulty" to "EASY",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to "J", "optionDescription" to "Join lines with space"),
//                    mapOf("optionId" to 2, "optionBody" to "gJ", "optionDescription" to "Join lines without space"),
//                    mapOf("optionId" to 3, "optionBody" to "K", "optionDescription" to "Move up and join lines"),
//                    mapOf("optionId" to 4, "optionBody" to "C", "optionDescription" to "Change to end of line")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 4,
//                "questionBody" to "Which command sequence would delete all text from the cursor position to the first occurrence of the character 'x' on the current line?",
//                "difficulty" to "MEDIUM",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to "d/x", "optionDescription" to "Delete until search pattern 'x'"),
//                    mapOf("optionId" to 2, "optionBody" to "dtx", "optionDescription" to "Delete until (not including) character 'x'"),
//                    mapOf("optionId" to 3, "optionBody" to "dfx", "optionDescription" to "Delete including character 'x'"),
//                    mapOf("optionId" to 4, "optionBody" to "dFx", "optionDescription" to "Delete backwards to character 'x'")
//                ),
//                "correctOptionId" to 2
//            ),
//            mapOf(
//                "questionId" to 5,
//                "questionBody" to "What command would you use to copy (yank) all text between paired curly braces { }, including the braces themselves?",
//                "difficulty" to "HARD",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to "yi{", "optionDescription" to "Yank inner curly braces"),
//                    mapOf("optionId" to 2, "optionBody" to "ya{", "optionDescription" to "Yank around curly braces"),
//                    mapOf("optionId" to 3, "optionBody" to "yib", "optionDescription" to "Yank inner block"),
//                    mapOf("optionId" to 4, "optionBody" to "y%", "optionDescription" to "Yank to matching bracket")
//                ),
//                "correctOptionId" to 2
//            ),
//            mapOf(
//                "questionId" to 6,
//                "questionBody" to "Which command would you use to replace every occurrence of the word 'foo' with 'bar' in the entire file, but ask for confirmation before each replacement?",
//                "difficulty" to "MEDIUM",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to ":%s/foo/bar/g", "optionDescription" to "Global substitute without confirmation"),
//                    mapOf("optionId" to 2, "optionBody" to ":%s/foo/bar/gc", "optionDescription" to "Global substitute with confirmation"),
//                    mapOf("optionId" to 3, "optionBody" to ":g/foo/s//bar/g", "optionDescription" to "Global search and substitute"),
//                    mapOf("optionId" to 4, "optionBody" to ":g/foo/s//bar/gc", "optionDescription" to "Global search and substitute with confirmation")
//                ),
//                "correctOptionId" to 2
//            ),
//            mapOf(
//                "questionId" to 7,
//                "questionBody" to "What command would you use to delete all text from the current line to the end of the file?",
//                "difficulty" to "HARD",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to "dG", "optionDescription" to "Delete to end of file"),
//                    mapOf("optionId" to 2, "optionBody" to "D$", "optionDescription" to "Delete to end of line"),
//                    mapOf("optionId" to 3, "optionBody" to "d1G", "optionDescription" to "Delete to beginning of file"),
//                    mapOf("optionId" to 4, "optionBody" to "d%", "optionDescription" to "Delete to matching bracket")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 8,
//                "questionBody" to "Which Vim command appends text at the end of the current line?",
//                "difficulty" to "EASY",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to "A", "optionDescription" to "Append text at the end of the current line"),
//                    mapOf("optionId" to 2, "optionBody" to "a", "optionDescription" to "Append text after the cursor"),
//                    mapOf("optionId" to 3, "optionBody" to "i", "optionDescription" to "Insert text before the cursor"),
//                    mapOf("optionId" to 4, "optionBody" to "I", "optionDescription" to "Insert text at the beginning of the line")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 9,
//                "questionBody" to "What command deletes the word under the cursor and switches to insert mode?",
//                "difficulty" to "EASY",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to "cw", "optionDescription" to "Change word"),
//                    mapOf("optionId" to 2, "optionBody" to "dw", "optionDescription" to "Delete word"),
//                    mapOf("optionId" to 3, "optionBody" to "ciw", "optionDescription" to "Change inner word"),
//                    mapOf("optionId" to 4, "optionBody" to "diw", "optionDescription" to "Delete inner word")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 10,
//                "questionBody" to "Which command in normal mode searches forward for the word under the cursor?",
//                "difficulty" to "MEDIUM",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to "*", "optionDescription" to "Search forward for the word under the cursor"),
//                    mapOf("optionId" to 2, "optionBody" to "#", "optionDescription" to "Search backward for the word under the cursor"),
//                    mapOf("optionId" to 3, "optionBody" to "/", "optionDescription" to "Search forward for a pattern"),
//                    mapOf("optionId" to 4, "optionBody" to "?", "optionDescription" to "Search backward for a pattern")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 11,
//                "questionBody" to "What command would you use to undo the last change?",
//                "difficulty" to "EASY",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to "u", "optionDescription" to "Undo the last change"),
//                    mapOf("optionId" to 2, "optionBody" to "Ctrl-r", "optionDescription" to "Redo the last undone change"),
//                    mapOf("optionId" to 3, "optionBody" to ".", "optionDescription" to "Repeat the last command"),
//                    mapOf("optionId" to 4, "optionBody" to "U", "optionDescription" to "Undo all changes on the current line")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 12,
//                "questionBody" to "Which Vim command splits the window horizontally?",
//                "difficulty" to "MEDIUM",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to ":split", "optionDescription" to "Split the window horizontally"),
//                    mapOf("optionId" to 2, "optionBody" to ":vsplit", "optionDescription" to "Split the window vertically"),
//                    mapOf("optionId" to 3, "optionBody" to ":new", "optionDescription" to "Open a new horizontal split"),
//                    mapOf("optionId" to 4, "optionBody" to ":tabnew", "optionDescription" to "Open a new tab")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 13,
//                "questionBody" to "What command in normal mode deletes from the cursor to the end of the line?",
//                "difficulty" to "EASY",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to "d$", "optionDescription" to "Delete to the end of the line"),
//                    mapOf("optionId" to 2, "optionBody" to "D", "optionDescription" to "Delete to the end of the line"),
//                    mapOf("optionId" to 3, "optionBody" to "dd", "optionDescription" to "Delete the entire line"),
//                    mapOf("optionId" to 4, "optionBody" to "d0", "optionDescription" to "Delete to the beginning of the line")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 14,
//                "questionBody" to "Which command yanks the current line, including the newline character?",
//                "difficulty" to "EASY",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to "yy", "optionDescription" to "Yank the current line"),
//                    mapOf("optionId" to 2, "optionBody" to "Y", "optionDescription" to "Yank the current line"),
//                    mapOf("optionId" to 3, "optionBody" to "y$", "optionDescription" to "Yank to the end of the line"),
//                    mapOf("optionId" to 4, "optionBody" to "y0", "optionDescription" to "Yank to the beginning of the line")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 15,
//                "questionBody" to "Which command in Vim lets you delete everything inside a pair of parentheses?",
//                "difficulty" to "MEDIUM",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to "di(", "optionDescription" to "Delete everything inside parentheses"),
//                    mapOf("optionId" to 2, "optionBody" to "ci(", "optionDescription" to "Change everything inside parentheses"),
//                    mapOf("optionId" to 3, "optionBody" to "yi(", "optionDescription" to "Yank everything inside parentheses"),
//                    mapOf("optionId" to 4, "optionBody" to "da(", "optionDescription" to "Delete including the parentheses")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 16,
//                "questionBody" to "What command would you use to copy (yank) from the cursor to the end of the word?",
//                "difficulty" to "EASY",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to "yw", "optionDescription" to "Yank to the end of the word"),
//                    mapOf("optionId" to 2, "optionBody" to "yW", "optionDescription" to "Yank to the end of the WORD"),
//                    mapOf("optionId" to 3, "optionBody" to "y$", "optionDescription" to "Yank to the end of the line"),
//                    mapOf("optionId" to 4, "optionBody" to "yy", "optionDescription" to "Yank the entire line")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 17,
//                "questionBody" to "Which Vim command replaces the character under the cursor?",
//                "difficulty" to "EASY",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to "r", "optionDescription" to "Replace the character under the cursor"),
//                    mapOf("optionId" to 2, "optionBody" to "R", "optionDescription" to "Enter replace mode"),
//                    mapOf("optionId" to 3, "optionBody" to "cw", "optionDescription" to "Change word"),
//                    mapOf("optionId" to 4, "optionBody" to "ciw", "optionDescription" to "Change inner word")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 18,
//                "questionBody" to "What command moves the cursor to the next occurrence of the character 'x' on the current line?",
//                "difficulty" to "MEDIUM",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to "fx", "optionDescription" to "Move to the next occurrence of 'x'"),
//                    mapOf("optionId" to 2, "optionBody" to "tx", "optionDescription" to "Move to just before the next 'x'"),
//                    mapOf("optionId" to 3, "optionBody" to "Fx", "optionDescription" to "Move to the previous occurrence of 'x'"),
//                    mapOf("optionId" to 4, "optionBody" to "Tz", "optionDescription" to "Move to just before the previous 'z'")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 19,
//                "questionBody" to "What command would you use to open a new tab in Vim?",
//                "difficulty" to "EASY",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to ":tabnew", "optionDescription" to "Open a new tab"),
//                    mapOf("optionId" to 2, "optionBody" to ":new", "optionDescription" to "Open a new split"),
//                    mapOf("optionId" to 3, "optionBody" to ":vs", "optionDescription" to "Open a vertical split"),
//                    mapOf("optionId" to 4, "optionBody" to ":tabs", "optionDescription" to "List all tabs")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 20,
//                "questionBody" to "Which command in Vim visually selects the entire line?",
//                "difficulty" to "EASY",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to "V", "optionDescription" to "Enter line visual mode"),
//                    mapOf("optionId" to 2, "optionBody" to "v", "optionDescription" to "Enter character visual mode"),
//                    mapOf("optionId" to 3, "optionBody" to "Ctrl-v", "optionDescription" to "Enter block visual mode"),
//                    mapOf("optionId" to 4, "optionBody" to "gv", "optionDescription" to "Re-select last visual selection")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 21,
//                "questionBody" to "What command moves the cursor to the first line of the file?",
//                "difficulty" to "EASY",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to "gg", "optionDescription" to "Move to the first line of the file"),
//                    mapOf("optionId" to 2, "optionBody" to "G", "optionDescription" to "Move to the last line of the file"),
//                    mapOf("optionId" to 3, "optionBody" to "zz", "optionDescription" to "Center the current line"),
//                    mapOf("optionId" to 4, "optionBody" to "Ctrl-g", "optionDescription" to "Display file information")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 22,
//                "questionBody" to "What command would you use to repeat the last command?",
//                "difficulty" to "EASY",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to ".", "optionDescription" to "Repeat the last command"),
//                    mapOf("optionId" to 2, "optionBody" to "@@", "optionDescription" to "Repeat the last macro"),
//                    mapOf("optionId" to 3, "optionBody" to "Ctrl-r", "optionDescription" to "Redo the last undone change"),
//                    mapOf("optionId" to 4, "optionBody" to "u", "optionDescription" to "Undo the last change")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 23,
//                "questionBody" to "Which Vim command opens the help documentation for a specific command?",
//                "difficulty" to "MEDIUM",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to ":help", "optionDescription" to "Open help documentation"),
//                    mapOf("optionId" to 2, "optionBody" to ":man", "optionDescription" to "Open the manual for a command"),
//                    mapOf("optionId" to 3, "optionBody" to ":doc", "optionDescription" to "Open documentation"),
//                    mapOf("optionId" to 4, "optionBody" to ":info", "optionDescription" to "Open information on a command")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 24,
//                "questionBody" to "What command in Vim substitutes the word under the cursor with 'foo' globally in the file?",
//                "difficulty" to "HARD",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to ":%s/<cword>/foo/g", "optionDescription" to "Substitute the word under the cursor with 'foo' globally"),
//                    mapOf("optionId" to 2, "optionBody" to ":s/<cword>/foo/g", "optionDescription" to "Substitute the word under the cursor on the current line"),
//                    mapOf("optionId" to 3, "optionBody" to ":%s/foo/<cword>/g", "optionDescription" to "Substitute 'foo' with the word under the cursor globally"),
//                    mapOf("optionId" to 4, "optionBody" to ":%s/foo/bar/g", "optionDescription" to "Substitute 'foo' with 'bar' globally")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 25,
//                "questionBody" to "Which command deletes the current line and places it in the default register?",
//                "difficulty" to "EASY",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to "dd", "optionDescription" to "Delete the current line"),
//                    mapOf("optionId" to 2, "optionBody" to "D", "optionDescription" to "Delete from the cursor to the end of the line"),
//                    mapOf("optionId" to 3, "optionBody" to "dw", "optionDescription" to "Delete the word under the cursor"),
//                    mapOf("optionId" to 4, "optionBody" to "diw", "optionDescription" to "Delete the inner word")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 26,
//                "questionBody" to "What command would you use to write changes to the file and exit Vim?",
//                "difficulty" to "EASY",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to ":wq", "optionDescription" to "Write changes and quit"),
//                    mapOf("optionId" to 2, "optionBody" to ":q", "optionDescription" to "Quit without writing"),
//                    mapOf("optionId" to 3, "optionBody" to ":x", "optionDescription" to "Write changes and quit"),
//                    mapOf("optionId" to 4, "optionBody" to ":w", "optionDescription" to "Write changes but stay in Vim")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 27,
//                "questionBody" to "Which command deletes all lines in the file?",
//                "difficulty" to "HARD",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to ":%d", "optionDescription" to "Delete all lines"),
//                    mapOf("optionId" to 2, "optionBody" to ":d*", "optionDescription" to "Invalid command"),
//                    mapOf("optionId" to 3, "optionBody" to ":deleteall", "optionDescription" to "Invalid command"),
//                    mapOf("optionId" to 4, "optionBody" to "dG", "optionDescription" to "Delete from the cursor to the end of the file")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 28,
//                "questionBody" to "What command allows you to paste the contents of register 'a'?",
//                "difficulty" to "MEDIUM",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to "\"ap", "optionDescription" to "Paste the contents of register 'a' after the cursor"),
//                    mapOf("optionId" to 2, "optionBody" to "\"aP", "optionDescription" to "Paste the contents of register 'a' before the cursor"),
//                    mapOf("optionId" to 3, "optionBody" to "\"ay", "optionDescription" to "Yank into register 'a'"),
//                    mapOf("optionId" to 4, "optionBody" to ":put a", "optionDescription" to "Put the contents of register 'a' on a new line")
//                ),
//                "correctOptionId" to 1
//            ),
//            mapOf(
//                "questionId" to 29,
//                "questionBody" to "What command in Vim deletes from the cursor to the first occurrence of 'x' on the current line?",
//                "difficulty" to "MEDIUM",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to "d/x", "optionDescription" to "Delete until the first occurrence of 'x'"),
//                    mapOf("optionId" to 2, "optionBody" to "dtx", "optionDescription" to "Delete up to the character 'x'"),
//                    mapOf("optionId" to 3, "optionBody" to "dfx", "optionDescription" to "Delete up to and including the character 'x'"),
//                    mapOf("optionId" to 4, "optionBody" to "d+x", "optionDescription" to "Invalid command")
//                ),
//                "correctOptionId" to 3
//            ),
//            mapOf(
//                "questionId" to 30,
//                "questionBody" to "Which command in Vim toggles the case of the character under the cursor?",
//                "difficulty" to "MEDIUM",
//                "options" to listOf(
//                    mapOf("optionId" to 1, "optionBody" to "g~", "optionDescription" to "Toggle the case of the character under the cursor"),
//                    mapOf("optionId" to 2, "optionBody" to "~", "optionDescription" to "Toggle the case of the character and move to the next one"),
//                    mapOf("optionId" to 3, "optionBody" to "gu", "optionDescription" to "Make the character under the cursor lowercase"),
//                    mapOf("optionId" to 4, "optionBody" to "gU", "optionDescription" to "Make the character under the cursor uppercase")
//                ),
//                "correctOptionId" to 1
//            ),
            mapOf(
                "questionId" to 31,
                "questionBody" to "What command in Vim allows you to join multiple lines into a single line without adding spaces?",
                "difficulty" to "MEDIUM",
                "options" to listOf(
                    mapOf("optionId" to 1, "optionBody" to "gJ", "optionDescription" to "Join lines without adding spaces"),
                    mapOf("optionId" to 2, "optionBody" to "J", "optionDescription" to "Join lines and add spaces"),
                    mapOf("optionId" to 3, "optionBody" to "gj", "optionDescription" to "Move down one display line"),
                    mapOf("optionId" to 4, "optionBody" to ":join", "optionDescription" to "Join lines (default adds spaces)")
                ),
                "correctOptionId" to 1
            ),
            mapOf(
                "questionId" to 32,
                "questionBody" to "Which command would you use to visually select text until the end of the file?",
                "difficulty" to "MEDIUM",
                "options" to listOf(
                    mapOf("optionId" to 1, "optionBody" to "VG", "optionDescription" to "Visually select until the end of the file"),
                    mapOf("optionId" to 2, "optionBody" to "V$", "optionDescription" to "Visually select until the end of the line"),
                    mapOf("optionId" to 3, "optionBody" to "gG", "optionDescription" to "Move to the end of the file"),
                    mapOf("optionId" to 4, "optionBody" to "vG", "optionDescription" to "Enter visual mode until the end of the file")
                ),
                "correctOptionId" to 1
            ),
            mapOf(
                "questionId" to 33,
                "questionBody" to "What Vim command would you use to delete a line without copying it into a register?",
                "difficulty" to "HARD",
                "options" to listOf(
                    mapOf("optionId" to 1, "optionBody" to "\"_dd", "optionDescription" to "Delete line into the black hole register"),
                    mapOf("optionId" to 2, "optionBody" to "dd", "optionDescription" to "Delete line into the default register"),
                    mapOf("optionId" to 3, "optionBody" to "\"+dd", "optionDescription" to "Delete line into the system clipboard"),
                    mapOf("optionId" to 4, "optionBody" to "d$", "optionDescription" to "Delete from cursor to the end of the line")
                ),
                "correctOptionId" to 1
            ),
            mapOf(
                "questionId" to 34,
                "questionBody" to "Which command in Vim allows you to open a file in read-only mode?",
                "difficulty" to "MEDIUM",
                "options" to listOf(
                    mapOf("optionId" to 1, "optionBody" to "vim -R filename", "optionDescription" to "Open file in read-only mode"),
                    mapOf("optionId" to 2, "optionBody" to ":view filename", "optionDescription" to "Open file in read-only mode"),
                    mapOf("optionId" to 3, "optionBody" to ":readonly filename", "optionDescription" to "Invalid command"),
                    mapOf("optionId" to 4, "optionBody" to ":ro filename", "optionDescription" to "Invalid command")
                ),
                "correctOptionId" to 1
            ),
            mapOf(
                "questionId" to 35,
                "questionBody" to "What command in Vim searches for the next occurrence of the last search term?",
                "difficulty" to "EASY",
                "options" to listOf(
                    mapOf("optionId" to 1, "optionBody" to "n", "optionDescription" to "Search for the next occurrence of the last search term"),
                    mapOf("optionId" to 2, "optionBody" to "N", "optionDescription" to "Search for the previous occurrence of the last search term"),
                    mapOf("optionId" to 3, "optionBody" to "/", "optionDescription" to "Search forward for a new term"),
                    mapOf("optionId" to 4, "optionBody" to "?", "optionDescription" to "Search backward for a new term")
                ),
                "correctOptionId" to 1
            ),
            mapOf(
                "questionId" to 36,
                "questionBody" to "Which command in Vim lets you delete all blank lines in the current file?",
                "difficulty" to "HARD",
                "options" to listOf(
                    mapOf("optionId" to 1, "optionBody" to "g/^$/d", "optionDescription" to "Delete all blank lines in the file"),
                    mapOf("optionId" to 2, "optionBody" to ":%d", "optionDescription" to "Delete all lines in the file"),
                    mapOf("optionId" to 3, "optionBody" to ":d$", "optionDescription" to "Delete from the cursor to the end of the line"),
                    mapOf("optionId" to 4, "optionBody" to "ggdG", "optionDescription" to "Delete all lines in the file")
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


