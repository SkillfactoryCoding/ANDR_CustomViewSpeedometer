package com.amsdevelops.speedometer.domain

import com.amsdevelops.speedometer.data.Repository
import io.reactivex.Flowable
import timber.log.Timber

class Interactor(val repository: Repository) {

    init {
        Timber.e(this.toString())
        repository.speedCacheMock = Flowable.fromIterable(mockSpeedArray())
            .doOnNext {
                val randomDelay = (1 .. 2000).random().toLong()
                Thread.sleep(randomDelay)
            }
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
}