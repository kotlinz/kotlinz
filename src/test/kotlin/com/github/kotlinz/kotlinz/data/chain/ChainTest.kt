package com.github.kotlinz.kotlinz.data.chain

import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.data.monoid.SumMonoid
import com.github.kotlinz.kotlinz.law.AlignLaw
import com.github.kotlinz.kotlinz.law.ApplicativeLaw
import com.github.kotlinz.kotlinz.law.FoldableLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import com.github.kotlinz.kotlinz.law.MonadLaw
import com.github.kotlinz.kotlinz.law.MonoidLaw
import com.github.kotlinz.kotlinz.law.SemialignLaw
import com.github.kotlinz.kotlinz.law.TraversableLaw
import com.github.kotlinz.kotlinz.law.ZipLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ChainTest : DescribeSpec({
    describe("Chain") {
        val xs = Chain.one(1) + chainOf(2) + Chain.empty() + chainOf(3)
        val f = { x: Int -> chainOf(x, x * 10) }
        val g = { x: Int -> if (x > 5) Chain.empty() else Chain.one(x + 1) }

        context("as Functor and Applicative") {
            it("should satisfy functor laws") { FunctorLaw(object : ChainFunctor {}).assertSatisfyingFunctorLaw(xs, { it + 1 }, { it * 2 }) }
            it("should satisfy applicative laws") {
                ApplicativeLaw(object : ChainApplicative {}).assertSatisfyingApplicativeLaw(xs, 1, chainOf({ x: Int -> x + 1 }), chainOf({ x: Int -> x * 2 }, { x: Int -> x - 1 }), { x: Int -> x * 3 })
            }
            it("should map and apply") {
                (xs fmap { it * 2 }) shouldBe chainOf(2, 4, 6)
                (chainOf(1, 2) ap chainOf({ x: Int -> x + 1 }, { x: Int -> x * 2 })) shouldBe chainOf(2, 3, 2, 4)
            }
        }

        context("as Monad") {
            val law = MonadLaw(object : ChainMonad {})
            it("should satisfy left identity") { law.leftIdentity(1, f).shouldBeTrue() }
            it("should satisfy right identity") { law.rightIdentity(xs).shouldBeTrue() }
            it("should satisfy associativity") { law.associativity(xs, f, g).shouldBeTrue() }
            it("should bind every element") { (xs bind f) shouldBe chainOf(1, 10, 2, 20, 3, 30) }
            it("should flatten with join") { Chain.join(chainOf(xs, Chain.empty(), xs)) shouldBe chainOf(1, 2, 3, 1, 2, 3) }
            it("should replay fx for every element") { Chain.fx { chainOf(1, 2).bind() * chainOf(10, 100).bind() } shouldBe chainOf(10, 100, 20, 200) }
        }

        context("as Foldable and Traversable") {
            val maybe = object : MaybeApplicative {}
            val positive = { x: Int -> if (x > 0) Maybe.Just(x) else Maybe.None<Int>() }

            it("should satisfy foldable laws") { FoldableLaw(object : ChainFoldable {}).assertSatisfyingFoldableLaw(xs) }
            it("should satisfy traversable laws") { TraversableLaw(object : ChainTraversable {}).assertSatisfyingTraversableLaw(xs, positive) { listKOf(it, -it) } }
            it("should fold") {
                xs.foldl("") { acc, x -> acc + x } shouldBe "123"
                xs.foldr("") { x, acc -> acc + x } shouldBe "321"
                xs.foldMap(SumMonoid::class.java) { SumMonoid(it) } shouldBe SumMonoid(6)
            }
            it("should traverse and sequence") {
                xs.traverse(maybe, positive) shouldBe Maybe.Just(xs)
                chainOf(1, -1).traverse(maybe, positive) shouldBe Maybe.None()
                Chain.sequence(maybe, chainOf(Maybe.Just(1), Maybe.Just(2))) shouldBe Maybe.Just(chainOf(1, 2))
            }
        }

        context("as Monoid") {
            val type = Chain::class.java as Class<Chain<Int>>

            it("should satisfy laws") { MonoidLaw(type).assertSatisfyingMonoidLaw(xs, chainOf(4), Chain.empty()) }
            it("should offer a first-class monoid") { Chain.monoid<Int>().combine(chainOf(1), chainOf(2)) shouldBe chainOf(1, 2) }
        }

        context("as Semialign, Align and Zip") {
            val ys = chainOf(10)
            val zs = chainOf(100, 200)
            it("should satisfy semialign laws") { SemialignLaw(object : ChainSemialign {}).assertSatisfyingSemialignLaw(xs, ys, zs, { it + 1 }, { it * 2 }) }
            it("should satisfy align laws") { AlignLaw(object : ChainAlign {}).assertSatisfyingAlignLaw(xs) }
            it("should satisfy zip laws") { ZipLaw(object : ChainZip {}).assertSatisfyingZipLaw(xs, ys, zs) }
            it("should zip") {
                (xs zip chainOf("a", "b")) shouldBe chainOf(Pair(1, "a"), Pair(2, "b"))
                Chain.nil<Int>() shouldBe Chain.empty()
            }
        }

        context("structure") {
            it("should prepend and append") { chainOf(2).prepend(1).append(3) shouldBe chainOf(1, 2, 3) }
            it("should track size") {
                xs.size shouldBe 3
                Chain.empty<Int>().isEmpty().shouldBeTrue()
            }
            it("should uncons") {
                xs.uncons() shouldBe Maybe.Just(Pair(1, chainOf(2, 3)))
                (chainOf(1, 2) + chainOf(3)).uncons() shouldBe Maybe.Just(Pair(1, chainOf(2, 3)))
                Chain.empty<Int>().uncons() shouldBe Maybe.None()
            }
            it("should be stack safe for deep chains") {
                val deep = (1..100_000).fold(Chain.empty<Int>()) { acc, i -> acc append i }
                val deepLeft = (1..100_000).fold(Chain.empty<Int>()) { acc, i -> acc prepend i }
                deep.size shouldBe 100_000
                deep.foldl(0L) { acc, i -> acc + i } shouldBe 5_000_050_000L
                deepLeft.uncons().fmap { it.first } shouldBe Maybe.Just(100_000)
                deep.uncons().fmap { it.second.size } shouldBe Maybe.Just(99_999)
            }
            it("should implement equals, hashCode and toString") {
                xs shouldBe chainOf(1, 2, 3)
                xs shouldNotBe chainOf(1, 2)
                xs.hashCode() shouldBe chainOf(1, 2, 3).hashCode()
                xs.toString() shouldBe "Chain(1, 2, 3)"
                listOf(1, 2).toChain() shouldBe chainOf(1, 2)
            }
        }
    }
})
