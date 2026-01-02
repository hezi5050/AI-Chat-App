package com.hezi.aichatapp.di

import com.hezi.aichatapp.data.ChatDiagnosticsRepository
import com.hezi.aichatapp.data.DiagnosticsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DiagnosticsModule {

    @Binds
    @Singleton
    abstract fun bindDiagnosticsRepository(
        impl: ChatDiagnosticsRepository
    ): DiagnosticsRepository
}

