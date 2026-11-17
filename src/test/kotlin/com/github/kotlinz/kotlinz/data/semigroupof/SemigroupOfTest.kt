package com.github.kotlinz.kotlinz.data.semigroupof

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.monoid.toStringMonoid
import com.github.kotlinz.kotlinz.law.InvariantLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class SemigroupOfTest : DescribeSpec({
    describe("SemigroupOf") {
        context("as Invariant") {
            val law = InvariantLaw(object : SemigroupOfInvariant {}, ::sameSemigroup)

            it("should satisfy laws") { law.assertSatisfyingInvariantLaw(SemigroupOf { a: Int, b: Int -> maxOf(a, b) }) }

            it("should transport a Semigroup through an isomorphism") {
                val semigroup = SemigroupOf.from(StringMonoid::class.java).imap({ it.value }, { it.toStringMonoid() })
                semigroup.combine("foo", "bar") shouldBe "foobar"
            }
        }
    }
})

private fun sameSemigroup(a: K1<SemigroupOf.T, Int>, b: K1<SemigroupOf.T, Int>): Boolean =
        (-5..5).all { x -> (-5..5).all { y -> SemigroupOf.narrow(a).combine(x, y) == SemigroupOf.narrow(b).combine(x, y) } }
