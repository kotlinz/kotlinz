package com.github.kotlinz.kotlinz.data.parser

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx

/**
 * A backtracking parser: a function from the input and a start offset to a [ParseResult].
 *
 * Failures carry the absolute offset where they happened and what was expected there ([ParseError]).
 * [orElse] backtracks fully, retrying the alternative from the same offset even after the first
 * branch consumed input, and reports the furthest error, including expectations from attempts that
 * ended a repetition (see [ParseResult.Success.hint]); [label] renames or hides expectations.
 * Recursive grammars are tied with [defer].
 * A parser yields at most one result per run, so `fx` (single-shot) is sufficient.
 *
 * Instances: [ParserFunctor], [ParserApplicative], [ParserMonad], [ParserAlternative], [ParserMonadPlus].
 *
 * ```kotlin
 * val digit = Parser.satisfy("digit") { it.isDigit() }
 * val number = Parser.some(digit) fmap { it.joinToString("").toInt() }
 * val list = Parser.char('[') keepRight Parser.sepBy(number, Parser.char(',')) keepLeft Parser.char(']')
 * list.parseAll("[1,22,3]") // Success([1, 22, 3], 8)
 * ```
 *
 * @property run parses `input` starting at the given offset.
 */
class Parser<A>(val run: (String, Int) -> ParseResult<A>): K1<Parser.T, A> {
  class T

  companion object {
    /** Downcasts a higher-kinded representation back to [Parser]. */
    fun <A> narrow(v: K1<T, A>): Parser<A> = v as Parser<A>

    // Monad
    /** Succeeds with [v] without consuming input. */
    fun <A> pure(v: A): Parser<A> = monad.pure(v)
    /** Flattens a parser that produces a parser. */
    fun <A> join(v: K1<T, K1<T, A>>): Parser<A> = monad.join(v)
    private val monad = object: ParserMonadPlus {}
    /** Builds a [Parser] with monad comprehension syntax. */
    fun <A> fx(block: suspend MonadScope<T>.() -> A): Parser<A> = narrow(monad.fx(block))

    // Alternative / MonadPlus
    /** Always fails without expectations; the identity of [orElse]. */
    fun <A> empty(): Parser<A> = monad.empty()
    /** Same as [empty]. */
    fun <A> mzero(): Parser<A> = monad.mzero()
    /** Tries each parser in order and returns the first success. */
    fun <A> choice(vararg parsers: Parser<A>): Parser<A> = parsers.fold(empty()) { acc, p -> acc orElse p }

    // Primitives
    /** Always fails, reporting [expected] at the current offset. */
    fun <A> fail(expected: String): Parser<A> = Parser { _, position -> failure(position, expected) }
    /** Consumes one character satisfying [predicate], reporting [expected] otherwise. */
    fun satisfy(expected: String, predicate: (Char) -> Boolean): Parser<Char> = satisfyParser(expected, predicate)
    /** Consumes any single character. */
    fun anyChar(): Parser<Char> = satisfy("any character") { true }
    /** Consumes the character [c]. */
    fun char(c: Char): Parser<Char> = satisfy("'$c'") { it == c }
    /** Consumes the exact string [s], failing at its start offset otherwise. */
    fun string(s: String): Parser<String> = stringParser(s)
    /** Succeeds only at the end of the input. */
    fun eof(): Parser<Unit> = eofParser()
    /** Builds the parser lazily on first use; needed for recursive grammars. */
    fun <A> defer(f: () -> Parser<A>): Parser<A> = deferParser(f)

    // Repetition
    /** Applies [p] zero or more times; stops at the first failure or at a success that consumes nothing. */
    fun <A> many(p: Parser<A>): Parser<List<A>> = manyParser(p)
    /** Applies [p] one or more times. */
    fun <A> some(p: Parser<A>): Parser<List<A>> = p bind { a -> many(p) fmap { listOf(a) + it } }
    /** Parses zero or more [p] separated by [sep]. */
    fun <A, S> sepBy(p: Parser<A>, sep: Parser<S>): Parser<List<A>> = sepBy1(p, sep) orElse pure(emptyList())
    /** Parses one or more [p] separated by [sep]. */
    fun <A, S> sepBy1(p: Parser<A>, sep: Parser<S>): Parser<List<A>> = p bind { a -> many(sep keepRight p) fmap { listOf(a) + it } }
  }

  // Monad
  /** Maps the parsed value. */
  infix fun <B> fmap(f: (A) -> B): Parser<B> = monad.fmap(f, this)
  /** Parses [f], then this, and applies the function to the value. */
  infix fun <B> ap(f: Parser<(A) -> B>): Parser<B> = monad.ap(f, this)
  /** Parses this, then the parser chosen by [f] from where this stopped. */
  infix fun <B> bind(f: (A) -> Parser<B>): Parser<B> = monad.bind(f, this)

  // Alternative
  /** Tries this parser, falling back to [other] from the same offset on failure. */
  infix fun orElse(other: Parser<A>): Parser<A> = monad.orElse(this, other)
  /** Succeeds with `Just` the value, or `None` without consuming input when this fails. */
  fun optional(): Parser<Maybe<A>> = narrow(monad.optional(this))
  private val monad = object: ParserMonadPlus {}

  /** Parses this then [other], keeping this value. */
  infix fun <B> keepLeft(other: Parser<B>): Parser<A> = bind { a -> other fmap { a } }
  /** Parses this then [other], keeping the value of [other]. */
  infix fun <B> keepRight(other: Parser<B>): Parser<B> = bind { other }

  /** Reports [name] instead of this parser's expectations when it fails without consuming input; an empty name hides them. */
  infix fun label(name: String): Parser<A> = Parser { input, position -> relabel(run(input, position), position, name) }

  /** Runs the parser from the start of [input]; trailing input is allowed. */
  fun parse(input: String): ParseResult<A> = run(input, 0)
  /** Runs the parser from the start of [input] and requires it to consume everything. */
  fun parseAll(input: String): ParseResult<A> = (this keepLeft eof()).parse(input)
}
