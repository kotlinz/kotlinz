package com.github.kotlinz.kotlinz.data.parser

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.law.ObservedMonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ParserTest : DescribeSpec({
    describe("Parser") {
        val inputs = listOf("", "a", "ab", "abc", "ba", "aab")
        val observe = { p: K1<Parser.T, *> -> inputs.map { Parser.narrow(p).parse(it) } }
        val a = Parser.char('a')
        val b = Parser.char('b')

        context("as Monad") {
            val law = ObservedMonadLaw(object : ParserMonad {}, observe)
            val f = { c: Char -> Parser.char(c + 1) }
            val g = { c: Char -> Parser.char(c) orElse Parser.pure('z') }

            it("should satisfy laws with a consuming parser") { law.assertSatisfyingMonadLaw('a', a, f, g) }
            it("should satisfy laws with a failing parser") { law.assertSatisfyingMonadLaw('a', Parser.fail("x"), f, g) }
            it("should satisfy laws with pure") { law.assertSatisfyingMonadLaw('a', Parser.pure('a'), f, g) }
        }

        context("as Alternative") {
            val alt = object : ParserMonadPlus {}
            val parsers = listOf(a, b, Parser.string("ab"), Parser.empty(), Parser.fail("x"))

            it("should satisfy identity laws") {
                parsers.forEach { p ->
                    observe(alt.orElse(alt.empty(), p)) shouldBe observe(p)
                    observe(alt.orElse(p, alt.empty())) shouldBe observe(p)
                }
            }

            it("should satisfy associativity") {
                val (x, y, z) = Triple(Parser.string("ab") fmap { 'x' }, a, Parser.fail<Char>("x") orElse b)
                observe(alt.orElse(alt.orElse(x, y), z)) shouldBe observe(alt.orElse(x, alt.orElse(y, z)))
            }

            it("should satisfy MonadPlus zero laws") {
                observe(alt.bind({ c: Char -> Parser.pure(c) }, alt.mzero<Char>())) shouldBe observe(alt.mzero<Char>())
                observe(alt.bind({ _: Char -> alt.mzero<Char>() }, a)).forEach { (it is ParseResult.Failure) shouldBe true }
            }
        }

        context("primitives") {
            it("should parse characters and strings") {
                a.parse("abc") shouldBe ParseResult.Success('a', 1)
                Parser.string("ab").parse("abc") shouldBe ParseResult.Success("ab", 2)
                Parser.anyChar().parse("") shouldBe ParseResult.Failure(ParseError(0, setOf("any character")))
            }

            it("should backtrack after consuming input") {
                val p = (Parser.char('a') keepRight Parser.string("x")) orElse Parser.string("ab")
                p.parse("ab") shouldBe ParseResult.Success("ab", 2)
            }

            it("should repeat with many and some") {
                Parser.many(a).parse("aab") shouldBe ParseResult.Success(listOf('a', 'a'), 2, ParseError(2, setOf("'a'")))
                Parser.many(a).parse("b") shouldBe ParseResult.Success(listOf(), 0, ParseError(0, setOf("'a'")))
                Parser.some(a).parse("b") shouldBe ParseResult.Failure(ParseError(0, setOf("'a'")))
                Parser.many(Parser.pure(1)).parse("x") shouldBe ParseResult.Success(listOf(), 0)
            }

            it("should parse separated lists") {
                Parser.sepBy(a, Parser.char(',')).parse("a,a,ab") shouldBe ParseResult.Success(listOf('a', 'a', 'a'), 5, ParseError(5, setOf("','")))
                Parser.sepBy(a, Parser.char(',')).parse("b") shouldBe ParseResult.Success(listOf(), 0, ParseError(0, setOf("'a'")))
            }

            it("should make parsers optional and choose among alternatives") {
                a.optional().parse("b") shouldBe ParseResult.Success(Maybe.None<Char>(), 0, ParseError(0, setOf("'a'")))
                a.optional().parse("a") shouldBe ParseResult.Success(Maybe.Just('a'), 1)
                Parser.choice(a, b).parse("b") shouldBe ParseResult.Success('b', 1)
            }

            it("should require full consumption with parseAll") {
                a.parseAll("ab") shouldBe ParseResult.Failure(ParseError(1, setOf("end of input")))
            }
        }

        context("errors") {
            it("should report the furthest failure") {
                val p = (a keepRight a keepRight b) orElse (a keepRight b)
                p.parse("aac") shouldBe ParseResult.Failure(ParseError(2, setOf("'b'")))
            }

            it("should merge expectations at the same position") {
                (a orElse b).parse("c") shouldBe ParseResult.Failure(ParseError(0, setOf("'a'", "'b'")))
            }

            it("should merge hints from a stopped repetition into the next failure") {
                (Parser.many(a) keepLeft b).parse("aac") shouldBe ParseResult.Failure(ParseError(2, setOf("'a'", "'b'")))
            }

            it("should relabel or hide expectations without consumption") {
                (a label "letter a").parse("b") shouldBe ParseResult.Failure(ParseError(0, setOf("letter a")))
                (Parser.many(a label "") keepLeft b).parse("c") shouldBe ParseResult.Failure(ParseError(0, setOf("'b'")))
                ((a keepRight b) label "ab").parse("ac") shouldBe ParseResult.Failure(ParseError(1, setOf("'b'")))
            }

            it("should describe errors with line and column") {
                ParseError(4, setOf("'a'", "'b'")).describe("xy\nzw") shouldBe "line 2, column 2: expected 'a' or 'b'"
            }
        }

        context("fx") {
            it("should sequence parsers") {
                val pair = Parser.fx { Pair(a.bind(), b.bind()) }
                pair.parse("ab") shouldBe ParseResult.Success(Pair('a', 'b'), 2)
                pair.parse("aa") shouldBe ParseResult.Failure(ParseError(1, setOf("'b'")))
            }

            it("should be reusable under repetition and backtracking") {
                val pair = Parser.fx { "${a.bind()}${b.bind()}" }
                (Parser.many(pair) keepLeft Parser.char('a')).parse("ababa") shouldBe ParseResult.Success(listOf("ab", "ab"), 5, ParseError(5, setOf("'b'")))
            }
        }
    }
})
