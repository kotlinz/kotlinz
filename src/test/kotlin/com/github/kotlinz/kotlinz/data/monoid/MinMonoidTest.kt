package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.law.MonoidLaw
import com.github.kotlinz.kotlinz.type.group.Monoid
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

@Suppress("UNCHECKED_CAST")
class MinMonoidTest : DescribeSpec({
    describe("Min Monoid") {
        val clazz = MinMonoid::class.java as Class<MinMonoid<Int>>
        val law = MonoidLaw(clazz)
        val one = Maybe.Just(1).toMinMonoid()
        val two = Maybe.Just(2).toMinMonoid()
        val none = Maybe.None<Int>().toMinMonoid()

        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(two, one, Maybe.Just(3).toMinMonoid()) }
        it("should satisfy laws with empty values") { law.assertSatisfyingMonoidLaw(none, two, none) }
        it("should keep the minimum value") { Monoid.op(clazz, two, one) shouldBe one }
        it("should skip empty values") { Monoid.op(clazz, none, two) shouldBe two }
    }
})
