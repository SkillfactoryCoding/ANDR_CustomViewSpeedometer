package com.amsdevelops.speedometer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.amsdevelops.speedometer.App
import com.amsdevelops.speedometer.domain.Interactor
import io.reactivex.Observable
import javax.inject.Inject

class SpeedometerViewModel : ViewModel() {
    @Inject
    lateinit var interactor: Interactor

    private var _speedData: Observable<Int>
    val speedData: Observable<Int>
        get() = _speedData

    init {
        App.instance.appComponent.inject(this)

        _speedData = interactor.repository.speedCacheMock
    }

    fun initTest() {
        interactor.emitTestSpeedValues()
    }

    fun changeSpeed(speed: Int) {
        interactor.changeSpeed(speed)
    }
}