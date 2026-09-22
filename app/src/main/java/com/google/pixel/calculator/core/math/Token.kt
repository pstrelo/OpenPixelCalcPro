package com.google.pixel.calculator.core.math

/**
 * Token types supported by the mathematical expression parser.
 */
sealed class Token {
    data class Number(val value: String) : Token()
    data class Variable(val name: String) : Token()
    data class Operator(val symbol: String, val precedence: Int, val isRightAssociative: Boolean = false) : Token()
    data class Function(val name: String) : Token()
    object OpenParen : Token()
    object CloseParen : Token()
    object Comma : Token()
    
    // 2D and Advanced tokens
    object FractionStart : Token()
    object FractionBar : Token()
    object FractionEnd : Token()
    object Sqrt : Token()
}
