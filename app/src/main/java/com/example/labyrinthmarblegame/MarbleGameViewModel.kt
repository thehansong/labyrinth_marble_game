package com.example.labyrinthmarblegame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MarbleGameViewModel(private val repository: MarbleGameRepository) : ViewModel() {

    // Private mutable state to modify scores inside ViewModel
    private val _highScores = MutableStateFlow<List<MarbleGameScore>>(emptyList())
    val highScores: StateFlow<List<MarbleGameScore>> = _highScores.asStateFlow() // Public Read-only

    init {
        loadHighScores()
    }

    private fun loadHighScores() {
        viewModelScope.launch {
            repository.topHighScores
                .map { it.sortedByDescending { score -> score.completionTime }.take(10) }
                .collect { scores ->
                    _highScores.value = scores // Correct way to update MutableStateFlow
                }
        }
    }

    fun insertScore(score: MarbleGameScore) {
        viewModelScope.launch {
            repository.insertScore(score)
            loadHighScores() // Refresh scores after inserting
        }
    }

    fun clearScores() {
        viewModelScope.launch {
            repository.clearScores()
            _highScores.value = emptyList() // Clear scores in UI
        }
    }
}
