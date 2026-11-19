package com.github.kotlinz.kotlinz.data.chain

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.law.FilterableLaw
import com.github.kotlinz.kotlinz.law.WitherableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ChainFilterableTest : DescribeSpec({
    describe("Chain") {
        val even = { x: Int -> if (x % 2 == 0) Maybe.Just(x / 2) else Maybe.None<Int>() }
        val positive = { x: Int -> if (x > 0) Maybe.Just(x - 1) else Maybe.None<Int>() }
        val xs = chainOf(0, 1) + chainOf(2, 3) append 4

        context("as Filterable") {
            val law = FilterableLaw(object : ChainFilterable {})

            it("should satisfy laws") {
                law.assertSatisfyingFilterableLaw(xs, even, positive, { x -> x > 1 }, { x -> x + 1 })
                law.assertSatisfyingFilterableLaw(Chain.empty(), even, positive, { x -> x > 1 }, { x -> x + 1 })
            }
            it("should filter, mapFilter and collect preserving order") {
                (xs filter { it % 2 == 1 }) shouldBe chainOf(1, 3)
                (xs mapFilter even) shouldBe chainOf(0, 1, 2)
                (xs collect { if (it > 2) "n$it" else null }) shouldBe chainOf("n3", "n4")
            }
        }

        context("as Witherable") {
            val law = WitherableLaw(object : ChainWitherable {})
            val maybe = object : MaybeApplicative {}

            it("should satisfy laws") { law.assertSatisfyingWitherableLaw(xs, even) { x -> x > 1 } }
            it("should wither and filterA with an effect") {
                xs.wither(maybe) { Maybe.Just(even(it)) } shouldBe Maybe.Just(chainOf(0, 1, 2))
                xs.filterA(maybe) { Maybe.Just(it > 2) } shouldBe Maybe.Just(chainOf(3, 4))
            }
        }
    }
})
