package com.github.kotlinz.kotlinz.type

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityFunctor
import com.github.kotlinz.kotlinz.data.predicate.Predicate
import com.github.kotlinz.kotlinz.data.predicate.PredicateContravariant
import com.github.kotlinz.kotlinz.law.InvariantLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class InvariantTest : DescribeSpec({
    describe("Invariant") {
        context("derived from Functor") {
            val functor = object : IdentityFunctor {}
            val law = InvariantLaw(functor)

            it("should satisfy laws") { law.assertSatisfyingInvariantLaw(Identity(10)) }
            it("should use the covariant function") { functor.imap({ a: Int -> a + 1 }, { a: Int -> a - 1 }, Identity(1)) shouldBe Identity(2) }
        }

        context("derived from Contravariant") {
            val contravariant = object : PredicateContravariant {}
            val law = InvariantLaw(contravariant, ::samePredicate)

            it("should satisfy laws") { law.assertSatisfyingInvariantLaw(Predicate { a: Int -> a > 0 }) }
            it("should use the contravariant function") {
                val mapped = Predicate.narrow(contravariant.imap({ a: Int -> a + 1 }, { a: Int -> a - 1 }, Predicate { a: Int -> a > 0 }))
                mapped.test(1) shouldBe false
                mapped.test(2) shouldBe true
            }
        }
    }
})

private fun samePredicate(a: K1<Predicate.T, Int>, b: K1<Predicate.T, Int>): Boolean =
        (-10..10).all { Predicate.narrow(a).test(it) == Predicate.narrow(b).test(it) }
