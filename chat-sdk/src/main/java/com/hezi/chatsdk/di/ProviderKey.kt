package com.hezi.chatsdk.di

import com.hezi.chatsdk.core.config.Provider
import dagger.MapKey

@MapKey
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ProviderKey(val value: Provider)