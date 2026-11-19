package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.law.AlignLaw
import com.github.kotlinz.kotlinz.law.SemialignLaw
import com.github.kotlinz.kotlinz.law.ZipLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class MaybeAlignTest : DescribeSpec({
    describe("Maybe") {
        val just1 = Maybe.Just(1)
        val just2 = Maybe.Just(2)
        val none = Maybe.None<Int>()
        val samples = listOf(just1, just2, none)
        val triples = samples.flatMap { x -> samples.flatMap { y -> samples.map { z -> Triple(x, y, z) } } }

        context("as Semialign") {
            val law = SemialignLaw(object : MaybeSemialign {})

            it("should satisfy laws") { triples.forEach { (x, y, z) -> law.assertSatisfyingSemialignLaw(x, y, z, { it + 1 }, { it * 2 }) } }

            it("should align values") {
                (just1 align Maybe.Just("a")) shouldBe Maybe.Just(Ior.both(1, "a"))
                (just1 align Maybe.None<String>()) shouldBe Maybe.Just(Ior.left(1))
                (none align Maybe.Just("a")) shouldBe Maybe.Just(Ior.right("a"))
                (none align Maybe.None<String>()) shouldBe Maybe.None()
            }

            it("should alignWith a function") {
                just1.alignWith(none) { it.toString() } shouldBe Maybe.Just("Left(value=1)")
            }
        }

        context("as Align") {
            val law = AlignLaw(object : MaybeAlign {})

            it("should satisfy laws") { samples.forEach { law.assertSatisfyingAlignLaw(it) } }
            it("should expose nil") { Maybe.nil<Int>() shouldBe Maybe.None() }
        }

        context("as Zip") {
            val law = ZipLaw(object : MaybeZip {})

            it("should satisfy laws") { triples.forEach { (x, y, z) -> law.assertSatisfyingZipLaw(x, y, z) } }

            it("should zip only when both are present") {
                (just1 zip Maybe.Just("a")) shouldBe Maybe.Just(Pair(1, "a"))
                (just1 zip Maybe.None<String>()) shouldBe Maybe.None()
                just1.zipWith(just2) { a, b -> a + b } shouldBe Maybe.Just(3)
            }
        }
    }
})
