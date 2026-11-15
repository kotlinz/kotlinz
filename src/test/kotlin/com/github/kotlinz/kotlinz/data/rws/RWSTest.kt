package com.github.kotlinz.kotlinz.data.rws

import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class RWSTest : DescribeSpec({
    describe("RWS") {
        val type = StringMonoid::class.java
        val monad = object : RWSMonad<Int, StringMonoid, Int> { override val type = type }
        val f = { x: Int -> RWS<Int, StringMonoid, Int, Int>(type) { r, s -> Triple(x + r, s * 2, StringMonoid("f")) } }
        val g = { x: Int -> RWS<Int, StringMonoid, Int, Int>(type) { r, s -> Triple(x * r, s + 1, StringMonoid("g")) } }
        val m = RWS<Int, StringMonoid, Int, Int>(type) { r, s -> Triple(r + s, s, StringMonoid("m")) }

        context("as Monad") {
            it("should satisfy left identity") {
                monad.bind(f, monad.pure(1)).run(3, 5) shouldBe f(1).run(3, 5)
            }

            it("should satisfy right identity") {
                monad.bind({ monad.pure(it) }, m).run(3, 5) shouldBe m.run(3, 5)
            }

            it("should satisfy associativity") {
                val left = monad.bind(g, monad.bind(f, m))
                val right = monad.bind({ x: Int -> monad.bind(g, f(x)) }, m)
                left.run(3, 5) shouldBe right.run(3, 5)
            }
        }

        context("with primitives") {
            it("should combine reader, writer and state") {
                val program = RWS.ask<Int, StringMonoid, Int>(type) bind { r ->
                    RWS.tell<Int, StringMonoid, Int>(type, StringMonoid("r=$r;")) bind {
                        RWS.modify<Int, StringMonoid, Int>(type) { it + r } bind {
                            RWS.get<Int, StringMonoid, Int>(type)
                        }
                    }
                }
                program.run(2, 10) shouldBe Triple(12, 12, StringMonoid("r=2;"))
            }

            it("should run with local environment") {
                RWS.ask<Int, StringMonoid, Int>(type).local { it * 10 }.eval(2, 0).first shouldBe 20
            }

            it("should replace state with put") {
                RWS.put<Int, StringMonoid, Int>(type, 7).exec(0, 1) shouldBe Pair(7, StringMonoid(""))
            }
        }
    }
})
