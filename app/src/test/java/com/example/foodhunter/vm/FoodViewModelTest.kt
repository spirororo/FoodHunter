package com.example.foodhunter.vm

import com.example.foodhunter.db.HistoryDao
import com.example.foodhunter.db.HistoryItem
import com.example.foodhunter.db.HistoryStorage
import com.example.foodhunter.model.Dish
import com.example.foodhunter.net.MealDbSearchResult
import com.example.foodhunter.net.MealDbService
import com.example.foodhunter.net.RawMeal
import com.example.foodhunter.repo.DishRepo
import com.example.foodhunter.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class FoodViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialState_isEmptyLoadingAndBlankQuery() {
        val viewModel = createViewModel()

        assertEquals("", viewModel.query.value)
        assertEquals(SearchState.Empty, viewModel.searchState.value)
        assertEquals(DetailState.Idle, viewModel.detailState.value)
    }

    @Test
    fun doSearch_withBlankQuery_setsFailureWithoutCallingApi() {
        val mealDbService = FakeMealDbService()
        val viewModel = createViewModel(mealDbService = mealDbService)

        viewModel.doSearch()

        assertTrue(viewModel.searchState.value is SearchState.Failure)
        assertEquals("Введите название блюда", (viewModel.searchState.value as SearchState.Failure).msg)
        assertEquals(emptyList<String>(), mealDbService.searchRequests)
    }

    @Test
    fun doSearch_emitsEmptyLoadingFound() = runTest {
        val mealDbService = FakeMealDbService().apply {
            searchHandler = { query ->
                MealDbSearchResult(
                    meals = listOf(
                        rawMeal(id = "1", name = "Chicken Soup", category = "Soup")
                    )
                )
            }
        }
        val viewModel = createViewModel(mealDbService = mealDbService)
        val emissions = mutableListOf<SearchState>()

        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.searchState.take(3).toList(emissions)
        }

        viewModel.changeQuery("chicken")
        viewModel.doSearch()
        collectJob.join()

        assertEquals(
            listOf(
                SearchState.Empty,
                SearchState.Loading,
                SearchState.Found(
                    listOf(Dish(id = "1", name = "Chicken Soup", thumb = "", category = "Soup"))
                )
            ),
            emissions
        )
        assertEquals(listOf("chicken"), mealDbService.searchRequests)
    }

    @Test
    fun doSearch_emitsEmptyLoadingFailureWhenApiThrows() = runTest {
        val mealDbService = FakeMealDbService().apply {
            searchHandler = {
                throw IllegalStateException("network down")
            }
        }
        val viewModel = createViewModel(mealDbService = mealDbService)
        val emissions = mutableListOf<SearchState>()

        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.searchState.take(3).toList(emissions)
        }

        viewModel.changeQuery("pasta")
        viewModel.doSearch()
        collectJob.join()

        assertEquals(SearchState.Empty, emissions[0])
        assertEquals(SearchState.Loading, emissions[1])
        assertEquals(
            SearchState.Failure("Не удалось выполнить поиск. Проверьте подключение и повторите попытку."),
            emissions[2]
        )
    }

    @Test
    fun openDishById_returnsFailureWhenDishIsMissing() = runTest {
        val mealDbService = FakeMealDbService().apply {
            detailsHandler = {
                MealDbSearchResult(meals = emptyList())
            }
        }
        val viewModel = createViewModel(mealDbService = mealDbService)
        val emissions = mutableListOf<DetailState>()

        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.detailState.take(3).toList(emissions)
        }

        viewModel.openDishById("404")
        collectJob.join()

        assertEquals(
            listOf(
                DetailState.Idle,
                DetailState.Loading,
                DetailState.Failure("Не удалось найти выбранное блюдо")
            ),
            emissions
        )
        assertEquals(listOf("404"), mealDbService.detailRequests)
    }

    @Test
    fun retryOpenDish_afterFailure_emitsFailureLoadingReadyAndRecordsHistory() = runTest {
        val fakeHistoryDao = FakeHistoryDao()
        var attempt = 0
        val mealDbService = FakeMealDbService().apply {
            detailsHandler = { id ->
                attempt += 1
                if (attempt == 1) {
                    throw IllegalStateException("timeout")
                }
                MealDbSearchResult(
                    meals = listOf(
                        rawMeal(
                            id = id,
                            name = "Arrabiata",
                            category = "Pasta",
                            area = "Italian",
                            instructions = "Boil pasta",
                            ingredient1 = "Tomato",
                            measure1 = "2 pcs"
                        )
                    )
                )
            }
        }
        val viewModel = createViewModel(
            mealDbService = mealDbService,
            historyDao = fakeHistoryDao
        )
        val firstAttemptEmissions = mutableListOf<DetailState>()
        val retryEmissions = mutableListOf<DetailState>()

        val firstAttemptJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.detailState.take(3).toList(firstAttemptEmissions)
        }

        viewModel.openDishById("10")
        firstAttemptJob.join()

        val retryJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.detailState.take(3).toList(retryEmissions)
        }

        viewModel.retryOpenDish()
        retryJob.join()

        assertEquals(
            listOf(
                DetailState.Idle,
                DetailState.Loading,
                DetailState.Failure("Не удалось загрузить данные блюда. Повторите попытку позже.")
            ),
            firstAttemptEmissions
        )
        assertEquals(
            listOf(
                DetailState.Failure("Не удалось загрузить данные блюда. Повторите попытку позже."),
                DetailState.Loading,
                DetailState.Ready(createExpectedDetails(id = "10"))
            ),
            retryEmissions
        )
        assertEquals(1, fakeHistoryDao.items.value.size)
        assertEquals("10", fakeHistoryDao.items.value.single().dishId)
        assertEquals(listOf("10", "10"), mealDbService.detailRequests)
    }

    @Test
    fun doSearch_keepsLatestResultWhenPreviousRequestFinishesLater() {
        val firstRequestStarted = CountDownLatch(1)
        val releaseFirstRequest = CountDownLatch(1)
        val mealDbService = FakeMealDbService().apply {
            searchHandler = { query ->
                when (query) {
                    "first" -> {
                        firstRequestStarted.countDown()
                        releaseFirstRequest.await(1, TimeUnit.SECONDS)
                        MealDbSearchResult(
                            meals = listOf(rawMeal(id = "1", name = "Old Soup", category = "Soup"))
                        )
                    }

                    else -> {
                        MealDbSearchResult(
                            meals = listOf(rawMeal(id = "2", name = "Fresh Salad", category = "Salad"))
                        )
                    }
                }
            }
        }
        val viewModel = createViewModel(mealDbService = mealDbService)
        val latestResult = SearchState.Found(
            listOf(Dish(id = "2", name = "Fresh Salad", thumb = "", category = "Salad"))
        )

        viewModel.changeQuery("first")
        viewModel.doSearch()
        assertTrue(firstRequestStarted.await(1, TimeUnit.SECONDS))

        viewModel.changeQuery("second")
        viewModel.doSearch()
        assertSearchStateEventually(viewModel, latestResult)

        releaseFirstRequest.countDown()
        waitForBackgroundWork()

        assertEquals(latestResult, viewModel.searchState.value)
    }

    @Test
    fun openDishById_keepsLatestDishWhenPreviousRequestFinishesLater() {
        val firstRequestStarted = CountDownLatch(1)
        val releaseFirstRequest = CountDownLatch(1)
        val fakeHistoryDao = FakeHistoryDao()
        val mealDbService = FakeMealDbService().apply {
            detailsHandler = { id ->
                when (id) {
                    "1" -> {
                        firstRequestStarted.countDown()
                        releaseFirstRequest.await(1, TimeUnit.SECONDS)
                        MealDbSearchResult(
                            meals = listOf(
                                rawMeal(
                                    id = "1",
                                    name = "First Dish",
                                    category = "Soup",
                                    area = "Thai",
                                    instructions = "Simmer slowly",
                                    ingredient1 = "Broth",
                                    measure1 = "1 l"
                                )
                            )
                        )
                    }

                    else -> {
                        MealDbSearchResult(
                            meals = listOf(
                                rawMeal(
                                    id = "2",
                                    name = "Second Dish",
                                    category = "Pasta",
                                    area = "Italian",
                                    instructions = "Boil pasta",
                                    ingredient1 = "Tomato",
                                    measure1 = "2 pcs"
                                )
                            )
                        )
                    }
                }
            }
        }
        val viewModel = createViewModel(
            mealDbService = mealDbService,
            historyDao = fakeHistoryDao
        )
        val latestDetails = createExpectedDetails(id = "2").copy(name = "Second Dish")

        viewModel.openDishById("1")
        assertTrue(firstRequestStarted.await(1, TimeUnit.SECONDS))

        viewModel.openDishById("2")
        assertDetailStateEventually(viewModel, DetailState.Ready(latestDetails))

        releaseFirstRequest.countDown()
        waitForBackgroundWork()

        assertEquals(DetailState.Ready(latestDetails), viewModel.detailState.value)
        assertEquals(listOf("2"), fakeHistoryDao.items.value.map { it.dishId })
    }

    private fun createViewModel(
        mealDbService: FakeMealDbService = FakeMealDbService(),
        historyDao: FakeHistoryDao = FakeHistoryDao()
    ): FoodViewModel {
        return FoodViewModel(
            dishRepo = DishRepo(mealDbService),
            historyStorage = HistoryStorage(historyDao)
        )
    }

    private fun createExpectedDetails(id: String) =
        com.example.foodhunter.model.DishDetails(
            id = id,
            name = "Arrabiata",
            thumb = "",
            category = "Pasta",
            area = "Italian",
            instructions = "Boil pasta",
            youtube = null,
            ingredients = listOf(
                com.example.foodhunter.model.IngredientLine("Tomato", "2 pcs")
            )
        )

    private fun assertSearchStateEventually(viewModel: FoodViewModel, expectedState: SearchState) {
        repeat(100) {
            if (viewModel.searchState.value == expectedState) return
            Thread.sleep(10)
        }
        fail("Expected search state $expectedState, but was ${viewModel.searchState.value}")
    }

    private fun assertDetailStateEventually(viewModel: FoodViewModel, expectedState: DetailState) {
        repeat(100) {
            if (viewModel.detailState.value == expectedState) return
            Thread.sleep(10)
        }
        fail("Expected detail state $expectedState, but was ${viewModel.detailState.value}")
    }

    private fun waitForBackgroundWork() {
        Thread.sleep(100)
    }

    private class FakeMealDbService : MealDbService {
        val searchRequests = mutableListOf<String>()
        val detailRequests = mutableListOf<String>()
        var searchHandler: suspend (String) -> MealDbSearchResult = { MealDbSearchResult(emptyList()) }
        var detailsHandler: suspend (String) -> MealDbSearchResult = { MealDbSearchResult(emptyList()) }

        override suspend fun findByName(name: String): MealDbSearchResult {
            searchRequests += name
            return searchHandler(name)
        }

        override suspend fun getById(id: String): MealDbSearchResult {
            detailRequests += id
            return detailsHandler(id)
        }
    }

    private class FakeHistoryDao : HistoryDao {
        val items = MutableStateFlow<List<HistoryItem>>(emptyList())

        override fun observeAll(): Flow<List<HistoryItem>> = items

        override suspend fun upsert(item: HistoryItem) {
            items.value = (items.value - items.value.filter { it.dishId == item.dishId }.toSet()) + item
        }

        override suspend fun deleteByDishId(id: String) {
            items.value = items.value.filterNot { it.dishId == id }
        }

        override suspend fun clear() {
            items.value = emptyList()
        }
    }

    private fun rawMeal(
        id: String,
        name: String,
        category: String? = null,
        area: String? = null,
        instructions: String? = null,
        ingredient1: String? = null,
        measure1: String? = null
    ) = RawMeal(
        idMeal = id,
        strMeal = name,
        strCategory = category,
        strArea = area,
        strInstructions = instructions,
        strMealThumb = null,
        strYoutube = null,
        strIngredient1 = ingredient1,
        strMeasure1 = measure1,
        strIngredient2 = null,
        strMeasure2 = null,
        strIngredient3 = null,
        strMeasure3 = null,
        strIngredient4 = null,
        strMeasure4 = null,
        strIngredient5 = null,
        strMeasure5 = null,
        strIngredient6 = null,
        strMeasure6 = null,
        strIngredient7 = null,
        strMeasure7 = null,
        strIngredient8 = null,
        strMeasure8 = null,
        strIngredient9 = null,
        strMeasure9 = null,
        strIngredient10 = null,
        strMeasure10 = null,
        strIngredient11 = null,
        strMeasure11 = null,
        strIngredient12 = null,
        strMeasure12 = null,
        strIngredient13 = null,
        strMeasure13 = null,
        strIngredient14 = null,
        strMeasure14 = null,
        strIngredient15 = null,
        strMeasure15 = null,
        strIngredient16 = null,
        strMeasure16 = null,
        strIngredient17 = null,
        strMeasure17 = null,
        strIngredient18 = null,
        strMeasure18 = null,
        strIngredient19 = null,
        strMeasure19 = null,
        strIngredient20 = null,
        strMeasure20 = null
    )
}
