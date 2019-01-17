package com.example.jdeiv.picoftheday

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.DataSource
import io.reactivex.disposables.CompositeDisposable

class PicOfTheDayDataSourceFactory(private val compositeDisposable: CompositeDisposable, private val location: FetchedLocation)
    : DataSource.Factory<String, Polaroid>() {
    val picOfTheDayDataSourceLiveData = MutableLiveData<PicOfTheDayDataSource>()

    override fun create(): DataSource<String, Polaroid> {
        val picOfTheDayDataSource = PicOfTheDayDataSource(compositeDisposable, location)
        picOfTheDayDataSourceLiveData.postValue(picOfTheDayDataSource)
        return picOfTheDayDataSource
    }
}