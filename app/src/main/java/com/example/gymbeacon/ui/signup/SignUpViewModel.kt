package com.example.gymbeacon.ui.signup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.LoginResult
import com.example.domain.model.SignUpResult
import com.example.domain.repository.UserRepository
import com.example.gymbeacon.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repository: UserRepository
): ViewModel(){
    val account = MutableLiveData<String>("")
    val password = MutableLiveData<String>("")

    private val _signUpState = SingleLiveEvent<SignUpResult>()
    val signUpState: SingleLiveEvent<SignUpResult> get() = _signUpState

    fun signUp() {
        viewModelScope.launch {
            repository.signUp(account.value!!, password.value!!){
                _signUpState.value = it
            }
        }

    }

}