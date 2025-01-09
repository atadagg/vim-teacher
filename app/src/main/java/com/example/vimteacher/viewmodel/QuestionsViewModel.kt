package com.example.vimteacher.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vimteacher.model.QuestionModel
import com.example.vimteacher.services.FirebaseService
import kotlinx.coroutines.launch

class QuestionsViewModel : ViewModel() {

    private val firebaseService = FirebaseService()
    private val _allQuestionsLiveData = MutableLiveData<List<QuestionModel>>()
    val questions: LiveData<List<QuestionModel>> = _allQuestionsLiveData

    val currentQuestionLiveData = MutableLiveData<QuestionModel>()
    val explanations = MutableLiveData<List<String>>()
    val optionStatuses = MutableLiveData<Map<Int, String>>() // Maps optionId to "Correct" or "Incorrect"
    val isAnswered = MutableLiveData<Boolean>().apply { value = false }

    private var currentIndex = 0 // Index to track the current question

    init {
        fetchQuestions()
    }

    private fun fetchQuestions() {
        viewModelScope.launch {
            try {
                val questionsList = firebaseService.getQuestions()
                _allQuestionsLiveData.value = questionsList

                if (questionsList.isNotEmpty()) {
                    currentIndex = 0
                    currentQuestionLiveData.value = questionsList[currentIndex]
                }
            } catch (e: Exception) {
                Log.e("QuestionsViewModel", "Error fetching questions from Firebase", e)
            }
        }
    }

    fun setQuestionById(id: Int) {
        currentIndex = _allQuestionsLiveData.value?.indexOfFirst { it.questionId == id } ?: 0
        updateCurrentQuestion()
    }

    fun checkAnswer(selectedOptionId: Int): Boolean {
        val currentQuestion = currentQuestionLiveData.value ?: return false

        val isCorrect = selectedOptionId == currentQuestion.correctOptionId

        val statuses = currentQuestion.options.associate { option ->
            option.optionId to if (option.optionId == currentQuestion.correctOptionId) {
                "Correct"
            } else {
                "Incorrect"
            }
        }

        optionStatuses.value = statuses
        explanations.value = currentQuestion.options.map { option ->
            "${option.optionBody}: ${option.optionDescription}"
        }
        isAnswered.value = true

        // If answer is correct, update the database
        if (isCorrect) {
            viewModelScope.launch {
                currentQuestion.questionId?.let { questionId ->
                    firebaseService.checkAndUpdateSolvedQuestion(questionId)
                        .onSuccess { isNewlySolved ->
                            Log.d("QuestionsViewModel", "Question solved status updated: $isNewlySolved")
                        }
                        .onFailure { exception ->
                            Log.e("QuestionsViewModel", "Failed to update solved status", exception)
                        }
                }
            }
        }

        return isCorrect
    }

    fun hasNextQuestion(): Boolean {
        val questionsList = _allQuestionsLiveData.value ?: return false
        return currentIndex < questionsList.size - 1
    }

    fun nextQuestion() {
        val questionsList = _allQuestionsLiveData.value ?: return

        if (currentIndex < questionsList.size - 1) {
            currentIndex++
            updateCurrentQuestion()
        }
    }

    private fun updateCurrentQuestion() {
        val questionsList = _allQuestionsLiveData.value ?: return
        if (currentIndex in questionsList.indices) {
            currentQuestionLiveData.value = questionsList[currentIndex]
            isAnswered.value = false
            optionStatuses.value = emptyMap()
            explanations.value = emptyList()
        }
    }
}