package com.example.foodhunter.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhunter.db.HistoryStorage
import com.example.foodhunter.model.Dish
import com.example.foodhunter.model.DishDetails
import com.example.foodhunter.repo.DishRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodViewModel @Inject constructor(
    private val dishRepo: DishRepo,
    private val historyStorage: HistoryStorage
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Empty)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    private val _detailState = MutableStateFlow<DetailState>(DetailState.Idle)
    val detailState: StateFlow<DetailState> = _detailState.asStateFlow()

    val history: StateFlow<List<Dish>> = historyStorage.watchHistory()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var currentDishId: String? = null
    private var searchJob: Job? = null
    private var detailJob: Job? = null
    private var searchRequestVersion = 0L
    private var detailRequestVersion = 0L

    fun changeQuery(text: String) {
        _query.value = text
    }

    fun doSearch() {
        val query = _query.value.trim()
        if (query.isEmpty()) {
            _searchState.value = SearchState.Failure("Введите название блюда")
            return
        }

        searchJob?.cancel()
        val requestVersion = ++searchRequestVersion
        _searchState.value = SearchState.Loading
        searchJob = viewModelScope.launch {
            try {
                val results = dishRepo.search(query)
                if (requestVersion != searchRequestVersion || query != _query.value.trim()) return@launch
                _searchState.value = if (results.isEmpty()) {
                    SearchState.Failure("По запросу \"$query\" ничего не найдено")
                } else {
                    SearchState.Found(results)
                }
            } catch (_: CancellationException) {
            } catch (e: Exception) {
                if (requestVersion != searchRequestVersion) return@launch
                _searchState.value = SearchState.Failure(networkErrorMessage())
            }
        }
    }

    fun openDishById(id: String) {
        currentDishId = id
        loadDish(id)
    }

    fun retryOpenDish() {
        currentDishId?.let(::loadDish)
    }

    fun showInvalidDishRequest() {
        currentDishId = null
        detailJob?.cancel()
        _detailState.value = DetailState.Failure("Не удалось открыть карточку блюда")
    }

    private fun loadDish(id: String) {
        detailJob?.cancel()
        val requestVersion = ++detailRequestVersion
        _detailState.value = DetailState.Loading
        detailJob = viewModelScope.launch {
            try {
                val details = dishRepo.loadDetails(id)
                if (requestVersion != detailRequestVersion || id != currentDishId) return@launch
                if (details != null) {
                    historyStorage.recordView(details.toDish())
                    _detailState.value = DetailState.Ready(details)
                } else {
                    _detailState.value = DetailState.Failure("Не удалось найти выбранное блюдо")
                }
            } catch (_: CancellationException) {
            } catch (e: Exception) {
                if (requestVersion != detailRequestVersion || id != currentDishId) return@launch
                _detailState.value = DetailState.Failure(loadDishErrorMessage())
            }
        }
    }

    fun removeFromHistory(id: String) {
        viewModelScope.launch {
            historyStorage.deleteOne(id)
        }
    }

    fun wipeHistory() {
        viewModelScope.launch {
            historyStorage.clearAll()
        }
    }

    private fun networkErrorMessage(): String =
        "Не удалось выполнить поиск. Проверьте подключение и повторите попытку."

    private fun loadDishErrorMessage(): String =
        "Не удалось загрузить данные блюда. Повторите попытку позже."

    private fun DishDetails.toDish(): Dish =
        Dish(
            id = id,
            name = name,
            thumb = thumb,
            category = category
        )
}
