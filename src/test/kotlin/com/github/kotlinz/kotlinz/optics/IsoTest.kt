package com.github.kotlinz.kotlinz.optics

import com.github.kotlinz.kotlinz.law.IsoLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class IsoTest : DescribeSpec({
  val chars = Iso({ s: String -> s.toList() }, { cs: List<Char> -> cs.joinToString("") })
  val negate = Iso({ n: Int -> -n }, { n: Int -> -n })
  val strings = listOf("", "a", "abc")

  describe("Iso") {
    it("should satisfy laws") {
      IsoLaw(chars).assertSatisfyingIsoLaw(strings, listOf(listOf(), listOf('x', 'y')))
      IsoLaw(chars.reverse()).assertSatisfyingIsoLaw(listOf(listOf('z')), strings)
      IsoLaw(chars andThen Iso({ cs: List<Char> -> cs.size to cs }, { p: Pair<Int, List<Char>> -> p.second }))
        .assertSatisfyingIsoLaw(strings, listOf(1 to listOf('q')))
    }

    it("should view and rebuild") {
      chars.get("ab") shouldBe listOf('a', 'b')
      chars.reverseGet(listOf('x')) shouldBe "x"
    }

    it("should flip with reverse") {
      chars.reverse().get(listOf('h', 'i')) shouldBe "hi"
      chars.reverse().reverseGet("ok") shouldBe listOf('o', 'k')
    }

    it("should modify through the view") { chars.modify { it.reversed() }("abc") shouldBe "cba" }

    it("should support polymorphic isos") {
      val boxed = Iso({ p: Pair<Int, Int> -> p.first }, { s: String -> Pair(s.length, 0) })
      boxed.modify { n: Int -> "#".repeat(n) }(Pair(3, 9)) shouldBe Pair(3, 0)
    }

    it("should compose with andThen") {
      val composed = negate andThen Iso({ n: Int -> n.toString() }, { s: String -> s.toInt() })
      composed.get(5) shouldBe "-5"
      composed.reverseGet("7") shouldBe -7
    }

    it("should provide the identity") { Iso.id<Int>().get(3) shouldBe 3 }
  }
})
