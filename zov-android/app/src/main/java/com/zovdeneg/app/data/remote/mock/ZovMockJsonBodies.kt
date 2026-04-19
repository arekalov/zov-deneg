package com.zovdeneg.app.data.remote.mock

import com.zovdeneg.app.data.remote.ZovJson
import com.zovdeneg.app.data.remote.dto.AuthEnvelopeDto
import com.zovdeneg.app.data.remote.dto.BalanceDto
import com.zovdeneg.app.data.remote.dto.HoldingDto
import com.zovdeneg.app.data.remote.dto.HoldingsEnvelopeDto
import com.zovdeneg.app.data.remote.dto.OrderResponseDto
import com.zovdeneg.app.data.remote.dto.PinChangeAckDto
import com.zovdeneg.app.data.remote.dto.PopularSecuritiesEnvelopeDto
import com.zovdeneg.app.data.remote.dto.PopularSecurityDto
import com.zovdeneg.app.data.remote.dto.PortfolioSummaryDto
import com.zovdeneg.app.data.remote.dto.SecurityDetailDto
import com.zovdeneg.app.data.remote.dto.TokensDto
import com.zovdeneg.app.data.remote.dto.TransactionDto
import com.zovdeneg.app.data.remote.dto.TransactionsEnvelopeDto
import com.zovdeneg.app.data.remote.dto.UserProfileDto
import kotlinx.serialization.encodeToString

internal object ZovMockJsonBodies {
    private const val ID_SBER = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
    private const val ID_LKOH = "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12"
    private const val ID_GAZP = "c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13"
    private const val ID_DEFAULT = "d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14"

