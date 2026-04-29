package com.zovdeneg.app.domain.orders

/** Сервер отклонил заявку из‑за нехватки средств ([openapi.yaml] `INSUFFICIENT_FUNDS`). */
class InsufficientFundsForOrderException(
    override val message: String? = null,
) : Exception(message)
