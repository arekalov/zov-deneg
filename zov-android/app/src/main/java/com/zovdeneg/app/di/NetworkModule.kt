package com.zovdeneg.app.di

import com.zovdeneg.app.BuildConfig
import com.zovdeneg.app.data.remote.ZovHttpClientFactory
import com.zovdeneg.app.data.remote.ZovJson
import com.zovdeneg.app.data.remote.mock.zovMockEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = ZovJson

    @Provides
    @Singleton
    @ZovMockHttpEngine
    fun provideMockEngine(): HttpClientEngine = zovMockEngine()

    @Provides
    @Singleton
    @ZovOkHttpEngine
    fun provideOkHttpEngine(): HttpClientEngine = OkHttp.create { }

    @Provides
    @Singleton
    fun provideHttpClientEngine(
        @ZovMockHttpEngine mockEngine: HttpClientEngine,
        @ZovOkHttpEngine okHttpEngine: HttpClientEngine,
    ): HttpClientEngine =
        if (BuildConfig.USE_MOCK_HTTP_ENGINE) {
            mockEngine
        } else {
            okHttpEngine
        }

    @Provides
    @Singleton
    fun provideHttpClient(
        engine: HttpClientEngine,
        json: Json,
    ): HttpClient = ZovHttpClientFactory.create(engine, json, BuildConfig.API_BASE_URL)
}
