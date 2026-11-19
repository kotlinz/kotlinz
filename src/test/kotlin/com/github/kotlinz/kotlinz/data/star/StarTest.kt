package com.github.kotlinz.kotlinz.data.star

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.law.ChoiceLaw
import com.github.kotlinz.kotlinz.law.ProfunctorLaw
import com.github.kotlinz.kotlinz.law.StrongLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class StarTest : DescribeSpec({
  val maybe = object : MaybeApplicative {}
  val half = Star { n: Int -> if (n % 2 == 0) Maybe.Just(n / 2) else Maybe.None() }

  describe("Star") {
    context("as Profunctor") {
      val profunctor = object : StarProfunctor<Maybe.T> { override val functor = maybe }

      it("should satisfy laws") { ProfunctorLaw(profunctor, ::sameStar).assertSatisfyingProfunctorLaw(half) }

      it("should map the input and the effectful output with dimap") {
        Star.narrow(profunctor.dimap({ s: String -> s.length }, { n: Int -> n + 1 }, half))("abcd") shouldBe Maybe.Just(3)
        Star.narrow(profunctor.dimap({ s: String -> s.length }, { n: Int -> n + 1 }, half))("abc") shouldBe Maybe.None()
      }

      it("should pre-compose with lmap") { (half lmap { s: String -> s.length })("ab") shouldBe Maybe.Just(1) }
    }

    context("as Strong") {
      val strong = object : StarStrong<Maybe.T> { override val functor = maybe }

      it("should satisfy laws") { StrongLaw(strong, ::observe).assertSatisfyingStrongLaw(half) }

      it("should restore the other component after the effect") {
        Star.narrow(strong.first<Int, Int, String>(half))(Pair(4, "x")) shouldBe Maybe.Just(Pair(2, "x"))
      }
    }

    context("as Choice") {
      val choice = object : StarChoice<Maybe.T> { override val applicative = maybe }

      it("should satisfy laws") { ChoiceLaw(choice, ::observe).assertSatisfyingChoiceLaw(half) }

      it("should lift the skipped branch with pure") {
        Star.narrow(choice.left<Int, Int, String>(half))(Either.Right("x")) shouldBe Maybe.Just(Either.Right<Int, String>("x"))
        Star.narrow(choice.left<Int, Int, String>(half))(Either.Left(3)) shouldBe Maybe.None()
      }
    }
  }
})

@Suppress("UNCHECKED_CAST")
private fun observe(v: K3<Star.T, Maybe.T, *, *>): (Any?) -> Any? = (v as Star<Maybe.T, Any?, Any?>).run

private fun sameStar(a: K3<Star.T, Maybe.T, Int, Int>, b: K3<Star.T, Maybe.T, Int, Int>): Boolean =
  (-10..10).all { Star.narrow(a)(it) == Star.narrow(b)(it) }
