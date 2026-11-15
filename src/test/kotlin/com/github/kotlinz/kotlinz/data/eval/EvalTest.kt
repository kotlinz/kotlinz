package com.github.kotlinz.kotlinz.data.eval

import com.github.kotlinz.kotlinz.law.MonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class EvalTest : DescribeSpec({
    describe("Eval") {
        context("as Monad") {
            val law = MonadLaw(object : EvalMonad {})

            context("with Int.MAX_VALUE") {
                it("should satisfy laws") { law.assertSatisfyingMonadLaw(Int.MAX_VALUE) }
            }

            context("with Int.MIN_VALUE") {
                it("should satisfy laws") { law.assertSatisfyingMonadLaw(Int.MIN_VALUE) }
            }

            context("with String") {
                it("should satisfy laws") { law.assertSatisfyingMonadLaw("Monad Law") }
            }
        }

        context("evaluation") {
            it("should defer and memoize the computation") {
                var count = 0
                val eval = Eval.later { count += 1; count } fmap { it * 10 }
                count shouldBe 0
                eval.value shouldBe 10
                eval.value shouldBe 10
                count shouldBe 1
            }

            it("should extract the value as Comonad") {
                Eval.now(42).extend { Eval.narrow(it).value + 1 }.extract() shouldBe 43
            }
        }
    }
})
