package com.github.kotlinz.kotlinz.data.trying

import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.law.TraversableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class TryTraversableTest : DescribeSpec({
    describe("Try") {
        context("as Traversable") {
            val law = TraversableLaw(object : TryTraversable {})
            val maybe = object : MaybeApplicative {}
            val positive = { x: Int -> if (x > 0) Maybe.Just(x) else Maybe.None<Int>() }
            val g = { x: Int -> listKOf(x, x * 2) }
            val error = RuntimeException("error")

            it("should satisfy laws with Success") { law.assertSatisfyingTraversableLaw(Try.Success(1), positive, g) }
            it("should satisfy laws with failing Success") { law.assertSatisfyingTraversableLaw(Try.Success(-1), positive, g) }
            it("should satisfy laws with Failure") { law.assertSatisfyingTraversableLaw(Try.Failure(error), positive, g) }

            it("should traverse Success") {
                Try.Success(1).traverse(maybe, positive) shouldBe Maybe.Just(Try.Success(1))
                Try.Success(-1).traverse(maybe, positive) shouldBe Maybe.None<Try<Int>>()
            }

            it("should wrap Failure in pure") {
                Try.Failure<Int>(error).traverse(maybe, positive) shouldBe Maybe.Just(Try.Failure<Int>(error))
            }

            it("should sequence Try of Maybe") {
                Try.sequence(maybe, Try.Success(Maybe.Just(1))) shouldBe Maybe.Just(Try.Success(1))
            }
        }
    }
})
