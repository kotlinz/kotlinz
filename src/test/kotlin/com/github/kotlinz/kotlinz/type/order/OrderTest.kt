package com.github.kotlinz.kotlinz.type.order

import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.either.order
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.order
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.data.ior.order
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.order
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.order
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyList
import com.github.kotlinz.kotlinz.data.nonemptylist.order
import com.github.kotlinz.kotlinz.data.tuple2.Tuple2
import com.github.kotlinz.kotlinz.data.tuple2.order
import com.github.kotlinz.kotlinz.data.validated.Validated
import com.github.kotlinz.kotlinz.data.validated.order
import com.github.kotlinz.kotlinz.law.OrderLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class OrderTest : DescribeSpec({
    val ignoreCase = Order.by<String> { a, b -> a.compareTo(b, ignoreCase = true) }

    describe("primitive instances") {
        it("should satisfy laws") {
            OrderLaw(Int.order).assertSatisfyingOrderLaw(1, 3, 2)
            OrderLaw(Long.order).assertSatisfyingOrderLaw(1L, 1L, -1L)
            OrderLaw(Short.order).assertSatisfyingOrderLaw(1, 2, 3)
            OrderLaw(Byte.order).assertSatisfyingOrderLaw(3, 2, 1)
            OrderLaw(Double.order).assertSatisfyingOrderLaw(Double.NaN, 0.0, -0.0)
            OrderLaw(Float.order).assertSatisfyingOrderLaw(Float.NaN, 1.5f, -1.5f)
            OrderLaw(Char.order).assertSatisfyingOrderLaw('a', 'c', 'b')
            OrderLaw(Boolean.order).assertSatisfyingOrderLaw(true, false, true)
            OrderLaw(String.order).assertSatisfyingOrderLaw("b", "a", "ab")
        }
        it("should provide comparison helpers") {
            Int.order.lt(1, 2).shouldBeTrue()
            Int.order.lteqv(2, 2).shouldBeTrue()
            Int.order.gt(1, 2).shouldBeFalse()
            Int.order.gteqv(2, 1).shouldBeTrue()
            Int.order.min(1, 2) shouldBe 1
            Int.order.max(1, 2) shouldBe 2
            Int.order.eqv(1, 1).shouldBeTrue()
        }
    }

    describe("Order.by and comparators") {
        it("should satisfy laws") { OrderLaw(ignoreCase).assertSatisfyingOrderLaw("a", "A", "b") }
        it("should prefer the left operand on ties") {
            ignoreCase.min("a", "A") shouldBe "a"
            ignoreCase.max("a", "A") shouldBe "a"
        }
        it("should convert to and from Comparator") {
            listOf(3, 1, 2).sortedWith(Int.order.toComparator()) shouldBe listOf(1, 2, 3)
            Order.fromComparator(reverseOrder<Int>()).lt(2, 1).shouldBeTrue()
        }
    }

    describe("List instance") {
        val order = Order.list(Int.order)
        it("should satisfy laws") { OrderLaw(order).assertSatisfyingOrderLaw(listOf(1, 2), listOf(1), listOf(2)) }
        it("should order lexicographically") {
            order.lt(listOf(1), listOf(1, 0)).shouldBeTrue()
            order.lt(listOf(1, 9), listOf(2)).shouldBeTrue()
        }
    }

    describe("Maybe instance") {
        val order = Maybe.order(Int.order)
        it("should satisfy laws") { OrderLaw(order).assertSatisfyingOrderLaw(Maybe.Just(2), Maybe.None(), Maybe.Just(1)) }
        it("should put None first") { order.lt(Maybe.None(), Maybe.Just(Int.MIN_VALUE)).shouldBeTrue() }
    }

    describe("Either instance") {
        val order = Either.order(Int.order, Int.order)
        it("should satisfy laws") { OrderLaw(order).assertSatisfyingOrderLaw(Either.Right(1), Either.Left(2), Either.Left(1)) }
        it("should put Left first") { order.lt(Either.Left(9), Either.Right(0)).shouldBeTrue() }
    }

    describe("Validated instance") {
        val order = Validated.order(Int.order, Int.order)
        it("should satisfy laws") { OrderLaw(order).assertSatisfyingOrderLaw(Validated.Valid(1), Validated.Invalid(2), Validated.Valid(0)) }
        it("should put Invalid first") { order.lt(Validated.Invalid(9), Validated.Valid(0)).shouldBeTrue() }
    }

    describe("Ior instance") {
        val order = Ior.order(Int.order, Int.order)
        it("should satisfy laws") {
            OrderLaw(order).assertSatisfyingOrderLaw(Ior.Both(1, 2), Ior.Right(5), Ior.Left(9))
            OrderLaw(order).assertSatisfyingOrderLaw(Ior.Both(1, 2), Ior.Both(1, 1), Ior.Both(0, 3))
        }
    }

    describe("Tuple2 instance") {
        val order = Tuple2.order(Int.order, ignoreCase)
        it("should satisfy laws") { OrderLaw(order).assertSatisfyingOrderLaw(Tuple2(1, "b"), Tuple2(1, "A"), Tuple2(0, "z")) }
    }

    describe("ListK instance") {
        val order = ListK.order(Int.order)
        it("should satisfy laws") { OrderLaw(order).assertSatisfyingOrderLaw(ListK(listOf(1)), ListK(listOf()), ListK(listOf(0, 1))) }
    }

    describe("NonEmptyList instance") {
        val order = NonEmptyList.order(Int.order)
        it("should satisfy laws") {
            OrderLaw(order).assertSatisfyingOrderLaw(NonEmptyList(1, listOf(2)), NonEmptyList(1, listOf()), NonEmptyList(0, listOf(5)))
        }
    }

    describe("Identity instance") {
        val order = Identity.order(Int.order)
        it("should satisfy laws") { OrderLaw(order).assertSatisfyingOrderLaw(Identity(2), Identity(1), Identity(2)) }
    }
})
