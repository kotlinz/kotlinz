package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.law.AlternativeLaw
import com.github.kotlinz.kotlinz.law.MonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class MaybeAlternativeTest : DescribeSpec({
    describe("Maybe") {
        context("as Alternative") {
            val law = AlternativeLaw(object : MaybeAlternative {})

            it("should satisfy laws with Just values") {
                law.assertSatisfyingAlternativeLaw(Maybe.Just(1), Maybe.Just(2), Maybe.Just(3))
            }

            it("should satisfy laws with None values") {
                law.assertSatisfyingAlternativeLaw(Maybe.None<Int>(), Maybe.Just(2), Maybe.None())
            }

            it("should pick the first Just") {
                Maybe.Just(1) orElse Maybe.Just(2) shouldBe Maybe.Just(1)
                Maybe.None<Int>() orElse Maybe.Just(2) shouldBe Maybe.Just(2)
                Maybe.None<Int>() orElse Maybe.empty() shouldBe Maybe.None<Int>()
            }

            it("should make a value optional") {
                Maybe.Just(1).optional() shouldBe Maybe.Just(Maybe.Just(1))
                Maybe.None<Int>().optional() shouldBe Maybe.Just(Maybe.None<Int>())
            }
        }

        context("as MonadPlus") {
            it("should satisfy monad laws") { MonadLaw(object : MaybeMonadPlus {}).assertSatisfyingMonadLaw(1) }

            it("should treat mzero as left zero of bind") {
                Maybe.mzero<Int>() bind { Maybe.Just(it + 1) } shouldBe Maybe.None<Int>()
            }

            it("should behave as orElse") {
                Maybe.None<Int>() mplus Maybe.Just(1) shouldBe Maybe.Just(1)
            }
        }
    }
})
