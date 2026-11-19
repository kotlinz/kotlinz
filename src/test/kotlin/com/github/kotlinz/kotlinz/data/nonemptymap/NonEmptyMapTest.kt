package com.github.kotlinz.kotlinz.data.nonemptymap

import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.monoid.SumMonoid
import com.github.kotlinz.kotlinz.data.nonemptylist.nonEmptyListOf
import com.github.kotlinz.kotlinz.law.Foldable1Law
import com.github.kotlinz.kotlinz.law.FoldableLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import com.github.kotlinz.kotlinz.law.SemialignLaw
import com.github.kotlinz.kotlinz.law.Traversable1Law
import com.github.kotlinz.kotlinz.law.TraversableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class NonEmptyMapTest : DescribeSpec({
    describe("NonEmptyMap") {
        val m = nonEmptyMapOf("a" to 1, "b" to 2, "c" to 3)
        val positive = { x: Int -> if (x > 0) Maybe.Just(x) else Maybe.None<Int>() }

        context("construction") {
            it("should keep insertion order and expose entries") {
                m.head shouldBe ("a" to 1)
                m.size shouldBe 3
                m.keys shouldBe setOf("a", "b", "c")
                m.values shouldBe nonEmptyListOf(1, 2, 3)
                m["b"] shouldBe 2
                m["z"] shouldBe null
                m.containsKey("c").shouldBeTrue()
            }
            it("should convert from a Map") {
                NonEmptyMap.fromMap(mapOf("x" to 1)) shouldBe nonEmptyMapOf("x" to 1)
                emptyMap<String, Int>().toNonEmptyMapOrNull() shouldBe null
                m.toMap() shouldBe mapOf("a" to 1, "b" to 2, "c" to 3)
            }
            it("should add entries") { (nonEmptyMapOf("a" to 1) + ("a" to 5)) shouldBe nonEmptyMapOf("a" to 5) }
        }

        context("as Functor, Foldable and Foldable1") {
            it("should satisfy functor laws") { FunctorLaw(object : NonEmptyMapFunctor<String> {}).assertSatisfyingFunctorLaw(m, { it + 1 }, { it * 2 }) }
            it("should satisfy foldable laws") { FoldableLaw(object : NonEmptyMapFoldable<String> {}).assertSatisfyingFoldableLaw(m) }
            it("should satisfy foldable1 laws") { Foldable1Law(object : NonEmptyMapFoldable1<String> {}).assertSatisfyingFoldable1Law(m) { x, y -> x - y } }
            it("should map and fold the values") {
                (m fmap { it * 10 }) shouldBe nonEmptyMapOf("a" to 10, "b" to 20, "c" to 30)
                m.foldl("") { acc, x -> acc + x } shouldBe "123"
                m.foldr("") { x, acc -> acc + x } shouldBe "321"
                m.foldMap(SumMonoid::class.java) { SumMonoid(it) } shouldBe SumMonoid(6)
                m.reduceLeft { x, y -> x - y } shouldBe -4
                m.reduceMap(StringMonoid::class.java) { StringMonoid("$it") } shouldBe StringMonoid("123")
            }
        }

        context("as Traversable and Traversable1") {
            val maybe = object : MaybeApplicative {}

            it("should satisfy traversable laws") { TraversableLaw(object : NonEmptyMapTraversable<String> {}).assertSatisfyingTraversableLaw(m, positive) { listKOf(it, -it) } }
            it("should satisfy traversable1 laws") { Traversable1Law(object : NonEmptyMapTraversable1<String> {}).assertSatisfyingTraversable1Law(m, positive) { listKOf(it, -it) } }
            it("should traverse the values") {
                m.traverse(maybe, positive) shouldBe Maybe.Just(m)
                (m + ("d" to -1)).traverse1(maybe, positive) shouldBe Maybe.None()
                NonEmptyMap.sequence1(maybe, nonEmptyMapOf("a" to Maybe.Just(1))) shouldBe Maybe.Just(nonEmptyMapOf("a" to 1))
            }
        }

        context("as Semialign") {
            val n = nonEmptyMapOf("b" to 20, "d" to 40)
            val o = nonEmptyMapOf("a" to 100, "e" to 500)

            it("should satisfy laws") { SemialignLaw(object : NonEmptyMapSemialign<String> {}).assertSatisfyingSemialignLaw(m, n, o, { it + 1 }, { it * 2 }) }
            it("should align by key") {
                (nonEmptyMapOf("a" to 1, "b" to 2) align nonEmptyMapOf("b" to "x", "c" to "y")) shouldBe
                        nonEmptyMapOf("a" to Ior.left(1), "b" to Ior.both(2, "x"), "c" to Ior.right("y"))
            }
        }

        context("as Semigroup") {
            val a = nonEmptyMapOf("a" to StringMonoid("1"), "b" to StringMonoid("2"))
            val b = nonEmptyMapOf("b" to StringMonoid("3"))
            val c = nonEmptyMapOf("b" to StringMonoid("4"), "c" to StringMonoid("5"))
            val semigroup = NonEmptyMap.semigroup<String, StringMonoid>(StringMonoid::class.java)

            it("should be associative") { semigroup.combine(semigroup.combine(a, b), c) shouldBe semigroup.combine(a, semigroup.combine(b, c)) }
            it("should combine shared values") {
                semigroup.combine(a, b) shouldBe nonEmptyMapOf("a" to StringMonoid("1"), "b" to StringMonoid("23"))
                m.union(nonEmptyMapOf("c" to 10, "d" to 4)) { x, y -> x + y } shouldBe nonEmptyMapOf("a" to 1, "b" to 2, "c" to 13, "d" to 4)
            }
        }

        context("equality") {
            it("should implement equals, hashCode and toString") {
                nonEmptyMapOf("a" to 1) shouldBe nonEmptyMapOf("a" to 1)
                nonEmptyMapOf("a" to 1) shouldNotBe nonEmptyMapOf("a" to 2)
                nonEmptyMapOf("a" to 1).hashCode() shouldBe mapOf("a" to 1).hashCode()
                m.toString() shouldBe "NonEmptyMap(a=1, b=2, c=3)"
            }
        }
    }
})
