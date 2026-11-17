package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Monoid
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

@Suppress("UNCHECKED_CAST")
class EndoMonoidTest : DescribeSpec({
    describe("Endo Monoid") {
        val clazz = EndoMonoid::class.java as Class<EndoMonoid<Int>>
        val inputs = listOf(-3, 0, 1, 42)
        fun EndoMonoid<Int>.sameAs(other: EndoMonoid<Int>) = inputs.all { this(it) == other(it) }
        fun op(a: EndoMonoid<Int>, b: EndoMonoid<Int>) = Monoid.op(clazz, a, b)

        val inc = { a: Int -> a + 1 }.toEndoMonoid()
        val double = { a: Int -> a * 2 }.toEndoMonoid()
        val square = { a: Int -> a * a }.toEndoMonoid()
        val zero = Monoid.mzero(clazz)

        it("should satisfy left identity") { op(zero, inc).sameAs(inc).shouldBeTrue() }
        it("should satisfy right identity") { op(inc, zero).sameAs(inc).shouldBeTrue() }
        it("should satisfy associativity") { op(op(inc, double), square).sameAs(op(inc, op(double, square))).shouldBeTrue() }
        it("should compose right to left") { op(inc, double)(3) shouldBe 7 }
    }
})
