package com.zovdeneg.app.di

import com.zovdeneg.app.BuildConfig
import com.zovdeneg.app.data.remote.ZovAuthTokenRefresher
import com.zovdeneg.app.data.remote.ZovBearerAuthInvalidator
import com.zovdeneg.app.data.remote.ZovBearerClientDeps
import com.zovdeneg.app.data.remote.ZovHttpClientFactory
import com.zovdeneg.app.data.remote.ZovJson
import com.zovdeneg.app.data.remote.ZovSessionTokens
import com.zovdeneg.app.data.remote.mock.ZovMockAssetJson
import com.zovdeneg.app.data.remote.mock.zovMockEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.serialization.json.Json
import okhttp3.Protocol
import java.util.concurrent.TimeUnit
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
    fun provideMockEngine(mockJson: ZovMockAssetJson): HttpClientEngine = zovMockEngine(mockJson)

    @Provides
    @Singleton
    @ZovOkHttpEngine
    fun provideOkHttpEngine(): HttpClientEngine =
        OkHttp.create {
            // Локальный Ktor + эмулятор: избегаем h2c/HTTP2 на cleartext и обрывов «unexpected end of stream».
            config {
                protocols(listOf(Protocol.HTTP_1_1))
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(60, TimeUnit.SECONDS)
                writeTimeout(60, TimeUnit.SECONDS)
                retryOnConnectionFailure(true)
            }
        }

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
    @ZovPlainHttpClient
    fun providePlainHttpClient(
        engine: HttpClientEngine,
        json: Json,
    ): HttpClient =
        ZovHttpClientFactory.createPlainClient(
            engine,
            json,
            BuildConfig.API_BASE_URL,
            BuildConfig.DEBUG,
        )

    @Provides
    @Singleton
    fun provideHttpClient(
        engine: HttpClientEngine,
        json: Json,
        sessionTokens: ZovSessionTokens,
        tokenRefresher: ZovAuthTokenRefresher,
        invalidator: ZovBearerAuthInvalidator,
    ): HttpClient =
        ZovHttpClientFactory.createAuthenticatedClient(
            engine,
            json,
            BuildConfig.API_BASE_URL,
            ZovBearerClientDeps(sessionTokens, tokenRefresher, invalidator),
            BuildConfig.DEBUG,
        )

    @Provides
    @Singleton
    @ZovSecuritiesHttpClient
    fun provideSecuritiesHttpClient(
        engine: HttpClientEngine,
        json: Json,
        sessionTokens: ZovSessionTokens,
        tokenRefresher: ZovAuthTokenRefresher,
        invalidator: ZovBearerAuthInvalidator,
    ): HttpClient =
        ZovHttpClientFactory.createAuthenticatedClient(
            engine,
            json,
            BuildConfig.SECURITIES_API_BASE_URL,
            ZovBearerClientDeps(sessionTokens, tokenRefresher, invalidator),
            BuildConfig.DEBUG,
        )
}
