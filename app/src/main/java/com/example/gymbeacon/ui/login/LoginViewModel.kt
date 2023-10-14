package com.example.gymbeacon.ui.login

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.LoginResult
import com.example.domain.repository.UserRepository
import com.example.gymbeacon.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UserRepository
): ViewModel() {
    val account = MutableLiveData<String>("")
    val password = MutableLiveData<String>("")

    private val _loginState = SingleLiveEvent<LoginResult>()
    val loginState: SingleLiveEvent<LoginResult> get() = _loginState

    fun login() {
        if (!TextUtils.isEmpty(account.value) && !TextUtils.isEmpty(password.value)) {
            viewModelScope.launch() {
                repository.postLogin(account.value!!, password.value!!) {
                    _loginState.value = it
                }
            }
        }
    }

}