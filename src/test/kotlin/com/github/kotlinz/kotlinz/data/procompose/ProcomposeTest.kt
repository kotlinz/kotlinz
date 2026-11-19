package com.github.kotlinz.kotlinz.data.procompose

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.function.Function1
import com.github.kotlinz.kotlinz.data.function.Function1Compose
import com.github.kotlinz.kotlinz.data.function.Function1Strong
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.kleisli.Kleisli
import com.github.kotlinz.kotlinz.data.kleisli.KleisliStrong
import com.github.kotlinz.kotlinz.law.ProfunctorLaw
import com.github.kotlinz.kotlinz.law.StrongLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private typealias F1 = Function1.T
private typealias Of = Procompose.Of<F1, Unit, F1, Unit>

private val functions = object : ProcomposeStrong<F1, Unit, F1, Unit> {
    override val p = object : Function1Strong {}
    override val q = object : Function1Strong {}
}

@Suppress("UNCHECKED_CAST")
private fun observe(v: K3<Procompose.T, Of, *, *>): (Any?) -> Any? =
    Function1.narrow((v as Procompose<F1, Unit, F1, Unit, Any?, Any?>).collapse(object : Function1Compose {})).run

private fun same(a: K3<Procompose.T, Of, Int, Int>, b: K3<Procompose.T, Of, Int, Int>): Boolean =
    (-3..3).all { observe(a)(it) == observe(b)(it) }

class ProcomposeTest : DescribeSpec({
    describe("Procompose") {
        val composed = Procompose.of(Function1 { n: Int -> n * 2 }, Function1 { n: Int -> n + 3 })

        context("as Profunctor") {
            it("should satisfy laws") { ProfunctorLaw(functions, ::same).assertSatisfyingProfunctorLaw(composed) }

            it("should map the inner input and the outer output") {
                val mapped = functions.dimap({ s: String -> s.length }, { n: Int -> "n=$n" }, composed)
                observe(mapped)("ab") shouldBe "n=10"
            }
        }

        context("as Strong") {
            it("should satisfy laws") { StrongLaw(functions, ::observe).assertSatisfyingStrongLaw(composed) }
            it("should thread the context through both steps") { observe(functions.first<Int, Int, String>(composed))(Pair(1, "x")) shouldBe Pair(8, "x") }
        }

        context("collapse") {
            it("should run the inner step first") { observe(composed)(1) shouldBe 8 }
        }

        context("with different profunctors") {
            val monad = object : IdentityMonad {}
            val mixed = Procompose.of(Function1 { n: Int -> n * 2 }, Kleisli(monad) { s: String -> Identity(s.length) })
            val profunctor = object : ProcomposeStrong<F1, Unit, Kleisli.T, Identity.T> {
                override val p = object : Function1Strong {}
                override val q = object : KleisliStrong<Identity.T> { override val monad = monad }
            }

            it("should compose a Kleisli arrow with a function") {
                val firsted = profunctor.first<String, Int, Boolean>(mixed)
                val inner = Identity.narrow(Kleisli.narrow(firsted.inner).run(Pair("abc", true))).value
                Function1.narrow(firsted.outer)(inner) shouldBe Pair(6, true)
            }
        }
    }
})
