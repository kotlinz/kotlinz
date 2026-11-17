package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.law.MonoidLaw
import com.github.kotlinz.kotlinz.type.group.Monoid
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

@Suppress("UNCHECKED_CAST")
class DualMonoidTest : DescribeSpec({
    describe("Dual Monoid") {
        val clazz = DualMonoid::class.java as Class<DualMonoid<StringMonoid>>
        val law = MonoidLaw(clazz)
        val a = "a".toStringMonoid().toDualMonoid()
        val b = "b".toStringMonoid().toDualMonoid()
        val c = "c".toStringMonoid().toDualMonoid()
        val empty = DualMonoid<StringMonoid>(Maybe.None())

        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(a, b, c) }
        it("should satisfy laws with empty values") { law.assertSatisfyingMonoidLaw(empty, a, empty) }
        it("should flip the operand order") { Monoid.op(clazz, a, b) shouldBe "ba".toStringMonoid().toDualMonoid() }
        it("should treat the wrapped identity as identity") { Monoid.op(clazz, a, "".toStringMonoid().toDualMonoid()) shouldBe a }
    }
})