    private val knownSecurityDetails: Map<String, SecurityDetailDto> =
        mapOf(
            "SBER" to
                SecurityDetailDto(
                    ticker = "SBER",
                    subtitle = "Сбербанк · финансы",
                    priceLine = "298,12 ₽",
                    changeLine = "+1,2% · +3,52 ₽ за день",
                    changePositive = true,
                    securityId = ID_SBER,
                    lotSize = 10,
                    orderBookText = "Покупка 298,10 · 298,12 · Продажа 298,14",
                ),
            "LKOH" to
                SecurityDetailDto(
                    ticker = "LKOH",
                    subtitle = "ЛУКОЙЛ · энергетика",
                    priceLine = "6 542,00 ₽",
                    changeLine = "−0,4% · −26 ₽ за день",
                    changePositive = false,
                    securityId = ID_LKOH,
                    lotSize = 10,
                    orderBookText = "Стакан (мок): лучший bid 6 540, ask 6 545",
                ),
            "GAZP" to
                SecurityDetailDto(
                    ticker = "GAZP",
                    subtitle = "Газпром · энергетика",
                    priceLine = "167,40 ₽",
                    changeLine = "+0,8% · +1,32 ₽ за день",
                    changePositive = true,
                    securityId = ID_GAZP,
                    lotSize = 10,
                    orderBookText = "Стакан (мок): bid 167,38 · ask 167,42",
                ),
            "ROSN" to
                SecurityDetailDto(
                    ticker = "ROSN",
                    subtitle = "Роснефть · энергетика",
                    priceLine = "512,30 ₽",
                    changeLine = "+0,5% · +2,55 ₽ за день",
                    changePositive = true,
                    securityId = "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a01",
                    lotSize = 10,
                    orderBookText = "512,28 / 512,35 · объём дня 1,2 млн шт",
                ),
            "NVTK" to
                SecurityDetailDto(
                    ticker = "NVTK",
                    subtitle = "Новатэк · энергетика",
                    priceLine = "1 284,50 ₽",
                    changeLine = "−0,2% · −2,60 ₽ за день",
                    changePositive = false,
                    securityId = "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a02",
                    lotSize = 1,
                    orderBookText = "1 284,0 — 1 285,0 · спред 1 ₽",
                ),
            "GMKN" to
                SecurityDetailDto(
                    ticker = "GMKN",
                    subtitle = "Норникель · металлы",
                    priceLine = "15 890 ₽",
                    changeLine = "+1,1% · +173 ₽ за день",
                    changePositive = true,
                    securityId = "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a03",
                    lotSize = 1,
                    orderBookText = "15 885 / 15 895 · глубина 120 / 85 лотов",
                ),
            "PLZL" to
                SecurityDetailDto(
                    ticker = "PLZL",
                    subtitle = "Полюс · золото",
                    priceLine = "11 234 ₽",
                    changeLine = "+0,3% · +34 ₽ за день",
                    changePositive = true,
                    securityId = "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a04",
                    lotSize = 1,
                    orderBookText = "11 230 — 11 240",
                ),
            "TATN" to
                SecurityDetailDto(
                    ticker = "TATN",
                    subtitle = "Татнефть · энергетика",
                    priceLine = "642,10 ₽",
                    changeLine = "+0,6% · +3,83 ₽ за день",
                    changePositive = true,
                    securityId = "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a05",
                    lotSize = 1,
                    orderBookText = "641,90 / 642,30",
                ),
            "MGNT" to
                SecurityDetailDto(
                    ticker = "MGNT",
                    subtitle = "Магнит · ритейл",
                    priceLine = "7 015 ₽",
                    changeLine = "−0,9% · −64 ₽ за день",
                    changePositive = false,
                    securityId = "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a06",
                    lotSize = 1,
                    orderBookText = "7 010 — 7 020 · после дивидендного отсечения",
                ),
            "YDEX" to
                SecurityDetailDto(
                    ticker = "YDEX",
                    subtitle = "Яндекс · IT",
                    priceLine = "4 128 ₽",
                    changeLine = "+2,1% · +85 ₽ за день",
                    changePositive = true,
                    securityId = "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a07",
                    lotSize = 1,
                    orderBookText = "4 125 / 4 130 · ликвидность высокая",
                ),
            "VTBR" to
                SecurityDetailDto(
                    ticker = "VTBR",
                    subtitle = "ВТБ · финансы",
                    priceLine = "0,0284 ₽",
                    changeLine = "+3,2% · +0,0009 ₽ за день",
                    changePositive = true,
                    securityId = "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a08",
                    lotSize = 10000,
                    orderBookText = "0,02835 / 0,02845 · пачка 10 000",
                ),
            "MOEX" to
                SecurityDetailDto(
                    ticker = "MOEX",
                    subtitle = "МосБиржа · финансы",
                    priceLine = "214,80 ₽",
                    changeLine = "+0,4% · +0,86 ₽ за день",
                    changePositive = true,
                    securityId = "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a09",
                    lotSize = 10,
                    orderBookText = "214,75 / 214,85",
                ),
            "CHMF" to
                SecurityDetailDto(
                    ticker = "CHMF",
                    subtitle = "Северсталь · металлы",
                    priceLine = "1 098 ₽",
                    changeLine = "−0,3% · −3,30 ₽ за день",
                    changePositive = false,
                    securityId = "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a0a",
                    lotSize = 10,
                    orderBookText = "1 097,5 — 1 098,5",
                ),
            "PHOR" to
                SecurityDetailDto(
                    ticker = "PHOR",
                    subtitle = "ФосАгро · химия",
                    priceLine = "7 456 ₽",
                    changeLine = "+0,7% · +51,90 ₽ за день",
                    changePositive = true,
                    securityId = "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a0b",
                    lotSize = 1,
                    orderBookText = "7 450 / 7 460",
                ),
            "ALRS" to
                SecurityDetailDto(
                    ticker = "ALRS",
                    subtitle = "Алроса · добыча",
                    priceLine = "76,42 ₽",
                    changeLine = "+1,5% · +1,13 ₽ за день",
                    changePositive = true,
                    securityId = "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a0c",
                    lotSize = 10,
                    orderBookText = "76,40 / 76,45",
                ),
            "SNGS" to
                SecurityDetailDto(
                    ticker = "SNGS",
                    subtitle = "Сургутнефтегаз · энергетика",
                    priceLine = "28,15 ₽",
                    changeLine = "0,0% · 0,00 ₽ за день",
                    changePositive = true,
                    securityId = "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a0d",
                    lotSize = 100,
                    orderBookText = "28,14 / 28,16",
                ),
            "FEES" to
                SecurityDetailDto(
                    ticker = "FEES",
                    subtitle = "Россети · электроэнергетика",
                    priceLine = "0,1042 ₽",
                    changeLine = "−0,5% · −0,0005 ₽ за день",
                    changePositive = false,
                    securityId = "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a0e",
                    lotSize = 10000,
                    orderBookText = "0,1040 / 0,1044",
                ),
            "OZON" to
                SecurityDetailDto(
                    ticker = "OZON",
                    subtitle = "Ozon · e-commerce",
                    priceLine = "4 890 ₽",
                    changeLine = "+4,2% · +197 ₽ за день",
                    changePositive = true,
                    securityId = "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a0f",
                    lotSize = 1,
                    orderBookText = "4 885 / 4 895",
                ),
            "TCSG" to
                SecurityDetailDto(
                    ticker = "TCSG",
                    subtitle = "Т-Технологии · финансы",
                    priceLine = "3 012 ₽",
                    changeLine = "+0,9% · +26,90 ₽ за день",
                    changePositive = true,
                    securityId = "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
                    lotSize = 1,
                    orderBookText = "3 010 / 3 015",
                ),
            "X5" to
                SecurityDetailDto(
                    ticker = "X5",
                    subtitle = "X5 Group · ритейл",
                    priceLine = "3 156 ₽",
                    changeLine = "−0,6% · −19 ₽ за день",
                    changePositive = false,
                    securityId = "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380b02",
                    lotSize = 1,
                    orderBookText = "3 152 — 3 160",
                ),
            "LQDT" to
                SecurityDetailDto(
                    ticker = "LQDT",
                    subtitle = "Видео · IT (мок ETF)",
                    priceLine = "6 540 ₽",
                    changeLine = "+0,3% · +19,60 ₽ за день",
                    changePositive = true,
                    securityId = "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380b03",
                    lotSize = 1,
                    orderBookText = "ETF: NAV 6 538 · рыночная 6 540",
                ),
            "SU26238RMFS5" to
                SecurityDetailDto(
                    ticker = "SU26238RMFS5",
                    subtitle = "ОФЗ 26238 · гособлигация",
                    priceLine = "98,20 ₽",
                    changeLine = "−0,1% · −0,10 ₽ за день",
                    changePositive = false,
                    securityId = "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380b04",
                    lotSize = 1,
                    orderBookText = "Дюрация ~6 лет · доходность к погашению (мок) 12,4%",
                ),
        )

