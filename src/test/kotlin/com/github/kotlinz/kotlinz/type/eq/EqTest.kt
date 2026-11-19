package com.github.kotlinz.kotlinz.type.eq

import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.either.eq
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.eq
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.data.ior.eq
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.eq
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.eq
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyList
import com.github.kotlinz.kotlinz.data.nonemptylist.eq
import com.github.kotlinz.kotlinz.data.trying.Try
import com.github.kotlinz.kotlinz.data.trying.eq
import com.github.kotlinz.kotlinz.data.tuple2.Tuple2
import com.github.kotlinz.kotlinz.data.tuple2.eq
import com.github.kotlinz.kotlinz.data.validated.Validated
import com.github.kotlinz.kotlinz.data.validated.eq
import com.github.kotlinz.kotlinz.law.EqLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue

class EqTest : DescribeSpec({
    val ignoreCase = Eq.by<String> { a, b -> a.equals(b, ignoreCase = true) }

    describe("primitive instances") {
        it("should satisfy laws") {
            EqLaw(Int.eq).assertSatisfyingEqLaw(1, 1, 2)
            EqLaw(Long.eq).assertSatisfyingEqLaw(1L, 2L, 1L)
            EqLaw(Short.eq).assertSatisfyingEqLaw(1, 2, 3)
            EqLaw(Byte.eq).assertSatisfyingEqLaw(1, 1, 1)
            EqLaw(Double.eq).assertSatisfyingEqLaw(Double.NaN, 0.0, -0.0)
            EqLaw(Float.eq).assertSatisfyingEqLaw(Float.NaN, 1.5f, 1.5f)
            EqLaw(Char.eq).assertSatisfyingEqLaw('a', 'b', 'a')
            EqLaw(Boolean.eq).assertSatisfyingEqLaw(true, false, true)
            EqLaw(String.eq).assertSatisfyingEqLaw("a", "b", "a")
        }
        it("should compare values") {
            Int.eq.eqv(1, 1).shouldBeTrue()
            Int.eq.neqv(1, 2).shouldBeTrue()
            Double.eq.eqv(Double.NaN, Double.NaN).shouldBeTrue()
        }
    }

    describe("Eq.by") {
        it("should satisfy laws") { EqLaw(ignoreCase).assertSatisfyingEqLaw("a", "A", "b") }
    }

    describe("List instance") {
        val eq = Eq.list(ignoreCase)
        it("should satisfy laws") { EqLaw(eq).assertSatisfyingEqLaw(listOf("a"), listOf("A"), listOf("a", "b")) }
        it("should compare element-wise") {
            eq.eqv(listOf("a", "B"), listOf("A", "b")).shouldBeTrue()
            eq.eqv(listOf("a"), listOf("a", "a")).shouldBeFalse()
        }
    }

    describe("Maybe instance") {
        val eq = Maybe.eq(ignoreCase)
        it("should satisfy laws") { EqLaw(eq).assertSatisfyingEqLaw(Maybe.Just("a"), Maybe.Just("A"), Maybe.None()) }
        it("should use the element instance") {
            eq.eqv(Maybe.Just("a"), Maybe.Just("A")).shouldBeTrue()
            eq.eqv(Maybe.None(), Maybe.None()).shouldBeTrue()
            eq.eqv(Maybe.Just("a"), Maybe.None()).shouldBeFalse()
        }
    }

    describe("Either instance") {
        val eq = Either.eq(Int.eq, ignoreCase)
        it("should satisfy laws") { EqLaw(eq).assertSatisfyingEqLaw(Either.Right("a"), Either.Right("A"), Either.Left(1)) }
        it("should distinguish sides") {
            eq.eqv(Either.Left(1), Either.Left(1)).shouldBeTrue()
            Either.eq(Int.eq, Int.eq).eqv(Either.Left(1), Either.Right(1)).shouldBeFalse()
        }
    }

    describe("Try instance") {
        val error = IllegalStateException()
        val eq = Try.eq(ignoreCase)
        it("should satisfy laws") { EqLaw(eq).assertSatisfyingEqLaw(Try.Success("a"), Try.Success("A"), Try.Failure(error)) }
        it("should compare failures with the error instance") {
            eq.eqv(Try.Failure(error), Try.Failure(error)).shouldBeTrue()
            eq.eqv(Try.Failure(error), Try.Failure(IllegalStateException())).shouldBeFalse()
        }
    }

    describe("Validated instance") {
        val eq = Validated.eq(ignoreCase, Int.eq)
        it("should satisfy laws") { EqLaw(eq).assertSatisfyingEqLaw(Validated.Invalid("e"), Validated.Invalid("E"), Validated.Valid(1)) }
    }

    describe("Ior instance") {
        val eq = Ior.eq(ignoreCase, Int.eq)
        it("should satisfy laws") { EqLaw(eq).assertSatisfyingEqLaw(Ior.Both("a", 1), Ior.Both("A", 1), Ior.Left("a")) }
        it("should distinguish cases") { eq.eqv(Ior.Right(1), Ior.Both("a", 1)).shouldBeFalse() }
    }

    describe("Tuple2 instance") {
        val eq = Tuple2.eq(ignoreCase, Int.eq)
        it("should satisfy laws") { EqLaw(eq).assertSatisfyingEqLaw(Tuple2("a", 1), Tuple2("A", 1), Tuple2("a", 2)) }
    }

    describe("ListK instance") {
        val eq = ListK.eq(ignoreCase)
        it("should satisfy laws") { EqLaw(eq).assertSatisfyingEqLaw(ListK(listOf("a")), ListK(listOf("A")), ListK(listOf())) }
    }

    describe("NonEmptyList instance") {
        val eq = NonEmptyList.eq(ignoreCase)
        it("should satisfy laws") {
            EqLaw(eq).assertSatisfyingEqLaw(NonEmptyList("a", listOf("b")), NonEmptyList("A", listOf("B")), NonEmptyList("a", listOf()))
        }
    }

    describe("Identity instance") {
        val eq = Identity.eq(ignoreCase)
        it("should satisfy laws") { EqLaw(eq).assertSatisfyingEqLaw(Identity("a"), Identity("A"), Identity("b")) }
    }
})
