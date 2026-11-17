package com.github.kotlinz.kotlinz.data.op

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.law.ContravariantLaw
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
    }
})

private fun sameOp(a: K1<K1<Op.T, String>, Int>, b: K1<K1<Op.T, String>, Int>): Boolean =
        (-10..10).all { Op.narrow(a).run(it) == Op.narrow(b).run(it) }
