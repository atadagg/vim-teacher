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

    init {
        fetchQuestions()
    }

    private fun fetchQuestions() {
        viewModelScope.launch {
            try {
                val questionsList = firebaseService.getQuestions()
                _allQuestionsLiveData.value = questionsList

                // Set the first question as the current question
                if (questionsList.isNotEmpty()) {
                    currentQuestionLiveData.value = questionsList.first()
                }
            } catch (e: Exception) {
                Log.e("QuestionsViewModel", "Error fetching questions from Firebase", e)
            }
        }
    }
    fun getQuestionById(id: Int) : QuestionModel? {
        return _allQuestionsLiveData.value?.find { it.questionId == id }
    }

    fun setQuestionById(id: Int) {
        currentQuestionLiveData.value = _allQuestionsLiveData.value?.find { it.questionId == id }
    }
}
