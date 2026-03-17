package com.example.foodhunter.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhunter.db.HistoryStorage
import com.example.foodhunter.model.Dish
import com.example.foodhunter.repo.DishRepo
import dagger.hilt.android.lifecycle.HiltViewModel
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

    // текст в строке поиска
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // состояние экрана поиска
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Empty)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    // состояние экрана деталей
    private val _detailState = MutableStateFlow<DetailState>(DetailState.Loading)
    val detailState: StateFlow<DetailState> = _detailState.asStateFlow()

    // история просмотров из Room - подписываемся через stateIn
    val history: StateFlow<List<Dish>> = historyStorage.watchHistory()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun changeQuery(text: String) {
        _query.value = text
    }

    // запускаем поиск по кнопке
    fun doSearch() {
        val q = _query.value.trim()
        if (q.isEmpty()) {
            _searchState.value = SearchState.Failure("Введите что-нибудь для поиска")
            return
        }

        _searchState.value = SearchState.Loading
        viewModelScope.launch {
            try {
                val results = dishRepo.search(q)
                _searchState.value = if (results.isEmpty()) {
                    SearchState.Failure("Ничего не нашлось по запросу \"$q\"")
                } else {
                    SearchState.Found(results)
                }
            } catch (e: Exception) {
                _searchState.value = SearchState.Failure("Ошибка сети: ${e.localizedMessage}")
            }
        }
    }

    // загружаем детали и пишем в историю
    fun openDish(dish: Dish) {
        _detailState.value = DetailState.Loading
        viewModelScope.launch {
            // сохраняем в историю просмотров
            historyStorage.recordView(dish)
            try {
                val details = dishRepo.loadDetails(dish.id)
                _detailState.value = if (details != null) {
                    DetailState.Ready(details)
                } else {
                    DetailState.Failure("Блюдо куда-то пропало")
                }
            } catch (e: Exception) {
                _detailState.value = DetailState.Failure("Не получилось загрузить: ${e.localizedMessage}")
            }
        }
    }

    // загрузка по id (когда переходим через навигацию)
    fun openDishById(id: String) {
        _detailState.value = DetailState.Loading
        viewModelScope.launch {
            try {
                val details = dishRepo.loadDetails(id)
                if (details != null) {
                    // раз открыли - запишем в историю
                    historyStorage.recordView(
                        Dish(id = details.id, name = details.name, thumb = details.thumb, category = details.category)
                    )
                    _detailState.value = DetailState.Ready(details)
                } else {
                    _detailState.value = DetailState.Failure("Блюдо не найдено")
                }
            } catch (e: Exception) {
                _detailState.value = DetailState.Failure("Не получилось загрузить: ${e.localizedMessage}")
            }
        }
    }

    // удаляем из истории
    fun removeFromHistory(id: String) {
        viewModelScope.launch {
            historyStorage.deleteOne(id)
        }
    }

    // полная очистка истории
    fun wipeHistory() {
        viewModelScope.launch {
            historyStorage.clearAll()
        }
    }
}
