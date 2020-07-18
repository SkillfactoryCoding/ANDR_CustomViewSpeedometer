package com.amsdevelops.speedometer.di

import com.amsdevelops.speedometer.di.modules.AppModule
import com.amsdevelops.speedometer.di.modules.DatabaseModule
import com.amsdevelops.speedometer.presentation.view.MainActivity
import com.amsdevelops.speedometer.presentation.viewmodel.SpeedometerViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        DatabaseModule::class
    ]
)
interface AppComponent {
    fun inject(speedometerViewModel: SpeedometerViewModel)
    fun inject(mainActivity: MainActivity)
}