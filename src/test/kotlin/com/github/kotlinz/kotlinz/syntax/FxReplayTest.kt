package com.github.kotlinz.kotlinz.syntax

import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKMonad
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyList
import com.github.kotlinz.kotlinz.data.nonemptylist.nonEmptyListOf
import com.github.kotlinz.kotlinz.data.state.StateMonad
import com.github.kotlinz.kotlinz.data.state.State
import com.github.kotlinz.kotlinz.data.state.get
import com.github.kotlinz.kotlinz.data.state.put
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class FxReplayTest : DescribeSpec({
    describe("fxReplay") {
        it("should build every combination of ListK values") {
            val result = ListK.fx {
                val a = listKOf(1, 2).bind()
                val b = listKOf("x", "y").bind()
                "$a$b"
            }
            result shouldBe listKOf("1x", "1y", "2x", "2y")
        }

        it("should drop branches bound to an empty ListK") {
            val result = ListK.fx {
                val a = listKOf(1, 2, 3).bind()
                if (a % 2 == 0) listKOf<Unit>().bind()
                a
            }
            result shouldBe listKOf(1, 3)
        }

        it("should keep bound values that are lists themselves") {
            ListK.fx { listKOf(listOf(1, 2), listOf(3)).bind() } shouldBe listKOf(listOf(1, 2), listOf(3))
        }

        it("should build every combination of NonEmptyList values") {
            val result = NonEmptyList.fx {
                val a = nonEmptyListOf(1, 2).bind()
                val b = nonEmptyListOf(10, 20).bind()
                a + b
            }
            result shouldBe nonEmptyListOf(11, 21, 12, 22)
        }

        it("should work with single-shot monads too") {
            val result = (object : MaybeMonad {}).fxReplay {
                Maybe.Just(1).bind() + Maybe.Just(2).bind()
            }
            result shouldBe Maybe.Just(3)
        }

        it("should allow running lazy monads repeatedly") {
            val counter = State.narrow((object : StateMonad<Int> {}).fxReplay {
                val n = get<Int>().bind()
                put(n + 1).bind()
                n
            })
            counter.run(1) shouldBe Pair(1, 2)
            counter.run(7) shouldBe Pair(7, 8)
        }

        it("should agree with nested bind on ListK") {
            val monad = object : ListKMonad {}
            val viaFx = monad.fxReplay { listKOf(1, 2).bind() * listKOf(3, 4).bind() }
            val viaBind = listKOf(1, 2) bind { a -> listKOf(3, 4) bind { b -> listKOf(a * b) } }
            viaFx shouldBe viaBind
        }
    }
})
