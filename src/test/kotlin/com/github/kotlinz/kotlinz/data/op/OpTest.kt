package com.github.kotlinz.kotlinz.data.op

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.law.ContravariantLaw
import com.github.kotlinz.kotlinz.law.DecidableLaw
import com.github.kotlinz.kotlinz.law.DivisibleLaw
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.monoid.toStringMonoid
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class OpTest : DescribeSpec({
    describe("Op") {
        context("as Contravariant") {
            val law = ContravariantLaw(object : OpContravariant<String> {}, ::sameOp)

            it("should satisfy laws") { law.assertSatisfyingContravariantLaw(Op { a: Int -> "value: $a" }) }

            it("should apply the mapping before running") {
                (Op { a: Int -> a.toString() } contramap { s: String -> s.length }).run("abc") shouldBe "3"
            }
        }

        context("as Divisible and Decidable") {
            val decidable = object : OpDecidable<StringMonoid> { override val type = StringMonoid::class.java }
            val m = Op { a: Int -> "m$a".toStringMonoid() }
            val n = Op { a: Int -> "n${a * 2}".toStringMonoid() }
            val o = Op { a: Int -> "o${-a}".toStringMonoid() }

            it("should satisfy Divisible laws") { DivisibleLaw(decidable, ::sameMonoidOp).assertSatisfyingDivisibleLaw(m, n, o) }
            it("should satisfy Decidable laws") { DecidableLaw(decidable, ::sameMonoidOp).assertSatisfyingDecidableLaw(m, n, o) }

            it("should combine both results with divide") {
                decidable.divide({ a: Int -> Pair(a, a) }, m, n).run(1) shouldBe "m1n2".toStringMonoid()
                decidable.conquer<Int>().run(1) shouldBe "".toStringMonoid()
            }
        }
    }
})

private fun sameOp(a: K1<K1<Op.T, String>, Int>, b: K1<K1<Op.T, String>, Int>): Boolean =
        (-10..10).all { Op.narrow(a).run(it) == Op.narrow(b).run(it) }

private fun sameMonoidOp(a: K1<K1<Op.T, StringMonoid>, Int>, b: K1<K1<Op.T, StringMonoid>, Int>): Boolean =
        (-10..10).all { Op.narrow(a).run(it) == Op.narrow(b).run(it) }
