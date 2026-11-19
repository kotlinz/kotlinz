package com.github.kotlinz.kotlinz.data.parser

/** The outcome of running a [Parser] at some position of the input. */
sealed class ParseResult<out A> {
  /**
   * The parser produced [value] and stopped before [position].
   *
   * [hint] is the furthest failure seen at or beyond [position] on the way (e.g. the attempt that
   * ended a `many`); it is merged into the error if parsing fails right afterwards.
   */
  data class Success<out A>(val value: A, val position: Int, val hint: ParseError? = null): ParseResult<A>()
  /** The parser failed with [error]. */
  data class Failure(val error: ParseError): ParseResult<Nothing>()
}

/**
 * A parse failure at the absolute character offset [position], listing what was [expected] there.
 *
 * When alternatives fail, the error that got furthest wins; errors at the same position are merged.
 */
data class ParseError(val position: Int, val expected: Set<String>) {
  /** Keeps the furthest of the two errors, merging their expectations on a tie. */
  infix fun merge(other: ParseError): ParseError = when {
    position > other.position -> this
    position < other.position -> other
    else -> ParseError(position, expected + other.expected)
  }

  /** Renders the error with a 1-based line and column computed from [input]. */
  fun describe(input: String): String {
    val before = input.take(position)
    val line = before.count { it == '\n' } + 1
    val column = position - (before.lastIndexOf('\n') + 1) + 1
    return "line $line, column $column: expected ${expected.joinToString(" or ").ifEmpty { "nothing" }}"
  }
}

internal fun failure(position: Int, expected: String) = ParseResult.Failure(ParseError(position, setOf(expected)))

internal fun <A> success(value: A, position: Int, hint: ParseError?): ParseResult.Success<A> =
  ParseResult.Success(value, position, hint?.takeIf { it.position >= position && it.expected.isNotEmpty() })

internal infix fun ParseError?.mergeHint(other: ParseError?): ParseError? = this?.let { e -> other?.let(e::merge) ?: e } ?: other

/** Carries an earlier failure or hint into the result of the next step. */
internal fun <B> ParseError?.prependTo(next: ParseResult<B>): ParseResult<B> = when (next) {
  is ParseResult.Success -> success(next.value, next.position, this mergeHint next.hint)
  is ParseResult.Failure -> ParseResult.Failure(this?.merge(next.error) ?: next.error)
}
