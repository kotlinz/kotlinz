package com.github.kotlinz.kotlinz.data.cayley

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.function.Function1
import com.github.kotlinz.kotlinz.data.function.Function1Strong
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKFunctor
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.law.ProfunctorLaw
import com.github.kotlinz.kotlinz.law.StrongLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private typealias Of = Cayley.Of<ListK.T, Function1.T, Unit>

private val cayley = object : CayleyStrong<ListK.T, Function1.T, Unit> {
    override val functor = object : ListKFunctor {}
    override val profunctor = object : Function1Strong {}
}

@Suppress("UNCHECKED_CAST")
private fun observe(v: K3<Cayley.T, Of, *, *>): (Any?) -> Any? =
    { x -> ListK.narrow((v as Cayley<ListK.T, Function1.T, Unit, Any?, Any?>).value).value.map { Function1.narrow(it)(x) } }

private fun same(a: K3<Cayley.T, Of, Int, Int>, b: K3<Cayley.T, Of, Int, Int>): Boolean =
    (-3..3).all { observe(a)(it) == observe(b)(it) }

class CayleyTest : DescribeSpec({
    describe("Cayley") {
        val fs = Cayley(listKOf<K3<Function1.T, Unit, Int, Int>>(Function1 { it + 1 }, Function1 { it * 2 }))

        context("as Profunctor") {
            it("should satisfy laws") { ProfunctorLaw(cayley, ::same).assertSatisfyingProfunctorLaw(fs) }

            it("should map every wrapped value") {
                observe(cayley.dimap({ s: String -> s.length }, { n: Int -> "n=$n" }, fs))("abc") shouldBe listOf("n=4", "n=6")
            }
        }

        context("as Strong") {
            it("should satisfy laws") { StrongLaw(cayley, ::observe).assertSatisfyingStrongLaw(fs) }
            it("should lift first through the functor") { observe(cayley.first<Int, Int, String>(fs))(Pair(3, "x")) shouldBe listOf(Pair(4, "x"), Pair(6, "x")) }
        }
    }
})
