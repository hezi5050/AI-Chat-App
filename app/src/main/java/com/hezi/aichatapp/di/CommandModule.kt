package com.hezi.aichatapp.di

import com.hezi.aichatapp.commands.ChatCommandHandler
import com.hezi.aichatapp.commands.ChatCommandParser
import com.hezi.aichatapp.commands.CommandHandler
import com.hezi.aichatapp.commands.CommandParser
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger module for providing command-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CommandModule {

    @Binds
    @Singleton
    abstract fun bindCommandParser(impl: ChatCommandParser): CommandParser

    @Binds
    @Singleton
    abstract fun bindCommandHandler(impl: ChatCommandHandler): CommandHandler
}

