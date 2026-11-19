package com.github.kotlinz.kotlinz.type

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityTraversable1
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyListTraversable1
import com.github.kotlinz.kotlinz.data.nonemptylist.nonEmptyListOf
import com.github.kotlinz.kotlinz.data.tuple2.Tuple2
import com.github.kotlinz.kotlinz.data.tuple2.Tuple2Functor
import com.github.kotlinz.kotlinz.data.tuple2.Tuple2Traversable1
import com.github.kotlinz.kotlinz.law.Foldable1Law
import com.github.kotlinz.kotlinz.law.FoldableLaw
import com.github.kotlinz.kotlinz.law.Traversable1Law
import com.github.kotlinz.kotlinz.law.TraversableLaw
import com.github.kotlinz.kotlinz.type.applicative.Apply
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

/** A writer over the String semigroup: an [Apply] with no `pure`, so only `traverse1` can use it. */
private object LogApply : Apply<K1<Tuple2.T, String>>, Tuple2Functor<String> {
    override fun <A, B> ap(f: K1<K1<Tuple2.T, String>, (A) -> B>, v: K1<K1<Tuple2.T, String>, A>): Tuple2<String, B> {
        val tf = Tuple2.narrow(f)
        val tv = Tuple2.narrow(v)
        return Tuple2(tf.first + tv.first, tf.second(tv.second))
    }
}

private val half = { n: Int -> if (n % 2 == 0) Maybe.Just(n / 2) else Maybe.None() }
private val twice = { n: Int -> ListK(listOf(n, n * 10)) }
private val minus = { a: Int, b: Int -> a - b }

class Traversable1Test : DescribeSpec({
    describe("Foldable1 and Traversable1") {
        context("NonEmptyList") {
            val instance = object : NonEmptyListTraversable1 {}
            val values = listOf(nonEmptyListOf(4), nonEmptyListOf(2, 4, 8), nonEmptyListOf(2, 3))

            it("should satisfy Foldable1 laws") { values.forEach { Foldable1Law(instance).assertSatisfyingFoldable1Law(it, minus) } }
            it("should satisfy Traversable1 laws") { values.forEach { Traversable1Law(instance).assertSatisfyingTraversable1Law(it, half, twice) } }

            it("should reduce without an initial value") {
                nonEmptyListOf(1, 2, 3).reduceLeft(minus) shouldBe -4
                instance.reduceRight(minus, nonEmptyListOf(1, 2, 3)) shouldBe 2
                nonEmptyListOf(1, 2).reduceMap(StringMonoid::class.java) { StringMonoid("$it") } shouldBe StringMonoid("12")
                instance.head(nonEmptyListOf(1, 2, 3)) shouldBe 1
                instance.last(nonEmptyListOf(1, 2, 3)) shouldBe 3
            }

            it("should traverse with an Apply that has no pure, left to right") {
                val logged = nonEmptyListOf(1, 2, 3).traverse1(LogApply) { n -> Tuple2("[$n]", n * 2) }
                logged shouldBe Tuple2("[1][2][3]", nonEmptyListOf(2, 4, 6))
                instance.sequence1(LogApply, nonEmptyListOf(Tuple2("a", 1), Tuple2("b", 2))) shouldBe Tuple2("ab", nonEmptyListOf(1, 2))
            }
        }

        context("Identity") {
            val instance = object : IdentityTraversable1 {}

            it("should satisfy Foldable1 laws") { Foldable1Law(instance).assertSatisfyingFoldable1Law(Identity(4), minus) }
            it("should satisfy Traversable1 laws") { Traversable1Law(instance).assertSatisfyingTraversable1Law(Identity(4), half, twice) }

            it("should traverse with an Apply") {
                instance.traverse1(LogApply, { n: Int -> Tuple2("x", n + 1) }, Identity(1)) shouldBe Tuple2("x", Identity(2))
            }
        }

        context("Tuple2") {
            val instance = object : Tuple2Traversable1<String> {}
            val values = listOf(Tuple2("s", 4), Tuple2("t", 3))

            it("should satisfy Foldable laws") { values.forEach { FoldableLaw(instance).assertSatisfyingFoldableLaw(it) } }
            it("should satisfy Foldable1 laws") { values.forEach { Foldable1Law(instance).assertSatisfyingFoldable1Law(it, minus) } }
            it("should satisfy Traversable laws") { values.forEach { TraversableLaw(instance).assertSatisfyingTraversableLaw(it, half, twice) } }
            it("should satisfy Traversable1 laws") { values.forEach { Traversable1Law(instance).assertSatisfyingTraversable1Law(it, half, twice) } }

            it("should keep the first component while traversing the second") {
                instance.traverse1(LogApply, { n: Int -> Tuple2("log", n * 3) }, Tuple2("env", 2)) shouldBe
                    Tuple2("log", Tuple2("env", 6))
            }
        }
    }
})