    private val mockPopularItems: List<PopularSecurityDto> =
        listOf(
            PopularSecurityDto("GAZP", "Газпром · энергетика", "167,4 ₽", "+0,8%", true, "stock"),
            PopularSecurityDto("SBER", "Сбербанк · финансы", "298,12 ₽", "+1,2%", true, "stock"),
            PopularSecurityDto("LKOH", "ЛУКОЙЛ · энергетика", "6 542 ₽", "−0,4%", false, "stock"),
            PopularSecurityDto("ROSN", "Роснефть · энергетика", "512,3 ₽", "+0,5%", true, "stock"),
            PopularSecurityDto("NVTK", "Новатэк · энергетика", "1 284,5 ₽", "−0,2%", false, "stock"),
            PopularSecurityDto("GMKN", "Норникель · металлы", "15 890 ₽", "+1,1%", true, "stock"),
            PopularSecurityDto("PLZL", "Полюс · золото", "11 234 ₽", "+0,3%", true, "stock"),
            PopularSecurityDto("TATNP", "Татнефть pref · энергетика", "598,0 ₽", "+0,4%", true, "stock"),
            PopularSecurityDto("TATN", "Татнефть · энергетика", "642,1 ₽", "+0,6%", true, "stock"),
            PopularSecurityDto("MGNT", "Магнит · ритейл", "7 015 ₽", "−0,9%", false, "stock"),
            PopularSecurityDto("YDEX", "Яндекс · IT", "4 128 ₽", "+2,1%", true, "stock"),
            PopularSecurityDto("VTBR", "ВТБ · финансы", "0,0284 ₽", "+3,2%", true, "stock"),
            PopularSecurityDto("MOEX", "МосБиржа · финансы", "214,8 ₽", "+0,4%", true, "stock"),
            PopularSecurityDto("CHMF", "Северсталь · металлы", "1 098 ₽", "−0,3%", false, "stock"),
            PopularSecurityDto("PHOR", "ФосАгро · химия", "7 456 ₽", "+0,7%", true, "stock"),
            PopularSecurityDto("ALRS", "Алроса · добыча", "76,42 ₽", "+1,5%", true, "stock"),
            PopularSecurityDto("SNGS", "Сургутнефтегаз · энергетика", "28,15 ₽", "0,0%", true, "stock"),
            PopularSecurityDto("SNGSP", "Сургутнефтегаз pref", "45,02 ₽", "+0,2%", true, "stock"),
            PopularSecurityDto("FEES", "Россети · электроэнергетика", "0,1042 ₽", "−0,5%", false, "stock"),
            PopularSecurityDto("HYDR", "РусГидро · электроэнергетика", "0,542 ₽", "+0,1%", true, "stock"),
            PopularSecurityDto("IRAO", "Интер РАО · электроэнергетика", "3,12 ₽", "−0,3%", false, "stock"),
            PopularSecurityDto("MTSS", "МТС · телеком", "234,5 ₽", "+0,8%", true, "stock"),
            PopularSecurityDto("RTKM", "Ростелеком · телеком", "72,8 ₽", "−0,2%", false, "stock"),
            PopularSecurityDto("AFLT", "Аэрофлот · транспорт", "58,34 ₽", "+1,1%", true, "stock"),
            PopularSecurityDto("PIKK", "ПИК · девелопмент", "678 ₽", "+0,5%", true, "stock"),
            PopularSecurityDto("LSRG", "ЛСР · девелопмент", "712 ₽", "−0,7%", false, "stock"),
            PopularSecurityDto("POLY", "Polymetal · металлы", "412 ₽", "+0,9%", true, "stock"),
            PopularSecurityDto("RUAL", "Русал · металлы", "38,90 ₽", "−1,2%", false, "stock"),
            PopularSecurityDto("ENRU", "Энел Россия · электроэнергетика", "0,7156 ₽", "+0,4%", true, "stock"),
            PopularSecurityDto("TRNFP", "Транснефть pref · энергетика", "1 456 ₽", "+0,1%", true, "stock"),
            PopularSecurityDto("BSPB", "Банк Санкт-Петербург · финансы", "312 ₽", "+2,4%", true, "stock"),
            PopularSecurityDto("MVID", "М.Видео · ритейл", "98,5 ₽", "−0,8%", false, "stock"),
            PopularSecurityDto("OZON", "Ozon · e-commerce", "4 890 ₽", "+4,2%", true, "stock"),
            PopularSecurityDto("POSI", "Positive Technologies · IT", "612 ₽", "+1,0%", true, "stock"),
            PopularSecurityDto("SOFL", "Софтлайн · IT", "98,2 ₽", "−2,1%", false, "stock"),
            PopularSecurityDto("SPBE", "СПБ Биржа · финансы", "124,3 ₽", "+0,6%", true, "stock"),
            PopularSecurityDto("TCSG", "Т-Технологии · финансы", "3 012 ₽", "+0,9%", true, "stock"),
            PopularSecurityDto("VKCO", "VK · IT", "312 ₽", "+0,3%", true, "stock"),
            PopularSecurityDto("X5", "X5 Group · ритейл", "3 156 ₽", "−0,6%", false, "stock"),
            PopularSecurityDto("MAGN", "ММК · металлы", "42,15 ₽", "+0,2%", true, "stock"),
            PopularSecurityDto("AFKS", "Система · финансы", "15,42 ₽", "+0,5%", true, "stock"),
            PopularSecurityDto("APTK", "Аптеки36,6 · ритейл", "8,12 ₽", "−0,4%", false, "stock"),
            PopularSecurityDto("OGKB", "ОГК-2 · электроэнергетика", "0,612 ₽", "+0,9%", true, "stock"),
            PopularSecurityDto("CBOM", "МКБ · финансы", "98,4 ₽", "+1,3%", true, "stock"),
            PopularSecurityDto("MDMG", "МДМ Групп · девелопмент", "156 ₽", "−0,2%", false, "stock"),
            PopularSecurityDto("SIBN", "Газпром нефть · энергетика", "412 ₽", "+0,6%", true, "stock"),
            PopularSecurityDto("SU26238RMFS5", "ОФЗ 26238", "98,2 ₽", "−0,1%", false, "bond"),
            PopularSecurityDto("SU26233RMFS5", "ОФЗ 26233", "101,4 ₽", "+0,05%", true, "bond"),
            PopularSecurityDto("SU26229RMFS4", "ОФЗ 26229", "95,8 ₽", "−0,2%", false, "bond"),
            PopularSecurityDto("LQDT", "Видео · IT", "6 540 ₽", "+0,3%", true, "etf"),
            PopularSecurityDto("SBGB", "Сберегательный ETF · облигации", "12,45 ₽", "+0,1%", true, "etf"),
            PopularSecurityDto("TMOS", "Тинькофф голубые фишки", "18,92 ₽", "+0,4%", true, "etf"),
        )

