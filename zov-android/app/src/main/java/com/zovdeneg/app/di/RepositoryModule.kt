package com.zovdeneg.app.di

import com.zovdeneg.app.data.repository.AuthRepositoryImpl
import com.zovdeneg.app.data.repository.ClearRemoteSessionUseCaseImpl
import com.zovdeneg.app.data.repository.BalanceRepositoryImpl
import com.zovdeneg.app.data.repository.OrdersRepositoryImpl
import com.zovdeneg.app.data.repository.PortfolioRepositoryImpl
import com.zovdeneg.app.data.repository.SecuritiesRepositoryImpl
import com.zovdeneg.app.data.repository.TransactionsRepositoryImpl
import com.zovdeneg.app.data.repository.UserProfileRepositoryImpl
import com.zovdeneg.app.domain.auth.AuthRepository
import com.zovdeneg.app.domain.auth.ClearRemoteSessionUseCase
import com.zovdeneg.app.domain.balance.BalanceRepository
import com.zovdeneg.app.domain.market.SecuritiesRepository
import com.zovdeneg.app.domain.orders.OrdersRepository
import com.zovdeneg.app.domain.portfolio.PortfolioRepository
import com.zovdeneg.app.domain.profile.UserProfileRepository
import com.zovdeneg.app.domain.transactions.TransactionsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPortfolioRepository(impl: PortfolioRepositoryImpl): PortfolioRepository

    @Binds
    @Singleton
    abstract fun bindSecuritiesRepository(impl: SecuritiesRepositoryImpl): SecuritiesRepository

    @Binds
    @Singleton
    abstract fun bindTransactionsRepository(impl: TransactionsRepositoryImpl): TransactionsRepository

    @Binds
    @Singleton
    abstract fun bindOrdersRepository(impl: OrdersRepositoryImpl): OrdersRepository

    @Binds
    @Singleton
    abstract fun bindBalanceRepository(impl: BalanceRepositoryImpl): BalanceRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(impl: UserProfileRepositoryImpl): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindClearRemoteSessionUseCase(
        impl: ClearRemoteSessionUseCaseImpl,
    ): ClearRemoteSessionUseCase
}
