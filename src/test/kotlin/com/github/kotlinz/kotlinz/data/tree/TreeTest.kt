package com.github.kotlinz.kotlinz.data.tree

import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.data.monoid.SumMonoid
import com.github.kotlinz.kotlinz.law.ApplicativeLaw
import com.github.kotlinz.kotlinz.law.ComonadLaw
import com.github.kotlinz.kotlinz.law.FoldableLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import com.github.kotlinz.kotlinz.law.MonadLaw
import com.github.kotlinz.kotlinz.law.SemialignLaw
import com.github.kotlinz.kotlinz.law.TraversableLaw
import com.github.kotlinz.kotlinz.law.ZipLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class TreeTest : DescribeSpec({
    describe("Tree") {
        val t = tree(1, tree(2, Tree.leaf(4)), Tree.leaf(3))
        val u = tree(10, Tree.leaf(20), Tree.leaf(30), Tree.leaf(40))
        val w = tree(100, tree(200, Tree.leaf(300)))
        val f = { x: Int -> tree(x, Tree.leaf(x * 10)) }
        val g = { x: Int -> if (x > 5) Tree.leaf(-x) else tree(x, Tree.leaf(x + 1), Tree.leaf(x + 2)) }

        context("as Functor and Applicative") {
            it("should satisfy functor laws") { FunctorLaw(object : TreeFunctor {}).assertSatisfyingFunctorLaw(t, { it + 1 }, { it * 2 }) }
            it("should satisfy applicative laws") {
                val fs = tree({ x: Int -> x + 1 }, Tree.leaf { x: Int -> x * 2 })
                val gs = tree({ x: Int -> x - 1 }, Tree.leaf { x: Int -> x * 3 })
                ApplicativeLaw(object : TreeApplicative {}).assertSatisfyingApplicativeLaw(t, 1, fs, gs, { x: Int -> x * 3 })
            }
            it("should map every node") { (t fmap { it * 2 }) shouldBe tree(2, tree(4, Tree.leaf(8)), Tree.leaf(6)) }
            it("should apply like Data.Tree") {
                (tree(1, Tree.leaf(2)) ap tree({ x: Int -> x + 1 }, Tree.leaf { x: Int -> x * 10 })) shouldBe
                        tree(2, Tree.leaf(3), tree(10, Tree.leaf(20)))
            }
        }

        context("as Monad") {
            val law = MonadLaw(object : TreeMonad {})
            it("should satisfy left identity") { law.leftIdentity(1, f).shouldBeTrue() }
            it("should satisfy right identity") { law.rightIdentity(t).shouldBeTrue() }
            it("should satisfy associativity") { law.associativity(t, f, g).shouldBeTrue() }
            it("should graft trees") { (tree(1, Tree.leaf(2)) bind f) shouldBe tree(1, Tree.leaf(10), tree(2, Tree.leaf(20))) }
            it("should flatten with join") { Tree.join(Tree.leaf(tree(1, Tree.leaf(2)))) shouldBe tree(1, Tree.leaf(2)) }
            it("should replay fx for every node") {
                Tree.fx { tree(1, Tree.leaf(2)).bind() * 10 } shouldBe tree(10, Tree.leaf(20))
            }
        }

        context("as Foldable and Traversable") {
            val maybe = object : MaybeApplicative {}
            val positive = { x: Int -> if (x > 0) Maybe.Just(x) else Maybe.None<Int>() }

            it("should satisfy foldable laws") { FoldableLaw(object : TreeFoldable {}).assertSatisfyingFoldableLaw(t) }
            it("should satisfy traversable laws") { TraversableLaw(object : TreeTraversable {}).assertSatisfyingTraversableLaw(t, positive) { listKOf(it, -it) } }
            it("should fold in pre-order") {
                t.foldl("") { acc, x -> acc + x } shouldBe "1243"
                t.foldr("") { x, acc -> acc + x } shouldBe "3421"
                t.foldMap(SumMonoid::class.java) { SumMonoid(it) } shouldBe SumMonoid(10)
            }
            it("should traverse") {
                t.traverse(maybe, positive) shouldBe Maybe.Just(t)
                tree(1, Tree.leaf(-1)).traverse(maybe, positive) shouldBe Maybe.None()
                Tree.sequence(maybe, tree(Maybe.Just(1), Tree.leaf(Maybe.Just(2)))) shouldBe Maybe.Just(tree(1, Tree.leaf(2)))
            }
        }

        context("as Comonad") {
            it("should satisfy laws") {
                ComonadLaw(object : TreeComonad {}).assertSatisfyingComonadLaw(t)
                ComonadLaw(object : TreeComonad {}).assertSatisfyingComonadLaw(Tree.leaf(1))
            }
            it("should extract the root") { t.extract() shouldBe 1 }
            it("should extend over subtrees") { t.extend { Tree.narrow(it).flatten().sum() } shouldBe tree(10, tree(6, Tree.leaf(4)), Tree.leaf(3)) }
        }

        context("as Semialign and Zip") {
            it("should satisfy semialign laws") { SemialignLaw(object : TreeSemialign {}).assertSatisfyingSemialignLaw(t, u, w, { it + 1 }, { it * 2 }) }
            it("should satisfy zip laws") { ZipLaw(object : TreeZip {}).assertSatisfyingZipLaw(t, u, w) }
            it("should align and zip by position") {
                (tree(1, Tree.leaf(2)) align tree("a", Tree.leaf("b"), Tree.leaf("c"))) shouldBe
                        tree(Ior.both(1, "a"), Tree.leaf(Ior.both(2, "b")), Tree.leaf(Ior.right("c")))
                (t zip u) shouldBe tree(Pair(1, 10), Tree.leaf(Pair(2, 20)), Tree.leaf(Pair(3, 30)))
            }
        }

        context("helpers") {
            it("should flatten, list levels and count nodes") {
                t.flatten() shouldBe listOf(1, 2, 4, 3)
                t.levels() shouldBe listOf(listOf(1), listOf(2, 3), listOf(4))
                t.size() shouldBe 4
            }
            it("should unfold a tree") {
                Tree.unfoldTree(1) { n -> Pair(n, if (n < 3) listOf(n * 2, n * 2 + 1) else listOf()) } shouldBe
                        tree(1, tree(2, Tree.leaf(4), Tree.leaf(5)), Tree.leaf(3))
            }
        }
    }
})
