package com.example.jdeiv.picoftheday

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.DataSource
import io.reactivex.disposables.CompositeDisposable


class PolaroidDataSourceFactory(private val compositeDisposable: CompositeDisposable, private val location: FetchedLocation)
    : DataSource.Factory<String, Polaroid>() {
    val polaroidDataSourceLiveData = MutableLiveData<PolaroidDataSource>()

    override fun create(): DataSource<String, Polaroid> {
        val polaroidDataSource = PolaroidDataSource(compositeDisposable, location)
        polaroidDataSourceLiveData.postValue(polaroidDataSource)
        return polaroidDataSource
    }
}