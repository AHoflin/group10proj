package com.example.jdeiv.picoftheday

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import io.reactivex.disposables.CompositeDisposable


class PicOfTheDayViewModel : ViewModel() {
    private lateinit var location: FetchedLocation
    lateinit var picOfTheDayList: LiveData<PagedList<Polaroid>>
    private val pageSize = 5
    private val compositeDisposable = CompositeDisposable()
    private lateinit var picOfTheDayDataSourceFactory: PicOfTheDayDataSourceFactory

    init {
    }

    fun sketchyLateinitConstructorPlsDontJudgeMe(location: FetchedLocation) {
        picOfTheDayDataSourceFactory = PicOfTheDayDataSourceFactory(compositeDisposable, location)
        val config = PagedList.Config.Builder()
            .setPageSize(pageSize)
            .setInitialLoadSizeHint(pageSize).setPrefetchDistance(2)
            .setEnablePlaceholders(false)
            .build()
        picOfTheDayList = LivePagedListBuilder<String, Polaroid>(picOfTheDayDataSourceFactory, config).build()
    }

    fun getState(): LiveData<State> = Transformations.switchMap<PicOfTheDayDataSource,
            State>(picOfTheDayDataSourceFactory.picOfTheDayDataSourceLiveData, PicOfTheDayDataSource::state)

    fun retry() {
        picOfTheDayDataSourceFactory.picOfTheDayDataSourceLiveData.value?.retry()
    }

    fun listIsEmpty(): Boolean {
        return picOfTheDayList.value?.isEmpty() ?: true
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
