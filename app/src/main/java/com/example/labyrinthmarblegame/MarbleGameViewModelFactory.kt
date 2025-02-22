package com.example.labyrinthmarblegame
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MarbleGameViewModelFactory(private val repository: MarbleGameRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarbleGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MarbleGameViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
