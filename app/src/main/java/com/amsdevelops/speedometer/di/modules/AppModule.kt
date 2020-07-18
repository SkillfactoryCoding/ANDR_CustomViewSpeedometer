package com.amsdevelops.speedometer.di.modules

import android.app.Application
import com.amsdevelops.speedometer.data.Repository
import com.amsdevelops.speedometer.domain.Interactor
import com.amsdevelops.speedometer.presentation.viewmodel.AutoDisposable
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {
    @Singleton
    @Provides
    fun provideInteractor(repository: Repository) = Interactor(repository)

    @Singleton
    @Provides
    fun provideAutoDisposable() = AutoDisposable()
}