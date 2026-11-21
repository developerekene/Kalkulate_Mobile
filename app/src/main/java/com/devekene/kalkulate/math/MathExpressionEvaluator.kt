package com.devekene.kalkulate.math

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.*

sealed interface EvaluationResult {
  data class Success(val value: Double, val formatted: String) : EvaluationResult
  data class Error(val message: String) : EvaluationResult
}

/**
 * Precision recursive-descent scientific expression evaluator.
 * Supports:
 * - Basic arithmetic: +, -, *, /, ×, ÷, %, mod
 * - Powers & roots: ^, √, ∛, yroot(x, y)
 * - Trig (DEG / RAD / GRAD): sin, cos, tan, csc, sec, cot
 * - Inverse trig: asin, acos, atan, acsc, asec, acot (and sin⁻¹, cos⁻¹, tan⁻¹)
 * - Hyperbolic & inverse hyperbolic: sinh, cosh, tanh, csch, sech, coth, asinh, acosh, atanh
 * - Logarithms & exponentials: ln, log, log2, exp, pow10, pow2
 * - Combinatorics & utilities: nCr, nPr, ! (factorial + Gamma for non-integers), abs, floor, ceil
 * - Constants & variables: π, pi, e, φ, phi, Ans, x (for function graphing)
 * - Implicit multiplication: 2π, 2(3+4), (2+1)(3+4), 3sin(30), 2x
 */
class MathExpressionEvaluator {

  fun evaluate(
    expression: String,
    angleMode: AngleMode = AngleMode.DEG,
    ansValue: Double = 0.0,
    xValue: Double? = null,
    precision: Int = 10,
  ): EvaluationResult {
    val trimmed = expression.trim()
    if (trimmed.isEmpty()) {
      return EvaluationResult.Error("Empty expression")
    }

    return try {
      val tokens = tokenize(autoCloseParentheses(trimmed))
      if (tokens.isEmpty()) {
        return EvaluationResult.Error("Empty expression")
      }
      val parser = Parser(tokens, angleMode, ansValue, xValue)
      val raw = parser.parseExpression()
      if (!parser.isAtEnd()) {
        return EvaluationResult.Error("Syntax Error")
      }
      if (raw.isNaN() || raw.isInfinite()) {
        return EvaluationResult.Error("Math Error")
      }
      val cleaned = cleanFloatingPointNoise(raw)
      EvaluationResult.Success(cleaned, formatNumber(cleaned, precision))
    } catch (e: MathDomainException) {
      EvaluationResult.Error(e.message ?: "Math Error")
    } catch (_: IllegalArgumentException) {
      EvaluationResult.Error("Syntax Error")
    } catch (_: Exception) {
      EvaluationResult.Error("Math Error")
    }
  }

  fun formatNumber(value: Double, precision: Int = 10): String {
    if (value.isNaN() || value.isInfinite()) return "Error"
    val cleaned = cleanFloatingPointNoise(value)
    if (cleaned == 0.0) return "0"

    val absVal = abs(cleaned)
    val clampedPrecision = precision.coerceIn(2, 12)

    if (absVal >= 1e12 || absVal < 1e-7) {
      val mc = MathContext(clampedPrecision, RoundingMode.HALF_UP)
      val bd = BigDecimal.valueOf(cleaned).round(mc).stripTrailingZeros()
      val eng = bd.toString()
      return eng
    }

    val bd =
      BigDecimal.valueOf(cleaned)
        .setScale(clampedPrecision, RoundingMode.HALF_UP)
        .stripTrailingZeros()
    val plain = bd.toPlainString()
    return if (plain == "-0") "0" else plain
  }

  private fun cleanFloatingPointNoise(value: Double): Double {
    if (abs(value) < 1e-13) return 0.0
    val rounded = value.roundToLong()
    if (abs(value - rounded.toDouble()) < 1e-12 && abs(value) < 1e15) {
      return rounded.toDouble()
    }
    return value
  }

  private fun autoCloseParentheses(input: String): String {
    var openCount = 0
    for (ch in input) {
      if (ch == '(') openCount++
      else if (ch == ')' && openCount > 0) openCount--
    }
    return if (openCount > 0) input + ")".repeat(openCount) else input
  }

  private sealed interface Token {
    data class Number(val value: Double) : Token
    data class Identifier(val name: String) : Token
    data class Operator(val symbol: String) : Token
    data object LeftParen : Token
    data object RightParen : Token
    data object Comma : Token
  }

