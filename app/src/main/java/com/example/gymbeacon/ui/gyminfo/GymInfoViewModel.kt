package com.example.gymbeacon.ui.gyminfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbeacon.model.GymInfo
import com.example.gymbeacon.repository.GymInfoRepository
import kotlinx.coroutines.launch

class GymInfoViewModel(
    private val gymInfoRepository: GymInfoRepository
): ViewModel() {

    private val _items = MutableLiveData<List<GymInfo>>()
    val items : LiveData<List<GymInfo>> = _items

    init {
        loadGymInfo()
    }

    private fun loadGymInfo() {
        viewModelScope.launch {
            val gyminfo = gymInfoRepository.getGyminfo()
            _items.value = gyminfo
        }
    }
}