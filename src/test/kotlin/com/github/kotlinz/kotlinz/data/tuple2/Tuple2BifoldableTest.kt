package com.github.kotlinz.kotlinz.data.tuple2

import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.monoid.toStringMonoid
import com.github.kotlinz.kotlinz.law.BifoldableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class Tuple2BifoldableTest : DescribeSpec({
    describe("Tuple2") {
        context("as Bifoldable") {
            val law = BifoldableLaw(object : Tuple2Bifoldable {})
            val tuple = Tuple2("a", 1)

            it("should satisfy laws") { law.assertSatisfyingBifoldableLaw(tuple) }

            it("should fold the first component before the second") {
                tuple.bifoldLeft("", { acc, s -> acc + s }, { acc, i -> acc + i }) shouldBe "a1"
                tuple.bifoldRight("", { s, acc -> s + acc }, { i, acc -> "$i$acc" }) shouldBe "a1"
            }

            it("should bifoldMap into a monoid") {
                tuple.bifoldMap(StringMonoid::class.java, { it.toStringMonoid() }, { "#$it".toStringMonoid() }) shouldBe "a#1".toStringMonoid()
            }
        }
    }
})
