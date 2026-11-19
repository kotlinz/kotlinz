package com.github.kotlinz.kotlinz.type.hash

import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.either.hash
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.hash
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.data.ior.hash
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.hash
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.hash
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyList
import com.github.kotlinz.kotlinz.data.nonemptylist.hash
import com.github.kotlinz.kotlinz.data.trying.Try
import com.github.kotlinz.kotlinz.data.trying.hash
import com.github.kotlinz.kotlinz.data.tuple2.Tuple2
import com.github.kotlinz.kotlinz.data.tuple2.hash
import com.github.kotlinz.kotlinz.data.validated.Validated
import com.github.kotlinz.kotlinz.data.validated.hash
import com.github.kotlinz.kotlinz.law.HashLaw
import com.github.kotlinz.kotlinz.type.eq.Eq
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class HashTest : DescribeSpec({
    val ignoreCase = Hash.by(Eq.by<String> { a, b -> a.equals(b, ignoreCase = true) }) { it.lowercase().hashCode() }

    describe("primitive instances") {
        it("should satisfy laws") {
            HashLaw(Int.hash).assertSatisfyingHashLaw(1, 1, 2)
            HashLaw(Long.hash).assertSatisfyingHashLaw(1L, 2L, 1L)
            HashLaw(Short.hash).assertSatisfyingHashLaw(1, 2, 3)
            HashLaw(Byte.hash).assertSatisfyingHashLaw(1, 1, 1)
            HashLaw(Double.hash).assertSatisfyingHashLaw(Double.NaN, 0.0, -0.0)
            HashLaw(Float.hash).assertSatisfyingHashLaw(Float.NaN, Float.NaN, 1.5f)
            HashLaw(Char.hash).assertSatisfyingHashLaw('a', 'b', 'a')
            HashLaw(Boolean.hash).assertSatisfyingHashLaw(true, false, true)
            HashLaw(String.hash).assertSatisfyingHashLaw("a", "b", "a")
        }
        it("should agree with hashCode") {
            Int.hash.hash(42) shouldBe 42.hashCode()
            String.hash.hash("a") shouldBe "a".hashCode()
        }
    }

    describe("Hash.by and Hash.combine") {
        it("should satisfy laws") { HashLaw(ignoreCase).assertSatisfyingHashLaw("a", "A", "b") }
        it("should combine like List.hashCode") { Hash.combine(1, 2, 3) shouldBe listOf(1, 2, 3).hashCode() }
    }

    describe("List instance") {
        val hash = Hash.list(ignoreCase)
        it("should satisfy laws") { HashLaw(hash).assertSatisfyingHashLaw(listOf("a"), listOf("A"), listOf("a", "b")) }
        it("should agree with List.hashCode for the default instance") {
            Hash.list(Int.hash).hash(listOf(1, 2)) shouldBe listOf(1, 2).hashCode()
        }
    }

    describe("Maybe instance") {
        val hash = Maybe.hash(ignoreCase)
        it("should satisfy laws") { HashLaw(hash).assertSatisfyingHashLaw(Maybe.Just("a"), Maybe.Just("A"), Maybe.None()) }
        it("should hash with the element instance") {
            hash.hash(Maybe.Just("a")) shouldBe hash.hash(Maybe.Just("A"))
            hash.eqv(Maybe.Just("a"), Maybe.Just("A")).shouldBeTrue()
        }
    }

    describe("Either instance") {
        val hash = Either.hash(Int.hash, Int.hash)
        it("should satisfy laws") { HashLaw(hash).assertSatisfyingHashLaw(Either.Right(1), Either.Right(1), Either.Left(1)) }
        it("should distinguish sides") {
            hash.eqv(Either.Left(1), Either.Right(1)).shouldBeFalse()
            hash.hash(Either.Left(1)) shouldNotBe hash.hash(Either.Right(1))
        }
    }

    describe("Try instance") {
        val error = IllegalStateException()
        val hash = Try.hash(ignoreCase)
        it("should satisfy laws") { HashLaw(hash).assertSatisfyingHashLaw(Try.Success("a"), Try.Success("A"), Try.Failure(error)) }
        it("should hash failures with the error instance") {
            hash.hash(Try.Failure(error)) shouldBe hash.hash(Try.Failure(error))
        }
    }

    describe("Validated instance") {
        val hash = Validated.hash(ignoreCase, Int.hash)
        it("should satisfy laws") { HashLaw(hash).assertSatisfyingHashLaw(Validated.Invalid("e"), Validated.Invalid("E"), Validated.Valid(1)) }
    }

    describe("Ior instance") {
        val hash = Ior.hash(ignoreCase, Int.hash)
        it("should satisfy laws") { HashLaw(hash).assertSatisfyingHashLaw(Ior.Both("a", 1), Ior.Both("A", 1), Ior.Right(1)) }
    }

    describe("Tuple2 instance") {
        val hash = Tuple2.hash(ignoreCase, Int.hash)
        it("should satisfy laws") { HashLaw(hash).assertSatisfyingHashLaw(Tuple2("a", 1), Tuple2("A", 1), Tuple2("a", 2)) }
    }

    describe("ListK instance") {
        val hash = ListK.hash(ignoreCase)
        it("should satisfy laws") { HashLaw(hash).assertSatisfyingHashLaw(ListK(listOf("a")), ListK(listOf("A")), ListK(listOf())) }
    }

    describe("NonEmptyList instance") {
        val hash = NonEmptyList.hash(ignoreCase)
        it("should satisfy laws") {
            HashLaw(hash).assertSatisfyingHashLaw(NonEmptyList("a", listOf("b")), NonEmptyList("A", listOf("B")), NonEmptyList("a", listOf()))
        }
    }

    describe("Identity instance") {
        val hash = Identity.hash(ignoreCase)
        it("should satisfy laws") { HashLaw(hash).assertSatisfyingHashLaw(Identity("a"), Identity("A"), Identity("b")) }
    }
})
