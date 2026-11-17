package com.github.kotlinz.kotlinz.data.traced

import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.law.ComonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private val traces = listOf("", "a", "bc").map(::StringMonoid)

@Suppress("UNCHECKED_CAST")
private fun tracedEq(a: Any?, b: Any?): Boolean {
    if (a !is Traced<*, *> || b !is Traced<*, *>) return a == b
    val x = a as Traced<StringMonoid, Any?>
    val y = b as Traced<StringMonoid, Any?>
    return traces.all { tracedEq(x.trace(it), y.trace(it)) }
}

class TracedTest : DescribeSpec({
    describe("Traced") {
        val type = StringMonoid::class.java
        val traced = Traced(type) { m: StringMonoid -> m.value.length }

        context("as Comonad") {
            val law = ComonadLaw(object : TracedComonad<StringMonoid> { override val type = type }, ::tracedEq)

            it("should satisfy laws") { law.assertSatisfyingComonadLaw(traced) }
            it("should satisfy laws with String values") { law.assertSatisfyingComonadLaw(Traced(type) { m: StringMonoid -> "<${m.value}>" }) }
        }

        context("operations") {
            it("should extract the value at mzero") { traced.extract() shouldBe 0 }
            it("should trace with an accumulated value") { traced.trace(StringMonoid("abc")) shouldBe 3 }
            it("should trace with a value derived from the focus") { traced.traces { StringMonoid("x".repeat(it + 2)) } shouldBe 2 }
            it("should listen to the accumulated value") { traced.listen().trace(StringMonoid("ab")) shouldBe Pair(2, StringMonoid("ab")) }
            it("should censor the accumulated value") { traced.censor { StringMonoid(it.value + "!") }.extract() shouldBe 1 }

            it("should accumulate traces through duplicate") {
                val nested = Traced.narrow(traced.duplicate().trace(StringMonoid("ab")))
                nested.trace(StringMonoid("cde")) shouldBe 5
            }

            it("should extend with a lookahead computation") {
                val ahead = traced extend { w -> Traced.narrow(w).trace(StringMonoid("xy")) }
                ahead.trace(StringMonoid("a")) shouldBe 3
            }
        }
    }
})
