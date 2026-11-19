package com.github.kotlinz.kotlinz.optics

import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKTraversable
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.data.nonemptylist.nonEmptyListOf
import com.github.kotlinz.kotlinz.law.TraversalLaw
import com.github.kotlinz.kotlinz.law.pairWander
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private data class Team(val name: String, val members: List<String>)

class TraversalTest : DescribeSpec({
  val maybe = object : MaybeApplicative {}
  val both = Traversal(pairWander<Int, Int>())
  val members = Lens({ t: Team -> t.members }, { t: Team, m: List<String> -> t.copy(members = m) }) andThen Traversal.list()
  val team = Team("core", listOf("ann", "bob"))

  describe("Traversal") {
    it("should satisfy laws") {
      val inc = { n: Int -> n + 1 }
      val double = { n: Int -> n * 2 }
      TraversalLaw(Traversal.list<Int, Int>()).assertSatisfyingTraversalLaw(listOf(listOf(), listOf(1, 2, 3)), inc, double)
      TraversalLaw(Traversal.listK<Int, Int>()).assertSatisfyingTraversalLaw(listOf(listKOf(4, 5)), inc, double)
      TraversalLaw(Traversal.nonEmptyList<Int, Int>()).assertSatisfyingTraversalLaw(listOf(nonEmptyListOf(1, 2)), inc, double)
      TraversalLaw(Traversal.maybe<Int, Int>()).assertSatisfyingTraversalLaw(listOf(Maybe.Just(1), Maybe.None()), inc, double)
      TraversalLaw(both).assertSatisfyingTraversalLaw(listOf(1 to 2), inc, double)
      TraversalLaw(Traversal.list<Pair<Int, Int>, Pair<Int, Int>>() andThen both).assertSatisfyingTraversalLaw(listOf(listOf(1 to 2, 3 to 4)), inc, double)
    }

    it("should modify, set and collect every focus") {
      Traversal.list<Int, Int>().modify { it * 10 }(listOf(1, 2)) shouldBe listOf(10, 20)
      both.set(0)(1 to 2) shouldBe Pair(0, 0)
      both.getAll(1 to 2) shouldBe listOf(1, 2)
      Traversal.maybe<Int, Int>().getAll(Maybe.None()) shouldBe listOf()
    }

    it("should traverse with an applicative") {
      val half = { n: Int -> if (n % 2 == 0) Maybe.Just(n / 2) else Maybe.None() }
      Traversal.list<Int, Int>().traverseOf(maybe, half, listOf(2, 4)) shouldBe Maybe.Just(listOf(1, 2))
      Traversal.list<Int, Int>().traverseOf(maybe, half, listOf(2, 3)) shouldBe Maybe.None()
    }

    it("should compose with lenses and change types") {
      members.getAll(team) shouldBe listOf("ann", "bob")
      members.modify { it.uppercase() }(team) shouldBe Team("core", listOf("ANN", "BOB"))
      Traversal.nonEmptyList<Int, String>().modify { "#$it" }(nonEmptyListOf(1, 2)) shouldBe nonEmptyListOf("#1", "#2")
    }

    it("should build from any Traversable") {
      val each = Traversal.fromTraversable<ListK.T, Int, Int>(object : ListKTraversable {})
      each.getAll(listKOf(1, 2)) shouldBe listOf(1, 2)
    }

    it("should treat lenses, prisms and optionals as traversals") {
      Lens.first<Int, Int, String>().getAll(1 to "a") shouldBe listOf(1)
      Prism.just<Int, Int>().getAll(Maybe.None()) shouldBe listOf()
      (Prism.just<List<Int>, List<Int>>() andThen Traversal.list()).getAll(Maybe.Just(listOf(1, 2))) shouldBe listOf(1, 2)
    }
  }
})
