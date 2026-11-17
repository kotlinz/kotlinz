package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.law.TraversableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class EitherTraversableTest : DescribeSpec({
    describe("Either") {
        context("as Traversable") {
            val law = TraversableLaw(object : EitherTraversable<String> {})
            val maybe = object : MaybeApplicative {}
            val positive = { x: Int -> if (x > 0) Maybe.Just(x) else Maybe.None<Int>() }
            val g = { x: Int -> listKOf(x, x * 2) }

            it("should satisfy laws with Right") { law.assertSatisfyingTraversableLaw(Either.Right<String, Int>(1), positive, g) }
            it("should satisfy laws with failing Right") { law.assertSatisfyingTraversableLaw(Either.Right<String, Int>(-1), positive, g) }
            it("should satisfy laws with Left") { law.assertSatisfyingTraversableLaw(Either.Left<String, Int>("error"), positive, g) }

            it("should traverse Right") {
                Either.Right<String, Int>(1).traverse(maybe, positive) shouldBe Maybe.Just(Either.Right<String, Int>(1))
                Either.Right<String, Int>(-1).traverse(maybe, positive) shouldBe Maybe.None<Either<String, Int>>()
            }

            it("should wrap Left in pure") {
                Either.Left<String, Int>("error").traverse(maybe, positive) shouldBe Maybe.Just(Either.Left<String, Int>("error"))
            }

            it("should sequence Either of Maybe") {
                Either.sequence(maybe, Either.Right<String, K1<Maybe.T, Int>>(Maybe.Just(1))) shouldBe Maybe.Just(Either.Right<String, Int>(1))
            }
        }
    }
})
