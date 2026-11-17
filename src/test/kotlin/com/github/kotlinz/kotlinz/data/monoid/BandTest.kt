package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.law.BandLaw
import io.kotest.core.spec.style.DescribeSpec

@Suppress("UNCHECKED_CAST")
class BandTest : DescribeSpec({
    val values = listOf(Maybe.Just(2), Maybe.None(), Maybe.Just(1))

    describe("First Monoid") {
        val law = BandLaw(FirstMonoid::class.java as Class<FirstMonoid<Int>>)
        val (a, b, c) = values.map { it.toFirstMonoid() }
        it("should satisfy band laws") { law.assertSatisfyingBandLaw(a, b, c) }
    }

    describe("Last Monoid") {
        val law = BandLaw(LastMonoid::class.java as Class<LastMonoid<Int>>)
        val (a, b, c) = values.map { it.toLastMonoid() }
        it("should satisfy band laws") { law.assertSatisfyingBandLaw(a, b, c) }
    }

    describe("Min Monoid") {
        val law = BandLaw(MinMonoid::class.java as Class<MinMonoid<Int>>)
        val (a, b, c) = values.map { it.toMinMonoid() }
        it("should satisfy band laws") { law.assertSatisfyingBandLaw(a, b, c) }
    }

    describe("Max Monoid") {
        val law = BandLaw(MaxMonoid::class.java as Class<MaxMonoid<Int>>)
        val (a, b, c) = values.map { it.toMaxMonoid() }
        it("should satisfy band laws") { law.assertSatisfyingBandLaw(a, b, c) }
    }

    describe("All Monoid") {
        val law = BandLaw(AllMonoid::class.java)
        it("should satisfy band laws") { law.assertSatisfyingBandLaw(true.toAllMonoid(), false.toAllMonoid(), true.toAllMonoid()) }
    }

    describe("Any Monoid") {
        val law = BandLaw(AnyMonoid::class.java)
        it("should satisfy band laws") { law.assertSatisfyingBandLaw(false.toAnyMonoid(), true.toAnyMonoid(), false.toAnyMonoid()) }
    }
})
