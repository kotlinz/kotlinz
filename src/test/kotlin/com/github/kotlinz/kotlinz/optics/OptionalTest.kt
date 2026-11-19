package com.github.kotlinz.kotlinz.optics

import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.law.OptionalLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private data class Account(val owner: String, val email: Maybe<String>)

class OptionalTest : DescribeSpec({
  val head = Optional({ xs: List<Int> -> xs.firstOrNull()?.let { Maybe.Just(it) } ?: Maybe.None() },
    { xs: List<Int>, x: Int -> if (xs.isEmpty()) xs else listOf(x) + xs.drop(1) })
  val email = Lens({ a: Account -> a.email }, { a: Account, e: Maybe<String> -> a.copy(email = e) })
  val accountEmail = email andThen Prism.just<String, String>()
  val accounts = listOf(Account("ann", Maybe.Just("a@x")), Account("bob", Maybe.None()))

  describe("Optional") {
    it("should satisfy laws") {
      OptionalLaw(head).assertSatisfyingOptionalLaw(listOf(listOf(), listOf(1), listOf(2, 3)), listOf(7, 8))
      OptionalLaw(accountEmail).assertSatisfyingOptionalLaw(accounts, listOf("c@x", "d@x"))
      OptionalLaw(head andThen Prism({ n: Int -> if (n > 0) Maybe.Just(n) else Maybe.None() }, { n: Int -> n }))
        .assertSatisfyingOptionalLaw(listOf(listOf(-1), listOf(1, 2)), listOf(5))
    }

    it("should read, set and modify an optional focus") {
      head.getMaybe(listOf(1, 2)) shouldBe Maybe.Just(1)
      head.getMaybe(listOf()) shouldBe Maybe.None()
      head.set(9)(listOf(1, 2)) shouldBe listOf(9, 2)
      head.modify { it + 1 }(listOf()) shouldBe listOf()
    }

    it("should be the composition of a Lens and a Prism") {
      accountEmail.getMaybe(accounts[0]) shouldBe Maybe.Just("a@x")
      accountEmail.getMaybe(accounts[1]) shouldBe Maybe.None()
      accountEmail.modify { it.uppercase() }(accounts[0]) shouldBe Account("ann", Maybe.Just("A@X"))
      accountEmail.set("z@x")(accounts[1]) shouldBe accounts[1]
    }

    it("should compose a Prism with a Lens") {
      val firstOfRight = Prism.right<Pair<Int, String>, Pair<Int, String>, Unit>() andThen Lens.first<Int, Int, String>()
      firstOfRight.getMaybe(Either.Right(1 to "a")) shouldBe Maybe.Just(1)
      firstOfRight.set(5)(Either.Left(Unit)) shouldBe Either.Left<Unit, Pair<Int, String>>(Unit)
    }

    it("should treat lenses, prisms and isos as optionals") {
      Lens.first<Int, Int, String>().getMaybe(1 to "a") shouldBe Maybe.Just(1)
      Prism.just<Int, Int>().set(3)(Maybe.Just(1)) shouldBe Maybe.Just(3)
      Iso({ n: Int -> -n }, { n: Int -> -n }).set(3)(9) shouldBe -3
    }

    it("should change types with polymorphic optionals") {
      val polymorphic = Optional.matching(
        { p: Pair<Maybe<Int>, String> -> if (p.first is Maybe.Just) Either.Right((p.first as Maybe.Just).value) else Either.Left(Pair(Maybe.None<Boolean>(), p.second)) },
        { p: Pair<Maybe<Int>, String>, b: Boolean -> Pair(Maybe.Just(b), p.second) }
      )
      polymorphic.modify { it > 0 }(Pair(Maybe.Just(1), "x")) shouldBe Pair(Maybe.Just(true), "x")
    }
  }
})
