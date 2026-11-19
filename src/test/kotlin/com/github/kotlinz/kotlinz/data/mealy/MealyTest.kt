package com.github.kotlinz.kotlinz.data.mealy

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.law.ArrowChoiceLaw
import com.github.kotlinz.kotlinz.law.ArrowLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import com.github.kotlinz.kotlinz.law.ProfunctorLaw
import com.github.kotlinz.kotlinz.law.StrongLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private val sequences = listOf(listOf(1, 2, 3), listOf(-4, 0, 7, 7), emptyList())

@Suppress("UNCHECKED_CAST")
private fun observe(v: K3<Mealy.T, Unit, *, *>): (Any?) -> Any? = { x -> (v as Mealy<Any?, Any?>).run(listOf(x, x, x)) }

@Suppress("UNCHECKED_CAST")
private fun mealyEq(a: Any?, b: Any?): Boolean =
  sequences.all { (a as Mealy<Int, Any?>).run(it) == (b as Mealy<Int, Any?>).run(it) }

class MealyTest : DescribeSpec({
  describe("Mealy") {
    val runningSum = Mealy.unfold(0) { s, i: Int -> Pair(s + i, s + i) }
    val counter = Mealy.unfold(0) { s, i: Int -> Pair(i * 10 + s, s + 1) }

    context("as Functor") {
      it("should satisfy laws") {
        FunctorLaw(object : MealyFunctor<Int> {}, ::mealyEq).assertSatisfyingFunctorLaw(runningSum, { it + 1 }, { it * 2 })
      }
    }

    context("as Category and Arrow") {
      val law = ArrowLaw(object : MealyArrow {}, ::observe)

      it("should satisfy category laws") { law.assertSatisfyingCategoryLaw(runningSum, counter, runningSum) }
      it("should satisfy arrow laws") { law.assertSatisfyingArrowLaw(runningSum, counter) }

      it("should compose machines in lockstep") {
        (runningSum andThen Mealy.arr { it * 10 }).run(listOf(1, 2)) shouldBe listOf(10, 30)
        (runningSum compose Mealy.arr { s: String -> s.length }).run(listOf("ab", "c")) shouldBe listOf(2, 3)
        Mealy.id<Int>().run(listOf(4, 5)) shouldBe listOf(4, 5)
      }

      it("should split and combine machines") {
        (runningSum split counter).run(listOf(Pair(1, 1), Pair(2, 1))) shouldBe listOf(Pair(1, 10), Pair(3, 11))
        (runningSum combine counter).run(listOf(1, 2)) shouldBe listOf(Pair(1, 10), Pair(3, 21))
        runningSum.second<String>().run(listOf(Pair("a", 1), Pair("b", 2))) shouldBe listOf(Pair("a", 1), Pair("b", 3))
      }
    }

    context("as ArrowChoice") {
      it("should satisfy laws") { ArrowChoiceLaw(object : MealyArrowChoice {}, ::observe).assertSatisfyingArrowChoiceLaw(runningSum, counter) }

      it("should only advance the machine on routed inputs") {
        val inputs = listOf(Either.Left<Int, String>(1), Either.Right<Int, String>("x"), Either.Left<Int, String>(2))
        runningSum.left<String>().run(inputs) shouldBe listOf(Either.Left(1), Either.Right("x"), Either.Left(3))
      }

      it("should route and merge with splitChoice and fanin") {
        val inputs = listOf(Either.Left<Int, Int>(1), Either.Right<Int, Int>(5), Either.Left<Int, Int>(2))
        (runningSum splitChoice counter).run(inputs) shouldBe listOf(Either.Left(1), Either.Right(50), Either.Left(3))
        (runningSum fanin counter).run(inputs) shouldBe listOf(1, 50, 3)
        runningSum.right<Int>().run(listOf(Either.Right(2), Either.Left(9))) shouldBe listOf(Either.Right(2), Either.Left(9))
      }
    }

    context("as Profunctor") {
      it("should satisfy laws") { ProfunctorLaw(object : MealyProfunctor {}) { a, b -> mealyEq(a, b) }.assertSatisfyingProfunctorLaw(runningSum) }

      it("should adapt inputs and outputs") {
        (runningSum lmap { s: String -> s.length }).run(listOf("ab", "c")) shouldBe listOf(2, 3)
        (runningSum rmap { "n=$it" }).run(listOf(1)) shouldBe listOf("n=1")
        runningSum.dimap({ s: String -> s.length }, { it * 2 }).run(listOf("abc")) shouldBe listOf(6)
      }
    }

    context("as Strong") {
      it("should satisfy laws") { StrongLaw(object : MealyStrong {}, ::observe).assertSatisfyingStrongLaw(runningSum) }
    }

    context("running") {
      it("should return outputs and the resulting machine") {
        val (outputs, next) = runningSum.feedAll(listOf(1, 2))
        outputs shouldBe listOf(1, 3)
        next.run(listOf(10)) shouldBe listOf(13)
      }

      it("should map outputs") { (runningSum fmap { it * 2 }).run(listOf(1, 2)) shouldBe listOf(2, 6) }
      it("should handle long input streams") { runningSum.run(List(100_000) { 1 }).last() shouldBe 100_000 }
    }
  }
})
