package com.github.kotlinz.kotlinz.data.monoidof

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.monoid.toStringMonoid
import com.github.kotlinz.kotlinz.law.InvariantLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class MonoidOfTest : DescribeSpec({
    describe("MonoidOf") {
        context("as Invariant") {
            val law = InvariantLaw(object : MonoidOfInvariant {}, ::sameMonoid)

            it("should satisfy laws") { law.assertSatisfyingInvariantLaw(MonoidOf(0) { a: Int, b: Int -> a + b }) }

            it("should transport a Monoid through an isomorphism") {
                val monoid = MonoidOf.from(StringMonoid::class.java).imap({ it.value }, { it.toStringMonoid() })
                monoid.empty shouldBe ""
                monoid.combine("foo", "bar") shouldBe "foobar"
            }
        }
    }
})

private fun sameMonoid(a: K1<MonoidOf.T, Int>, b: K1<MonoidOf.T, Int>): Boolean {
    val (ma, mb) = Pair(MonoidOf.narrow(a), MonoidOf.narrow(b))
    return ma.empty == mb.empty && (-5..5).all { x -> (-5..5).all { y -> ma.combine(x, y) == mb.combine(x, y) } }
}
