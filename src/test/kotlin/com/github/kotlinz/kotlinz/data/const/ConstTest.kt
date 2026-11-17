package com.github.kotlinz.kotlinz.data.const

import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.law.ApplicativeLaw
import com.github.kotlinz.kotlinz.law.ContravariantLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ConstTest : DescribeSpec({
    describe("Const") {
        val type = StringMonoid::class.java
        val const = Const<StringMonoid, Int>(StringMonoid("a"))

        context("as Functor") {
            val law = FunctorLaw(object : ConstFunctor<StringMonoid> {})

            it("should satisfy laws") { law.assertSatisfyingFunctorLaw(const, { it + 1 }, { it * 2 }) }
            it("should ignore the mapped function") { (const fmap { it.toString() }) shouldBe Const(StringMonoid("a")) }
        }

        context("as Contravariant") {
            val law = ContravariantLaw(object : ConstContravariant<StringMonoid> {}) { a, b -> a == b }

            it("should satisfy laws") { law.assertSatisfyingContravariantLaw(const) }
            it("should ignore the mapped function") { (const contramap { s: String -> s.length }) shouldBe Const(StringMonoid("a")) }
        }

        context("as Applicative") {
            val law = ApplicativeLaw(object : ConstApplicative<StringMonoid> { override val type = type })
            val u = Const<StringMonoid, (String) -> Int>(StringMonoid("u"))
            val v = Const<StringMonoid, (Int) -> String>(StringMonoid("v"))

            it("should satisfy laws") { law.assertSatisfyingApplicativeLaw(const, 1, u, v) { it.toString() } }

            it("should combine values with the monoid") {
                val add = Const.liftA2<StringMonoid, Int, Int, Int>(type) { a, b -> a + b }
                add(const, Const(StringMonoid("b"))) shouldBe Const(StringMonoid("ab"))
            }

            it("should use the monoid identity for pure") {
                Const.pure(type, 42) shouldBe Const(StringMonoid(""))
            }

            it("should combine values with ap") {
                const.ap(type, Const<StringMonoid, (Int) -> Int>(StringMonoid("f"))) shouldBe Const<StringMonoid, Int>(StringMonoid("fa"))
            }
        }
    }
})
