package com.zovdeneg.app.domain.orders

/** Сервер отклонил заявку из‑за нехватки бумаг ([openapi.yaml] `INSUFFICIENT_SECURITIES`). */
class InsufficientSecuritiesForOrderException(
    override val message: String? = null,
) : Exception(message)
