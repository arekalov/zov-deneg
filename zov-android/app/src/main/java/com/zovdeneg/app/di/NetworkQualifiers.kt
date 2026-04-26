package com.zovdeneg.app.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ZovMockHttpEngine

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ZovOkHttpEngine

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ZovSecuritiesHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ZovPlainHttpClient
