package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKApplicative
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.law.FilterableLaw
import com.github.kotlinz.kotlinz.law.WitherableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class MaybeFilterableTest : DescribeSpec({
    describe("Maybe") {
        val even = { x: Int -> if (x % 2 == 0) Maybe.Just(x / 2) else Maybe.None<Int>() }
        val positive = { x: Int -> if (x > 0) Maybe.Just(x - 1) else Maybe.None<Int>() }
        val samples = listOf(Maybe.Just(4), Maybe.Just(3), Maybe.Just(0), Maybe.None())

        context("as Filterable") {
            val law = FilterableLaw(object : MaybeFilterable {})

            it("should satisfy laws") { samples.forEach { law.assertSatisfyingFilterableLaw(it, even, positive, { x -> x > 1 }, { x -> x + 1 }) } }

            it("should filter the value") {
                (Maybe.Just(2) filter { it > 1 }) shouldBe Maybe.Just(2)
                (Maybe.Just(1) filter { it > 1 }) shouldBe Maybe.None()
            }

            it("should mapFilter and collect") {
                (Maybe.Just(4) mapFilter even) shouldBe Maybe.Just(2)
                (Maybe.Just(3) collect { if (it > 2) "big" else null }) shouldBe Maybe.Just("big")
                (Maybe.Just(1) collect { if (it > 2) "big" else null }) shouldBe Maybe.None()
            }

            it("should flatten with catMaybes") {
                Maybe.catMaybes(Maybe.Just(Maybe.Just(1))) shouldBe Maybe.Just(1)
                Maybe.catMaybes(Maybe.Just(Maybe.None<Int>())) shouldBe Maybe.None()
            }
        }

        context("as Witherable") {
            val law = WitherableLaw(object : MaybeWitherable {})
            val listK = object : ListKApplicative {}

            it("should satisfy laws") { samples.forEach { law.assertSatisfyingWitherableLaw(it, even) { x -> x > 1 } } }

            it("should wither with an effect") {
                ListK.narrow(Maybe.Just(2).wither(listK) { x -> listKOf(Maybe.Just(x), Maybe.None()) }).value shouldBe
                        listOf(Maybe.Just(2), Maybe.None())
            }

            it("should filterA with an effect") {
                ListK.narrow(Maybe.Just(2).filterA(listK) { listKOf(true, false) }).value shouldBe listOf(Maybe.Just(2), Maybe.None())
            }
        }
    }
})
