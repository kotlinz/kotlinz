package com.github.kotlinz.kotlinz.data.moore

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.law.ApplicativeLaw
import com.github.kotlinz.kotlinz.law.ComonadLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import com.github.kotlinz.kotlinz.law.ProfunctorLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private val inputs = listOf(-2, 1, 5)

@Suppress("UNCHECKED_CAST")
private fun mooreEq(a: Any?, b: Any?, depth: Int = 3): Boolean {
  if (a !is Moore<*, *> || b !is Moore<*, *>) return a == b
  val x = a as Moore<Int, Any?>
  val y = b as Moore<Int, Any?>
  return mooreEq(x.output, y.output, depth) && (depth == 0 || inputs.all { mooreEq(x.step(it), y.step(it), depth - 1) })
}

private fun anyEq(a: K1<*, *>, b: K1<*, *>): Boolean = mooreEq(a, b)

class MooreTest : DescribeSpec({
  describe("Moore") {
    val sum = Moore.unfold(0, { s -> s }) { s, i: Int -> s + i }
    val count = Moore.unfold(0, { s -> s }) { s, _: Int -> s + 1 }

    context("as Functor") {
      it("should satisfy laws") { FunctorLaw(object : MooreFunctor<Int> {}, ::anyEq).assertSatisfyingFunctorLaw(sum, { it + 1 }, { it * 2 }) }
    }

    context("as Applicative") {
      val u = count fmap { n -> { s: String -> s.length + n } }
      val v = sum fmap { n -> { i: Int -> (i * n).toString() } }

      it("should satisfy laws") {
        ApplicativeLaw(object : MooreApplicative<Int> {}, ::anyEq).assertSatisfyingApplicativeLaw(sum, 1, u, v) { it.toString() }
      }

      it("should run machines in lockstep") {
        val mean = (sum fmap { s -> { c: Int -> if (c == 0) 0 else s / c } }).let { count ap it }
        mean.run(listOf(2, 4, 6)) shouldBe 4
      }

      it("should keep a pure value forever") { Moore.pure<Int, String>("x").run(listOf(1, 2, 3)) shouldBe "x" }
    }

    context("as Comonad") {
      val law = ComonadLaw(object : MooreComonad<Int> {}, ::mooreEq)

      it("should satisfy laws") { law.assertSatisfyingComonadLaw(sum) }
      it("should extract the current output") { sum.feedAll(listOf(1, 2)).extract() shouldBe 3 }
      it("should expose reachable machines with duplicate") { sum.duplicate().run(listOf(1, 2)).extract() shouldBe 3 }
      it("should look ahead with extend") { (sum extend { m -> m.step(10).output }).run(listOf(1, 2)) shouldBe 13 }
    }

    context("as Profunctor") {
      it("should satisfy laws") { ProfunctorLaw(object : MooreProfunctor {}) { a, b -> mooreEq(a, b) }.assertSatisfyingProfunctorLaw(sum) }

      it("should adapt inputs and outputs") {
        (sum lmap { s: String -> s.length }).run(listOf("ab", "c")) shouldBe 3
        sum.dimap({ s: String -> s.length }, { "n=$it" }).run(listOf("abc")) shouldBe "n=3"
        (sum rmap { it * 10 }).run(listOf(1, 2)) shouldBe 30
      }
    }

    context("running") {
      it("should return the final output") { sum.run(listOf(1, 2, 3)) shouldBe 6 }
      it("should return the initial output for no inputs") { sum.run(emptyList()) shouldBe 0 }
      it("should scan every output") { sum.scan(listOf(1, 2, 3)) shouldBe listOf(0, 1, 3, 6) }
      it("should handle long input streams") { count.run(List(100_000) { it }) shouldBe 100_000 }
    }
  }
})
