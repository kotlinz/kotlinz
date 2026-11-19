package com.github.kotlinz.kotlinz.data.traced

import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.law.ComonadTracedLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class TracedComonadTracedTest : DescribeSpec({
    describe("Traced as ComonadTraced") {
        val type = StringMonoid::class.java
        val instance = object : TracedComonadTraced<StringMonoid> { override val type = type }
        val traced = Traced(type) { m: StringMonoid -> "<${m.value}>" }
        val law = ComonadTracedLaw(instance, StringMonoid("")) { a, b -> StringMonoid(a.value + b.value) }

        it("should satisfy laws") { law.assertSatisfyingComonadTracedLaw(StringMonoid("ab"), StringMonoid("c"), traced) }

        it("should trace at an offset") { instance.trace(StringMonoid("xy"), traced) shouldBe "<xy>" }

        it("should trace at an offset derived from the focus") { instance.traces({ StringMonoid(it) }, traced) shouldBe "<<>>" }
    }
})
