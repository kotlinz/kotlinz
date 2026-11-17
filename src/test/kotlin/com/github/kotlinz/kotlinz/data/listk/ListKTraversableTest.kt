package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.law.TraversableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ListKTraversableTest : DescribeSpec({
    describe("ListK") {
        context("as Traversable") {
            val law = TraversableLaw(object : ListKTraversable {})
            val maybe = object : MaybeApplicative {}
            val positive = { x: Int -> if (x > 0) Maybe.Just(x) else Maybe.None<Int>() }
            val g = { x: Int -> listKOf(x, x * 2) }

            it("should satisfy laws with non-empty list") { law.assertSatisfyingTraversableLaw(listKOf(1, 2, 3), positive, g) }
            it("should satisfy laws with failing element") { law.assertSatisfyingTraversableLaw(listKOf(1, -2, 3), positive, g) }
            it("should satisfy laws with empty list") { law.assertSatisfyingTraversableLaw(listKOf<Int>(), positive, g) }

            it("should traverse when every element succeeds") {
                listKOf(1, 2, 3).traverse(maybe, positive) shouldBe Maybe.Just(listKOf(1, 2, 3))
            }

            it("should short-circuit when an element fails") {
                listKOf(1, -2, 3).traverse(maybe, positive) shouldBe Maybe.None<ListK<Int>>()
            }

            it("should sequence a list of Maybe") {
                ListK.sequence(maybe, listKOf(Maybe.Just(1), Maybe.Just(2))) shouldBe Maybe.Just(listKOf(1, 2))
                ListK.sequence(maybe, listKOf(Maybe.Just(1), Maybe.None())) shouldBe Maybe.None<ListK<Int>>()
            }

            it("should sequence into every combination with ListK applicative") {
                val result = ListK.sequence(object : ListKApplicative {}, listKOf(listKOf(1, 2), listKOf(3, 4)))
                result shouldBe listKOf(listKOf(1, 3), listKOf(1, 4), listKOf(2, 3), listKOf(2, 4))
            }
        }
    }
})
