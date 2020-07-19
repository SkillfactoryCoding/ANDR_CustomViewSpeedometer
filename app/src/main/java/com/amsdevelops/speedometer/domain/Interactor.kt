package com.amsdevelops.speedometer.domain

import com.amsdevelops.speedometer.data.Repository
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class Interactor(val repository: Repository) {

    fun emitTestSpeedValues() {
        val testSource = Observable.fromIterable(mockSpeedArray())
            .subscribeOn(Schedulers.single())
            .subscribeBy(
                onError = {
                    Timber.e(it.localizedMessage)
                },
                onNext = {
                    val randomDelay = (1 .. 2000).random().toLong()
                    Thread.sleep(randomDelay)
                    repository.speedCacheMock.onNext(it)
                }
            )

    }

    private fun mockSpeedArray(): Iterable<Int> {
        val list = mutableListOf<Int>()
        repeat((0 .. 2).count()) {
            val randomSpeed = (0 .. 140).random()
            list.add(randomSpeed)
        }
        list.add(0)
        return list
    }

    fun changeSpeed(speed: Int) {
        repository.speedCacheMock.onNext(speed)
    }
}