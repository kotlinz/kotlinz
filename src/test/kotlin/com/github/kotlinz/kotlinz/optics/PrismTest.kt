package com.github.kotlinz.kotlinz.optics

import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.law.PrismLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private sealed class Shape {
  data class Circle(val radius: Int) : Shape()
  data class Square(val side: Int) : Shape()
}

class PrismTest : DescribeSpec({
  val circle = Prism<Shape, Int>({ s -> if (s is Shape.Circle) Maybe.Just(s.radius) else Maybe.None() }, { r: Int -> Shape.Circle(r) })
  val positive = Prism({ n: Int -> if (n > 0) Maybe.Just(n) else Maybe.None() }, { n: Int -> n })
  val shapes = listOf(Shape.Circle(1), Shape.Square(2))

  describe("Prism") {
    it("should satisfy laws") {
      PrismLaw(circle).assertSatisfyingPrismLaw(shapes, listOf(0, 3))
      PrismLaw(Prism.just<Int, Int>()).assertSatisfyingPrismLaw(listOf(Maybe.Just(1), Maybe.None()), listOf(1, 2))
      PrismLaw(Prism.none<Int>()).assertSatisfyingPrismLaw(listOf(Maybe.Just(1), Maybe.None()), listOf(Unit))
      PrismLaw(Prism.left<Int, Int, String>()).assertSatisfyingPrismLaw(listOf(Either.Left(1), Either.Right("x")), listOf(4))
      PrismLaw(Prism.right<Int, Int, String>()).assertSatisfyingPrismLaw(listOf(Either.Left("x"), Either.Right(2)), listOf(4))
      PrismLaw(circle andThen positive).assertSatisfyingPrismLaw(shapes + Shape.Circle(-1), listOf(5))
    }

    it("should match only its case") {
      circle.getMaybe(Shape.Circle(3)) shouldBe Maybe.Just(3)
      circle.getMaybe(Shape.Square(3)) shouldBe Maybe.None()
      circle.reverseGet(2) shouldBe Shape.Circle(2)
    }

    it("should modify only matching values") {
      circle.modify { it * 10 }(Shape.Circle(2)) shouldBe Shape.Circle(20)
      circle.modify { it * 10 }(Shape.Square(2)) shouldBe Shape.Square(2)
    }

    it("should change types with polymorphic prisms") {
      Prism.just<Int, String>().modify { "#$it" }(Maybe.Just(1)) shouldBe Maybe.Just("#1")
      Prism.right<Int, String, Boolean>().modify { "#$it" }(Either.Left(true)) shouldBe Either.Left<Boolean, String>(true)
    }

    it("should compose with andThen and isos") {
      (circle andThen positive).getMaybe(Shape.Circle(-1)) shouldBe Maybe.None()
      val doubled = Iso({ n: Int -> n * 2 }, { n: Int -> n / 2 })
      (circle andThen doubled).getMaybe(Shape.Circle(4)) shouldBe Maybe.Just(8)
      (doubled andThen positive).reverseGet(6) shouldBe 3
      (Prism.just<Maybe<Int>, Maybe<Int>>() andThen Prism.just<Int, Int>()).getMaybe(Maybe.Just(Maybe.Just(1))) shouldBe Maybe.Just(1)
    }

    it("should treat an Iso as a Prism") {
      Iso({ n: Int -> n.toString() }, { s: String -> s.toInt() }).getMaybe(5) shouldBe Maybe.Just("5")
    }
  }
})
