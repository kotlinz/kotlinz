package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.law.MonoidLaw
import com.github.kotlinz.kotlinz.type.group.Monoid
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

@Suppress("UNCHECKED_CAST")
class FirstMonoidTest : DescribeSpec({
    describe("First Monoid") {
        val clazz = FirstMonoid::class.java as Class<FirstMonoid<Int>>
        val law = MonoidLaw(clazz)
        val one = Maybe.Just(1).toFirstMonoid()
        val two = Maybe.Just(2).toFirstMonoid()
        val none = Maybe.None<Int>().toFirstMonoid()

        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(one, two, Maybe.Just(3).toFirstMonoid()) }
        it("should satisfy laws with empty values") { law.assertSatisfyingMonoidLaw(none, one, none) }
        it("should keep the first value") { Monoid.op(clazz, one, two) shouldBe one }
        it("should skip empty values") { Monoid.op(clazz, none, two) shouldBe two }
    }
})
