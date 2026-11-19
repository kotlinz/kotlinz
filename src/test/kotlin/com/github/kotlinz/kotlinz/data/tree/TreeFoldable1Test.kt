package com.github.kotlinz.kotlinz.data.tree

import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.law.Foldable1Law
import com.github.kotlinz.kotlinz.law.Traversable1Law
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class TreeFoldable1Test : DescribeSpec({
    describe("Tree") {
        val t = tree(1, tree(2, Tree.leaf(4)), Tree.leaf(3))
        val samples = listOf(t, Tree.leaf(1), tree(1, Tree.leaf(2)))
        val positive = { x: Int -> if (x > 0) Maybe.Just(x) else Maybe.None<Int>() }

        context("as Foldable1") {
            it("should satisfy laws") { samples.forEach { Foldable1Law(object : TreeFoldable1 {}).assertSatisfyingFoldable1Law(it) { x, y -> x - y } } }
            it("should reduce in pre-order") {
                t.reduceLeft { x, y -> x - y } shouldBe -8
                t.reduceMap(StringMonoid::class.java) { StringMonoid("$it") } shouldBe StringMonoid("1243")
            }
        }

        context("as Traversable1") {
            val maybe = object : MaybeApplicative {}

            it("should satisfy laws") { samples.forEach { Traversable1Law(object : TreeTraversable1 {}).assertSatisfyingTraversable1Law(it, positive) { x -> listKOf(x, -x) } } }
            it("should traverse with an Apply") {
                t.traverse1(maybe, positive) shouldBe Maybe.Just(t)
                tree(1, Tree.leaf(-1)).traverse1(maybe, positive) shouldBe Maybe.None()
            }
        }
    }
})
