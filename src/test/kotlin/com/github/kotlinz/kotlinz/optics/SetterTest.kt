package com.github.kotlinz.kotlinz.optics

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeFunctor
import com.github.kotlinz.kotlinz.law.SetterLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class SetterTest : DescribeSpec({
  val values = Setter { f: (Int) -> Int -> { m: Map<String, Int> -> m.mapValues { f(it.value) } } }
  val inc = { n: Int -> n + 1 }
  val double = { n: Int -> n * 2 }

  describe("Setter") {
    it("should satisfy laws") {
      SetterLaw(values).assertSatisfyingSetterLaw(listOf(mapOf(), mapOf("a" to 1, "b" to 2)), listOf(0, 9), inc, double)
      SetterLaw(Traversal.list<Int, Int>()).assertSatisfyingSetterLaw(listOf(listOf(1, 2)), listOf(5), inc, double)
      SetterLaw(Lens.first<Int, Int, String>()).assertSatisfyingSetterLaw(listOf(1 to "a"), listOf(5), inc, double)
      SetterLaw(Setter.fromFunctor<Maybe.T, Int, Int>(object : MaybeFunctor {}))
        .assertSatisfyingSetterLaw(listOf<K1<Maybe.T, Int>>(Maybe.Just(1), Maybe.None()), listOf(3), inc, double)
    }

    it("should map and replace every focus") {
      values.over(double)(mapOf("a" to 1, "b" to 2)) shouldBe mapOf("a" to 2, "b" to 4)
      values.set(0)(mapOf("a" to 1)) shouldBe mapOf("a" to 0)
    }

    it("should change types with polymorphic setters") {
      Setter.fromFunctor<Maybe.T, Int, String>(object : MaybeFunctor {}).over { "#$it" }(Maybe.Just(1)) shouldBe Maybe.Just("#1")
    }

    it("should compose with setters and other optics") {
      (Traversal.list<Map<String, Int>, Map<String, Int>>() andThen values).over(inc)(listOf(mapOf("a" to 1))) shouldBe listOf(mapOf("a" to 2))
      (values andThen Iso({ n: Int -> -n }, { n: Int -> -n })).over(inc)(mapOf("a" to 1)) shouldBe mapOf("a" to 0)
      (Lens.second<Map<String, Int>, Map<String, Int>, String>() andThen values).set(7)("x" to mapOf("a" to 1)) shouldBe Pair("x", mapOf("a" to 7))
    }

    it("should treat traversals, optionals, lenses, prisms and isos as setters") {
      Traversal.maybe<Int, Int>().over(inc)(Maybe.Just(1)) shouldBe Maybe.Just(2)
      Prism.just<Int, Int>().over(inc)(Maybe.None()) shouldBe Maybe.None()
      Iso({ s: String -> s.length }, { n: Int -> "x".repeat(n) }).over(inc)("ab") shouldBe "xxx"
    }
  }
})
