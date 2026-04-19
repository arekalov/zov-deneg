#!/usr/bin/env python3
"""Writes mock JSON files under app/src/main/assets/mock/ (UTF-8)."""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "app" / "src" / "main" / "assets" / "mock"


def write(name: str, obj: object) -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    path = OUT / name
    path.write_text(json.dumps(obj, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(path)


def main() -> None:
    write(
        "portfolio_summary.json",
        {
            "portfolioAmountRub": "4 876 234,56 ₽",
            "totalGainText": "+234 567,89 ₽ (+5,1%)",
        },
    )

    write(
        "balance.json",
        {"available": "45320.00", "total": "47800.00", "blocked": "2480.00"},
    )
    write(
        "balance_after_withdraw.json",
        {"available": "40320.00", "total": "42800.00", "blocked": "2480.00"},
    )

    write(
        "user_profile.json",
        {
            "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            "firstName": "Иван",
            "lastName": "Иванов",
            "email": "ivan@example.com",
            "phone": "+79001234567",
            "role": "user",
            "isBlocked": False,
            "createdAt": 1731484800,
            "updatedAt": 1731571200,
        },
    )

    write(
        "auth_envelope.json",
        {
            "user": {
                "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                "firstName": "Иван",
                "lastName": "Иванов",
                "email": "ivan@example.com",
                "phone": "+79001234567",
                "role": "user",
                "isBlocked": False,
                "createdAt": 1731484800,
                "updatedAt": 1731571200,
            },
            "tokens": {
                "accessToken": "mock-access-token",
                "refreshToken": "mock-refresh-token",
                "expiresIn": 900,
            },
        },
    )

    write("pin_change_ok.json", {"ok": True})

    write(
        "order_created.json",
        {
            "id": "e1eebc99-9c0b-4ef8-bb6d-6bb9bd380a21",
            "securityId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
            "ticker": "SBER",
            "type": "market",
            "side": "buy",
            "status": "executed",
            "quantity": 10,
            "executedPrice": "298.45",
            "executedQuantity": 10,
            "totalAmount": "2984.50",
            "commission": "8.95",
            "createdAt": 1731571200,
            "updatedAt": 1731571250,
        },
    )

    popular_items = [
        ("GAZP", "Газпром · энергетика", "167,4 ₽", "+0,8%", True, "stock"),
        ("SBER", "Сбербанк · финансы", "298,12 ₽", "+1,2%", True, "stock"),
        ("LKOH", "ЛУКОЙЛ · энергетика", "6 542 ₽", "−0,4%", False, "stock"),
        ("ROSN", "Роснефть · энергетика", "512,3 ₽", "+0,5%", True, "stock"),
        ("NVTK", "Новатэк · энергетика", "1 284,5 ₽", "−0,2%", False, "stock"),
        ("GMKN", "Норникель · металлы", "15 890 ₽", "+1,1%", True, "stock"),
        ("PLZL", "Полюс · золото", "11 234 ₽", "+0,3%", True, "stock"),
        ("TATNP", "Татнефть pref · энергетика", "598,0 ₽", "+0,4%", True, "stock"),
        ("TATN", "Татнефть · энергетика", "642,1 ₽", "+0,6%", True, "stock"),
        ("MGNT", "Магнит · ритейл", "7 015 ₽", "−0,9%", False, "stock"),
        ("YDEX", "Яндекс · IT", "4 128 ₽", "+2,1%", True, "stock"),
        ("VTBR", "ВТБ · финансы", "0,0284 ₽", "+3,2%", True, "stock"),
        ("MOEX", "МосБиржа · финансы", "214,8 ₽", "+0,4%", True, "stock"),
        ("CHMF", "Северсталь · металлы", "1 098 ₽", "−0,3%", False, "stock"),
        ("PHOR", "ФосАгро · химия", "7 456 ₽", "+0,7%", True, "stock"),
        ("ALRS", "Алроса · добыча", "76,42 ₽", "+1,5%", True, "stock"),
        ("SNGS", "Сургутнефтегаз · энергетика", "28,15 ₽", "0,0%", True, "stock"),
        ("SNGSP", "Сургутнефтегаз pref", "45,02 ₽", "+0,2%", True, "stock"),
        ("FEES", "Россети · электроэнергетика", "0,1042 ₽", "−0,5%", False, "stock"),
        ("HYDR", "РусГидро · электроэнергетика", "0,542 ₽", "+0,1%", True, "stock"),
        ("IRAO", "Интер РАО · электроэнергетика", "3,12 ₽", "−0,3%", False, "stock"),
        ("MTSS", "МТС · телеком", "234,5 ₽", "+0,8%", True, "stock"),
        ("RTKM", "Ростелеком · телеком", "72,8 ₽", "−0,2%", False, "stock"),
        ("AFLT", "Аэрофлот · транспорт", "58,34 ₽", "+1,1%", True, "stock"),
        ("PIKK", "ПИК · девелопмент", "678 ₽", "+0,5%", True, "stock"),
        ("LSRG", "ЛСР · девелопмент", "712 ₽", "−0,7%", False, "stock"),
        ("POLY", "Polymetal · металлы", "412 ₽", "+0,9%", True, "stock"),
        ("RUAL", "Русал · металлы", "38,90 ₽", "−1,2%", False, "stock"),
        ("ENRU", "Энел Россия · электроэнергетика", "0,7156 ₽", "+0,4%", True, "stock"),
        ("TRNFP", "Транснефть pref · энергетика", "1 456 ₽", "+0,1%", True, "stock"),
        ("BSPB", "Банк Санкт-Петербург · финансы", "312 ₽", "+2,4%", True, "stock"),
        ("MVID", "М.Видео · ритейл", "98,5 ₽", "−0,8%", False, "stock"),
        ("OZON", "Ozon · e-commerce", "4 890 ₽", "+4,2%", True, "stock"),
        ("POSI", "Positive Technologies · IT", "612 ₽", "+1,0%", True, "stock"),
        ("SOFL", "Софтлайн · IT", "98,2 ₽", "−2,1%", False, "stock"),
        ("SPBE", "СПБ Биржа · финансы", "124,3 ₽", "+0,6%", True, "stock"),
        ("TCSG", "Т-Технологии · финансы", "3 012 ₽", "+0,9%", True, "stock"),
        ("VKCO", "VK · IT", "312 ₽", "+0,3%", True, "stock"),
        ("X5", "X5 Group · ритейл", "3 156 ₽", "−0,6%", False, "stock"),
        ("MAGN", "ММК · металлы", "42,15 ₽", "+0,2%", True, "stock"),
        ("AFKS", "Система · финансы", "15,42 ₽", "+0,5%", True, "stock"),
        ("APTK", "Аптеки36,6 · ритейл", "8,12 ₽", "−0,4%", False, "stock"),
        ("OGKB", "ОГК-2 · электроэнергетика", "0,612 ₽", "+0,9%", True, "stock"),
        ("CBOM", "МКБ · финансы", "98,4 ₽", "+1,3%", True, "stock"),
        ("MDMG", "МДМ Групп · девелопмент", "156 ₽", "−0,2%", False, "stock"),
        ("SIBN", "Газпром нефть · энергетика", "412 ₽", "+0,6%", True, "stock"),
        ("SU26238RMFS5", "ОФЗ 26238", "98,2 ₽", "−0,1%", False, "bond"),
        ("SU26233RMFS5", "ОФЗ 26233", "101,4 ₽", "+0,05%", True, "bond"),
        ("SU26229RMFS4", "ОФЗ 26229", "95,8 ₽", "−0,2%", False, "bond"),
        ("LQDT", "Видео · IT", "6 540 ₽", "+0,3%", True, "etf"),
        ("SBGB", "Сберегательный ETF · облигации", "12,45 ₽", "+0,1%", True, "etf"),
        ("TMOS", "Тинькофф голубые фишки", "18,92 ₽", "+0,4%", True, "etf"),
    ]
    write(
        "popular_securities.json",
        {
            "items": [
                {
                    "ticker": t[0],
                    "subtitle": t[1],
                    "valueText": t[2],
                    "deltaText": t[3],
                    "deltaPositive": t[4],
                    "kind": t[5],
                }
                for t in popular_items
            ]
        },
    )

    holdings = [
        ("SBER", "120 шт. · ср. 276 ₽", "33 120 ₽", "+2 640 ₽", True),
        ("LKOH", "8 шт. · ср. 6 100 ₽", "52 336 ₽", "+2 536 ₽", True),
        ("GAZP", "400 шт. · ср. 162 ₽", "66 960 ₽", "+1 920 ₽", True),
        ("ROSN", "60 шт. · ср. 498 ₽", "30 738 ₽", "+858 ₽", True),
        ("NVTK", "15 шт. · ср. 1 260 ₽", "19 267 ₽", "−101 ₽", False),
        ("GMKN", "4 шт. · ср. 15 200 ₽", "63 560 ₽", "+2 760 ₽", True),
        ("PLZL", "3 шт. · ср. 10 900 ₽", "32 802 ₽", "+102 ₽", True),
        ("TATN", "25 шт. · ср. 618 ₽", "16 052 ₽", "+602 ₽", True),
        ("MGNT", "5 шт. · ср. 7 200 ₽", "35 075 ₽", "−925 ₽", False),
        ("YDEX", "10 шт. · ср. 3 900 ₽", "41 280 ₽", "+2 280 ₽", True),
        ("VTBR", "500 000 шт. · ср. 0,027 ₽", "14 200 ₽", "+700 ₽", True),
        ("MOEX", "80 шт. · ср. 205 ₽", "17 184 ₽", "+784 ₽", True),
        ("CHMF", "30 шт. · ср. 1 080 ₽", "32 940 ₽", "+540 ₽", True),
        ("PHOR", "4 шт. · ср. 7 300 ₽", "29 824 ₽", "+624 ₽", True),
        ("ALRS", "200 шт. · ср. 72 ₽", "15 284 ₽", "+884 ₽", True),
        ("SNGS", "1 000 шт. · ср. 27,5 ₽", "28 150 ₽", "+150 ₽", True),
        ("FEES", "80 000 шт. · ср. 0,102 ₽", "8 336 ₽", "+176 ₽", True),
        ("OZON", "6 шт. · ср. 4 500 ₽", "29 340 ₽", "+2 340 ₽", True),
        ("TCSG", "7 шт. · ср. 2 850 ₽", "21 084 ₽", "+1 134 ₽", True),
        ("X5", "4 шт. · ср. 3 200 ₽", "12 624 ₽", "+224 ₽", True),
        ("AFLT", "150 шт. · ср. 55 ₽", "8 751 ₽", "+501 ₽", True),
        ("PIKK", "20 шт. · ср. 640 ₽", "13 560 ₽", "+760 ₽", True),
        ("RUAL", "300 шт. · ср. 40 ₽", "11 670 ₽", "−330 ₽", False),
        ("ENRU", "5 000 шт. · ср. 0,70 ₽", "3 578 ₽", "+78 ₽", True),
        ("BSPB", "40 шт. · ср. 285 ₽", "12 480 ₽", "+1 080 ₽", True),
        ("SPBE", "90 шт. · ср. 118 ₽", "11 187 ₽", "+567 ₽", True),
        ("VKCO", "35 шт. · ср. 298 ₽", "10 920 ₽", "+390 ₽", True),
        ("MAGN", "400 шт. · ср. 41 ₽", "16 860 ₽", "+420 ₽", True),
        ("AFKS", "800 шт. · ср. 14,8 ₽", "12 336 ₽", "+496 ₽", True),
        ("LQDT", "2 шт. · ср. 6 400 ₽", "13 080 ₽", "+80 ₽", True),
    ]
    write(
        "portfolio_holdings.json",
        {
            "holdings": [
                {
                    "ticker": h[0],
                    "subtitle": h[1],
                    "valueText": h[2],
                    "deltaText": h[3],
                    "deltaPositive": h[4],
                }
                for h in holdings
            ]
        },
    )

    tx_rows = [
        ("Покупка · SBER", "19 апр. 2026, 15:42", "+35 774 ₽", "purchase"),
        ("Продажа · LKOH", "19 апр. 2026, 11:08", "−52 336 ₽", "sale"),
        ("Дивиденды · GMKN", "18 апр. 2026, 09:00", "+1 240 ₽", "purchase"),
        ("Покупка · OZON", "18 апр. 2026, 14:22", "+29 340 ₽", "purchase"),
        ("Продажа · MGNT", "17 апр. 2026, 16:01", "−35 075 ₽", "sale"),
        ("Покупка · YDEX", "17 апр. 2026, 10:30", "+41 280 ₽", "purchase"),
        ("Покупка · NVTK", "16 апр. 2026, 12:55", "+19 267 ₽", "purchase"),
        ("Продажа · RUAL", "16 апр. 2026, 09:12", "−11 670 ₽", "sale"),
        ("Покупка · TCSG", "15 апр. 2026, 13:40", "+21 084 ₽", "purchase"),
        ("Покупка · VTBR", "15 апр. 2026, 09:05", "+14 200 ₽", "purchase"),
        ("Продажа · X5", "14 апр. 2026, 15:18", "−12 624 ₽", "sale"),
        ("Покупка · ROSN", "14 апр. 2026, 11:00", "+30 738 ₽", "purchase"),
        ("Покупка · GAZP", "13 апр. 2026, 16:44", "+66 960 ₽", "purchase"),
        ("Продажа · AFLT", "13 апр. 2026, 10:20", "−8 751 ₽", "sale"),
        ("Покупка · MOEX", "12 апр. 2026, 14:11", "+17 184 ₽", "purchase"),
        ("Покупка · CHMF", "12 апр. 2026, 09:33", "+32 940 ₽", "purchase"),
        ("Продажа · FEES", "11 апр. 2026, 15:50", "−8 336 ₽", "sale"),
        ("Покупка · PHOR", "11 апр. 2026, 12:08", "+29 824 ₽", "purchase"),
        ("Покупка · PLZL", "10 апр. 2026, 16:02", "+32 802 ₽", "purchase"),
        ("Продажа · ENRU", "10 апр. 2026, 09:45", "−3 578 ₽", "sale"),
        ("Покупка · ALRS", "9 апр. 2026, 13:17", "+15 284 ₽", "purchase"),
        ("Покупка · SNGS", "9 апр. 2026, 09:28", "+28 150 ₽", "purchase"),
        ("Продажа · PIKK", "8 апр. 2026, 15:33", "−13 560 ₽", "sale"),
        ("Покупка · BSPB", "8 апр. 2026, 10:55", "+12 480 ₽", "purchase"),
        ("Покупка · SPBE", "7 апр. 2026, 14:40", "+11 187 ₽", "purchase"),
        ("Продажа · VKCO", "7 апр. 2026, 09:15", "−10 920 ₽", "sale"),
        ("Покупка · MAGN", "6 апр. 2026, 16:08", "+16 860 ₽", "purchase"),
        ("Покупка · AFKS", "6 апр. 2026, 11:22", "+12 336 ₽", "purchase"),
        ("Продажа · LQDT", "5 апр. 2026, 15:01", "−13 080 ₽", "sale"),
        ("Покупка · TATN", "5 апр. 2026, 09:50", "+16 052 ₽", "purchase"),
        ("Покупка · HYDR", "4 апр. 2026, 13:25", "+5 420 ₽", "purchase"),
        ("Продажа · IRAO", "4 апр. 2026, 10:11", "−3 120 ₽", "sale"),
        ("Покупка · MTSS", "3 апр. 2026, 14:58", "+7 035 ₽", "purchase"),
        ("Покупка · RTKM", "3 апр. 2026, 09:02", "+3 640 ₽", "purchase"),
        ("Продажа · CBOM", "2 апр. 2026, 16:30", "−3 936 ₽", "sale"),
        ("Покупка · OGKB", "2 апр. 2026, 12:44", "+3 060 ₽", "purchase"),
        ("Покупка · SU26238RMFS5", "1 апр. 2026, 15:10", "+98 200 ₽", "purchase"),
        ("Продажа · SU26233RMFS5", "1 апр. 2026, 09:30", "−50 700 ₽", "sale"),
        ("Покупка · SBGB", "31 мар. 2026, 14:05", "+12 450 ₽", "purchase"),
        ("Покупка · TMOS", "31 мар. 2026, 10:18", "+18 920 ₽", "purchase"),
        ("Продажа · SOFL", "30 мар. 2026, 16:55", "−4 910 ₽", "sale"),
        ("Покупка · POSI", "30 мар. 2026, 11:40", "+6 120 ₽", "purchase"),
        ("Покупка · MDMG", "29 мар. 2026, 15:22", "+6 240 ₽", "purchase"),
        ("Продажа · MVID", "29 мар. 2026, 09:08", "−4 925 ₽", "sale"),
        ("Покупка · SIBN", "28 мар. 2026, 13:33", "+16 480 ₽", "purchase"),
        ("Покупка · TRNFP", "28 мар. 2026, 09:55", "+14 560 ₽", "purchase"),
        ("Продажа · LSRG", "27 мар. 2026, 16:12", "−7 120 ₽", "sale"),
        ("Покупка · POLY", "27 мар. 2026, 10:44", "+4 120 ₽", "purchase"),
        ("Покупка · SBER", "26 мар. 2026, 14:32", "+2 981 ₽", "purchase"),
        ("Продажа · LKOH", "26 мар. 2026, 10:14", "−13 080 ₽", "sale"),
        ("Покупка · GAZP", "25 мар. 2026, 09:00", "+5 000 ₽", "purchase"),
        ("Продажа · VTBR", "24 мар. 2026, 15:40", "−7 100 ₽", "sale"),
        ("Покупка · FEES", "24 мар. 2026, 11:05", "+8 160 ₽", "purchase"),
        ("Комиссия брокера", "24 мар. 2026, 09:00", "−125 ₽", "sale"),
        ("Покупка · OZON", "23 мар. 2026, 14:18", "+9 780 ₽", "purchase"),
        ("Продажа · YDEX", "23 мар. 2026, 10:22", "−20 640 ₽", "sale"),
        ("Покупка · NVTK", "22 мар. 2026, 16:50", "+12 845 ₽", "purchase"),
        ("Покупка · GMKN", "22 мар. 2026, 09:33", "+31 780 ₽", "purchase"),
        ("Продажа · SNGSP", "21 мар. 2026, 15:11", "−4 502 ₽", "sale"),
        ("Покупка · APTK", "21 мар. 2026, 12:00", "+812 ₽", "purchase"),
        ("Пополнение счёта", "20 мар. 2026, 10:00", "+100 000 ₽", "purchase"),
        ("Вывод средств", "19 мар. 2026, 14:00", "−25 000 ₽", "sale"),
    ]
    write(
        "transactions.json",
        {
            "transactions": [
                {
                    "title": r[0],
                    "date": r[1],
                    "amountText": r[2],
                    "side": r[3],
                }
                for r in tx_rows
            ]
        },
    )

    details: dict[str, dict] = {
        "SBER": {
            "ticker": "SBER",
            "subtitle": "Сбербанк · финансы",
            "priceLine": "298,12 ₽",
            "changeLine": "+1,2% · +3,52 ₽ за день",
            "changePositive": True,
            "securityId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
            "lotSize": 10,
            "orderBookText": "Покупка 298,10 · 298,12 · Продажа 298,14",
        },
        "LKOH": {
            "ticker": "LKOH",
            "subtitle": "ЛУКОЙЛ · энергетика",
            "priceLine": "6 542,00 ₽",
            "changeLine": "−0,4% · −26 ₽ за день",
            "changePositive": False,
            "securityId": "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12",
            "lotSize": 10,
            "orderBookText": "Стакан (мок): лучший bid 6 540, ask 6 545",
        },
        "GAZP": {
            "ticker": "GAZP",
            "subtitle": "Газпром · энергетика",
            "priceLine": "167,40 ₽",
            "changeLine": "+0,8% · +1,32 ₽ за день",
            "changePositive": True,
            "securityId": "c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13",
            "lotSize": 10,
            "orderBookText": "Стакан (мок): bid 167,38 · ask 167,42",
        },
        "ROSN": {
            "ticker": "ROSN",
            "subtitle": "Роснефть · энергетика",
            "priceLine": "512,30 ₽",
            "changeLine": "+0,5% · +2,55 ₽ за день",
            "changePositive": True,
            "securityId": "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a01",
            "lotSize": 10,
            "orderBookText": "512,28 / 512,35 · объём дня 1,2 млн шт",
        },
        "NVTK": {
            "ticker": "NVTK",
            "subtitle": "Новатэк · энергетика",
            "priceLine": "1 284,50 ₽",
            "changeLine": "−0,2% · −2,60 ₽ за день",
            "changePositive": False,
            "securityId": "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a02",
            "lotSize": 1,
            "orderBookText": "1 284,0 — 1 285,0 · спред 1 ₽",
        },
        "GMKN": {
            "ticker": "GMKN",
            "subtitle": "Норникель · металлы",
            "priceLine": "15 890 ₽",
            "changeLine": "+1,1% · +173 ₽ за день",
            "changePositive": True,
            "securityId": "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a03",
            "lotSize": 1,
            "orderBookText": "15 885 / 15 895 · глубина 120 / 85 лотов",
        },
        "PLZL": {
            "ticker": "PLZL",
            "subtitle": "Полюс · золото",
            "priceLine": "11 234 ₽",
            "changeLine": "+0,3% · +34 ₽ за день",
            "changePositive": True,
            "securityId": "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a04",
            "lotSize": 1,
            "orderBookText": "11 230 — 11 240",
        },
        "TATN": {
            "ticker": "TATN",
            "subtitle": "Татнефть · энергетика",
            "priceLine": "642,10 ₽",
            "changeLine": "+0,6% · +3,83 ₽ за день",
            "changePositive": True,
            "securityId": "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a05",
            "lotSize": 1,
            "orderBookText": "641,90 / 642,30",
        },
        "MGNT": {
            "ticker": "MGNT",
            "subtitle": "Магнит · ритейл",
            "priceLine": "7 015 ₽",
            "changeLine": "−0,9% · −64 ₽ за день",
            "changePositive": False,
            "securityId": "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a06",
            "lotSize": 1,
            "orderBookText": "7 010 — 7 020 · после дивидендного отсечения",
        },
        "YDEX": {
            "ticker": "YDEX",
            "subtitle": "Яндекс · IT",
            "priceLine": "4 128 ₽",
            "changeLine": "+2,1% · +85 ₽ за день",
            "changePositive": True,
            "securityId": "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a07",
            "lotSize": 1,
            "orderBookText": "4 125 / 4 130 · ликвидность высокая",
        },
        "VTBR": {
            "ticker": "VTBR",
            "subtitle": "ВТБ · финансы",
            "priceLine": "0,0284 ₽",
            "changeLine": "+3,2% · +0,0009 ₽ за день",
            "changePositive": True,
            "securityId": "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a08",
            "lotSize": 10000,
            "orderBookText": "0,02835 / 0,02845 · пачка 10 000",
        },
        "MOEX": {
            "ticker": "MOEX",
            "subtitle": "МосБиржа · финансы",
            "priceLine": "214,80 ₽",
            "changeLine": "+0,4% · +0,86 ₽ за день",
            "changePositive": True,
            "securityId": "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a09",
            "lotSize": 10,
            "orderBookText": "214,75 / 214,85",
        },
        "CHMF": {
            "ticker": "CHMF",
            "subtitle": "Северсталь · металлы",
            "priceLine": "1 098 ₽",
            "changeLine": "−0,3% · −3,30 ₽ за день",
            "changePositive": False,
            "securityId": "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a0a",
            "lotSize": 10,
            "orderBookText": "1 097,5 — 1 098,5",
        },
        "PHOR": {
            "ticker": "PHOR",
            "subtitle": "ФосАгро · химия",
            "priceLine": "7 456 ₽",
            "changeLine": "+0,7% · +51,90 ₽ за день",
            "changePositive": True,
            "securityId": "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a0b",
            "lotSize": 1,
            "orderBookText": "7 450 / 7 460",
        },
        "ALRS": {
            "ticker": "ALRS",
            "subtitle": "Алроса · добыча",
            "priceLine": "76,42 ₽",
            "changeLine": "+1,5% · +1,13 ₽ за день",
            "changePositive": True,
            "securityId": "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a0c",
            "lotSize": 10,
            "orderBookText": "76,40 / 76,45",
        },
        "SNGS": {
            "ticker": "SNGS",
            "subtitle": "Сургутнефтегаз · энергетика",
            "priceLine": "28,15 ₽",
            "changeLine": "0,0% · 0,00 ₽ за день",
            "changePositive": True,
            "securityId": "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a0d",
            "lotSize": 100,
            "orderBookText": "28,14 / 28,16",
        },
        "FEES": {
            "ticker": "FEES",
            "subtitle": "Россети · электроэнергетика",
            "priceLine": "0,1042 ₽",
            "changeLine": "−0,5% · −0,0005 ₽ за день",
            "changePositive": False,
            "securityId": "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a0e",
            "lotSize": 10000,
            "orderBookText": "0,1040 / 0,1044",
        },
        "OZON": {
            "ticker": "OZON",
            "subtitle": "Ozon · e-commerce",
            "priceLine": "4 890 ₽",
            "changeLine": "+4,2% · +197 ₽ за день",
            "changePositive": True,
            "securityId": "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a0f",
            "lotSize": 1,
            "orderBookText": "4 885 / 4 895",
        },
        "TCSG": {
            "ticker": "TCSG",
            "subtitle": "Т-Технологии · финансы",
            "priceLine": "3 012 ₽",
            "changeLine": "+0,9% · +26,90 ₽ за день",
            "changePositive": True,
            "securityId": "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
            "lotSize": 1,
            "orderBookText": "3 010 / 3 015",
        },
        "X5": {
            "ticker": "X5",
            "subtitle": "X5 Group · ритейл",
            "priceLine": "3 156 ₽",
            "changeLine": "−0,6% · −19 ₽ за день",
            "changePositive": False,
            "securityId": "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380b02",
            "lotSize": 1,
            "orderBookText": "3 152 — 3 160",
        },
        "LQDT": {
            "ticker": "LQDT",
            "subtitle": "Видео · IT (мок ETF)",
            "priceLine": "6 540 ₽",
            "changeLine": "+0,3% · +19,60 ₽ за день",
            "changePositive": True,
            "securityId": "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380b03",
            "lotSize": 1,
            "orderBookText": "ETF: NAV 6 538 · рыночная 6 540",
        },
        "SU26238RMFS5": {
            "ticker": "SU26238RMFS5",
            "subtitle": "ОФЗ 26238 · гособлигация",
            "priceLine": "98,20 ₽",
            "changeLine": "−0,1% · −0,10 ₽ за день",
            "changePositive": False,
            "securityId": "a1eebc99-9c0b-4ef8-bb6d-6bb9bd380b04",
            "lotSize": 1,
            "orderBookText": "Дюрация ~6 лет · доходность к погашению (мок) 12,4%",
        },
    }
    details["__DEFAULT__"] = {
        "ticker": "__TICKER__",
        "subtitle": "__TICKER__ · бумага (расширенный мок)",
        "priceLine": "—",
        "changeLine": "—",
        "changePositive": True,
        "securityId": "d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14",
        "lotSize": 10,
        "orderBookText": "Стакан заявок (заглушка) · bid — · ask — · глубина рынка (мок)",
    }
    write("security_details.json", details)


if __name__ == "__main__":
    main()
