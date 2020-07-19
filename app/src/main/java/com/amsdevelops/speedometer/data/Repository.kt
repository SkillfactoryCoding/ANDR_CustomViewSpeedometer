package com.amsdevelops.speedometer.data

import io.reactivex.subjects.BehaviorSubject

class Repository {
    private val _speedCacheMock = BehaviorSubject.create<Int>()

    val speedCacheMock: BehaviorSubject<Int>
        get() = _speedCacheMock
}