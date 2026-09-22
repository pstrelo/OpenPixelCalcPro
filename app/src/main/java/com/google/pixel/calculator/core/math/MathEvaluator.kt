package com.google.pixel.calculator.core.math

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Motore di calcolo principale della calcolatrice Pixel.
 * Fornisce valutazione di stringhe, anteprima del risultato in tempo reale
 * e formattazione con separatori di migliaia e notazione scientifica automatica.
 */
class MathEvaluator(
    private val parser: ExpressionParser = ExpressionParser()
) {

    data class EvaluationResult(
        val isSuccess: Boolean,
        val formattedResult: String,
        val rawValue: BigDecimal? = null,
        val ast: MathNode? = null,
        val errorMessage: String? = null
    )

    fun autoCloseParentheses(expression: String): String {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) return ""

        val openCount = trimmed.count { it == '(' }
        val closeCount = trimmed.count { it == ')' }

        if (openCount > closeCount) {
            val lastChar = trimmed.last()
            // Se termina con un operatore incompleto o con '(', non chiudiamo ancora
            if (lastChar == '(' || lastChar in "+-×÷/*^,ʸ√") {
                return trimmed
            }
            val missing = openCount - closeCount
            return trimmed + ")".repeat(missing)
        }
        return trimmed
    }

    fun evaluate(expression: String, context: EvaluationContext = EvaluationContext.DEFAULT): EvaluationResult {
        val exprToEval = autoCloseParentheses(expression)
        if (exprToEval.isBlank()) {
            return EvaluationResult(isSuccess = true, formattedResult = "0", rawValue = BigDecimal.ZERO)
        }

        return try {
            val ast = parser.parse(exprToEval)
            val result = ast.evaluate(context)
            val formatted = formatResult(result)
            EvaluationResult(
                isSuccess = true,
                formattedResult = formatted,
                rawValue = result,
                ast = ast
            )
        } catch (e: ArithmeticException) {
            EvaluationResult(
                isSuccess = false,
                formattedResult = "Errore",
                errorMessage = e.message ?: "Errore aritmetico"
            )
        } catch (t: Throwable) {
            EvaluationResult(
                isSuccess = false,
                formattedResult = "Sintassi non valida",
                errorMessage = t.message ?: "Errore di calcolo o sintassi"
            )
        }
    }

    /**
     * Formattazione del risultato ad alta leggibilità:
     * - Rimuove zeri decimali superflui
     * - Passa a notazione scientifica se il numero supera 12 cifre intere o è infinitesimo
     */
    fun formatResult(value: BigDecimal, maxDecimals: Int = 10): String {
        // Se il valore è un intero o con zeri finali, stripTrailingZeros
        val stripped = try {
            value.stripTrailingZeros()
        } catch (e: Exception) {
            value
        }

        val absVal = stripped.abs()
        val plain = stripped.toPlainString()

        // Se è troppo grande o troppo vicino a zero, usa notazione scientifica
        if (absVal > BigDecimal("1e12") || (absVal < BigDecimal("1e-6") && absVal > BigDecimal.ZERO)) {
            val symbols = DecimalFormatSymbols(Locale.ITALY)
            val df = DecimalFormat("0.######E0", symbols)
            return df.format(stripped)
        }

        // Altrimenti formattazione decimale classica con separatori di migliaia
        val parts = plain.split(".")
        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1) parts[1] else null

        val sb = StringBuilder()
        val isNegative = integerPart.startsWith("-")
        val digits = if (isNegative) integerPart.substring(1) else integerPart

        for (i in digits.indices) {
            if (i > 0 && (digits.length - i) % 3 == 0) {
                sb.append(".")
            }
            sb.append(digits[i])
        }

        val formattedInt = if (isNegative) "-$sb" else sb.toString()

        return if (decimalPart != null) {
            val limitedDec = if (decimalPart.length > maxDecimals) {
                decimalPart.substring(0, maxDecimals).trimEnd('0')
            } else {
                decimalPart
            }
            if (limitedDec.isNotEmpty()) "$formattedInt,$limitedDec" else formattedInt
        } else {
            formattedInt
        }
    }
}
