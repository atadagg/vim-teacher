package com.example.vimteacher.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vimteacher.repositories.FirebaseRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel(private val firebaseRepository: FirebaseRepository) : ViewModel() {
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            firebaseRepository.loginUser(email, password)
                .onSuccess { user ->
                    _authState.value = AuthState.Success(user)
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Login failed")
                }
        }
    }

    fun register(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            firebaseRepository.registerUser(email, password)
                .onSuccess { user ->
                    _authState.value = AuthState.Success(user)
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Registration failed")
                }
        }
    }



}

sealed class AuthState {
    data class Success(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
    object Loading : AuthState()
}

class AuthViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(FirebaseRepository()) as T  // Create FirebaseService here
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}