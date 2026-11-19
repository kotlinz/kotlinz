package com.github.kotlinz.kotlinz.data.envt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.env.Env
import com.github.kotlinz.kotlinz.data.env.EnvComonad
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityComonad
import com.github.kotlinz.kotlinz.law.ComonadEnvLaw
import com.github.kotlinz.kotlinz.law.ComonadLaw
import com.github.kotlinz.kotlinz.law.ComonadTransLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private typealias Base = K1<Env.T, String>

private fun envTEq(a: Any?, b: Any?): Boolean = when {
    a is EnvT<*, *, *> && b is EnvT<*, *, *> -> a.env == b.env && envTEq(a.base, b.base)
    a is Env<*, *> && b is Env<*, *> -> a.env == b.env && envTEq(a.value, b.value)
    else -> a == b
}

class EnvTTest : DescribeSpec({
    describe("EnvT") {
        val base = object : EnvComonad<String> {}
        val instance = object : EnvTComonadTrans<Int, Base>, EnvTComonadEnv<Int, Base> { override val comonad = base }
        val envT = EnvT(base, 10, Env("ctx", 3))

        context("as Comonad") {
            val law = ComonadLaw(instance, ::envTEq)
            it("should satisfy laws") { law.assertSatisfyingComonadLaw(envT) }
            it("should satisfy laws with String values") { law.assertSatisfyingComonadLaw(envT fmap { "v$it" }) }
        }

        context("as ComonadTrans") {
            it("should satisfy laws") {
                ComonadTransLaw(base, instance, ::envTEq).assertSatisfyingComonadTransLaw(envT) { w -> Env.narrow(w).let { "${it.env}:${it.value}" } }
            }
        }

        context("as ComonadEnv") {
            it("should satisfy laws") { ComonadEnvLaw(instance).assertSatisfyingComonadEnvLaw(envT) { it * 2 } }
        }

        context("operations") {
            val identity = object : IdentityComonad {}
            val simple = EnvT(identity, 10, Identity("value"))

            it("should extract from the base comonad") { simple.extract() shouldBe "value" }
            it("should ask the environment") { simple.ask() shouldBe 10 }
            it("should project the environment") { simple.asks { it + 1 } shouldBe 11 }
            it("should modify the environment locally") { simple.local { it * 3 }.ask() shouldBe 30 }
            it("should lower to the base comonad") { simple.lower() shouldBe Identity("value") }

            it("should extend with access to the environment") {
                val result = envT extend { w -> EnvT.narrow(w).let { it.extract() * it.ask() } }
                envTEq(result, EnvT(base, 10, Env("ctx", 30))) shouldBe true
            }
        }
    }
})
