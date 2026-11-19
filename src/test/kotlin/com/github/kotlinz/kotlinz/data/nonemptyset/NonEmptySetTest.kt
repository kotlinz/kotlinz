package com.github.kotlinz.kotlinz.data.nonemptyset

import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.monoid.SumMonoid
import com.github.kotlinz.kotlinz.data.nonemptylist.nonEmptyListOf
import com.github.kotlinz.kotlinz.law.Foldable1Law
import com.github.kotlinz.kotlinz.law.FoldableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class NonEmptySetTest : DescribeSpec({
    describe("NonEmptySet") {
        val s = nonEmptySetOf(1, 2, 2, 3)

        context("construction") {
            it("should drop duplicates and keep insertion order") {
                s.toSet().toList() shouldBe listOf(1, 2, 3)
                s.head shouldBe 1
                s.size shouldBe 3
            }
            it("should convert from a Set") {
                NonEmptySet.fromSet(setOf(3, 1)) shouldBe nonEmptySetOf(3, 1)
                NonEmptySet.fromSet(emptySet<Int>()) shouldBe null
                setOf(1).toNonEmptySetOrNull() shouldBe nonEmptySetOf(1)
                emptySet<Int>().toNonEmptySetOrNull() shouldBe null
            }
            it("should convert from and to NonEmptyList") {
                nonEmptyListOf(2, 1, 2).toNonEmptySet() shouldBe nonEmptySetOf(2, 1)
                s.toNonEmptyList() shouldBe nonEmptyListOf(1, 2, 3)
            }
        }

        context("as Semigroup") {
            val a = nonEmptySetOf(1, 2)
            val b = nonEmptySetOf(2, 3)
            val c = nonEmptySetOf(4)
            val semigroup = NonEmptySet.semigroup<Int>()

            it("should be associative") { semigroup.combine(semigroup.combine(a, b), c) shouldBe semigroup.combine(a, semigroup.combine(b, c)) }
            it("should union sets") {
                (a + b) shouldBe nonEmptySetOf(1, 2, 3)
                (a + 5) shouldBe nonEmptySetOf(1, 2, 5)
                a.plusAll(listOf(3, 1)) shouldBe nonEmptySetOf(1, 2, 3)
            }
        }

        context("as Foldable and Foldable1") {
            it("should satisfy foldable laws") { FoldableLaw(object : NonEmptySetFoldable {}).assertSatisfyingFoldableLaw(s) }
            it("should satisfy foldable1 laws") { Foldable1Law(object : NonEmptySetFoldable1 {}).assertSatisfyingFoldable1Law(s) { x, y -> x - y } }
            it("should fold and reduce") {
                s.foldl("") { acc, x -> acc + x } shouldBe "123"
                s.foldr("") { x, acc -> acc + x } shouldBe "321"
                s.foldMap(SumMonoid::class.java) { SumMonoid(it) } shouldBe SumMonoid(6)
                s.reduceLeft { x, y -> x - y } shouldBe -4
                s.reduceRight { x, y -> x - y } shouldBe 2
                s.reduceMap(StringMonoid::class.java) { StringMonoid("$it") } shouldBe StringMonoid("123")
            }
        }

        context("helpers") {
            it("should map and check membership") {
                s.map { it % 2 } shouldBe nonEmptySetOf(1, 0)
                (2 in s).shouldBeTrue()
                (5 in s).shouldBeFalse()
            }
            it("should implement equals, hashCode and toString") {
                nonEmptySetOf(1, 2) shouldBe nonEmptySetOf(1, 2)
                nonEmptySetOf(1, 2) shouldNotBe nonEmptySetOf(1)
                nonEmptySetOf(1, 2).hashCode() shouldBe setOf(1, 2).hashCode()
                s.toString() shouldBe "NonEmptySet(1, 2, 3)"
            }
        }
    }
})
