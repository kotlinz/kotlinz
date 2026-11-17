package com.github.kotlinz.kotlinz.data.env

import com.github.kotlinz.kotlinz.law.ComonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class EnvTest : DescribeSpec({
    describe("Env") {
        context("as Comonad") {
            val law = ComonadLaw(object : EnvComonad<String> {})

            it("should satisfy laws with Int") { law.assertSatisfyingComonadLaw(Env("env", Int.MAX_VALUE)) }
            it("should satisfy laws with String") { law.assertSatisfyingComonadLaw(Env("env", "Comonad Law")) }
        }

        context("operations") {
            val env = Env(10, "value")

            it("should extract the value") { env.extract() shouldBe "value" }
            it("should ask the environment") { env.ask() shouldBe 10 }
            it("should ask a projection of the environment") { env.asks { it * 2 } shouldBe 20 }
            it("should modify the environment locally") { env.local { it + 1 } shouldBe Env(11, "value") }

            it("should extend with access to the environment") {
                env extend { w -> Env.narrow(w).let { "${it.value}:${it.ask()}" } } shouldBe Env(10, "value:10")
            }
        }
    }
})
