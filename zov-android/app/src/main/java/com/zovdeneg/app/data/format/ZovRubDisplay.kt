package com.zovdeneg.app.data.format

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Отображение сумм в ₽: до 2 знаков после запятой с округлением,
 * без лишних нулей (целые рубли без «,00»).
 */
internal object ZovRubDisplay {
    /** Со знаком из строки API (`-12.3`, `+5`, `100`). */
    fun formatSignedDecimalRubLine(raw: String): String {
        val trimmed = raw.trim().removeSuffix("₽").trim().replace(',', '.')
        if (trimmed.isEmpty()) return "0 ₽"
        val bd = runCatching { BigDecimal(trimmed) }.getOrNull() ?: return "${raw.trim()} ₽"
        return formatSignedRub(bd.signum() < 0, bd.abs())
    }

    fun formatApiDecimalToRubLine(raw: String): String {
        val cleaned = raw.trim().removeSuffix("₽").trim()
        if (cleaned.isEmpty()) return "0 ₽"
        val negative = cleaned.startsWith("-")
        val unsigned = cleaned.removePrefix("-").replace(',', '.')
        val bd =
            runCatching { BigDecimal(unsigned) }.getOrNull()
                ?: return "${raw.trim()} ₽"
        return formatSignedRub(negative, bd)
    }

    private fun formatSignedRub(negative: Boolean, amount: BigDecimal): String {
        val sign = if (negative) "−" else ""
        val abs = amount.abs()
        val rounded = abs.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros()
        val plain = rounded.toPlainString()
        val dot = plain.indexOf('.')
        val intDigits = if (dot < 0) plain else plain.substring(0, dot)
        val fracRaw = if (dot < 0 || rounded.scale() == 0) "" else plain.substring(dot + 1)
        val fracDigits = fracRaw.trimEnd('0')
        val grouped = intDigits.ifEmpty { "0" }.reversed().chunked(3).joinToString(" ").reversed()
        val body =
            if (fracDigits.isEmpty()) {
                grouped
            } else {
                "$grouped,$fracDigits"
            }
        return "$sign$body ₽"
    }
}
