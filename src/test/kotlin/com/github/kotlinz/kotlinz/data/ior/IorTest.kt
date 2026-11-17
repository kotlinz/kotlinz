package com.github.kotlinz.kotlinz.data.ior

import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.law.ApplicativeLaw
import com.github.kotlinz.kotlinz.law.BifunctorLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import com.github.kotlinz.kotlinz.law.MonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class IorTest : DescribeSpec({
    describe("Ior") {
        val type = StringMonoid::class.java
        val monad = object : IorMonad<StringMonoid> { override val type = type }
        val samples = listOf(
            Ior.left<StringMonoid, Int>(StringMonoid("l")),
            Ior.right<StringMonoid, Int>(3),
            Ior.both<StringMonoid, Int>(StringMonoid("b"), 4)
        )
        val f = { x: Int -> Ior.both<StringMonoid, Int>(StringMonoid("f"), x + 1) }
        val g = { x: Int -> if (x > 3) Ior.left<StringMonoid, Int>(StringMonoid("g")) else Ior.right(x * 2) }

        context("as Functor") {
            val law = FunctorLaw(monad)

            it("should satisfy laws") { samples.forEach { law.assertSatisfyingFunctorLaw(it, { x -> x + 1 }, { x -> x * 2 }) } }
        }

        context("as Applicative") {
            val law = ApplicativeLaw(monad)
            val u = Ior.both<StringMonoid, (String) -> Int>(StringMonoid("u")) { it.length }
            val v = Ior.both<StringMonoid, (Int) -> String>(StringMonoid("v")) { it.toString() }

            it("should satisfy laws") { samples.forEach { law.assertSatisfyingApplicativeLaw(it, 1, u, v) { x -> x.toString() } } }
        }

        context("as Monad") {
            val law = MonadLaw(monad)

            it("should satisfy laws with pure") { law.assertSatisfyingMonadLaw(Int.MAX_VALUE) }
            it("should satisfy left identity") { law.leftIdentity(1, f).shouldBeTrue() }
            it("should satisfy right identity") { samples.forEach { law.rightIdentity(it).shouldBeTrue() } }
            it("should satisfy associativity") { samples.forEach { law.associativity(it, f, g).shouldBeTrue() } }
        }

        context("as Bifunctor") {
            val law = BifunctorLaw(object : IorBifunctor {})
            val f = { s: StringMonoid -> StringMonoid(s.value + "!") }
            val g = { s: StringMonoid -> StringMonoid(s.value.uppercase()) }

            it("should satisfy laws") { samples.forEach { law.assertSatisfyingBifunctorLaw(it, f, g, { x -> x + 1 }, { x -> x * 2 }) } }

            it("should map both sides") {
                Ior.both<StringMonoid, Int>(StringMonoid("b"), 4).bimap(g) { it + 1 } shouldBe Ior.both(StringMonoid("B"), 5)
            }
        }

        context("left accumulation") {
            it("should accumulate left values of Both") {
                Ior.both<StringMonoid, Int>(StringMonoid("a"), 1).bind(type, f) shouldBe Ior.both(StringMonoid("af"), 2)
            }

            it("should short-circuit on Left keeping accumulated values") {
                Ior.both<StringMonoid, Int>(StringMonoid("a"), 5).bind(type, g) shouldBe Ior.left(StringMonoid("ag"))
            }

            it("should accumulate within fx") {
                val result = Ior.fx(type) {
                    val a = Ior.both<StringMonoid, Int>(StringMonoid("x"), 1).bind()
                    val b = Ior.both<StringMonoid, Int>(StringMonoid("y"), 2).bind()
                    a + b
                }
                result shouldBe Ior.both(StringMonoid("xy"), 3)
            }
        }
    }
})
