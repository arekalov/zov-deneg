package com.zovdeneg.app.data.remote.dto

import com.zovdeneg.app.data.format.ZovRubDisplay

internal fun SecurityCardRemoteDto.toSecurityDetailDto(
    orderBook: OrderBookRemoteDto?,
): SecurityDetailDto =
    SecurityDetailDto(
        ticker = ticker,
        subtitle = "$name · $exchange",
        priceLine = ZovRubDisplay.formatApiDecimalToRubLine(lastPrice.trim()),
        changeLine = "$priceChangePct% · ${ZovRubDisplay.formatSignedDecimalRubLine(priceChange.trim())}",
        changePositive = !priceChange.trim().startsWith("-"),
        securityId = id,
        lotSize = lotSize,
        orderBookText = orderBook?.spread?.let { "Спред $it" },
        sectorName = sector,
        exchangeCode = exchange,
        companyDescription = description,
        portfolioQuantity = null,
        portfolioAvgPriceLine = null,
        orderBook = orderBook?.toSecurityOrderBookDto(),
    )

internal fun OrderBookRemoteDto.toSecurityOrderBookDto(): SecurityOrderBookDto =
    SecurityOrderBookDto(
        askLevels = asks.map { SecurityOrderBookLevelDto(it.quantity, it.price, it.quantity) },
        bestAskPrice = asks.firstOrNull()?.price ?: "0",
        bidLevels = bids.map { SecurityOrderBookLevelDto(it.quantity, it.price, it.quantity) },
        bestBidPrice = bids.firstOrNull()?.price ?: "0",
    )
