package com.example.labyrinthmarblegame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MarbleGameViewModel(private val repository: MarbleGameRepository) : ViewModel() {

    // Private mutable state to modify scores inside ViewModel
    private val _highScores = MutableStateFlow<List<MarbleGameScore>>(emptyList())
    val highScores: StateFlow<List<MarbleGameScore>> = _highScores.asStateFlow() // Public read-only

    // Mutable state for the player's name
    private val _playerName = MutableStateFlow("Player")
    val playerName: StateFlow<String> = _playerName.asStateFlow()

    init {
        loadHighScores()
    }

    private fun loadHighScores() {
        viewModelScope.launch {
            repository.topHighScores
                .map { it.sortedByDescending { score -> score.completionTime }.take(10) }
                .collect { scores ->
                    _highScores.value = scores // Update scores in UI
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

    // Update the player's name
    fun updatePlayerName(newName: String) {
        _playerName.value = newName
    }
}
