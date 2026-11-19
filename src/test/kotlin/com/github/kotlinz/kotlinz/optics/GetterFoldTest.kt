package com.github.kotlinz.kotlinz.optics

import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKFoldable
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.monoidof.MonoidOf
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class GetterFoldTest : DescribeSpec({
  val sum = MonoidOf(0) { a: Int, b: Int -> a + b }
  val length = Getter { s: String -> s.length }
  val digits = Fold { s: String -> s.filter(Char::isDigit).toList() }

  describe("Getter") {
    it("should view the focus") { length.view("abc") shouldBe 3 }

    it("should compose with getters, lenses and isos") {
      (length andThen Getter { n: Int -> n * 2 }).view("ab") shouldBe 4
      (Lens.first<String, String, Int>() andThen length).view("abc" to 1) shouldBe 3
      (Iso({ n: Int -> n.toString() }, { s: String -> s.toInt() }) andThen length).view(1234) shouldBe 4
    }

    it("should treat lenses and isos as getters") {
      Lens.second<Int, Int, String>().view("a" to 7) shouldBe 7
      Iso({ n: Int -> -n }, { n: Int -> -n }).view(3) shouldBe -3
    }

    it("should behave as a single-focus fold") {
      length.toListOf("ab") shouldBe listOf(2)
      length.preview("") shouldBe Maybe.Just(0)
    }
  }

  describe("Fold") {
    it("should fold, list and preview the foci") {
      digits.toListOf("a1b2") shouldBe listOf('1', '2')
      digits.preview("abc") shouldBe Maybe.None()
      digits.preview("x9y8") shouldBe Maybe.Just('9')
      digits.foldMapOf(sum, { it.digitToInt() }, "a1b2c3") shouldBe 6
    }

    it("should build from any Foldable") {
      Fold.fromFoldable<ListK.T, Int>(object : ListKFoldable {}).foldMapOf(sum, { it }, listKOf(1, 2, 3)) shouldBe 6
    }

    it("should compose with traversals, prisms and getters") {
      (Traversal.list<String, String>() andThen digits).toListOf(listOf("a1", "2b")) shouldBe listOf('1', '2')
      (digits andThen Getter { c: Char -> c.digitToInt() }).foldMapOf(sum, { it * 10 }, "1a2") shouldBe 30
      (Prism.just<String, String>() andThen length).preview(Maybe.Just("abc")) shouldBe Maybe.Just(3)
      (Prism.just<String, String>() andThen length).preview(Maybe.None()) shouldBe Maybe.None()
    }

    it("should treat traversals and optionals as folds") {
      Traversal.list<Int, Int>().foldMapOf(sum, { it }, listOf(1, 2, 3)) shouldBe 6
      Prism.just<Int, Int>().preview(Maybe.Just(1)) shouldBe Maybe.Just(1)
      Traversal.maybe<Int, Int>().toListOf(Maybe.None()) shouldBe listOf()
    }
  }
})
