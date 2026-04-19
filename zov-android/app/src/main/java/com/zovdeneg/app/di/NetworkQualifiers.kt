package com.zovdeneg.app.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ZovMockHttpEngine

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ZovOkHttpEngine
