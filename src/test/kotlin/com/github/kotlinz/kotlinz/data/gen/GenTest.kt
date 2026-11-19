package com.github.kotlinz.kotlinz.data.gen

import com.github.kotlinz.kotlinz.law.ObservedMonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class GenTest : DescribeSpec({
    describe("Gen") {
        val seeds = (0L..20L).toList()
        fun <A> samples(gen: Gen<A>): List<A> = (0L until 500L).map { gen.sample(it) }

        context("as Monad") {
            val law = ObservedMonadLaw(object : GenMonad {}) { g -> seeds.map { Gen.narrow(g).run(Seed(it)) } }
            val f = { x: Int -> Gen.choose(0, x + 10) }
            val g = { x: Int -> Gen.boolean() fmap { if (it) x else -x } }

            it("should satisfy laws") { law.assertSatisfyingMonadLaw(3, Gen.int(), f, g) }
            it("should satisfy laws with pure") { law.assertSatisfyingMonadLaw(3, Gen.pure(3), f, g) }
        }

        context("determinism") {
            it("should produce the same value and seed for the same seed") {
                val gen = Gen.listOf(5, Gen.int())
                gen.run(Seed(42L)) shouldBe gen.run(Seed(42L))
            }

            it("should produce different values for different seeds") {
                Gen.long().sample(1L) shouldNotBe Gen.long().sample(2L)
            }

            it("should match the SplitMix64 reference sequence of SplittableRandom") {
                val reference = java.util.SplittableRandom(1234567L)
                Gen.listOf(5, Gen.long()).sample(1234567L) shouldBe List(5) { reference.nextLong() }
            }

            it("should advance the seed through bind") {
                Gen.listOf(10, Gen.long()).sample(7L).toSet().size shouldBe 10
            }
        }

        context("primitives") {
            it("should keep choose within its inclusive bounds") {
                val values = samples(Gen.choose(-2, 3))
                values.all { it in -2..3 } shouldBe true
                values.toSet() shouldBe (-2..3).toSet()
            }

            it("should cover the full Int range edges without overflow") {
                samples(Gen.choose(Int.MIN_VALUE, Int.MAX_VALUE)).toSet().size shouldBe 500
                samples(Gen.int(5..5)).toSet() shouldBe setOf(5)
            }

            it("should generate both booleans and doubles in [0, 1)") {
                samples(Gen.boolean()).toSet() shouldBe setOf(true, false)
                samples(Gen.double()).all { it >= 0.0 && it < 1.0 } shouldBe true
            }
        }

        context("combinators") {
            it("should pick elements and generators") {
                samples(Gen.elements("a", "b", "c")).toSet() shouldBe setOf("a", "b", "c")
                samples(Gen.oneOf(Gen.pure(1), Gen.choose(10, 11))).toSet() shouldContainAll setOf(1, 10, 11)
            }

            it("should generate lists of a fixed or generated size") {
                Gen.listOf(4, Gen.int()).sample(3L).size shouldBe 4
                samples(Gen.listOf(Gen.choose(0, 3), Gen.int())).map { it.size }.toSet() shouldBe setOf(0, 1, 2, 3)
            }
        }

        context("fx") {
            it("should thread the seed through binds") {
                val pair = Gen.fx { Pair(Gen.long().bind(), Gen.long().bind()) }
                pair.sample(9L) shouldBe Gen.listOf(2, Gen.long()).sample(9L).let { Pair(it[0], it[1]) }
                pair.sample(9L) shouldBe pair.sample(9L)
            }
        }
    }
})
