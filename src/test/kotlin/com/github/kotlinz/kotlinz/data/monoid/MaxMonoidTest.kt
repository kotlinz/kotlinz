package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.law.MonoidLaw
import com.github.kotlinz.kotlinz.type.group.Monoid
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

@Suppress("UNCHECKED_CAST")
class MaxMonoidTest : DescribeSpec({
    describe("Max Monoid") {
        val clazz = MaxMonoid::class.java as Class<MaxMonoid<Int>>
        val law = MonoidLaw(clazz)
        val one = Maybe.Just(1).toMaxMonoid()
        val two = Maybe.Just(2).toMaxMonoid()
        val none = Maybe.None<Int>().toMaxMonoid()

        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(two, one, Maybe.Just(3).toMaxMonoid()) }
        it("should satisfy laws with empty values") { law.assertSatisfyingMonoidLaw(none, one, none) }
        it("should keep the maximum value") { Monoid.op(clazz, one, two) shouldBe two }
        it("should skip empty values") { Monoid.op(clazz, one, none) shouldBe one }
    }
})
