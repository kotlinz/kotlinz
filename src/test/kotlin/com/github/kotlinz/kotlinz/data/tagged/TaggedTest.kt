package com.github.kotlinz.kotlinz.data.tagged

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.law.ChoiceLaw
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
  }
})

private fun observe(v: K3<Tagged.T, Unit, *, *>): (Any?) -> Any? = { (v as Tagged<*, *>).value }
