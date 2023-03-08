package com.example.gymbeacon.ui.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbeacon.model.LowerBodyCategory
import com.example.gymbeacon.repository.lower.LowerBodyCategoryRepository
import kotlinx.coroutines.launch

class LowerBodyCategoryViewModel(
    private val lowerBodyCategoryRepository: LowerBodyCategoryRepository
): ViewModel() {
    private val _items = MutableLiveData<List<LowerBodyCategory>>()
    val items : LiveData<List<LowerBodyCategory>> = _items

    init {
        loadLowerBodyCategory()
    }

    private fun loadLowerBodyCategory() {
        viewModelScope.launch {
            val categories = lowerBodyCategoryRepository.getLowerCategories()
            _items.value = categories
        }
    }
}