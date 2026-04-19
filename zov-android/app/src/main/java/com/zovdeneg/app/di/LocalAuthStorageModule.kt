package com.zovdeneg.app.di

import com.zovdeneg.app.data.local.LocalAuthStorageImpl
import com.zovdeneg.app.domain.auth.LocalAuthStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class LocalAuthStorageModule {

    @Binds
    @Singleton
    abstract fun bindLocalAuthStorage(impl: LocalAuthStorageImpl): LocalAuthStorage
}
