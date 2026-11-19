package com.github.kotlinz.kotlinz.data.lazylist

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.law.AlignLaw
import com.github.kotlinz.kotlinz.law.AlternativeLaw
import com.github.kotlinz.kotlinz.law.ApplicativeLaw
import com.github.kotlinz.kotlinz.law.FoldableLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import com.github.kotlinz.kotlinz.law.MonadLaw
import com.github.kotlinz.kotlinz.law.SemialignLaw
import com.github.kotlinz.kotlinz.law.TraversableLaw
import com.github.kotlinz.kotlinz.law.ZipLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class LazyListTest : DescribeSpec({
    describe("LazyList") {
        val xs = lazyListOf(1, 2, 3)
        val f = { x: Int -> lazyListOf(x, x * 10) }
        val g = { x: Int -> if (x > 5) LazyList.empty() else lazyListOf(x + 1) }

        context("as Functor") {
            it("should satisfy laws") { FunctorLaw(object : LazyListFunctor {}).assertSatisfyingFunctorLaw(xs, { it + 1 }, { it * 2 }) }
        }

        context("as Applicative") {
            val law = ApplicativeLaw(object : LazyListApplicative {})
            it("should satisfy laws") {
                law.assertSatisfyingApplicativeLaw(xs, 1, lazyListOf({ x: Int -> x + 1 }), lazyListOf({ x: Int -> x * 2 }, { x: Int -> x - 1 }), { x: Int -> x * 3 })
            }
            it("should apply every function to every value") {
                (lazyListOf(1, 2) ap lazyListOf({ x: Int -> x + 1 }, { x: Int -> x * 2 })) shouldBe lazyListOf(2, 3, 2, 4)
            }
        }

        context("as Monad") {
            val law = MonadLaw(object : LazyListMonad {})
            it("should satisfy left identity") { law.leftIdentity(1, f).shouldBeTrue() }
            it("should satisfy right identity") { law.rightIdentity(xs).shouldBeTrue() }
            it("should satisfy associativity") { law.associativity(xs, f, g).shouldBeTrue() }
            it("should bind every element") { (xs bind f) shouldBe lazyListOf(1, 10, 2, 20, 3, 30) }
            it("should flatten with join") { LazyList.join(lazyListOf(xs, LazyList.empty(), xs)) shouldBe lazyListOf(1, 2, 3, 1, 2, 3) }
        }

        context("as Alternative") {
            it("should satisfy laws") { AlternativeLaw(object : LazyListAlternative {}).assertSatisfyingAlternativeLaw(xs, lazyListOf(4), LazyList.empty()) }
            it("should concatenate") { (xs orElse lazyListOf(4)) shouldBe lazyListOf(1, 2, 3, 4) }
        }

        context("as Foldable and Traversable") {
            val maybe = object : MaybeApplicative {}
            val positive = { x: Int -> if (x > 0) Maybe.Just(x) else Maybe.None<Int>() }

            it("should satisfy foldable laws") { FoldableLaw(object : LazyListFoldable {}).assertSatisfyingFoldableLaw(xs) }
            it("should satisfy traversable laws") { TraversableLaw(object : LazyListTraversable {}).assertSatisfyingTraversableLaw(xs, positive) { listKOf(it, -it) } }
            it("should fold") {
                xs.foldl("") { acc, x -> acc + x } shouldBe "123"
                xs.foldr("") { x, acc -> acc + x } shouldBe "321"
            }
            it("should traverse") {
                xs.traverse(maybe, positive) shouldBe Maybe.Just(xs)
                lazyListOf(1, -1).traverse(maybe, positive) shouldBe Maybe.None()
                LazyList.sequence(maybe, lazyListOf(Maybe.Just(1), Maybe.Just(2))) shouldBe Maybe.Just(lazyListOf(1, 2))
            }
        }

        context("as Semialign, Align and Zip") {
            val ys = lazyListOf(10)
            val zs = lazyListOf(100, 200)
            it("should satisfy semialign laws") { SemialignLaw(object : LazyListSemialign {}).assertSatisfyingSemialignLaw(xs, ys, zs, { it + 1 }, { it * 2 }) }
            it("should satisfy align laws") { AlignLaw(object : LazyListAlign {}).assertSatisfyingAlignLaw(xs) }
            it("should satisfy zip laws") { ZipLaw(object : LazyListZip {}).assertSatisfyingZipLaw(xs, ys, zs) }
            it("should zip an infinite list with a finite one") {
                (LazyList.iterate(0) { it + 1 } zip lazyListOf("a", "b")) shouldBe lazyListOf(Pair(0, "a"), Pair(1, "b"))
                LazyList.nil<Int>() shouldBe LazyList.empty()
            }
        }

        context("laziness") {
            val naturals = LazyList.iterate(0) { it + 1 }

            it("should take from an infinite list") { naturals.take(3).toList() shouldBe listOf(0, 1, 2) }
            it("should map and bind an infinite list lazily") {
                (naturals fmap { it * 2 }).take(3).toList() shouldBe listOf(0, 2, 4)
                (naturals bind { lazyListOf(it, it) }).take(4).toList() shouldBe listOf(0, 0, 1, 1)
                (naturals bind { if (it % 2 == 0) LazyList.empty() else lazyListOf(it) }).take(2).toList() shouldBe listOf(1, 3)
            }
            it("should not evaluate elements that are never inspected") {
                var calls = 0
                val mapped = naturals fmap { calls++; it }
                calls shouldBe 0
                mapped.take(2).toList()
                calls shouldBe 2
            }
            it("should memoize evaluated cells") {
                var calls = 0
                val list = LazyList.unfold(0) { s -> calls++; if (s < 3) Maybe.Just(Pair(s, s + 1)) else Maybe.None() }
                list.toList() shouldBe listOf(0, 1, 2)
                list.toList() shouldBe listOf(0, 1, 2)
                calls shouldBe 4
            }
            it("should show only the evaluated prefix") {
                val list = LazyList.iterate(1) { it + 1 }
                list.toString() shouldBe "LazyList(?)"
                list.take(2).toList()
                list.toString() shouldBe "LazyList(1, 2, ?)"
                lazyListOf(1, 2).also { it.toList() }.toString() shouldBe "LazyList(1, 2)"
            }
            it("should drop, takeWhile and repeat") {
                naturals.drop(5).take(2).toList() shouldBe listOf(5, 6)
                naturals.takeWhile { it < 3 }.toList() shouldBe listOf(0, 1, 2)
                LazyList.repeat("a").take(2).toList() shouldBe listOf("a", "a")
                naturals.drop(100_000).head() shouldBe Maybe.Just(100_000)
            }
            it("should concatenate lazily with an infinite list") { (lazyListOf(-1) + naturals).take(2).toList() shouldBe listOf(-1, 0) }
        }

        context("with fx") {
            it("should replay the block for every element") {
                LazyList.fx { lazyListOf(1, 2).bind() * lazyListOf(10, 100).bind() } shouldBe lazyListOf(10, 100, 20, 200)
            }
        }

        context("equality") {
            it("should compare elements") {
                lazyListOf(1, 2) shouldBe listOf(1, 2).toLazyList()
                lazyListOf(1, 2) shouldNotBe lazyListOf(1)
                lazyListOf(1, 2).hashCode() shouldBe lazyListOf(1, 2).hashCode()
                xs.uncons() shouldBe Maybe.Just(Pair(1, lazyListOf(2, 3)))
                LazyList.empty<Int>().isEmpty().shouldBeTrue()
            }
        }
    }
})
