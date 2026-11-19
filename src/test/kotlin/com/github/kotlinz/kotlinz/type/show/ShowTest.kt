package com.github.kotlinz.kotlinz.type.show

import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.either.show
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.show
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.data.ior.show
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.show
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.show
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyList
import com.github.kotlinz.kotlinz.data.nonemptylist.show
import com.github.kotlinz.kotlinz.data.trying.Try
import com.github.kotlinz.kotlinz.data.trying.show
import com.github.kotlinz.kotlinz.data.tuple2.Tuple2
import com.github.kotlinz.kotlinz.data.tuple2.show
import com.github.kotlinz.kotlinz.data.validated.Validated
import com.github.kotlinz.kotlinz.data.validated.show
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ShowTest : DescribeSpec({
    describe("primitive instances") {
        it("should render numbers and booleans with toString") {
            Int.show.show(1) shouldBe "1"
            Long.show.show(2L) shouldBe "2"
            Short.show.show(3) shouldBe "3"
            Byte.show.show(4) shouldBe "4"
            Double.show.show(1.5) shouldBe "1.5"
            Float.show.show(2.5f) shouldBe "2.5"
            Boolean.show.show(true) shouldBe "true"
        }
        it("should quote and escape chars and strings") {
            Char.show.show('a') shouldBe "'a'"
            Char.show.show('\'') shouldBe "'\\''"
            String.show.show("e") shouldBe "\"e\""
            String.show.show("say \"hi\" \\") shouldBe "\"say \\\"hi\\\" \\\\\""
        }
    }

    describe("Show.by and Show.fromToString") {
        it("should render with the given function") {
            Show.by<Int> { "#$it" }.show(1) shouldBe "#1"
            Show.fromToString<Pair<Int, Int>>().show(Pair(1, 2)) shouldBe "(1, 2)"
        }
    }

    describe("List instance") {
        it("should render elements with the element instance") {
            Show.list(String.show).show(listOf("a", "b")) shouldBe "[\"a\", \"b\"]"
            Show.list(Int.show).show(listOf()) shouldBe "[]"
        }
    }

    describe("data type instances") {
        it("should render Maybe") {
            Maybe.show(Int.show).show(Maybe.Just(1)) shouldBe "Just(1)"
            Maybe.show(Int.show).show(Maybe.None()) shouldBe "None"
        }
        it("should render Either") {
            val show = Either.show(String.show, Int.show)
            show.show(Either.Left("e")) shouldBe "Left(\"e\")"
            show.show(Either.Right(1)) shouldBe "Right(1)"
        }
        it("should render Try") {
            val show = Try.show(Int.show)
            show.show(Try.Success(1)) shouldBe "Success(1)"
            show.show(Try.Failure(IllegalStateException("boom"))) shouldBe "Failure(java.lang.IllegalStateException: boom)"
            Try.show(Int.show, Show.by { it.message ?: "" }).show(Try.Failure(Exception("x"))) shouldBe "Failure(x)"
        }
        it("should render Validated") {
            val show = Validated.show(String.show, Int.show)
            show.show(Validated.Valid(1)) shouldBe "Valid(1)"
            show.show(Validated.Invalid("e")) shouldBe "Invalid(\"e\")"
        }
        it("should render Ior") {
            val show = Ior.show(String.show, Int.show)
            show.show(Ior.Left("a")) shouldBe "Left(\"a\")"
            show.show(Ior.Right(1)) shouldBe "Right(1)"
            show.show(Ior.Both("a", 1)) shouldBe "Both(\"a\", 1)"
        }
        it("should render Tuple2") {
            Tuple2.show(Int.show, String.show).show(Tuple2(1, "a")) shouldBe "Tuple2(1, \"a\")"
        }
        it("should render ListK") {
            ListK.show(Int.show).show(ListK(listOf(1, 2))) shouldBe "ListK([1, 2])"
        }
        it("should render NonEmptyList") {
            NonEmptyList.show(Int.show).show(NonEmptyList(1, listOf(2, 3))) shouldBe "NonEmptyList(1, 2, 3)"
        }
        it("should render Identity") {
            Identity.show(Int.show).show(Identity(1)) shouldBe "Identity(1)"
        }
        it("should compose nested instances") {
            Maybe.show(Either.show(String.show, Show.list(Int.show))).show(Maybe.Just(Either.Right(listOf(1)))) shouldBe "Just(Right([1]))"
        }
    }
})
