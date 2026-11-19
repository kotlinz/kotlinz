package com.github.kotlinz.kotlinz.data.tracedt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.env.Env
import com.github.kotlinz.kotlinz.data.env.EnvComonad
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityComonad
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.law.ComonadLaw
import com.github.kotlinz.kotlinz.law.ComonadTracedLaw
import com.github.kotlinz.kotlinz.law.ComonadTransLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private typealias Base = K1<Env.T, String>

private val traces = listOf("", "a", "bc").map(::StringMonoid)

@Suppress("UNCHECKED_CAST")
private fun sample(tracedT: TracedT<*, *, *>): Env<*, List<Any?>> =
    (tracedT.run as Env<String, (StringMonoid) -> Any?>).let { Env(it.env, traces.map(it.value)) }

private fun tracedTEq(a: Any?, b: Any?): Boolean = when {
    a is TracedT<*, *, *> && b is TracedT<*, *, *> -> tracedTEq(sample(a), sample(b))
    a is Env<*, *> && b is Env<*, *> -> a.env == b.env && tracedTEq(a.value, b.value)
    a is List<*> && b is List<*> -> a.size == b.size && a.zip(b).all { (x, y) -> tracedTEq(x, y) }
    else -> a == b
}

class TracedTTest : DescribeSpec({
    describe("TracedT") {
        val type = StringMonoid::class.java
        val base = object : EnvComonad<String> {}
        val instance = object : TracedTComonadTrans<StringMonoid, Base>, TracedTComonadTraced<StringMonoid, Base> {
            override val comonad = base
            override val type = type
        }
        val tracedT = TracedT(base, type, Env("ctx") { m: StringMonoid -> "<${m.value}>" })

        context("as Comonad") {
            val law = ComonadLaw(instance, ::tracedTEq)
            it("should satisfy laws") { law.assertSatisfyingComonadLaw(tracedT) }
            it("should satisfy laws with Int values") { law.assertSatisfyingComonadLaw(tracedT fmap { it.length }) }
        }

        context("as ComonadTrans") {
            it("should satisfy laws") {
                ComonadTransLaw(base, instance, ::tracedTEq).assertSatisfyingComonadTransLaw(tracedT) { w -> Env.narrow(w).let { "${it.env}:${it.value}" } }
            }
        }

        context("as ComonadTraced") {
            val law = ComonadTracedLaw(instance, StringMonoid("")) { a, b -> StringMonoid(a.value + b.value) }
            it("should satisfy laws") { law.assertSatisfyingComonadTracedLaw(StringMonoid("ab"), StringMonoid("c"), tracedT) }
        }

        context("operations") {
            val identity = object : IdentityComonad {}
            val simple = TracedT(identity, type, Identity { m: StringMonoid -> m.value.length })

            it("should extract at mzero") { simple.extract() shouldBe 0 }
            it("should trace at an offset") { simple.trace(StringMonoid("abc")) shouldBe 3 }
            it("should trace at an offset derived from the focus") { (simple fmap { "x".repeat(it + 2) }).traces { StringMonoid(it) } shouldBe "xxxx" }
            it("should lower to the base comonad") { simple.lower() shouldBe Identity(0) }

            it("should extend with shifted offsets") {
                val result = simple extend { w -> TracedT.narrow(w).trace(StringMonoid("ab")) }
                result.trace(StringMonoid("c")) shouldBe 3
            }
        }
    }
})
