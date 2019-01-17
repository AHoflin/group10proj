package com.example.jdeiv.picoftheday

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import io.reactivex.disposables.CompositeDisposable


class PolaroidListViewModel : ViewModel() {
    private lateinit var location: FetchedLocation
    lateinit var polaroidList: LiveData<PagedList<Polaroid>>
    private val pageSize = 5
    private val compositeDisposable = CompositeDisposable()
    private lateinit var polaroidDataSourceFactory: PolaroidDataSourceFactory

    init {
    }

    fun sketchyLateinitConstructorPlsDontJudgeMe(location: FetchedLocation) {
        polaroidDataSourceFactory = PolaroidDataSourceFactory(compositeDisposable, location)
        val config = PagedList.Config.Builder()
            .setPageSize(pageSize)
            .setInitialLoadSizeHint(pageSize).setPrefetchDistance(2)
            .setEnablePlaceholders(false)
            .build()
        polaroidList = LivePagedListBuilder<String, Polaroid>(polaroidDataSourceFactory, config).build()
    }

    fun getState(): LiveData<State> = Transformations.switchMap<PolaroidDataSource,
            State>(polaroidDataSourceFactory.polaroidDataSourceLiveData, PolaroidDataSource::state)

    fun retry() {
        polaroidDataSourceFactory.polaroidDataSourceLiveData.value?.retry()
    }

    fun listIsEmpty(): Boolean {
        return polaroidList.value?.isEmpty() ?: true
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