  private fun tokenize(input: String): List<Token> {
    val rawTokens = mutableListOf<Token>()
    var i = 0
    val normalized =
      input
        .replace('×', '*')
        .replace('÷', '/')
        .replace('−', '-')
        .replace("sin⁻¹", "asin")
        .replace("cos⁻¹", "acos")
        .replace("tan⁻¹", "atan")
        .replace("sinh⁻¹", "asinh")
        .replace("cosh⁻¹", "acosh")
        .replace("tanh⁻¹", "atanh")
        .replace("log₁₀", "log")
        .replace("log₂", "log2")
        .replace("√", "sqrt")
        .replace("∛", "cbrt")
        .replace(",", "")

    while (i < normalized.length) {
      val ch = normalized[i]
      when {
        ch.isWhitespace() -> i++
        ch == '(' -> {
          rawTokens += Token.LeftParen
          i++
        }
        ch == ')' -> {
          rawTokens += Token.RightParen
          i++
        }
        ch == ';' -> {
          rawTokens += Token.Comma
          i++
        }
        ch == 'π' -> {
          rawTokens += Token.Identifier("pi")
          i++
        }
        ch == 'φ' -> {
          rawTokens += Token.Identifier("phi")
          i++
        }
        ch.isDigit() || ch == '.' -> {
          val start = i
          var hasDot = false
          while (i < normalized.length && (normalized[i].isDigit() || (normalized[i] == '.' && !hasDot))) {
            if (normalized[i] == '.') hasDot = true
            i++
          }
          // Scientific notation e.g. 1.5E-4 or 2e10
          if (
            i < normalized.length &&
              (normalized[i] == 'E' || normalized[i] == 'e') &&
              i + 1 < normalized.length &&
              (normalized[i + 1].isDigit() ||
                ((normalized[i + 1] == '+' || normalized[i + 1] == '-') &&
                  i + 2 < normalized.length &&
                  normalized[i + 2].isDigit()))
          ) {
            i++ // skip E/e
            if (normalized[i] == '+' || normalized[i] == '-') i++
            while (i < normalized.length && normalized[i].isDigit()) i++
          }
          val numStr = normalized.substring(start, i)
          val value = numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number")
          rawTokens += Token.Number(value)
        }
        ch.isLetter() -> {
          val start = i
          while (i < normalized.length && (normalized[i].isLetterOrDigit() || normalized[i] == '_')) {
            i++
          }
          val word = normalized.substring(start, i)
          when (word.lowercase()) {
            "mod" -> rawTokens += Token.Operator("mod")
            "ncr" -> rawTokens += Token.Operator("nCr")
            "npr" -> rawTokens += Token.Operator("nPr")
            else -> rawTokens += Token.Identifier(word)
          }
        }
        ch in listOf('+', '-', '*', '/', '^', '!', '%') -> {
          rawTokens += Token.Operator(ch.toString())
          i++
        }
        else -> throw IllegalArgumentException("Unexpected character: $ch")
      }
    }

    // Insert implicit multiplication tokens where mathematically implied
    val result = mutableListOf<Token>()
    for (idx in rawTokens.indices) {
      val current = rawTokens[idx]
      if (result.isNotEmpty()) {
        val prev = result.last()
        val prevEndsValue =
          prev is Token.Number ||
            prev is Token.RightParen ||
            (prev is Token.Operator && (prev.symbol == "!" || prev.symbol == "%")) ||
            (prev is Token.Identifier && isConstantOrVar(prev.name))
        val currStartsValue =
          current is Token.Number ||
            current is Token.LeftParen ||
            current is Token.Identifier
        if (prevEndsValue && currStartsValue) {
          result += Token.Operator("*")
        }
      }
      result += current
    }
    return result
  }

  private fun isConstantOrVar(name: String): Boolean {
    return when (name.lowercase()) {
      "pi", "e", "phi", "ans", "x" -> true
      else -> false
    }
  }

  private class MathDomainException(message: String) : RuntimeException(message)

