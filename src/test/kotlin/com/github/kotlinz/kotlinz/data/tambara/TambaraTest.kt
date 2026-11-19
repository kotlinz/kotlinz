package com.github.kotlinz.kotlinz.data.tambara

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.function.Function1
import com.github.kotlinz.kotlinz.data.function.Function1Strong
import com.github.kotlinz.kotlinz.law.ProfunctorLaw
import com.github.kotlinz.kotlinz.law.StrongLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private typealias Of = Tambara.Of<Function1.T, Unit>

private val function1 = object : Function1Strong {}

private val tambara = object : TambaraStrong<Function1.T, Unit> {
    override val profunctor = function1
}

@Suppress("UNCHECKED_CAST")
private fun observe(v: K3<Tambara.T, Of, *, *>): (Any?) -> Any? =
    Function1.narrow((v as Tambara<Function1.T, Unit, Any?, Any?>).untambara(function1)).run

private fun same(a: K3<Tambara.T, Of, Int, Int>, b: K3<Tambara.T, Of, Int, Int>): Boolean =
    (-3..3).all { observe(a)(it) == observe(b)(it) }

class TambaraTest : DescribeSpec({
    describe("Tambara") {
        val inc = Tambara.of(function1, Function1 { n: Int -> n + 1 })

        context("as Profunctor") {
            it("should satisfy laws") { ProfunctorLaw(tambara, ::same).assertSatisfyingProfunctorLaw(inc) }
            it("should map input and output") { observe(tambara.dimap({ s: String -> s.length }, { n: Int -> n * 10 }, inc))("ab") shouldBe 30 }
        }

        context("as Strong") {
            it("should satisfy laws") { StrongLaw(tambara, ::observe).assertSatisfyingStrongLaw(inc) }
            it("should keep the outer context") { observe(tambara.first<Int, Int, String>(inc))(Pair(1, "x")) shouldBe Pair(2, "x") }
        }

        context("run") {
            it("should work for any context type") {
                Function1.narrow(inc.run<String>())(Pair(1, "x")) shouldBe Pair(2, "x")
                Function1.narrow(inc.run<Boolean>())(Pair(5, true)) shouldBe Pair(6, true)
            }

            it("should round-trip through untambara") { Function1.narrow(inc.untambara(function1))(41) shouldBe 42 }

            it("should run nested contexts after first") {
                val nested = Tambara.narrow(tambara.first<Int, Int, String>(inc))
                Function1.narrow(nested.run<Boolean>())(Pair(Pair(1, "x"), true)) shouldBe Pair(Pair(2, "x"), true)
            }
        }
    }
})
