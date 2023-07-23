package com.dicoding.dicodingstoryapplication.view.liststory

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.dicoding.dicodingstoryapplication.adapter.StoryAdapter
import com.dicoding.dicodingstoryapplication.api.response.ListStoryItem
import com.dicoding.dicodingstoryapplication.data.StoryRepository
import com.dicoding.dicodingstoryapplication.utils.DataDummy
import com.dicoding.dicodingstoryapplication.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ListStoryViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var listStoryViewModel: ListStoryViewModel

    @Mock
    private lateinit var storyRepository: StoryRepository
    private val dummyStories = DataDummy.generateDummyStoryEntity(10)
    private val dummyEmptyStories = DataDummy.generateDummyStoryEntity(0)
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        listStoryViewModel = ListStoryViewModel(null, storyRepository)
        Dispatchers.setMain(testDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Test if get stories successfully loaded
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when Get All Stories Success`() = runTest{

        val expectedStory = MutableLiveData<PagingData<ListStoryItem>>()
        expectedStory.value = PagingData.from(dummyStories)

        `when`(storyRepository.getStory()).thenReturn(expectedStory)

        val actualStory = listStoryViewModel.getAllStories().getOrAwaitValue()
        Mockito.verify(storyRepository).getStory()

        // Convert PagingData Stream
        val diff = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = updateCallback,
            mainDispatcher = testDispatcher,
            workerDispatcher = testDispatcher
        )
        diff.submitData(actualStory)

        // Check if data not null
        Assert.assertNotNull(actualStory)

        // Check if size match
        Assert.assertEquals(dummyStories.size, diff.snapshot().size)

        // Check if the first data match
        Assert.assertEquals(dummyStories[0], diff.snapshot()[0])
    }

    // Test if there are no stories data
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when There Are No Stories`() = runTest{

        val expectedStory = MutableLiveData<PagingData<ListStoryItem>>()
        expectedStory.value = PagingData.from(dummyEmptyStories)

        `when`(storyRepository.getStory()).thenReturn(expectedStory)

        val actualStory = listStoryViewModel.getAllStories().getOrAwaitValue()
        Mockito.verify(storyRepository).getStory()

        // Convert PagingData Stream
        val diff = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = updateCallback,
            mainDispatcher = testDispatcher,
            workerDispatcher = testDispatcher
        )
        diff.submitData(actualStory)

        // Check if data size is 0
        Assert.assertEquals(0, diff.snapshot().size)
    }

    // list update callback for PagingData size
    private val updateCallback = object : ListUpdateCallback {
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
        override fun onInserted(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
    }
}