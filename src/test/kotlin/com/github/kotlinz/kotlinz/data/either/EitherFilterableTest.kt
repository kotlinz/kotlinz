package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.monoid.toStringMonoid
import com.github.kotlinz.kotlinz.law.FilterableLaw
import com.github.kotlinz.kotlinz.law.WitherableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class EitherFilterableTest : DescribeSpec({
    describe("Either") {
        val type = StringMonoid::class.java
        val even = { x: Int -> if (x % 2 == 0) Maybe.Just(x / 2) else Maybe.None<Int>() }
        val positive = { x: Int -> if (x > 0) Maybe.Just(x - 1) else Maybe.None<Int>() }
        val samples = listOf(Either.Right<StringMonoid, Int>(4), Either.Right(3), Either.Right(0), Either.Left("e".toStringMonoid()))

        context("as Filterable") {
            val law = FilterableLaw(object : EitherFilterable<StringMonoid> { override val type = type })

            it("should satisfy laws") { samples.forEach { law.assertSatisfyingFilterableLaw(it, even, positive, { x -> x > 1 }, { x -> x + 1 }) } }

            it("should turn a dropped Right into Left(mzero)") {
                Either.Right<StringMonoid, Int>(1).filter(type) { it > 1 } shouldBe Either.Left("".toStringMonoid())
                Either.Right<StringMonoid, Int>(2).filter(type) { it > 1 } shouldBe Either.Right(2)
                Either.Right<StringMonoid, Int>(3).mapFilter(type, even) shouldBe Either.Left("".toStringMonoid())
                Either.Right<StringMonoid, Int>(3).collect(type) { "n$it" } shouldBe Either.Right("n3")
            }

            it("should keep Left unchanged") {
                Either.Left<StringMonoid, Int>("e".toStringMonoid()).filter(type) { true } shouldBe Either.Left("e".toStringMonoid())
            }
        }

        context("as Witherable") {
            val law = WitherableLaw(object : EitherWitherable<StringMonoid> { override val type = type })
            val maybe = object : MaybeApplicative {}

            it("should satisfy laws") { samples.forEach { law.assertSatisfyingWitherableLaw(it, even) { x -> x > 1 } } }

            it("should wither and filterA with an effect") {
                Either.Right<StringMonoid, Int>(4).wither(type, maybe) { Maybe.Just(even(it)) } shouldBe Maybe.Just(Either.Right<StringMonoid, Int>(2))
                Either.Right<StringMonoid, Int>(1).filterA(type, maybe) { Maybe.Just(it > 1) } shouldBe Maybe.Just(Either.Left<StringMonoid, Int>("".toStringMonoid()))
            }
        }
    }
})
