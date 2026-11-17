package com.github.kotlinz.kotlinz.data.identity

import com.github.kotlinz.kotlinz.data.listk.ListKApplicative
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.law.TraversableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class IdentityTraversableTest : DescribeSpec({
    describe("Identity") {
        context("as Traversable") {
            val law = TraversableLaw(object : IdentityTraversable {})
            val positive = { x: Int -> if (x > 0) Maybe.Just(x) else Maybe.None<Int>() }
            val g = { x: Int -> listKOf(x, x * 2) }

            it("should satisfy laws") { law.assertSatisfyingTraversableLaw(Identity(1), positive, g) }
            it("should satisfy laws with failing function") { law.assertSatisfyingTraversableLaw(Identity(-1), positive, g) }

            it("should traverse the value") {
                Identity(1).traverse(object : ListKApplicative {}, g) shouldBe listKOf(Identity(1), Identity(2))
            }

            it("should sequence Identity of Maybe") {
                Identity.sequence(object : MaybeApplicative {}, Identity(Maybe.Just(1))) shouldBe Maybe.Just(Identity(1))
            }
        }
    }
})
