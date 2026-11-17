package com.github.kotlinz.kotlinz.data.rws

import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.law.MonadReaderLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class RWSMonadReaderTest : DescribeSpec({
    describe("RWS") {
        val type = StringMonoid::class.java
        val monad = object : RWSMonadReader<Int, StringMonoid, Int> { override val type = type }

        context("as MonadReader") {
            val law = MonadReaderLaw(monad) { (it as RWS<Int, StringMonoid, Int, *>).run(3, 5) }
            val m = RWS<Int, StringMonoid, Int, Int>(type) { r, s -> Triple(r + s, s + 1, StringMonoid("m")) }
            it("should satisfy laws") { law.assertSatisfyingMonadReaderLaw(m, { it + 1 }, { it * 2 }) }
        }

        context("with primitives") {
            it("should project the environment with reader") {
                RWS.reader<Int, StringMonoid, Int, String>(type) { "r=$it" }.eval(2, 0).first shouldBe "r=2"
            }

            it("should modify the environment with local") {
                monad.local({ it * 10 }, monad.ask()).eval(2, 0).first shouldBe 20
            }
        }
    }
})
