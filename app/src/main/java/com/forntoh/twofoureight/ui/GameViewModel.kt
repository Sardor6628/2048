package com.forntoh.twofoureight.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.forntoh.twofoureight.model.Game
import com.forntoh.twofoureight.store.PreferenceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class GameViewModel(
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    private val _playTimeInSecs = MutableStateFlow(preferenceRepository.timeElapsed)
    val playTimeInSecs: StateFlow<Long> = _playTimeInSecs.asStateFlow()

    private val _moves = MutableStateFlow(preferenceRepository.moves)
    val moves: StateFlow<Int> = _moves.asStateFlow()

    private val _score = MutableStateFlow(preferenceRepository.score)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _highScore = MutableStateFlow(preferenceRepository.highScore)
    val highScore: StateFlow<Int> = _highScore.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(preferenceRepository.isDarkTheme)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _game = MutableStateFlow(Game(4))
    val game: StateFlow<Game> = _game.asStateFlow()

    init {
        viewModelScope.launch {
            flow {
                while (true) {
                    emit(Unit)
                    delay(1.seconds)
                }
            }
                .map { preferenceRepository.paused }
                .distinctUntilChanged { _, new -> new }
                .onEach { preferenceRepository.timeElapsed = _playTimeInSecs.updateAndGet { prev -> prev + 1 } }
                .collect()
        }
        _game.update {
            Game(
                size = 4,
                onScoreChange = { score -> preferenceRepository.score = _score.updateAndGet { score } },
                onMove = {
                    preferenceRepository.moves = _moves.updateAndGet { it + 1 }
                    preferenceRepository.paused = false
                },
            )
        }
    }

    fun newGame() {
        preferenceRepository.moves = _moves.updateAndGet { 0 }
        preferenceRepository.score = _score.updateAndGet { 0 }
        preferenceRepository.timeElapsed = _playTimeInSecs.updateAndGet { 0 }
        _game.value.restart()
    }

}

class GameVieModelFactory(private val context: Context) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = GameViewModel(PreferenceRepository(context)) as T
}