    private val mockHoldings: List<HoldingDto> =
        listOf(
            HoldingDto("SBER", "120 шт. · ср. 276 ₽", "33 120 ₽", "+2 640 ₽", true),
            HoldingDto("LKOH", "8 шт. · ср. 6 100 ₽", "52 336 ₽", "+2 536 ₽", true),
            HoldingDto("GAZP", "400 шт. · ср. 162 ₽", "66 960 ₽", "+1 920 ₽", true),
            HoldingDto("ROSN", "60 шт. · ср. 498 ₽", "30 738 ₽", "+858 ₽", true),
            HoldingDto("NVTK", "15 шт. · ср. 1 260 ₽", "19 267 ₽", "−101 ₽", false),
            HoldingDto("GMKN", "4 шт. · ср. 15 200 ₽", "63 560 ₽", "+2 760 ₽", true),
            HoldingDto("PLZL", "3 шт. · ср. 10 900 ₽", "32 802 ₽", "+102 ₽", true),
            HoldingDto("TATN", "25 шт. · ср. 618 ₽", "16 052 ₽", "+602 ₽", true),
            HoldingDto("MGNT", "5 шт. · ср. 7 200 ₽", "35 075 ₽", "−925 ₽", false),
            HoldingDto("YDEX", "10 шт. · ср. 3 900 ₽", "41 280 ₽", "+2 280 ₽", true),
            HoldingDto("VTBR", "500 000 шт. · ср. 0,027 ₽", "14 200 ₽", "+700 ₽", true),
            HoldingDto("MOEX", "80 шт. · ср. 205 ₽", "17 184 ₽", "+784 ₽", true),
            HoldingDto("CHMF", "30 шт. · ср. 1 080 ₽", "32 940 ₽", "+540 ₽", true),
            HoldingDto("PHOR", "4 шт. · ср. 7 300 ₽", "29 824 ₽", "+624 ₽", true),
            HoldingDto("ALRS", "200 шт. · ср. 72 ₽", "15 284 ₽", "+884 ₽", true),
            HoldingDto("SNGS", "1 000 шт. · ср. 27,5 ₽", "28 150 ₽", "+150 ₽", true),
            HoldingDto("FEES", "80 000 шт. · ср. 0,102 ₽", "8 336 ₽", "+176 ₽", true),
            HoldingDto("OZON", "6 шт. · ср. 4 500 ₽", "29 340 ₽", "+2 340 ₽", true),
            HoldingDto("TCSG", "7 шт. · ср. 2 850 ₽", "21 084 ₽", "+1 134 ₽", true),
            HoldingDto("X5", "4 шт. · ср. 3 200 ₽", "12 624 ₽", "+224 ₽", true),
            HoldingDto("AFLT", "150 шт. · ср. 55 ₽", "8 751 ₽", "+501 ₽", true),
            HoldingDto("PIKK", "20 шт. · ср. 640 ₽", "13 560 ₽", "+760 ₽", true),
            HoldingDto("RUAL", "300 шт. · ср. 40 ₽", "11 670 ₽", "−330 ₽", false),
            HoldingDto("ENRU", "5 000 шт. · ср. 0,70 ₽", "3 578 ₽", "+78 ₽", true),
            HoldingDto("BSPB", "40 шт. · ср. 285 ₽", "12 480 ₽", "+1 080 ₽", true),
            HoldingDto("SPBE", "90 шт. · ср. 118 ₽", "11 187 ₽", "+567 ₽", true),
            HoldingDto("VKCO", "35 шт. · ср. 298 ₽", "10 920 ₽", "+390 ₽", true),
            HoldingDto("MAGN", "400 шт. · ср. 41 ₽", "16 860 ₽", "+420 ₽", true),
            HoldingDto("AFKS", "800 шт. · ср. 14,8 ₽", "12 336 ₽", "+496 ₽", true),
            HoldingDto("LQDT", "2 шт. · ср. 6 400 ₽", "13 080 ₽", "+80 ₽", true),
        )

