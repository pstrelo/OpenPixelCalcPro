package com.google.pixel.calculator.core.math

import java.math.BigDecimal
import java.math.MathContext

/**
 * Valutatore ad alte prestazioni per il rendering di grafici 2D f(x).
 * Campiona punti su un intervallo di visualizzazione [xMin, xMax].
 * Supporta valutazione con AST pre-parsato per garantire 120 FPS costanti.
 */
class GraphEvaluator(
    private val parser: ExpressionParser = ExpressionParser()
) {

    data class Point(val x: Float, val y: Float)

    fun sampleAst(
        ast: MathNode?,
        xMin: Float,
        xMax: Float,
        steps: Int = 250
    ): List<Point?> {
        if (ast == null) return emptyList()
        val points = mutableListOf<Point?>()

        val stepSize = (xMax - xMin) / steps
        val mc = MathContext.DECIMAL64

        for (i in 0..steps) {
            val x = xMin + i * stepSize
            try {
                val ctx = EvaluationContext(
                    variables = mapOf("x" to BigDecimal(x.toDouble().toString(), mc)),
                    mathContext = mc
                )
                val yVal = ast.evaluate(ctx).toDouble()
                if (yVal.isFinite() && !yVal.isNaN() && kotlin.math.abs(yVal) < 1e6) {
                    points.add(Point(x, yVal.toFloat()))
                } else {
                    points.add(null) // Discontinuità (es. asintoto verticale per 1/x o tan(x))
                }
            } catch (e: Exception) {
                points.add(null) // Discontinuità o valore fuori dominio
            }
        }
        return points
    }

    fun sampleFunction(
        expression: String,
        xMin: Float,
        xMax: Float,
        steps: Int = 250
    ): List<Point?> {
        val ast = try {
            parser.parse(expression)
        } catch (e: Exception) {
            return emptyList()
        }
        return sampleAst(ast, xMin, xMax, steps)
    }
}
