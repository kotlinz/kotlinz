package com.github.kotlinz.kotlinz.data.codensity

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKMonad
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.law.FunctorLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private val listMonad = object : ListKMonad {}

private fun <A> lowered(v: K1<K1<Codensity.T, ListK.T>, A>): ListK<A> = ListK.narrow(Codensity.narrow(v).lower(listMonad))

class CodensityTest : DescribeSpec({
    describe("Codensity") {
        val monad = object : CodensityMonad<ListK.T> {}
        val m = Codensity.lift(listMonad, listKOf(1, 2))
        val f = { x: Int -> Codensity.lift(listMonad, listKOf(x, x * 10)) }
        val g = { x: Int -> Codensity.pure<ListK.T, Int>(x + 1) }

        context("as Monad") {
            it("should satisfy left identity") { lowered(monad.bind(f, monad.pure(3))) shouldBe lowered(f(3)) }
            it("should satisfy right identity") { lowered(monad.bind({ monad.pure(it) }, m)) shouldBe lowered(m) }

            it("should satisfy associativity") {
                val left = monad.bind(g, monad.bind(f, m))
                val right = monad.bind({ x: Int -> monad.bind(g, f(x)) }, m)
                lowered(left) shouldBe lowered(right)
            }
        }

        context("as Functor") {
            val law = FunctorLaw(monad) { a, b -> lowered(a) == lowered(b) }
            it("should satisfy laws") { law.assertSatisfyingFunctorLaw(m, { it + 1 }, { it.toString() }) }
        }

        context("lift and lower") {
            it("should round-trip through lift and lower") { lowered(m) shouldBe listKOf(1, 2) }

            it("should agree with the underlying monad") {
                lowered(m bind f fmap { it + 1 }) shouldBe listMonad.fmap({ x: Int -> x + 1 }, listMonad.bind({ x: Int -> listKOf(x, x * 10) }, listKOf(1, 2)))
            }

            it("should apply functions") { lowered(m ap Codensity.pure { x: Int -> x * 3 }) shouldBe listKOf(3, 6) }

            it("should right-associate left-nested binds") {
                val nested = (1..1_000).fold(Codensity.pure<ListK.T, Int>(0)) { acc, _ -> acc bind { x -> Codensity.pure(x + 1) } }
                lowered(nested) shouldBe listKOf(1_000)
            }
        }
    }
})
