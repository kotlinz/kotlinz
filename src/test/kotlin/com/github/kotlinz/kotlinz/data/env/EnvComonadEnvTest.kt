package com.github.kotlinz.kotlinz.data.env

import com.github.kotlinz.kotlinz.law.ComonadEnvLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class EnvComonadEnvTest : DescribeSpec({
    describe("Env as ComonadEnv") {
        val instance = object : EnvComonadEnv<Int> {}
        val env = Env(10, "value")

        it("should satisfy laws") { ComonadEnvLaw(instance).assertSatisfyingComonadEnvLaw(env) { it.length } }

        it("should ask the environment") {
            instance.ask(env) shouldBe 10
            instance.asks({ it * 2 }, env) shouldBe 20
        }
    }
})
