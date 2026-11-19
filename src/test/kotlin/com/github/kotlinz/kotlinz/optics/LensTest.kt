package com.github.kotlinz.kotlinz.optics

import com.github.kotlinz.kotlinz.law.LensLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private data class Address(val city: String, val zip: Int)
private data class Person(val name: String, val address: Address)

class LensTest : DescribeSpec({
  val address = Lens({ p: Person -> p.address }, { p: Person, a: Address -> p.copy(address = a) })
  val city = Lens({ a: Address -> a.city }, { a: Address, c: String -> a.copy(city = c) })
  val upper = Iso({ s: String -> s.uppercase() }, { s: String -> s.lowercase() })
  val ann = Person("Ann", Address("tokyo", 100))
  val people = listOf(ann, Person("Bob", Address("osaka", 200)))

  describe("Lens") {
    it("should satisfy laws") {
      LensLaw(address).assertSatisfyingLensLaw(people, listOf(Address("kyoto", 1), Address("nara", 2)))
      LensLaw(address andThen city).assertSatisfyingLensLaw(people, listOf("kobe", "sendai"))
      LensLaw(Lens.first<Int, Int, String>()).assertSatisfyingLensLaw(listOf(1 to "a"), listOf(5, 6))
    }

    it("should get, set and modify the focus") {
      city.get(ann.address) shouldBe "tokyo"
      city.set("kyoto")(ann.address) shouldBe Address("kyoto", 100)
      city.modify { it.reversed() }(ann.address) shouldBe Address("oykot", 100)
    }

    it("should compose deeply with andThen") {
      (address andThen city).modify { it.uppercase() }(ann) shouldBe Person("Ann", Address("TOKYO", 100))
    }

    it("should compose with an Iso on either side") {
      (address andThen city andThen upper).get(ann) shouldBe "TOKYO"
      (address andThen city andThen upper).set("KYOTO")(ann) shouldBe Person("Ann", Address("kyoto", 100))
      val head = Lens({ s: String -> s.first() }, { s: String, c: Char -> c + s.drop(1) })
      (upper andThen head).get("abc") shouldBe 'A'
      (upper andThen head).set('Z')("ABC") shouldBe "zbc"
      (upper andThen upper).get("x") shouldBe "X"
    }

    it("should treat an Iso as a Lens") { upper.set("HI")("ignored") shouldBe "hi" }

    it("should change types with polymorphic lenses") {
      Lens.second<Int, String, Boolean>().modify { n: Int -> "#$n" }(true to 3) shouldBe Pair(true, "#3")
    }
  }
})
