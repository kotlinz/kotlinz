package com.github.kotlinz.kotlinz.data.costar

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKFunctor
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.law.ClosedLaw
import com.github.kotlinz.kotlinz.law.ProfunctorLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private val inputs = listOf(listKOf(), listKOf(1), listKOf(2, -3, 5))

class CostarTest : DescribeSpec({
  val profunctor = object : CostarProfunctor<ListK.T> { override val functor = object : ListKFunctor {} }
  val sum = Costar { xs: K1<ListK.T, Int> -> ListK.narrow(xs).value.sum() }

  describe("Costar") {
    context("as Profunctor") {
      it("should satisfy laws") { ProfunctorLaw(profunctor, ::sameCostar).assertSatisfyingProfunctorLaw(sum) }

      it("should map every input element and the output with dimap") {
        Costar.narrow(profunctor.dimap({ s: String -> s.length }, { n: Int -> n * 10 }, sum))(listKOf("a", "bc")) shouldBe 30
      }

      it("should post-compose with rmap") { (sum rmap { n: Int -> "n=$n" })(listKOf(1, 2)) shouldBe "n=3" }
    }

    context("as Closed") {
      val closed = object : CostarClosed<ListK.T> { override val functor = object : ListKFunctor {} }

      @Suppress("UNCHECKED_CAST")
      val observeSingleton = { v: K3<Costar.T, ListK.T, *, *> -> { a: Any? -> (v as Costar<ListK.T, Any?, Any?>)(listKOf(a)) } }

      @Suppress("UNCHECKED_CAST")
      val asFunction = { out: Any? -> out as (Any?) -> Any? }

      it("should satisfy laws") { ClosedLaw(closed, observeSingleton, asFunction).assertSatisfyingClosedLaw(sum) }

      it("should apply every function to the shared input") {
        val functions = listKOf({ s: String -> s.length }, { s: String -> s.length * 10 })
        closed.closed<Int, Int, String>(sum)(functions)("ab") shouldBe 22
      }
    }
  }
})

private fun sameCostar(a: K3<Costar.T, ListK.T, Int, Int>, b: K3<Costar.T, ListK.T, Int, Int>): Boolean =
  inputs.all { Costar.narrow(a)(it) == Costar.narrow(b)(it) }
