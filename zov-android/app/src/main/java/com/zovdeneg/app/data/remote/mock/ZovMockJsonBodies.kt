package com.zovdeneg.app.data.remote.mock

internal object ZovMockJsonBodies {
    private const val ID_SBER = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
    private const val ID_LKOH = "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12"
    private const val ID_GAZP = "c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13"
    private const val ID_DEFAULT = "d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14"

    fun securityDetail(tickerRaw: String): String {
        val t = tickerRaw.uppercase()
        return when (t) {
            "SBER" ->
                """
                {"ticker":"SBER","subtitle":"Сбербанк · финансы","priceLine":"298,12 ₽","changeLine":"+1,2% · +3,52 ₽ за день","changePositive":true,"securityId":"$ID_SBER","lotSize":10,"orderBookText":"Покупка 298,10 · 298,12 · Продажа 298,14"}
                """.trimIndent()
            "LKOH" ->
                """
                {"ticker":"LKOH","subtitle":"ЛУКОЙЛ · энергетика","priceLine":"6 542,00 ₽","changeLine":"−0,4% · −26 ₽ за день","changePositive":false,"securityId":"$ID_LKOH","lotSize":10,"orderBookText":"Стакан (мок): лучший bid 6 540, ask 6 545"}
                """.trimIndent()
            "GAZP" ->
                """
                {"ticker":"GAZP","subtitle":"Газпром · энергетика","priceLine":"167,40 ₽","changeLine":"+0,8% · +1,32 ₽ за день","changePositive":true,"securityId":"$ID_GAZP","lotSize":10,"orderBookText":"Стакан (мок): bid 167,38 · ask 167,42"}
                """.trimIndent()
            else ->
                """
                {"ticker":"$t","subtitle":"$t · бумага","priceLine":"—","changeLine":"нет данных (мок)","changePositive":true,"securityId":"$ID_DEFAULT","lotSize":10,"orderBookText":"Стакан заявок (заглушка)"}
                """.trimIndent()
        }
    }

    fun portfolioSummary(): String =
        """
        {"portfolioAmountRub":"1 234 567,89 ₽","totalGainText":"+12 345,67 ₽ (+2,3%)"}
        """.trimIndent()

    fun portfolioHoldings(): String =
        """
        {"holdings":[
          {"ticker":"SBER","subtitle":"10 шт. · ср. 285 ₽","valueText":"2 850 ₽","deltaText":"+150 ₽","deltaPositive":true},
          {"ticker":"LKOH","subtitle":"5 шт. · ср. 6 200 ₽","valueText":"32 710 ₽","deltaText":"+1 710 ₽","deltaPositive":false}
        ]}
        """.trimIndent()

    fun popularSecurities(): String =
        """
        {"items":[
          {"ticker":"GAZP","subtitle":"Газпром · энергетика","valueText":"167,4 ₽","deltaText":"+0,8%","deltaPositive":true,"kind":"stock"},
          {"ticker":"SBER","subtitle":"Сбербанк · финансы","valueText":"298,12 ₽","deltaText":"+1,2%","deltaPositive":true,"kind":"stock"},
          {"ticker":"SU26238RMFS5","subtitle":"ОФЗ 26238","valueText":"98,2 ₽","deltaText":"−0,1%","deltaPositive":false,"kind":"bond"},
          {"ticker":"LQDT","subtitle":"Видео · IT","valueText":"6 540 ₽","deltaText":"+0,3%","deltaPositive":true,"kind":"etf"}
        ]}
        """.trimIndent()

    fun transactions(): String =
        """
        {"transactions":[
          {"title":"Покупка · SBER","date":"28 мар. 2026, 14:32","amountText":"+2 981 ₽","side":"purchase"},
          {"title":"Продажа · LKOH","date":"27 мар. 2026, 10:14","amountText":"−13 080 ₽","side":"sale"},
          {"title":"Покупка · GAZP","date":"26 мар. 2026, 09:00","amountText":"+5 000 ₽","side":"purchase"}
        ]}
        """.trimIndent()

    fun orderCreated(): String =
        """
        {"id":"e1eebc99-9c0b-4ef8-bb6d-6bb9bd380a21","securityId":"$ID_SBER","ticker":"SBER","type":"market","side":"buy","status":"executed","quantity":10,"executedPrice":"298.45","executedQuantity":10,"totalAmount":"2984.50","commission":"8.95","createdAt":1731571200,"updatedAt":1731571250}
        """.trimIndent()

    fun balance(): String =
        """
        {"available":"45320.00","total":"47800.00","blocked":"2480.00"}
        """.trimIndent()

    fun balanceAfterWithdraw(): String =
        """
        {"available":"40320.00","total":"42800.00","blocked":"2480.00"}
        """.trimIndent()

    fun userProfile(): String =
        """
        {"id":"3fa85f64-5717-4562-b3fc-2c963f66afa6","firstName":"Иван","lastName":"Иванов","email":"ivan@example.com","phone":"+79001234567","role":"user","isBlocked":false,"createdAt":1731484800,"updatedAt":1731571200}
        """.trimIndent()

    fun authLoginResponse(): String =
        """
        {"user":${userProfile()},"tokens":{"accessToken":"mock-access-token","refreshToken":"mock-refresh-token","expiresIn":900}}
        """.trimIndent()

    fun authRegisterResponse(): String = authLoginResponse()

    fun pinChangeOk(): String = """{"ok":true}"""
}
