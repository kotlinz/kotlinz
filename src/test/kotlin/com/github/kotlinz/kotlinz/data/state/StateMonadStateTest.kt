package com.github.kotlinz.kotlinz.data.state

import com.github.kotlinz.kotlinz.law.MonadStateLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class StateMonadStateTest : DescribeSpec({
    describe("State") {
        context("as MonadState") {
            val law = MonadStateLaw(object : StateMonadState<Int> {}) { (it as State<Int, *>).run(3) }
            it("should satisfy laws") { law.assertSatisfyingMonadStateLaw(1, 2) { it * 10 } }
        }

        context("with primitives") {
            it("should modify the state") {
                State.modify<Int> { it + 1 }.exec(1) shouldBe 2
            }

            it("should project the state with gets") {
                State.gets<String, Int> { it.length }.run("abc") shouldBe Pair(3, "abc")
            }

            it("should run a state transition") {
                State.state<Int, String> { s -> Pair("s=$s", s * 2) }.run(4) shouldBe Pair("s=4", 8)
            }

            it("should compose get and put") {
                val program = State.get<Int>() bind { n -> State.put(n + 1) bind { State.pure<Int, Int>(n) } }
                program.run(5) shouldBe Pair(5, 6)
            }
        }
    }
})
