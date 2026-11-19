package com.github.kotlinz.kotlinz.data.forget

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.monoidof.MonoidOf
import com.github.kotlinz.kotlinz.law.ChoiceLaw
import com.github.kotlinz.kotlinz.law.ProfunctorLaw
import com.github.kotlinz.kotlinz.law.StrongLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ForgetTest : DescribeSpec({
  val double = Forget<Int, Int, Int> { it * 2 }

  describe("Forget") {
    context("as Profunctor") {
      val profunctor = object : ForgetProfunctor<Int> {}

      it("should satisfy laws") { ProfunctorLaw(profunctor, ::sameForget).assertSatisfyingProfunctorLaw(double) }

      it("should map only the input") {
        Forget.narrow(profunctor.dimap({ s: String -> s.length }, { n: Int -> n + 100 }, double))("abc") shouldBe 6
      }

      it("should change only the phantom type with retag") { double.retag<String>()(4) shouldBe 8 }
    }

    context("as Strong") {
      val strong = object : ForgetStrong<Int> {}

      it("should satisfy laws") { StrongLaw(strong, ::observe).assertSatisfyingStrongLaw(double) }

      it("should discard the other component") {
        Forget.narrow(strong.first<Int, Int, String>(double))(Pair(3, "x")) shouldBe 6
        Forget.narrow(strong.second<Int, Int, String>(double))(Pair("x", 4)) shouldBe 8
      }
    }

    context("as Choice") {
      val choice = object : ForgetChoice<Int> { override val monoid = MonoidOf(0) { a: Int, b: Int -> a + b } }

      it("should satisfy laws") { ChoiceLaw(choice, ::observe).assertSatisfyingChoiceLaw(double) }

      it("should yield the empty value for the skipped branch") {
        Forget.narrow(choice.left<Int, Int, String>(double))(Either.Left(5)) shouldBe 10
        Forget.narrow(choice.left<Int, Int, String>(double))(Either.Right("x")) shouldBe 0
      }
    }
  }
})

@Suppress("UNCHECKED_CAST")
private fun observe(v: K3<Forget.T, Int, *, *>): (Any?) -> Any? = (v as Forget<Int, Any?, Any?>).run

private fun sameForget(a: K3<Forget.T, Int, Int, Int>, b: K3<Forget.T, Int, Int, Int>): Boolean =
  (-10..10).all { Forget.narrow(a)(it) == Forget.narrow(b)(it) }
