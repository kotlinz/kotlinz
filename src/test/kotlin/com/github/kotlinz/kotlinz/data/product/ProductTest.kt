package com.github.kotlinz.kotlinz.data.product

import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKFoldable
import com.github.kotlinz.kotlinz.data.listk.ListKMonad
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeFoldable
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.law.ApplicativeLaw
import com.github.kotlinz.kotlinz.law.FoldableLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ProductTest : DescribeSpec({
    describe("Product") {
        val listK = object : ListKMonad {}
        val maybe = object : MaybeMonad {}
        val product = Product<ListK.T, Maybe.T, Int>(ListK(listOf(1, 2)), Maybe.Just(3))

        context("as Functor") {
            val functor = Product.functor(listK, maybe)

            it("should satisfy laws") { FunctorLaw(functor).assertSatisfyingFunctorLaw(product, { it + 1 }, { it * 2 }) }
            it("should map both sides") {
                functor.fmap({ x: Int -> x * 10 }, product) shouldBe Product<ListK.T, Maybe.T, Int>(ListK(listOf(10, 20)), Maybe.Just(30))
            }
        }

        context("as Applicative") {
            val applicative = Product.applicativeOps(listK, maybe)
            val u = Product<ListK.T, Maybe.T, (String) -> Int>(ListK(listOf({ s: String -> s.length })), Maybe.None())
            val v = Product<ListK.T, Maybe.T, (Int) -> String>(ListK(listOf({ i: Int -> "$i" }, { i: Int -> "#$i" })), Maybe.Just { i: Int -> "$i!" })

            it("should satisfy laws") { ApplicativeLaw(applicative).assertSatisfyingApplicativeLaw(product, 1, u, v) { it.toString() } }

            it("should combine each side independently") {
                val add = applicative.liftA2 { a: Int, b: Int -> a + b }
                add(product, Product.pure(listK, maybe, 10)) shouldBe Product<ListK.T, Maybe.T, Int>(ListK(listOf(11, 12)), Maybe.Just(13))
            }
        }

        context("as Foldable") {
            val foldable = Product.foldable(object : ListKFoldable {}, object : MaybeFoldable {})

            it("should satisfy laws") { FoldableLaw(foldable).assertSatisfyingFoldableLaw(product) }
            it("should fold the first then the second") { foldable.toList(product) shouldBe listOf(1, 2, 3) }
        }
    }
})
