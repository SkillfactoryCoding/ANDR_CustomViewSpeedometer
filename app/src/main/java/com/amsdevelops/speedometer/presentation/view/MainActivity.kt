package com.amsdevelops.speedometer.presentation.view

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.amsdevelops.speedometer.App
import com.amsdevelops.speedometer.R
import com.amsdevelops.speedometer.extensions.addTo
import com.amsdevelops.speedometer.presentation.viewmodel.AutoDisposable
import com.amsdevelops.speedometer.presentation.viewmodel.SpeedometerViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import javax.inject.Inject


class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var autoDisposable: AutoDisposable

    private val viewModel: SpeedometerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        initFullScreen()

        App.instance.appComponent.inject(this)
        autoDisposable.bindTo(lifecycle)

        viewModel.speedData
            .subscribeOn(Schedulers.single())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onError = {
                    Timber.e(it.localizedMessage)
                },
                onNext = {
                    speedometer.setSpeedChanged(it.toFloat())
                }
            )
            .addTo(autoDisposable)

        button_increase.setOnClickListener {
            viewModel.changeSpeed((speedometer.getCurrentSpeed() + 10).toInt())
        }

        button_decrease.setOnClickListener {
            viewModel.changeSpeed((speedometer.getCurrentSpeed() - 10).toInt())
        }

        button_test.setOnClickListener {
            viewModel.initTest()
        }
    }

    private fun initFullScreen() {
        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.KEEP_SCREEN_ON)

        window.decorView.systemUiVisibility = flags

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val decorView = window.decorView
        decorView
            .setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    decorView.systemUiVisibility = flags
                }
            }

    }
}