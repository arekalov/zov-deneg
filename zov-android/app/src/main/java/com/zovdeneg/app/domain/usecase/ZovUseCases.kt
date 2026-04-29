package com.zovdeneg.app.domain.usecase

import com.zovdeneg.app.domain.auth.AuthRepository
import com.zovdeneg.app.domain.balance.BalanceRepository
import com.zovdeneg.app.domain.balance.BrokerageBalance
import com.zovdeneg.app.domain.market.SecuritiesRepository
import com.zovdeneg.app.domain.market.SecurityDetail
import com.zovdeneg.app.domain.market.SecurityOrderBook
import com.zovdeneg.app.domain.PageEnvelope
import com.zovdeneg.app.domain.market.SecurityListItem
import com.zovdeneg.app.domain.market.SecurityPriceHistory
import com.zovdeneg.app.domain.orders.OrderReceipt
import com.zovdeneg.app.domain.orders.OrdersRepository
import com.zovdeneg.app.domain.orders.UserOrder
import com.zovdeneg.app.domain.portfolio.Holding
import com.zovdeneg.app.domain.portfolio.PortfolioRepository
import com.zovdeneg.app.domain.portfolio.PortfolioSummary
import com.zovdeneg.app.domain.profile.UserProfile
import com.zovdeneg.app.domain.profile.UserProfileRepository
import com.zovdeneg.app.domain.transactions.Transaction
import com.zovdeneg.app.domain.transactions.TransactionsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

import javax.inject.Inject

class LoadSecurityDetailUseCase @Inject constructor(
    private val securitiesRepository: SecuritiesRepository,
    private val portfolioRepository: PortfolioRepository,
) {
    suspend operator fun invoke(ticker: String): Result<SecurityDetail> {
        val detail =
            securitiesRepository.getSecurityDetail(ticker).getOrElse { e ->
                return Result.failure(e)
            }
        val needsPortfolioEnrichment =
            detail.portfolioQuantity == null ||
                detail.portfolioCurrentValueLine == null ||
                detail.portfolioUnitPriceLine == null
        if (!needsPortfolioEnrichment) {
            return Result.success(detail)
        }
        val holdings = portfolioRepository.refreshHoldings().getOrNull() ?: return Result.success(detail)
        val holding =
            holdings.find { it.detailNavKey.equals(detail.securityId, ignoreCase = true) }
                ?: holdings.find { it.ticker.equals(detail.ticker, ignoreCase = true) }
                ?: return Result.success(detail)
        val qty = holding.quantity
        if (qty < 1) return Result.success(detail)
        return Result.success(
            detail.copy(
                portfolioQuantity = detail.portfolioQuantity ?: qty,
                portfolioAvgPriceLine = detail.portfolioAvgPriceLine ?: holding.averagePriceLine,
                portfolioCurrentValueLine = detail.portfolioCurrentValueLine ?: holding.valueText,
                portfolioPositionDeltaLine = detail.portfolioPositionDeltaLine ?: holding.deltaText,
                portfolioPositionDeltaPositive = detail.portfolioPositionDeltaPositive
                    ?: holding.deltaPositive,
                portfolioUnitPriceLine = detail.portfolioUnitPriceLine ?: holding.currentPriceLine,
            ),
        )
    }
}

class LoadSecurityOrderBookUseCase @Inject constructor(
    private val securitiesRepository: SecuritiesRepository,
) {
    suspend operator fun invoke(navId: String): Result<SecurityOrderBook> =
        securitiesRepository.getSecurityOrderBook(navId)
}

class LoadSecurityPriceHistoryUseCase @Inject constructor(
    private val securitiesRepository: SecuritiesRepository,
) {
    suspend operator fun invoke(
        ticker: String,
        fromEpochSeconds: Long,
        toEpochSeconds: Long,
    ): Result<SecurityPriceHistory> =
        securitiesRepository.getSecurityPriceHistory(ticker, fromEpochSeconds, toEpochSeconds)
}

