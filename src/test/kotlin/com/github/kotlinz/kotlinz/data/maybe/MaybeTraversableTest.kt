package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKApplicative
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.law.TraversableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class MaybeTraversableTest : DescribeSpec({
    describe("Maybe") {
        context("as Traversable") {
            val law = TraversableLaw(object : MaybeTraversable {})
            val listK = object : ListKApplicative {}
            val positive = { x: Int -> if (x > 0) Maybe.Just(x) else Maybe.None<Int>() }
            val g = { x: Int -> listKOf(x, x * 2) }

            it("should satisfy laws with Just") { law.assertSatisfyingTraversableLaw(Maybe.Just(1), positive, g) }
            it("should satisfy laws with failing Just") { law.assertSatisfyingTraversableLaw(Maybe.Just(-1), positive, g) }
            it("should satisfy laws with None") { law.assertSatisfyingTraversableLaw(Maybe.None(), positive, g) }

            it("should traverse Just") {
                Maybe.Just(1).traverse(listK, g) shouldBe listKOf(Maybe.Just(1), Maybe.Just(2))
            }

            it("should wrap None in pure") {
                Maybe.None<Int>().traverse(listK, g) shouldBe listKOf(Maybe.None<Int>())
            }

            it("should sequence Maybe of ListK") {
                Maybe.sequence(listK, Maybe.Just(listKOf(1, 2))) shouldBe listKOf(Maybe.Just(1), Maybe.Just(2))
                Maybe.sequence(listK, Maybe.None<K1<ListK.T, Int>>()) shouldBe listKOf(Maybe.None<Int>())
            }
        }
    }
})
