package com.github.kotlinz.kotlinz.data.tuple2

import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.law.ApplicativeLaw
import com.github.kotlinz.kotlinz.law.BifunctorLaw
import com.github.kotlinz.kotlinz.law.ComonadLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import com.github.kotlinz.kotlinz.law.MonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class Tuple2Test : DescribeSpec({
    describe("Tuple2") {
        val type = StringMonoid::class.java
        val monad = object : Tuple2Monad<StringMonoid> { override val type = type }
        val tuple = Tuple2(StringMonoid("a"), 3)

        context("as Functor") {
            val law = FunctorLaw(object : Tuple2Functor<String> {})

            it("should satisfy laws") { law.assertSatisfyingFunctorLaw(Tuple2("env", 3), { it + 1 }, { it * 2 }) }
        }

        context("as Comonad") {
            val law = ComonadLaw(object : Tuple2Comonad<String> {})
            val w = Tuple2("env", 3)

            it("should satisfy laws") { law.assertSatisfyingComonadLaw(w) }

            it("should read the environment with extend") {
                w.extend { Tuple2.narrow(it).let { t -> t.first.length + t.second } }.extract() shouldBe 6
            }
        }

        context("as Applicative") {
            val law = ApplicativeLaw(monad)
            val u = Tuple2<StringMonoid, (String) -> Int>(StringMonoid("u")) { it.length }
            val v = Tuple2<StringMonoid, (Int) -> String>(StringMonoid("v")) { it.toString() }

            it("should satisfy laws") { law.assertSatisfyingApplicativeLaw(tuple, 1, u, v) { it.toString() } }
        }

        context("as Monad") {
            val law = MonadLaw(monad)
            val f = { x: Int -> Tuple2(StringMonoid("f"), x + 1) }
            val g = { x: Int -> Tuple2(StringMonoid("g"), x * 2) }

            it("should satisfy laws with pure") { law.assertSatisfyingMonadLaw("Monad Law") }
            it("should satisfy left identity") { law.leftIdentity(1, f).shouldBeTrue() }
            it("should satisfy right identity") { law.rightIdentity(tuple).shouldBeTrue() }
            it("should satisfy associativity") { law.associativity(tuple, f, g).shouldBeTrue() }

            it("should accumulate the first element") {
                tuple.bind(type, f).bind(type, g) shouldBe Tuple2(StringMonoid("afg"), 8)
            }
        }

        context("as Bifunctor") {
            val law = BifunctorLaw(object : Tuple2Bifunctor {})

            it("should satisfy laws") { law.assertSatisfyingBifunctorLaw(Tuple2("a", 1), { it + "!" }, { it.uppercase() }, { it + 1 }, { it * 2 }) }

            it("should map both elements") {
                Tuple2("a", 1).bimap({ it.length }, { it.toString() }) shouldBe Tuple2(1, "1")
            }
        }

        context("conversion") {
            it("should convert to and from Pair") {
                Pair(1, "x").toTuple2() shouldBe Tuple2(1, "x")
                Tuple2(1, "x").toPair() shouldBe Pair(1, "x")
            }
        }
    }
})
