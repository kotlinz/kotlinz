package com.github.kotlinz.kotlinz.data

import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.trying.Try
import com.github.kotlinz.kotlinz.data.writer.Writer
import com.github.kotlinz.kotlinz.data.writert.WriterT
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class EqualsHashCodeToStringTest : DescribeSpec({
    fun <A> assertContract(a: A, sameAsA: A, other: A) {
        a shouldBe sameAsA
        a.hashCode() shouldBe sameAsA.hashCode()
        a shouldNotBe other
        hashSetOf(a, sameAsA, other).size shouldBe 2
        hashMapOf(a to 1)[sameAsA] shouldBe 1
    }

    describe("Maybe") {
        it("should follow the equals/hashCode contract") {
            assertContract<Maybe<Int>>(Maybe.Just(1), Maybe.Just(1), Maybe.Just(2))
            assertContract<Maybe<Int>>(Maybe.None(), Maybe.None(), Maybe.Just(0))
        }
        it("should print readable values") {
            Maybe.Just(1).toString() shouldBe "Just(value=1)"
            Maybe.None<Int>().toString() shouldBe "None"
        }
    }

    describe("Either") {
        it("should follow the equals/hashCode contract") {
            assertContract<Either<String, Int>>(Either.Right(1), Either.Right(1), Either.Left("1"))
            assertContract<Either<Int, Int>>(Either.Left(1), Either.Left(1), Either.Right(1))
        }
        it("should print readable values") {
            Either.Left<String, Int>("e").toString() shouldBe "Left(value=e)"
            Either.Right<String, Int>(1).toString() shouldBe "Right(value=1)"
        }
    }

    describe("Try") {
        val error = IllegalStateException("boom")
        it("should follow the equals/hashCode contract") {
            assertContract<Try<Int>>(Try.Success(1), Try.Success(1), Try.Failure(error))
            assertContract<Try<Int>>(Try.Failure(error), Try.Failure(error), Try.Success(1))
        }
        it("should print readable values") {
            Try.Success(1).toString() shouldBe "Success(value=1)"
            Try.Failure<Int>(error).toString() shouldBe "Failure(error=java.lang.IllegalStateException: boom)"
        }
    }

    describe("Writer") {
        val type = StringMonoid::class.java
        fun writer(a: Int, w: String) = Writer(type, Pair(a, StringMonoid(w)))
        it("should follow the equals/hashCode contract") {
            assertContract(writer(1, "a"), writer(1, "a"), writer(1, "b"))
        }
        it("should print readable values") {
            writer(1, "a").toString() shouldBe "Writer(run=(1, StringMonoid(value=a)))"
        }
    }

    describe("WriterT") {
        val type = StringMonoid::class.java
        val monad = object : IdentityMonad {}
        fun writerT(a: Int, w: String) = WriterT(monad, type, Identity(Pair(a, StringMonoid(w))))
        it("should follow the equals/hashCode contract") {
            assertContract(writerT(1, "a"), writerT(1, "a"), writerT(2, "a"))
        }
        it("should print readable values") {
            writerT(1, "a").toString() shouldBe "WriterT(run=Identity(value=(1, StringMonoid(value=a))))"
        }
    }
})
