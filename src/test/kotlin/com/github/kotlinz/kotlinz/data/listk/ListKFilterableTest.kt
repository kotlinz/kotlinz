package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.law.FilterableLaw
import com.github.kotlinz.kotlinz.law.WitherableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ListKFilterableTest : DescribeSpec({
    describe("ListK") {
        val even = { x: Int -> if (x % 2 == 0) Maybe.Just(x / 2) else Maybe.None<Int>() }
        val positive = { x: Int -> if (x > 0) Maybe.Just(x - 1) else Maybe.None<Int>() }
        val xs = listKOf(0, 1, 2, 3, 4)

        context("as Filterable") {
            val law = FilterableLaw(object : ListKFilterable {})

            it("should satisfy laws") {
                law.assertSatisfyingFilterableLaw(xs, even, positive, { x -> x > 1 }, { x -> x + 1 })
                law.assertSatisfyingFilterableLaw(listKOf(), even, positive, { x -> x > 1 }, { x -> x + 1 })
            }

            it("should filter, mapFilter and collect preserving order") {
                (xs filter { it % 2 == 1 }) shouldBe listKOf(1, 3)
                (xs mapFilter even) shouldBe listKOf(0, 1, 2)
                (xs collect { if (it > 2) "n$it" else null }) shouldBe listKOf("n3", "n4")
            }

            it("should drop None elements with catMaybes") {
                ListK.catMaybes(listKOf(Maybe.Just(1), Maybe.None(), Maybe.Just(3))) shouldBe listKOf(1, 3)
            }
        }

        context("as Witherable") {
            val law = WitherableLaw(object : ListKWitherable {})
            val maybe = object : MaybeApplicative {}

            it("should satisfy laws") { law.assertSatisfyingWitherableLaw(xs, even) { x -> x > 1 } }

            it("should wither with an effect") {
                val halveNonNegative = { x: Int -> if (x < 0) Maybe.None<Maybe<Int>>() else Maybe.Just(even(x)) }
                xs.wither(maybe, halveNonNegative) shouldBe Maybe.Just(listKOf(0, 1, 2))
                listKOf(2, -1).wither(maybe, halveNonNegative) shouldBe Maybe.None()
            }

            it("should filterA with an effect") {
                xs.filterA(maybe) { Maybe.Just(it > 2) } shouldBe Maybe.Just(listKOf(3, 4))
                xs.filterA(maybe) { if (it == 3) Maybe.None() else Maybe.Just(true) } shouldBe Maybe.None()
            }
        }
    }
})