    private val mockTransactions: List<TransactionDto> =
        listOf(
            TransactionDto("Покупка · SBER", "19 апр. 2026, 15:42", "+35 774 ₽", "purchase"),
            TransactionDto("Продажа · LKOH", "19 апр. 2026, 11:08", "−52 336 ₽", "sale"),
            TransactionDto("Дивиденды · GMKN", "18 апр. 2026, 09:00", "+1 240 ₽", "purchase"),
            TransactionDto("Покупка · OZON", "18 апр. 2026, 14:22", "+29 340 ₽", "purchase"),
            TransactionDto("Продажа · MGNT", "17 апр. 2026, 16:01", "−35 075 ₽", "sale"),
            TransactionDto("Покупка · YDEX", "17 апр. 2026, 10:30", "+41 280 ₽", "purchase"),
            TransactionDto("Покупка · NVTK", "16 апр. 2026, 12:55", "+19 267 ₽", "purchase"),
            TransactionDto("Продажа · RUAL", "16 апр. 2026, 09:12", "−11 670 ₽", "sale"),
            TransactionDto("Покупка · TCSG", "15 апр. 2026, 13:40", "+21 084 ₽", "purchase"),
            TransactionDto("Покупка · VTBR", "15 апр. 2026, 09:05", "+14 200 ₽", "purchase"),
            TransactionDto("Продажа · X5", "14 апр. 2026, 15:18", "−12 624 ₽", "sale"),
            TransactionDto("Покупка · ROSN", "14 апр. 2026, 11:00", "+30 738 ₽", "purchase"),
            TransactionDto("Покупка · GAZP", "13 апр. 2026, 16:44", "+66 960 ₽", "purchase"),
            TransactionDto("Продажа · AFLT", "13 апр. 2026, 10:20", "−8 751 ₽", "sale"),
            TransactionDto("Покупка · MOEX", "12 апр. 2026, 14:11", "+17 184 ₽", "purchase"),
            TransactionDto("Покупка · CHMF", "12 апр. 2026, 09:33", "+32 940 ₽", "purchase"),
            TransactionDto("Продажа · FEES", "11 апр. 2026, 15:50", "−8 336 ₽", "sale"),
            TransactionDto("Покупка · PHOR", "11 апр. 2026, 12:08", "+29 824 ₽", "purchase"),
            TransactionDto("Покупка · PLZL", "10 апр. 2026, 16:02", "+32 802 ₽", "purchase"),
            TransactionDto("Продажа · ENRU", "10 апр. 2026, 09:45", "−3 578 ₽", "sale"),
            TransactionDto("Покупка · ALRS", "9 апр. 2026, 13:17", "+15 284 ₽", "purchase"),
            TransactionDto("Покупка · SNGS", "9 апр. 2026, 09:28", "+28 150 ₽", "purchase"),
            TransactionDto("Продажа · PIKK", "8 апр. 2026, 15:33", "−13 560 ₽", "sale"),
            TransactionDto("Покупка · BSPB", "8 апр. 2026, 10:55", "+12 480 ₽", "purchase"),
            TransactionDto("Покупка · SPBE", "7 апр. 2026, 14:40", "+11 187 ₽", "purchase"),
            TransactionDto("Продажа · VKCO", "7 апр. 2026, 09:15", "−10 920 ₽", "sale"),
            TransactionDto("Покупка · MAGN", "6 апр. 2026, 16:08", "+16 860 ₽", "purchase"),
            TransactionDto("Покупка · AFKS", "6 апр. 2026, 11:22", "+12 336 ₽", "purchase"),
            TransactionDto("Продажа · LQDT", "5 апр. 2026, 15:01", "−13 080 ₽", "sale"),
            TransactionDto("Покупка · TATN", "5 апр. 2026, 09:50", "+16 052 ₽", "purchase"),
            TransactionDto("Покупка · HYDR", "4 апр. 2026, 13:25", "+5 420 ₽", "purchase"),
            TransactionDto("Продажа · IRAO", "4 апр. 2026, 10:11", "−3 120 ₽", "sale"),
            TransactionDto("Покупка · MTSS", "3 апр. 2026, 14:58", "+7 035 ₽", "purchase"),
            TransactionDto("Покупка · RTKM", "3 апр. 2026, 09:02", "+3 640 ₽", "purchase"),
            TransactionDto("Продажа · CBOM", "2 апр. 2026, 16:30", "−3 936 ₽", "sale"),
            TransactionDto("Покупка · OGKB", "2 апр. 2026, 12:44", "+3 060 ₽", "purchase"),
            TransactionDto("Покупка · SU26238RMFS5", "1 апр. 2026, 15:10", "+98 200 ₽", "purchase"),
            TransactionDto("Продажа · SU26233RMFS5", "1 апр. 2026, 09:30", "−50 700 ₽", "sale"),
            TransactionDto("Покупка · SBGB", "31 мар. 2026, 14:05", "+12 450 ₽", "purchase"),
            TransactionDto("Покупка · TMOS", "31 мар. 2026, 10:18", "+18 920 ₽", "purchase"),
            TransactionDto("Продажа · SOFL", "30 мар. 2026, 16:55", "−4 910 ₽", "sale"),
            TransactionDto("Покупка · POSI", "30 мар. 2026, 11:40", "+6 120 ₽", "purchase"),
            TransactionDto("Покупка · MDMG", "29 мар. 2026, 15:22", "+6 240 ₽", "purchase"),
            TransactionDto("Продажа · MVID", "29 мар. 2026, 09:08", "−4 925 ₽", "sale"),
            TransactionDto("Покупка · SIBN", "28 мар. 2026, 13:33", "+16 480 ₽", "purchase"),
            TransactionDto("Покупка · TRNFP", "28 мар. 2026, 09:55", "+14 560 ₽", "purchase"),
            TransactionDto("Продажа · LSRG", "27 мар. 2026, 16:12", "−7 120 ₽", "sale"),
            TransactionDto("Покупка · POLY", "27 мар. 2026, 10:44", "+4 120 ₽", "purchase"),
            TransactionDto("Покупка · SBER", "26 мар. 2026, 14:32", "+2 981 ₽", "purchase"),
            TransactionDto("Продажа · LKOH", "26 мар. 2026, 10:14", "−13 080 ₽", "sale"),
            TransactionDto("Покупка · GAZP", "25 мар. 2026, 09:00", "+5 000 ₽", "purchase"),
            TransactionDto("Продажа · VTBR", "24 мар. 2026, 15:40", "−7 100 ₽", "sale"),
            TransactionDto("Покупка · FEES", "24 мар. 2026, 11:05", "+8 160 ₽", "purchase"),
            TransactionDto("Комиссия брокера", "24 мар. 2026, 09:00", "−125 ₽", "sale"),
            TransactionDto("Покупка · OZON", "23 мар. 2026, 14:18", "+9 780 ₽", "purchase"),
            TransactionDto("Продажа · YDEX", "23 мар. 2026, 10:22", "−20 640 ₽", "sale"),
            TransactionDto("Покупка · NVTK", "22 мар. 2026, 16:50", "+12 845 ₽", "purchase"),
            TransactionDto("Покупка · GMKN", "22 мар. 2026, 09:33", "+31 780 ₽", "purchase"),
            TransactionDto("Продажа · SNGSP", "21 мар. 2026, 15:11", "−4 502 ₽", "sale"),
            TransactionDto("Покупка · APTK", "21 мар. 2026, 12:00", "+812 ₽", "purchase"),
            TransactionDto("Пополнение счёта", "20 мар. 2026, 10:00", "+100 000 ₽", "purchase"),
            TransactionDto("Вывод средств", "19 мар. 2026, 14:00", "−25 000 ₽", "sale"),
        )

