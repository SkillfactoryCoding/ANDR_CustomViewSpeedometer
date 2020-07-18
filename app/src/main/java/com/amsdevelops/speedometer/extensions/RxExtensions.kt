package com.amsdevelops.speedometer.extensions

import com.amsdevelops.speedometer.presentation.viewmodel.AutoDisposable
import io.reactivex.disposables.Disposable

fun Disposable.addTo(autoDisposable: AutoDisposable) {
    autoDisposable.add(this)
}