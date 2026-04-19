package com.zovdeneg.app.di

import com.zovdeneg.app.domain.auth.LocalAuthStorage
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ZovLocalAuthEntryPoint {
    fun localAuthStorage(): LocalAuthStorage
}
