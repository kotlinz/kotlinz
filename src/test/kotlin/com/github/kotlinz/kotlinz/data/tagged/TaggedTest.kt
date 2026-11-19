package com.github.kotlinz.kotlinz.data.tagged

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.law.ChoiceLaw
import com.github.kotlinz.kotlinz.law.ClosedLaw
import com.github.kotlinz.kotlinz.law.CostrongLaw
import com.github.kotlinz.kotlinz.law.ProfunctorLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class TaggedTest : DescribeSpec({
  val tagged = Tagged<Int, Int>(7)

  describe("Tagged") {
    context("as Profunctor") {
      val profunctor = object : TaggedProfunctor {}

      it("should satisfy laws") { ProfunctorLaw(profunctor) { a, b -> a == b }.assertSatisfyingProfunctorLaw(tagged) }

      it("should map only the output") {
        profunctor.dimap({ s: String -> s.length }, { n: Int -> n + 1 }, tagged) shouldBe Tagged<String, Int>(8)
      }

      it("should map the value with fmap and keep it with retag") {
        (tagged fmap { it * 2 }).value shouldBe 14
        tagged.retag<String>().value shouldBe 7
      }
    }

    context("as Choice") {
      val choice = object : TaggedChoice {}

      it("should satisfy laws") { ChoiceLaw(choice, ::observe).assertSatisfyingChoiceLaw(tagged) }

      it("should inject the value into the selected branch") {
        choice.left<Int, Int, String>(tagged).value shouldBe Either.Left<Int, String>(7)
        choice.right<Int, Int, String>(tagged).value shouldBe Either.Right<String, Int>(7)
      }
    }

    context("as Closed") {
      val closed = object : TaggedClosed {}

      @Suppress("UNCHECKED_CAST")
      val asFunction = { out: Any? -> out as (Any?) -> Any? }

      it("should satisfy laws") { ClosedLaw(closed, ::observe, asFunction).assertSatisfyingClosedLaw(tagged) }
      it("should carry a constant function") { closed.closed<Int, Int, String>(tagged).value("any") shouldBe 7 }
    }

    context("as Costrong") {
      val costrong = object : TaggedCostrong {}
      val v = Tagged<Pair<Int, Int>, Pair<Int, Int>>(Pair(1, 2))
      val w = Tagged<Pair<Pair<Int, Int>, Int>, Pair<Pair<Int, Int>, Int>>(Pair(Pair(1, 2), 3))

      it("should satisfy laws") { CostrongLaw(costrong, ::observe).assertSatisfyingCostrongLaw(v, w) }

      it("should keep the wanted component") {
        costrong.unfirst(v).value shouldBe 1
        costrong.unsecond(v).value shouldBe 2
      }
    }
  }
})

private fun observe(v: K3<Tagged.T, Unit, *, *>): (Any?) -> Any? = { (v as Tagged<*, *>).value }