class LoadSecuritiesPageUseCase @Inject constructor(
    private val securitiesRepository: SecuritiesRepository,
) {
    suspend operator fun invoke(
        query: String,
        type: String?,
        page: Int,
        pageSize: Int,
    ): Result<PageEnvelope<SecurityListItem>> =
        securitiesRepository.getSecuritiesPage(query, type, page, pageSize)
}

class LoadTransactionsPageUseCase @Inject constructor(
    private val transactionsRepository: TransactionsRepository,
) {
    suspend operator fun invoke(
        page: Int,
        pageSize: Int,
        type: String? = null,
    ): Result<PageEnvelope<Transaction>> =
        transactionsRepository.getTransactionsPage(page, pageSize, type)
}

data class HomePortfolioSnapshot(
    val summary: Result<PortfolioSummary>,
    val holdings: Result<List<Holding>>,
)

class RefreshHomePortfolioUseCase @Inject constructor(
    private val portfolioRepository: PortfolioRepository,
) {
    suspend operator fun invoke(): HomePortfolioSnapshot =
        coroutineScope {
            val summaryAsync = async { portfolioRepository.refreshPortfolioSummary() }
            val holdingsAsync = async { portfolioRepository.refreshHoldings() }
            HomePortfolioSnapshot(summary = summaryAsync.await(), holdings = holdingsAsync.await())
        }
}

class PlaceMarketBuyOrderUseCase @Inject constructor(
    private val ordersRepository: OrdersRepository,
) {
    suspend operator fun invoke(securityId: String, quantity: Int): Result<OrderReceipt> =
        ordersRepository.placeMarketBuy(securityId, quantity)
}

class PlaceMarketSellOrderUseCase @Inject constructor(
    private val ordersRepository: OrdersRepository,
) {
    suspend operator fun invoke(securityId: String, quantity: Int): Result<OrderReceipt> =
        ordersRepository.placeMarketSell(securityId, quantity)
}

class LoadUserOrdersUseCase @Inject constructor(
    private val ordersRepository: OrdersRepository,
) {
    suspend operator fun invoke(): Result<List<UserOrder>> = ordersRepository.listOrders()
}

class LoadOrderDetailUseCase @Inject constructor(
    private val ordersRepository: OrdersRepository,
) {
    suspend operator fun invoke(orderId: String): Result<UserOrder> = ordersRepository.getOrder(orderId)
}

class CancelOrderUseCase @Inject constructor(
    private val ordersRepository: OrdersRepository,
) {
    suspend operator fun invoke(orderId: String): Result<Unit> = ordersRepository.cancelOrder(orderId)
}

class LoadBrokerageBalanceUseCase @Inject constructor(
    private val balanceRepository: BalanceRepository,
) {
    suspend operator fun invoke(): Result<BrokerageBalance> = balanceRepository.getBalance()
}

class SubmitBrokerageDepositUseCase @Inject constructor(
    private val balanceRepository: BalanceRepository,
) {
    suspend operator fun invoke(amount: String): Result<BrokerageBalance> =
        balanceRepository.depositDecimalString(amount)
}

class SubmitBrokerageWithdrawUseCase @Inject constructor(
    private val balanceRepository: BalanceRepository,
) {
    suspend operator fun invoke(amount: String): Result<BrokerageBalance> =
        balanceRepository.withdrawDecimalString(amount)
}

class LoadUserProfileUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
) {
    suspend operator fun invoke(): Result<UserProfile> = userProfileRepository.getProfile()
}

class UpdateUserProfileUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
) {
    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
    ): Result<UserProfile> = userProfileRepository.updateProfile(firstName, lastName, email, phone)
}

class ChangeAppPinUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
) {
    suspend operator fun invoke(): Result<Unit> = userProfileRepository.changePin()
}

class RegisterNewAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String,
    ): Result<Unit> = authRepository.register(firstName, lastName, email, phone, password)
}