  private class Parser(
    private val tokens: List<Token>,
    private val angleMode: AngleMode,
    private val ansValue: Double,
    private val xValue: Double?,
  ) {
    private var pos = 0

    fun isAtEnd(): Boolean = pos >= tokens.size

    private fun peek(): Token? = tokens.getOrNull(pos)

    private fun consume(): Token = tokens[pos++]

    private fun matchOperator(vararg ops: String): String? {
      val token = peek()
      if (token is Token.Operator && token.symbol in ops) {
        pos++
        return token.symbol
      }
      return null
    }

    fun parseExpression(): Double {
      var left = parseTerm()
      while (true) {
        val op = matchOperator("+", "-") ?: break
        val right = parseTerm()
        left = if (op == "+") left + right else left - right
      }
      return left
    }

    private fun parseTerm(): Double {
      var left = parseCombinatoric()
      while (true) {
        val op = matchOperator("*", "/", "mod") ?: break
        val right = parseCombinatoric()
        left =
          when (op) {
            "*" -> left * right
            "/" -> {
              if (right == 0.0) throw MathDomainException("Division by 0")
              left / right
            }
            "mod" -> {
              if (right == 0.0) throw MathDomainException("Modulo by 0")
              left % right
            }
            else -> left
          }
      }
      return left
    }

    private fun parseCombinatoric(): Double {
      var left = parsePower()
      while (true) {
        val op = matchOperator("nCr", "nPr") ?: break
        val right = parsePower()
        left =
          when (op) {
            "nCr" -> combinations(left, right)
            "nPr" -> permutations(left, right)
            else -> left
          }
      }
      return left
    }

    private fun parsePower(): Double {
      val base = parseUnary()
      val op = matchOperator("^")
      return if (op != null) {
        val exponent = parsePower() // Right-associative
        if (base < 0 && exponent != floor(exponent)) {
          throw MathDomainException("Imaginary result")
        }
        base.pow(exponent)
      } else {
        base
      }
    }

    private fun parseUnary(): Double {
      val op = matchOperator("+", "-")
      if (op != null) {
        val value = parseUnary()
        return if (op == "-") -value else value
      }
      return parsePostfix()
    }

    private fun parsePostfix(): Double {
      var value = parsePrimary()
      while (true) {
        val op = matchOperator("!", "%") ?: break
        value =
          when (op) {
            "!" -> factorial(value)
            "%" -> value / 100.0
            else -> value
          }
      }
      return value
    }

    private fun parsePrimary(): Double {
      val token = peek() ?: throw IllegalArgumentException("Unexpected end of expression")
      return when (token) {
        is Token.Number -> {
          consume()
          token.value
        }
        is Token.LeftParen -> {
          consume()
          val expr = parseExpression()
          if (peek() != Token.RightParen) {
            throw IllegalArgumentException("Missing closing parenthesis")
          }
          consume()
          expr
        }
        is Token.Identifier -> {
          consume()
          val name = token.name.lowercase()
          when (name) {
            "pi" -> Math.PI
            "e" -> Math.E
            "phi" -> (1.0 + sqrt(5.0)) / 2.0
            "ans" -> ansValue
            "x" -> xValue ?: throw IllegalArgumentException("Unknown variable x")
            "rand" -> {
              if (peek() == Token.LeftParen) {
                consume()
                if (peek() == Token.RightParen) consume()
              }
              Math.random()
            }
            else -> {
              val arg: Double
              val secondArg: Double?
              if (peek() == Token.LeftParen) {
                consume()
                arg = parseExpression()
                if (peek() == Token.Comma) {
                  consume()
                  secondArg = parseExpression()
                } else {
                  secondArg = null
                }
                if (peek() != Token.RightParen) {
                  throw IllegalArgumentException("Missing closing parenthesis")
                }
                consume()
              } else {
                // Allow prefix functions without parens like sin30 or √9
                arg = parseUnary()
                secondArg = null
              }
              applyFunction(name, arg, secondArg)
            }
          }
        }
        else -> throw IllegalArgumentException("Unexpected token: $token")
      }
    }

    private fun toRadians(angle: Double): Double =
      when (angleMode) {
        AngleMode.DEG -> Math.toRadians(angle)
        AngleMode.RAD -> angle
        AngleMode.GRAD -> angle * Math.PI / 200.0
      }

    private fun fromRadians(rad: Double): Double =
      when (angleMode) {
        AngleMode.DEG -> Math.toDegrees(rad)
        AngleMode.RAD -> rad
        AngleMode.GRAD -> rad * 200.0 / Math.PI
      }

    private fun applyFunction(name: String, x: Double, y: Double?): Double {
      return when (name) {
        "sin" -> sin(toRadians(x))
        "cos" -> cos(toRadians(x))
        "tan" -> {
          val rad = toRadians(x)
          if (abs(cos(rad)) < 1e-15) throw MathDomainException("Undefined (tan)")
          tan(rad)
        }
        "csc" -> {
          val s = sin(toRadians(x))
          if (abs(s) < 1e-15) throw MathDomainException("Undefined (csc)")
          1.0 / s
        }
        "sec" -> {
          val c = cos(toRadians(x))
          if (abs(c) < 1e-15) throw MathDomainException("Undefined (sec)")
          1.0 / c
        }
        "cot" -> {
          val rad = toRadians(x)
          val s = sin(rad)
          if (abs(s) < 1e-15) throw MathDomainException("Undefined (cot)")
          cos(rad) / s
        }
        "asin" -> {
          if (x < -1.0 || x > 1.0) throw MathDomainException("Domain: [-1, 1]")
          fromRadians(asin(x))
        }
        "acos" -> {
          if (x < -1.0 || x > 1.0) throw MathDomainException("Domain: [-1, 1]")
          fromRadians(acos(x))
        }
        "atan" -> fromRadians(atan(x))
        "acsc" -> {
          if (abs(x) < 1.0) throw MathDomainException("Domain: |x| >= 1")
          fromRadians(asin(1.0 / x))
        }
        "asec" -> {
          if (abs(x) < 1.0) throw MathDomainException("Domain: |x| >= 1")
          fromRadians(acos(1.0 / x))
        }
        "acot" -> {
          if (x == 0.0) return fromRadians(Math.PI / 2.0)
          fromRadians(atan(1.0 / x))
        }
        "sinh" -> sinh(x)
        "cosh" -> cosh(x)
        "tanh" -> tanh(x)
        "csch" -> {
          val s = sinh(x)
          if (abs(s) < 1e-15) throw MathDomainException("Undefined (csch)")
          1.0 / s
        }
        "sech" -> 1.0 / cosh(x)
        "coth" -> {
          val s = sinh(x)
          if (abs(s) < 1e-15) throw MathDomainException("Undefined (coth)")
          cosh(x) / s
        }
        "asinh" -> asinh(x)
        "acosh" -> {
          if (x < 1.0) throw MathDomainException("Domain: x >= 1")
          acosh(x)
        }
        "atanh" -> {
          if (x <= -1.0 || x >= 1.0) throw MathDomainException("Domain: (-1, 1)")
          atanh(x)
        }
        "ln" -> {
          if (x <= 0.0) throw MathDomainException("Domain: x > 0")
          ln(x)
        }
        "log", "log10" -> {
          if (x <= 0.0) throw MathDomainException("Domain: x > 0")
          log10(x)
        }
        "log2" -> {
          if (x <= 0.0) throw MathDomainException("Domain: x > 0")
          log2(x)
        }
        "exp" -> exp(x)
        "pow10" -> 10.0.pow(x)
        "pow2" -> 2.0.pow(x)
        "sqrt" -> {
          if (x < 0.0) throw MathDomainException("Domain: x >= 0")
          sqrt(x)
        }
        "cbrt" -> cbrt(x)
        "yroot" -> {
          val rootDeg = y ?: throw IllegalArgumentException("yroot requires 2 arguments")
          if (rootDeg == 0.0) throw MathDomainException("0th root undefined")
          if (x < 0.0) {
            if (rootDeg % 2.0 != 0.0 && rootDeg == floor(rootDeg)) {
              -((-x).pow(1.0 / rootDeg))
            } else {
              throw MathDomainException("Imaginary result")
            }
          } else {
            x.pow(1.0 / rootDeg)
          }
        }
        "abs" -> abs(x)
        "floor" -> floor(x)
        "ceil" -> ceil(x)
        "round" -> round(x)
        else -> throw IllegalArgumentException("Unknown function: $name")
      }
    }

    private fun factorial(n: Double): Double {
      if (n < 0 && n == floor(n)) throw MathDomainException("Factorial undefined for < 0")
      if (n > 170.0) throw MathDomainException("Overflow")
      if (n == floor(n)) {
        var res = 1.0
        for (k in 2..n.toInt()) {
          res *= k.toDouble()
        }
        return res
      }
      // Lanczos approximation for Gamma(n + 1)
      return gamma(n + 1.0)
    }

    private fun gamma(z: Double): Double {
      val p =
        doubleArrayOf(
          676.5203681218851,
          -1259.1392167224028,
          771.32342877765313,
          -176.61502916214059,
          12.507343278686905,
          -0.13857109526572012,
          9.9843695780195716e-6,
          1.5056327351493116e-7,
        )
      if (z < 0.5) {
        return Math.PI / (sin(Math.PI * z) * gamma(1.0 - z))
      }
      val t = z - 1.0
      var x = 0.99999999999980993
      for (i in p.indices) {
        x += p[i] / (t + i + 1.0)
      }
      val w = t + p.size - 0.5
      return sqrt(2.0 * Math.PI) * w.pow(t + 0.5) * exp(-w) * x
    }

    private fun combinations(n: Double, r: Double): Double {
      if (n < 0 || r < 0 || r > n || n != floor(n) || r != floor(r)) {
        throw MathDomainException("Invalid nCr arguments")
      }
      val ni = n.toInt()
      var ri = r.toInt()
      if (ri > ni - ri) ri = ni - ri
      var res = 1.0
      for (i in 1..ri) {
        res = res * (ni - ri + i) / i
      }
      return res
    }

    private fun permutations(n: Double, r: Double): Double {
      if (n < 0 || r < 0 || r > n || n != floor(n) || r != floor(r)) {
        throw MathDomainException("Invalid nPr arguments")
      }
      val ni = n.toInt()
      val ri = r.toInt()
      var res = 1.0
      for (i in 0 until ri) {
        res *= (ni - i).toDouble()
      }
      return res
    }
  }
}
