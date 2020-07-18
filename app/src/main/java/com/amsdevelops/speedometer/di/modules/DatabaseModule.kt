package com.amsdevelops.speedometer.di.modules

import com.amsdevelops.speedometer.data.Repository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideRepository() = Repository()
}