    private val profileDto =
        UserProfileDto(
            id = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            firstName = "Иван",
            lastName = "Иванов",
            email = "ivan@example.com",
            phone = "+79001234567",
            role = "user",
            isBlocked = false,
            createdAt = 1731484800L,
            updatedAt = 1731571200L,
        )

    fun securityDetail(tickerRaw: String): String {
        val t = tickerRaw.uppercase()
        val dto =
            knownSecurityDetails[t] ?: run {
                val positive = t.hashCode() % 2 == 0
                SecurityDetailDto(
                    ticker = t,
                    subtitle = "$t · бумага (расширенный мок)",
                    priceLine = "—",
                    changeLine = if (positive) "+0,0% · мок" else "−0,0% · мок",
                    changePositive = positive,
                    securityId = ID_DEFAULT,
                    lotSize = 10,
                    orderBookText = "Стакан заявок (заглушка) · bid — · ask — · глубина рынка (мок)",
                )
            }
        return ZovJson.encodeToString(dto)
    }

    fun portfolioSummary(): String =
        ZovJson.encodeToString(
            PortfolioSummaryDto(
                portfolioAmountRub = "4 876 234,56 ₽",
                totalGainText = "+234 567,89 ₽ (+5,1%)",
            ),
        )

    fun portfolioHoldings(): String =
        ZovJson.encodeToString(HoldingsEnvelopeDto(mockHoldings))

