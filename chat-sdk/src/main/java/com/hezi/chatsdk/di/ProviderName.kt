package com.hezi.chatsdk.di

import dagger.MapKey

@MapKey
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ProviderName(val value: String)