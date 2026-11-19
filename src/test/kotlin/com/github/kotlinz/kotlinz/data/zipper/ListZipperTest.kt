package com.github.kotlinz.kotlinz.data.zipper

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.monoid.SumMonoid
import com.github.kotlinz.kotlinz.data.nonemptylist.nonEmptyListOf
import com.github.kotlinz.kotlinz.law.ComonadLaw
import com.github.kotlinz.kotlinz.law.Foldable1Law
import com.github.kotlinz.kotlinz.law.FoldableLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import com.github.kotlinz.kotlinz.law.Traversable1Law
import com.github.kotlinz.kotlinz.law.TraversableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ListZipperTest : DescribeSpec({
    describe("ListZipper") {
        val z = ListZipper(listOf(1, 2), 3, listOf(4, 5))
        val samples = listOf(z, ListZipper.of(1), ListZipper.of(1, 2), ListZipper(listOf(1, 2), 3, listOf()))
        val positive = { x: Int -> if (x > 0) Maybe.Just(x) else Maybe.None<Int>() }

        context("navigation") {
            it("should move left and right") {
                z.left() shouldBe Maybe.Just(ListZipper(listOf(1), 2, listOf(3, 4, 5)))
                z.right() shouldBe Maybe.Just(ListZipper(listOf(1, 2, 3), 4, listOf(5)))
                ListZipper.of(1, 2).left() shouldBe Maybe.None()
                ListZipper(listOf(1), 2, listOf()).right() shouldBe Maybe.None()
            }
            it("should jump to positions") {
                z.start() shouldBe ListZipper(listOf(), 1, listOf(2, 3, 4, 5))
                z.end() shouldBe ListZipper(listOf(1, 2, 3, 4), 5, listOf())
                z.moveTo(1).focus shouldBe 2
                z.moveTo(99).focus shouldBe 5
                z.index shouldBe 2
                z.size shouldBe 5
            }
            it("should modify and set the focus") {
                z.modify { it * 10 } shouldBe ListZipper(listOf(1, 2), 30, listOf(4, 5))
                z.set(0).toList() shouldBe listOf(1, 2, 0, 4, 5)
            }
            it("should convert from and to lists") {
                ListZipper.fromList(listOf(1, 2)) shouldBe ListZipper.of(1, 2)
                ListZipper.fromList(listOf<Int>()) shouldBe null
                nonEmptyListOf(1, 2).toListZipper() shouldBe ListZipper.of(1, 2)
                z.toNonEmptyList() shouldBe nonEmptyListOf(1, 2, 3, 4, 5)
                ListZipper.of(1, 2).toNonEmptyList() shouldBe nonEmptyListOf(1, 2)
            }
        }

        context("as Functor") {
            it("should satisfy laws") { FunctorLaw(object : ListZipperFunctor {}).assertSatisfyingFunctorLaw(z, { it + 1 }, { it * 2 }) }
            it("should keep the focus position") { (z fmap { it * 2 }) shouldBe ListZipper(listOf(2, 4), 6, listOf(8, 10)) }
        }

        context("as Comonad") {
            val law = ComonadLaw(object : ListZipperComonad {})

            it("should satisfy laws") { samples.forEach { law.assertSatisfyingComonadLaw(it) } }
            it("should extract the focus") { z.extract() shouldBe 3 }
            it("should duplicate into every focus") {
                ListZipper.of(1, 2).duplicate() shouldBe ListZipper(listOf(), ListZipper.of(1, 2), listOf(ListZipper(listOf(1), 2, listOf())))
            }
            it("should extend with neighbourhood context") {
                val neighbourSum = { w: K1<ListZipper.T, Int> ->
                    val zipper = ListZipper.narrow(w)
                    (zipper.lefts.lastOrNull() ?: 0) + zipper.focus + (zipper.rights.firstOrNull() ?: 0)
                }
                z.extend(neighbourSum) shouldBe ListZipper(listOf(3, 6), 9, listOf(12, 9))
            }
        }

        context("as Foldable and Foldable1") {
            it("should satisfy foldable laws") { FoldableLaw(object : ListZipperFoldable {}).assertSatisfyingFoldableLaw(z) }
            it("should satisfy foldable1 laws") { Foldable1Law(object : ListZipperFoldable1 {}).assertSatisfyingFoldable1Law(z) { x, y -> x - y } }
            it("should fold in list order") {
                z.foldl("") { acc, x -> acc + x } shouldBe "12345"
                z.foldr("") { x, acc -> acc + x } shouldBe "54321"
                z.foldMap(SumMonoid::class.java) { SumMonoid(it) } shouldBe SumMonoid(15)
                z.reduceLeft { x, y -> x - y } shouldBe -13
                z.reduceMap(StringMonoid::class.java) { StringMonoid("$it") } shouldBe StringMonoid("12345")
            }
        }

        context("as Traversable and Traversable1") {
            val maybe = object : MaybeApplicative {}

            it("should satisfy traversable laws") { samples.forEach { TraversableLaw(object : ListZipperTraversable {}).assertSatisfyingTraversableLaw(it, positive) { x -> listKOf(x, -x) } } }
            it("should satisfy traversable1 laws") { samples.forEach { Traversable1Law(object : ListZipperTraversable1 {}).assertSatisfyingTraversable1Law(it, positive) { x -> listKOf(x, -x) } } }
            it("should traverse keeping the focus position") {
                z.traverse(maybe, positive) shouldBe Maybe.Just(z)
                z.set(-1).traverse1(maybe, positive) shouldBe Maybe.None()
            }
        }
    }
})
