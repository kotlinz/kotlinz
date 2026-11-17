package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.law.MonoidLaw
import com.github.kotlinz.kotlinz.type.group.Monoid
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

@Suppress("UNCHECKED_CAST")
class LastMonoidTest : DescribeSpec({
    describe("Last Monoid") {
        val clazz = LastMonoid::class.java as Class<LastMonoid<Int>>
        val law = MonoidLaw(clazz)
        val one = Maybe.Just(1).toLastMonoid()
        val two = Maybe.Just(2).toLastMonoid()
        val none = Maybe.None<Int>().toLastMonoid()

        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(one, two, Maybe.Just(3).toLastMonoid()) }
        it("should satisfy laws with empty values") { law.assertSatisfyingMonoidLaw(none, one, none) }
        it("should keep the last value") { Monoid.op(clazz, one, two) shouldBe two }
        it("should skip empty values") { Monoid.op(clazz, one, none) shouldBe one }
    }
})
