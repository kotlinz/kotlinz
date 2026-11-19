package com.github.kotlinz.kotlinz.data.lazylist

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.law.FilterableLaw
import com.github.kotlinz.kotlinz.law.WitherableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class LazyListFilterableTest : DescribeSpec({
    describe("LazyList") {
        val even = { x: Int -> if (x % 2 == 0) Maybe.Just(x / 2) else Maybe.None<Int>() }
        val positive = { x: Int -> if (x > 0) Maybe.Just(x - 1) else Maybe.None<Int>() }
        val xs = lazyListOf(0, 1, 2, 3, 4)

        context("as Filterable") {
            val law = FilterableLaw(object : LazyListFilterable {})

            it("should satisfy laws") {
                law.assertSatisfyingFilterableLaw(xs, even, positive, { x -> x > 1 }, { x -> x + 1 })
                law.assertSatisfyingFilterableLaw(LazyList.empty(), even, positive, { x -> x > 1 }, { x -> x + 1 })
            }
            it("should filter, mapFilter and collect") {
                (xs filter { it % 2 == 1 }) shouldBe lazyListOf(1, 3)
                (xs mapFilter even) shouldBe lazyListOf(0, 1, 2)
                (xs collect { if (it > 2) "n$it" else null }) shouldBe lazyListOf("n3", "n4")
            }
            it("should filter infinite lists lazily") {
                (LazyList.iterate(0) { it + 1 } filter { it % 1000 == 999 }).take(3).toList() shouldBe listOf(999, 1999, 2999)
            }
        }

        context("as Witherable") {
            val law = WitherableLaw(object : LazyListWitherable {})
            val maybe = object : MaybeApplicative {}

            it("should satisfy laws") { law.assertSatisfyingWitherableLaw(xs, even) { x -> x > 1 } }
            it("should wither and filterA with an effect") {
                xs.wither(maybe) { Maybe.Just(even(it)) } shouldBe Maybe.Just(lazyListOf(0, 1, 2))
                xs.filterA(maybe) { Maybe.Just(it > 2) } shouldBe Maybe.Just(lazyListOf(3, 4))
                xs.filterA(maybe) { if (it == 3) Maybe.None() else Maybe.Just(true) } shouldBe Maybe.None()
            }
        }
    }
})
