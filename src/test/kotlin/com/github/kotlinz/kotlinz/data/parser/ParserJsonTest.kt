package com.github.kotlinz.kotlinz.data.parser

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private sealed class Json {
    data object Null : Json()
    data class Bool(val value: Boolean) : Json()
    data class Num(val value: Int) : Json()
    data class Str(val value: String) : Json()
    data class Arr(val items: List<Json>) : Json()
    data class Obj(val fields: List<Pair<String, Json>>) : Json()
}

private object JsonParser {
    private val spaces = Parser.many(Parser.satisfy("whitespace") { it.isWhitespace() } label "")
    private fun <A> token(p: Parser<A>): Parser<A> = p keepLeft spaces
    private fun symbol(c: Char) = token(Parser.char(c))

    private val nul: Parser<Json> = token(Parser.string("null")) fmap { Json.Null }
    private val bool: Parser<Json> = token(Parser.string("true") fmap { true } orElse (Parser.string("false") fmap { false })) fmap { Json.Bool(it) }
    private val number: Parser<Json> = token(Parser.some(Parser.satisfy("digit") { it.isDigit() })) fmap { Json.Num(it.joinToString("").toInt()) }
    private val rawString = token(Parser.char('"') keepRight Parser.many(Parser.satisfy("character") { it != '"' }) keepLeft Parser.char('"')) fmap { it.joinToString("") }
    private val string: Parser<Json> = rawString fmap { Json.Str(it) }
    private val array: Parser<Json> = symbol('[') keepRight Parser.sepBy(Parser.defer { value }, symbol(',')) keepLeft symbol(']') fmap { Json.Arr(it) }
    private val field = Parser.fx { Pair(rawString.bind(), (symbol(':') keepRight Parser.defer { value }).bind()) }
    private val obj: Parser<Json> = symbol('{') keepRight Parser.sepBy(field, symbol(',')) keepLeft symbol('}') fmap { Json.Obj(it) }
    val value: Parser<Json> = Parser.choice(nul, bool, number, string, array, obj)
    val document: Parser<Json> = spaces keepRight value
}

class ParserJsonTest : DescribeSpec({
    describe("Parser") {
        context("JSON-ish example") {
            it("should parse nested documents") {
                val input = """ { "a": [1, 22, {"b": true}], "c": null, "d": "hi" } """
                JsonParser.document.parseAll(input) shouldBe ParseResult.Success(
                    Json.Obj(listOf(
                        "a" to Json.Arr(listOf(Json.Num(1), Json.Num(22), Json.Obj(listOf("b" to Json.Bool(true))))),
                        "c" to Json.Null,
                        "d" to Json.Str("hi"),
                    )),
                    input.length,
                )
            }

            it("should report where an unknown value starts") {
                val result = JsonParser.document.parseAll("[1, tru]")
                val error = (result as ParseResult.Failure).error
                error.position shouldBe 4
                error.expected shouldBe setOf("\"null\"", "\"true\"", "\"false\"", "digit", "'\"'", "'['", "'{'")
            }

            it("should report a missing closing bracket on the right line") {
                val input = "{\n  \"a\": [1, 2\n}"
                val error = (JsonParser.document.parseAll(input) as ParseResult.Failure).error
                error.describe(input) shouldBe "line 3, column 1: expected ',' or ']'"
            }
        }
    }
})
