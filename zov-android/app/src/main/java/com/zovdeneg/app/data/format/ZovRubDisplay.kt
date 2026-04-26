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

    /**
     * Процент для UI: всегда ровно 2 знака после запятой, десятичный разделитель «,».
     * Пример: `0.00000000` → `0,00%`, `-1.2` → `−1,20%`.
     */
    fun formatPercentTwoDecimals(raw: String): String {
        val normalized =
            raw.trim()
                .removeSuffix("%")
                .trim()
                .replace(',', '.')
                .removePrefix("+")
        if (normalized.isEmpty()) return "0,00%"
        val negative = normalized.startsWith("-")
        val unsigned = normalized.removePrefix("-")
        val bd =
            runCatching { BigDecimal(unsigned) }.getOrNull()
                ?: return "${raw.trim()}%"
        val scaled = bd.setScale(2, RoundingMode.HALF_UP)
        val plain = scaled.toPlainString()
        val dot = plain.indexOf('.')
        val intRaw = if (dot < 0) plain else plain.substring(0, dot)
        val frac = if (dot < 0) "00" else plain.substring(dot + 1).padEnd(2, '0').take(2)
        val grouped = intRaw.ifEmpty { "0" }.reversed().chunked(3).joinToString(" ").reversed()
        val sign = if (negative) "−" else ""
        return "$sign$grouped,$frac%"
    }
}
