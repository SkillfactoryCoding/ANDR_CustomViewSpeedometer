package com.amsdevelops.speedometer.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel
import com.amsdevelops.speedometer.App
import com.amsdevelops.speedometer.domain.Interactor
import io.reactivex.Flowable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class SpeedometerViewModel : ViewModel() {
    @Inject
    lateinit var interactor: Interactor

    private var _speedFlowable: Flowable<Int>
    val speedLiveData: Flowable<Int>
        get() = _speedFlowable

    init {
        App.instance.appComponent.inject(this)

        _speedFlowable = interactor.repository.speedCacheMock
    }
}