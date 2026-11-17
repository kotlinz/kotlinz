package com.github.kotlinz.kotlinz.data.rws

import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.law.MonadStateLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class RWSMonadStateTest : DescribeSpec({
    describe("RWS") {
        val type = StringMonoid::class.java
        val monad = object : RWSMonadState<Int, StringMonoid, Int> { override val type = type }

        context("as MonadState") {
            val law = MonadStateLaw(monad) { (it as RWS<Int, StringMonoid, Int, *>).run(3, 5) }
            it("should satisfy laws") { law.assertSatisfyingMonadStateLaw(1, 2) { it * 10 } }
        }

        context("with primitives") {
            it("should run a state transition") {
                RWS.state<Int, StringMonoid, Int, String>(type) { s -> Pair("s=$s", s + 1) }.run(0, 4) shouldBe Triple("s=4", 5, StringMonoid(""))
            }

            it("should project the state with gets") {
                RWS.narrow(monad.gets { it * 2 }).eval(0, 21).first shouldBe 42
            }
        }
    }
})
