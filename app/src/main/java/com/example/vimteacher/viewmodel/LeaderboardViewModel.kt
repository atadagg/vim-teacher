package com.example.vimteacher.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vimteacher.model.UserModel
import com.example.vimteacher.repositories.FirebaseRepository
import kotlinx.coroutines.launch

class LeaderboardViewModel : ViewModel() {
    private val firebaseRepository = FirebaseRepository()

    private val _leaderboardUsers = MutableLiveData<List<UserModel>>()
    val leaderboardUsers: LiveData<List<UserModel>> = _leaderboardUsers

    init {
        fetchLeaderboardUsers()
    }

    fun fetchLeaderboardUsers() {
        viewModelScope.launch {
            try {
                val usersList = firebaseRepository.getLeaderboardUsers()
                val sortedUsersList = usersList.sortedByDescending { it.questions_solved }

                // Limit to top 10 users
                val topUsers = sortedUsersList.take(10)

                _leaderboardUsers.value = topUsers
            } catch (e: Exception) {
                Log.e("LeaderboardViewModel", "Error fetching leaderboard users", e)
            }
        }
    }

    // Optional: Method to refresh leaderboard
    fun refreshLeaderboard() {
        fetchLeaderboardUsers()
    }
}