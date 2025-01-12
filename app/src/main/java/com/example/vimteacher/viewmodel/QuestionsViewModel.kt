package com.example.vimteacher.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vimteacher.model.QuestionModel
import com.example.vimteacher.repositories.FirebaseRepository
import kotlinx.coroutines.launch

class QuestionsViewModel : ViewModel() {

    private val firebaseRepository = FirebaseRepository()
    private val _allQuestionsLiveData = MutableLiveData<List<QuestionModel>>()
    private val _solvedQuestions = MutableLiveData<Set<Int>>(setOf())
    val questions: LiveData<List<QuestionModel>> = _allQuestionsLiveData

    val currentQuestionLiveData = MutableLiveData<QuestionModel>()
    val explanations = MutableLiveData<List<String>>()
    val optionStatuses = MutableLiveData<Map<Int, String>>()
    val isAnswered = MutableLiveData<Boolean>().apply { value = false }
    val solvedQuestions: LiveData<Set<Int>> = _solvedQuestions


    private var currentIndex = 0

    init {
        fetchQuestions()
    }

    private fun fetchQuestions() {
        viewModelScope.launch {
            try {
                val questionsList = firebaseRepository.getQuestions()
                _allQuestionsLiveData.value = questionsList

            } catch (e: Exception) {
                Log.e("QuestionsViewModel", "Error fetching questions from Firebase", e)
            }
        }
    }

    fun setQuestionById(id: Int) {
        viewModelScope.launch {
            // Wait for questions to be loaded if they haven't been yet
            if (_allQuestionsLiveData.value == null) {
                try {
                    val questionsList = firebaseRepository.getQuestions()
                    _allQuestionsLiveData.value = questionsList
                } catch (e: Exception) {
                    Log.e("QuestionsViewModel", "Error fetching questions", e)
                    return@launch
                }
            }

            // Now find the question with matching ID
            currentIndex = _allQuestionsLiveData.value?.indexOfFirst { it.questionId == id } ?: return@launch
            Log.d("QuestionsViewModel", "Setting to question ID $id at index $currentIndex")
            updateCurrentQuestion()
        }
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
                    firebaseRepository.checkAndUpdateSolvedQuestion(questionId)
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

    fun observeSolvedQuestions(userId: String) {
        firebaseRepository.observeSolvedQuestions(userId) { solvedIds ->
            _solvedQuestions.value = solvedIds
        }
    }
}