    fun popularSecurities(): String =
        ZovJson.encodeToString(PopularSecuritiesEnvelopeDto(mockPopularItems))

    fun transactions(): String =
        ZovJson.encodeToString(TransactionsEnvelopeDto(mockTransactions))

    fun orderCreated(): String =
        ZovJson.encodeToString(
            OrderResponseDto(
                id = "e1eebc99-9c0b-4ef8-bb6d-6bb9bd380a21",
                securityId = ID_SBER,
                ticker = "SBER",
                type = "market",
                side = "buy",
                status = "executed",
                quantity = 10,
                executedPrice = "298.45",
                executedQuantity = 10,
                totalAmount = "2984.50",
                commission = "8.95",
                createdAt = 1731571200L,
                updatedAt = 1731571250L,
            ),
        )

    fun balance(): String =
        ZovJson.encodeToString(
            BalanceDto(
                available = "45320.00",
                total = "47800.00",
                blocked = "2480.00",
            ),
        )

    fun balanceAfterWithdraw(): String =
        ZovJson.encodeToString(
            BalanceDto(
                available = "40320.00",
                total = "42800.00",
                blocked = "2480.00",
            ),
        )

    fun userProfile(): String = ZovJson.encodeToString(profileDto)

    fun authLoginResponse(): String =
        ZovJson.encodeToString(
            AuthEnvelopeDto(
                user = profileDto,
                tokens = TokensDto(
                    accessToken = "mock-access-token",
                    refreshToken = "mock-refresh-token",
                    expiresIn = 900,
                ),
            ),
        )

    fun authRegisterResponse(): String = authLoginResponse()

    fun pinChangeOk(): String = ZovJson.encodeToString(PinChangeAckDto(ok = true))
}
