package com.github.kotlinz.kotlinz.data.fold

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.law.ApplicativeLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import com.github.kotlinz.kotlinz.law.ProfunctorLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private val sequences = listOf(emptyList(), listOf(3), listOf(1, -2, 5, 5))

@Suppress("UNCHECKED_CAST")
private fun foldEq(a: Any?, b: Any?): Boolean =
  sequences.all { (a as Fold<Int, Any?>).run(it) == (b as Fold<Int, Any?>).run(it) }

class FoldTest : DescribeSpec({
  describe("Fold") {
    val sum = Fold.sum()
    val length = Fold.length<Int>()

    context("as Functor") {
      it("should satisfy laws") { FunctorLaw(object : FoldFunctor<Int> {}, ::foldEq).assertSatisfyingFunctorLaw(sum, { it + 1 }, { it * 2 }) }
    }

    context("as Applicative") {
      val u = length fmap { n -> { s: String -> s.length + n } }
      val v = sum fmap { n -> { i: Int -> (i * n).toString() } }

      it("should satisfy laws") {
        ApplicativeLaw(object : FoldApplicative<Int> {}, ::foldEq).assertSatisfyingApplicativeLaw(sum, 1, u, v) { it.toString() }
      }

      it("should compute several folds in one pass") {
        (sum zip length fmap { (s, n) -> s / n }).run(listOf(2, 4, 6)) shouldBe 4
      }

      it("should return the pure value") { Fold.pure<Int, String>("x").run(listOf(1, 2)) shouldBe "x" }
    }

    context("as Profunctor") {
      it("should satisfy laws") { ProfunctorLaw(object : FoldProfunctor {}) { a, b -> foldEq(a, b) }.assertSatisfyingProfunctorLaw(sum) }

      it("should adapt inputs and the result") {
        (sum lmap { s: String -> s.length }).run(listOf("ab", "c")) shouldBe 3
        (sum rmap { "n=$it" }).run(listOf(1, 2)) shouldBe "n=3"
        sum.dimap({ s: String -> s.length }, { it * 2 }).run(listOf("abc")) shouldBe 6
      }
    }

    context("common folds") {
      val inputs = listOf(3, 1, 4, 1, 5)

      it("should sum and multiply") {
        sum.run(inputs) shouldBe 14
        Fold.product().run(inputs) shouldBe 60
      }

      it("should count") { length.run(inputs) shouldBe 5 }

      it("should find the extremes") {
        Fold.maximum<Int>().run(inputs) shouldBe Maybe.Just(5)
        Fold.minimum<Int>().run(inputs) shouldBe Maybe.Just(1)
        Fold.maximum<Int>().run(emptyList()) shouldBe Maybe.None()
      }

      it("should collect into a list") { Fold.list<Int>().run(inputs) shouldBe inputs }

      it("should test predicates") {
        Fold.any { a: Int -> a > 4 }.run(inputs) shouldBe true
        Fold.all { a: Int -> a > 1 }.run(inputs) shouldBe false
      }
    }

    context("running") {
      it("should scan every prefix") { Fold.list<Int>().scan(listOf(1, 2)) shouldBe listOf(emptyList(), listOf(1), listOf(1, 2)) }
      it("should fold of a custom state") { Fold.of("", { s, a: Int -> s + a }) { it.length }.run(listOf(12, 3)) shouldBe 3 }
      it("should handle long inputs") { Fold.list<Int>().run(List(100_000) { it }).size shouldBe 100_000 }
    }
  }
})
